package com.prakash.prakashmart.dao.impl;

import com.prakash.prakashmart.dao.CartDAO;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.CartItem;
import com.prakash.prakashmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CartDAO using PreparedStatement and HikariCP.
 */
public class CartDAOImpl implements CartDAO {

    private static final Logger logger = LoggerFactory.getLogger(CartDAOImpl.class);

    @Override
    public void add(Long userId, Long productId, int quantity) throws DatabaseException {
        addItem(userId, productId, quantity);
    }

    @Override
    public void addItem(Long userId, Long productId, int quantity) throws DatabaseException {
        Optional<CartItem> existing = findItem(userId, productId);
        if (existing.isPresent()) {
            updateQuantity(existing.get().getId(), existing.get().getQuantity() + quantity);
        } else {
            String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.setLong(2, productId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error adding item to cart for user {} product {}", userId, productId, e);
                throw new DatabaseException("Failed to add item to cart", e);
            }
        }
    }

    @Override
    public void update(Long cartItemId, int newQuantity) throws DatabaseException {
        updateQuantity(cartItemId, newQuantity);
    }

    @Override
    public void updateQuantity(Long cartItemId, int newQuantity) throws DatabaseException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating cart item quantity for {}", cartItemId, e);
            throw new DatabaseException("Failed to update cart quantity", e);
        }
    }

    @Override
    public void remove(Long cartItemId) throws DatabaseException {
        removeItem(cartItemId);
    }

    @Override
    public void removeItem(Long cartItemId) throws DatabaseException {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error removing cart item {}", cartItemId, e);
            throw new DatabaseException("Failed to remove cart item", e);
        }
    }

    @Override
    public void clear(Long userId) throws DatabaseException {
        clearCart(null, userId);
    }

    @Override
    public void clearCart(Connection conn, Long userId) throws DatabaseException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        boolean closeRequired = false;
        try {
            Connection actualConn = conn;
            if (actualConn == null) {
                actualConn = DatabaseUtil.getConnection();
                closeRequired = true;
            }
            try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            } finally {
                if (closeRequired) {
                    actualConn.close();
                }
            }
        } catch (SQLException e) {
            logger.error("Error clearing cart for user {}", userId, e);
            throw new DatabaseException("Failed to clear cart", e);
        }
    }

    @Override
    public List<CartItem> findByUser(Long userId) throws DatabaseException {
        String sql = "SELECT id, user_id, product_id, quantity FROM cart_items WHERE user_id = ? ORDER BY id ASC";
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CartItem(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding cart items for user {}", userId, e);
            throw new DatabaseException("Failed to find cart items", e);
        }
    }

    @Override
    public List<CartItemDTO> getCartByUserId(Long userId) throws DatabaseException {
        String sql = "SELECT ci.id AS cart_item_id, p.id AS product_id, p.name AS product_name, "
                   + "p.price AS unit_price, ci.quantity, p.stock_qty AS available_stock "
                   + "FROM cart_items ci "
                   + "INNER JOIN products p ON ci.product_id = p.id "
                   + "WHERE ci.user_id = ? ORDER BY ci.id ASC";
        List<CartItemDTO> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setCartItemId(rs.getLong("cart_item_id"));
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setProductName(rs.getString("product_name"));
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    int qty = rs.getInt("quantity");
                    dto.setUnitPrice(unitPrice);
                    dto.setQuantity(qty);
                    dto.setAvailableStock(rs.getInt("available_stock"));
                    dto.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
                    list.add(dto);
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving cart DTOs for user {}", userId, e);
            throw new DatabaseException("Failed to retrieve cart items", e);
        }
    }

    @Override
    public Optional<CartItem> findById(Long cartItemId) throws DatabaseException {
        String sql = "SELECT id, user_id, product_id, quantity FROM cart_items WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CartItem(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding cart item by id {}", cartItemId, e);
            throw new DatabaseException("Failed to find cart item", e);
        }
    }

    @Override
    public Optional<CartItem> findItem(Long userId, Long productId) throws DatabaseException {
        String sql = "SELECT id, user_id, product_id, quantity FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CartItem(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error checking cart item", e);
            throw new DatabaseException("Failed to check cart item", e);
        }
    }
}
