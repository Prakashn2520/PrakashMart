package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dao.impl.OrderDAOImpl;
import com.prakash.prakashmart.dao.impl.OrderItemDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.*;
import com.prakash.prakashmart.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest extends BaseDAOTest {

    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;

    private Long sellerId;
    private Long buyerId;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() throws DatabaseException {
        this.orderDAO = new OrderDAOImpl();
        this.orderItemDAO = new OrderItemDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();

        User seller = userDAO.save(new User(null, "Order Seller", "orderseller@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.save(new User(null, "Order Buyer", "orderbuyer@test.com", "hash", Role.BUYER, null));
        this.sellerId = seller.getId();
        this.buyerId = buyer.getId();

        Product p1 = productDAO.save(new Product(null, sellerId, "Laptop", "Gaming laptop", new BigDecimal("999.00"), 10, "Electronics", null, null));
        Product p2 = productDAO.save(new Product(null, sellerId, "Mouse", "Optical mouse", new BigDecimal("25.00"), 50, "Electronics", null, null));
        this.productId1 = p1.getId();
        this.productId2 = p2.getId();
    }

    @Test
    @DisplayName("Should save order and items transactionally and retrieve them")
    void testSaveOrderTransactionally() throws Exception {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1024.00"));

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order savedOrder = orderDAO.save(conn, order);
                assertNotNull(savedOrder.getId());

                OrderItem item1 = new OrderItem(null, savedOrder.getId(), productId1, 1, new BigDecimal("999.00"));
                OrderItem item2 = new OrderItem(null, savedOrder.getId(), productId2, 1, new BigDecimal("25.00"));
                orderDAO.saveItem(conn, item1);
                orderDAO.saveItem(conn, item2);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // Verify order retrieval
        List<Order> buyerOrders = orderDAO.findByBuyerId(buyerId);
        assertEquals(1, buyerOrders.size());
        Order retrievedOrder = buyerOrders.get(0);
        assertEquals(OrderStatus.PENDING, retrievedOrder.getStatus());
        assertEquals(0, new BigDecimal("1024.00").compareTo(retrievedOrder.getTotalAmount()));

        // Verify order items retrieval
        List<OrderItem> items = orderDAO.findItemsByOrderId(retrievedOrder.getId());
        assertEquals(2, items.size());

        // Verify seller orders retrieval
        List<Order> sellerOrders = orderDAO.findBySellerId(sellerId);
        assertEquals(1, sellerOrders.size());

        // Update status
        orderDAO.updateStatus(retrievedOrder.getId(), OrderStatus.SHIPPED);
        Optional<Order> updated = orderDAO.findById(retrievedOrder.getId());
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.SHIPPED, updated.get().getStatus());
    }

    @Test
    @DisplayName("Should create order and items using OrderDAO.create atomic method")
    void testOrderDAOCreateMethod() throws DatabaseException {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("999.00"));

        OrderItem item = new OrderItem(null, null, productId1, 1, new BigDecimal("999.00"));

        Order created = orderDAO.create(order, List.of(item));
        assertNotNull(created.getId());

        List<Order> buyerOrders = orderDAO.findByBuyer(buyerId);
        assertEquals(1, buyerOrders.size());

        List<Order> sellerOrders = orderDAO.findBySeller(sellerId);
        assertEquals(1, sellerOrders.size());

        List<OrderItem> items = orderItemDAO.findByOrderId(created.getId());
        assertEquals(1, items.size());
        assertEquals(productId1, items.get(0).getProductId());
    }

    @Test
    @DisplayName("Should cancel order when PENDING or CONFIRMED, but reject when SHIPPED or DELIVERED")
    void testCancelOrder() throws DatabaseException {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("25.00"));
        OrderItem item = new OrderItem(null, null, productId2, 1, new BigDecimal("25.00"));

        Order created = orderDAO.create(order, List.of(item));

        // Cancel pending order
        boolean cancelled = orderDAO.cancel(created.getId());
        assertTrue(cancelled);

        Optional<Order> afterCancel = orderDAO.findById(created.getId());
        assertTrue(afterCancel.isPresent());
        assertEquals(OrderStatus.CANCELLED, afterCancel.get().getStatus());

        // Cannot cancel already cancelled order
        boolean cancelAgain = orderDAO.cancel(created.getId());
        assertFalse(cancelAgain);

        // Cannot cancel shipped order
        Order shippedOrder = new Order();
        shippedOrder.setBuyerId(buyerId);
        shippedOrder.setStatus(OrderStatus.SHIPPED);
        shippedOrder.setTotalAmount(new BigDecimal("25.00"));
        Order createdShipped = orderDAO.create(shippedOrder, List.of(new OrderItem(null, null, productId2, 1, new BigDecimal("25.00"))));

        boolean cancelShipped = orderDAO.cancel(createdShipped.getId());
        assertFalse(cancelShipped);
    }

    @Test
    @DisplayName("Should return empty lists for non-existent buyer/seller and empty optional for invalid ID")
    void testEmptyAndInvalidLookups() throws DatabaseException {
        Optional<Order> nonExistent = orderDAO.findById(999999L);
        assertFalse(nonExistent.isPresent());

        List<Order> noBuyer = orderDAO.findByBuyer(888888L);
        assertTrue(noBuyer.isEmpty());

        List<Order> noSeller = orderDAO.findBySeller(888888L);
        assertTrue(noSeller.isEmpty());

        List<OrderItem> noItems = orderDAO.findItemsByOrderId(999999L);
        assertTrue(noItems.isEmpty());
    }
}
