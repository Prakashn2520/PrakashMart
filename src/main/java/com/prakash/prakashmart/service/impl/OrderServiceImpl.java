package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dao.CartDAO;
import com.prakash.prakashmart.dao.OrderDAO;
import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.*;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service implementation for order management, atomic transaction checkout,
 * status workflows, and role-based visibility.
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderServiceImpl(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public Order checkout(Long buyerId) throws AppException {
        if (buyerId == null || buyerId <= 0) {
            throw new ValidationException("buyerId", "Invalid buyer ID");
        }

        // 1. Load cart
        List<CartItemDTO> cartItems = cartDAO.getCartByUserId(buyerId);

        // 2. Validate cart is not empty
        if (cartItems.isEmpty()) {
            throw new ValidationException("Cannot checkout with an empty cart");
        }

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            BigDecimal totalAmount = BigDecimal.ZERO;

            // 3 & 4. Reload current product prices from database & validate current stock
            for (CartItemDTO item : cartItems) {
                Product currentProduct = productDAO.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException("Product not found: " + item.getProductName()));

                if (Boolean.FALSE.equals(currentProduct.getIsActive())) {
                    throw new ValidationException("Product is no longer available: " + currentProduct.getName());
                }

                if (currentProduct.getStockQty() < item.getQuantity()) {
                    throw new InsufficientStockException(currentProduct.getId(), item.getQuantity(), currentProduct.getStockQty());
                }

                // 5. Calculate total on server using authoritative database price
                BigDecimal itemPrice = currentProduct.getPrice();
                item.setUnitPrice(itemPrice);
                BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(subtotal);
            }

            // 6. Create order
            Order order = new Order();
            order.setBuyerId(buyerId);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalAmount(totalAmount);

            Order savedOrder = orderDAO.save(conn, order);

            // 7. Create order items & 8. Reduce stock
            for (CartItemDTO item : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getId());
                orderItem.setProductId(item.getProductId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(item.getUnitPrice());
                orderDAO.saveItem(conn, orderItem);

                // Reload latest stock and decrement
                Product p = productDAO.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                productDAO.updateStock(item.getProductId(), p.getStockQty() - item.getQuantity());
            }

            // 9. Clear cart
            cartDAO.clearCart(conn, buyerId);

            // 10. Commit
            conn.commit();
            logger.info("Order {} placed successfully for buyer {} with total {}", savedOrder.getId(), buyerId, totalAmount);
            return savedOrder;

        } catch (AppException e) {
            rollbackTransaction(conn, buyerId);
            throw e;
        } catch (Exception e) {
            rollbackTransaction(conn, buyerId);
            logger.error("Checkout failed unexpectedly for buyer {}", buyerId, e);
            throw new DatabaseException("Checkout failed: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public Order getOrderById(Long orderId) throws AppException {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID");
        }
        return orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    @Override
    public Order getOrderById(Long orderId, Long userId, String role) throws AppException {
        Order order = getOrderById(orderId);

        if ("ADMIN".equalsIgnoreCase(role)) {
            return order; // Admins can view any order
        }

        if ("BUYER".equalsIgnoreCase(role)) {
            if (!order.getBuyerId().equals(userId)) {
                throw new AuthorizationException("You are not authorized to view this order");
            }
            return order;
        }

        if ("SELLER".equalsIgnoreCase(role)) {
            List<Order> sellerOrders = orderDAO.findBySellerId(userId);
            boolean ownsItem = sellerOrders.stream().anyMatch(o -> o.getId().equals(orderId));
            if (!ownsItem) {
                throw new AuthorizationException("You are not authorized to view this order");
            }
            return order;
        }

        throw new AuthorizationException("Unauthorized to view order details");
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) throws AppException {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID");
        }
        return orderDAO.findItemsByOrderId(orderId);
    }

    @Override
    public List<Order> getOrdersByBuyer(Long buyerId) throws AppException {
        if (buyerId == null || buyerId <= 0) {
            throw new ValidationException("buyerId", "Invalid buyer ID");
        }
        return orderDAO.findByBuyerId(buyerId);
    }

    @Override
    public List<Order> getOrdersForSeller(Long sellerId) throws AppException {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID");
        }
        return orderDAO.findBySellerId(sellerId);
    }

    @Override
    public List<Order> getAllOrders() throws AppException {
        return orderDAO.findAll();
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) throws AppException {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID");
        }
        if (newStatus == null) {
            throw new ValidationException("newStatus", "Order status cannot be null");
        }

        Order existing = getOrderById(orderId);
        OrderStatus current = existing.getStatus();

        // Validate status transition rules
        if (current == OrderStatus.CANCELLED) {
            throw new OrderException("Cannot change status of a cancelled order");
        }
        if (current == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED) {
            throw new OrderException("Delivered orders cannot be moved to other states");
        }

        orderDAO.updateStatus(orderId, newStatus);
        logger.info("Updated order {} status from {} to {}", orderId, current, newStatus);
    }

    @Override
    public void cancelOrder(Long orderId, Long userId, String role) throws AppException {
        Order order = getOrderById(orderId);

        // Authorization check: buyer can cancel their own order; admin can cancel any
        if (!"ADMIN".equalsIgnoreCase(role)) {
            if ("BUYER".equalsIgnoreCase(role) && !order.getBuyerId().equals(userId)) {
                throw new AuthorizationException("You are not authorized to cancel this order");
            }
        }

        // Validate cancellation rules: only PENDING or CONFIRMED can be cancelled
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderException("Cannot cancel order in " + order.getStatus() + " status");
        }

        // Perform cancellation and restore stock
        boolean cancelled = orderDAO.cancel(orderId);
        if (!cancelled) {
            throw new OrderException("Failed to cancel order");
        }

        // Restore inventory stock for the order items
        List<OrderItem> items = orderDAO.findItemsByOrderId(orderId);
        for (OrderItem item : items) {
            productDAO.findById(item.getProductId()).ifPresent(product -> {
                try {
                    productDAO.updateStock(product.getId(), product.getStockQty() + item.getQuantity());
                } catch (DatabaseException e) {
                    logger.error("Failed to restore stock for product {} upon order cancellation", product.getId(), e);
                }
            });
        }
        logger.info("Order {} successfully cancelled by user {} with restored stock", orderId, userId);
    }

    private void rollbackTransaction(Connection conn, Long buyerId) {
        if (conn != null) {
            try {
                conn.rollback();
                logger.warn("Transaction rolled back for checkout of buyer {}", buyerId);
            } catch (SQLException ex) {
                logger.error("Failed to rollback checkout transaction", ex);
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection after transaction", e);
            }
        }
    }
}
