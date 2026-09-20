package com.prakash.prakashmart.dao.impl;

import com.prakash.prakashmart.dao.OrderItemDAO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.OrderItem;
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
 * JDBC implementation of OrderItemDAO.
 */
public class OrderItemDAOImpl implements OrderItemDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemDAOImpl.class);

    @Override
    public OrderItem save(Connection conn, OrderItem item) throws DatabaseException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        boolean closeRequired = false;
        try {
            Connection actualConn = conn;
            if (actualConn == null) {
                actualConn = DatabaseUtil.getConnection();
                closeRequired = true;
            }
            try (PreparedStatement ps = actualConn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            } finally {
                if (closeRequired) {
                    actualConn.close();
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving order item", e);
            throw new DatabaseException("Failed to save order item", e);
        }
    }

    @Override
    public Optional<OrderItem> findById(Long id) throws DatabaseException {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price FROM order_items WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOrderItem(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding order item by id {}", id, e);
            throw new DatabaseException("Failed to find order item by ID", e);
        }
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) throws DatabaseException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return findByOrderId(conn, orderId);
        } catch (SQLException e) {
            logger.error("Error acquiring connection for finding items of order {}", orderId, e);
            throw new DatabaseException("Failed to find order items", e);
        }
    }

    @Override
    public List<OrderItem> findByOrderId(Connection conn, Long orderId) throws DatabaseException {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price FROM order_items WHERE order_id = ? ORDER BY id ASC";
        boolean closeRequired = false;
        try {
            Connection actualConn = conn;
            if (actualConn == null) {
                actualConn = DatabaseUtil.getConnection();
                closeRequired = true;
            }
            try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
                ps.setLong(1, orderId);
                List<OrderItem> list = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRowToOrderItem(rs));
                    }
                }
                return list;
            } finally {
                if (closeRequired) {
                    actualConn.close();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding items for order {}", orderId, e);
            throw new DatabaseException("Failed to find order items", e);
        }
    }

    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        return item;
    }
}
