package com.prakash.prakashmart.dao.impl;

import com.prakash.prakashmart.dao.OrderDAO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of OrderDAO supporting transactions.
 */
public class OrderDAOImpl implements OrderDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);

    @Override
    public Order create(Order order, List<OrderItem> items) throws DatabaseException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            save(conn, order);

            if (items != null) {
                for (OrderItem item : items) {
                    item.setOrderId(order.getId());
                    saveItem(conn, item);
                }
            }

            conn.commit();
            return order;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back order creation transaction", ex);
                }
            }
            logger.error("Error creating order with items", e);
            throw new DatabaseException("Failed to create order transactionally", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    @Override
    public Optional<Order> findById(Long id) throws DatabaseException {
        String sql = "SELECT id, buyer_id, status, total_amount, created_at FROM orders WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOrder(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding order by id {}", id, e);
            throw new DatabaseException("Failed to find order by ID", e);
        }
    }

    @Override
    public List<Order> findByBuyer(Long buyerId) throws DatabaseException {
        return findByBuyerId(buyerId);
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) throws DatabaseException {
        String sql = "SELECT id, buyer_id, status, total_amount, created_at FROM orders WHERE buyer_id = ? ORDER BY id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding orders by buyer {}", buyerId, e);
            throw new DatabaseException("Failed to find orders by buyer", e);
        }
    }

    @Override
    public List<Order> findBySeller(Long sellerId) throws DatabaseException {
        return findBySellerId(sellerId);
    }

    @Override
    public List<Order> findBySellerId(Long sellerId) throws DatabaseException {
        String sql = "SELECT DISTINCT o.id, o.buyer_id, o.status, o.total_amount, o.created_at "
                   + "FROM orders o "
                   + "INNER JOIN order_items oi ON o.id = oi.order_id "
                   + "INNER JOIN products p ON oi.product_id = p.id "
                   + "WHERE p.seller_id = ? ORDER BY o.id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding orders for seller {}", sellerId, e);
            throw new DatabaseException("Failed to find orders for seller", e);
        }
    }

    @Override
    public List<Order> findAll() throws DatabaseException {
        String sql = "SELECT id, buyer_id, status, total_amount, created_at FROM orders ORDER BY id DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToOrder(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            throw new DatabaseException("Failed to find all orders", e);
        }
    }

    @Override
    public Order save(Connection conn, Order order) throws DatabaseException {
        String sql = "INSERT INTO orders (buyer_id, status, total_amount, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setString(2, order.getStatus().name());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                }
            }
            return order;
        } catch (SQLException e) {
            logger.error("Error saving order within transaction", e);
            throw new DatabaseException("Failed to save order", e);
        }
    }

    @Override
    public OrderItem saveItem(Connection conn, OrderItem item) throws DatabaseException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getLong(1));
                }
            }
            return item;
        } catch (SQLException e) {
            logger.error("Error saving order item", e);
            throw new DatabaseException("Failed to save order item", e);
        }
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long orderId) throws DatabaseException {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price FROM order_items WHERE order_id = ? ORDER BY id ASC";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    list.add(item);
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding items for order {}", orderId, e);
            throw new DatabaseException("Failed to find order items", e);
        }
    }

    @Override
    public void updateStatus(Long orderId, OrderStatus newStatus) throws DatabaseException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating order status for {}", orderId, e);
            throw new DatabaseException("Failed to update order status", e);
        }
    }

    @Override
    public boolean cancel(Long orderId) throws DatabaseException {
        // Can only cancel if order is in PENDING or CONFIRMED state
        String checkSql = "SELECT status FROM orders WHERE id = ?";
        String updateSql = "UPDATE orders SET status = 'CANCELLED' WHERE id = ? AND status IN ('PENDING', 'CONFIRMED')";
        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setLong(1, orderId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    String currentStatus = rs.getString("status");
                    if (!"PENDING".equalsIgnoreCase(currentStatus) && !"CONFIRMED".equalsIgnoreCase(currentStatus)) {
                        return false;
                    }
                }
            }

            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setLong(1, orderId);
                return updatePs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Error cancelling order {}", orderId, e);
            throw new DatabaseException("Failed to cancel order", e);
        }
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        return order;
    }
}
