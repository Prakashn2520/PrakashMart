package com.prakash.prakashmart.dao.impl;

import com.prakash.prakashmart.dao.ReviewDAO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Review;
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
 * JDBC implementation of ReviewDAO.
 */
public class ReviewDAOImpl implements ReviewDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDAOImpl.class);

    @Override
    public Review create(Review review) throws DatabaseException {
        return save(review);
    }

    @Override
    public Review save(Review review) throws DatabaseException {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment, created_at) "
                   + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getLong(1));
                }
            }
            return review;
        } catch (SQLException e) {
            logger.error("Error saving review", e);
            throw new DatabaseException("Failed to save review", e);
        }
    }

    @Override
    public List<Review> findByProduct(Long productId) throws DatabaseException {
        return findByProductId(productId);
    }

    @Override
    public List<Review> findByProductId(Long productId) throws DatabaseException {
        String sql = "SELECT id, product_id, user_id, rating, comment, created_at "
                   + "FROM reviews WHERE product_id = ? ORDER BY id DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding reviews for product {}", productId, e);
            throw new DatabaseException("Failed to find reviews", e);
        }
    }

    @Override
    public double calculateAverageRating(Long productId) throws DatabaseException {
        String sql = "SELECT COALESCE(AVG(CAST(rating AS DOUBLE PRECISION)), 0.0) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            return 0.0;
        } catch (SQLException e) {
            logger.error("Error calculating average rating for product {}", productId, e);
            throw new DatabaseException("Failed to calculate average rating", e);
        }
    }

    @Override
    public boolean hasReviewed(Long userId, Long productId) throws DatabaseException {
        String sql = "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking existing review for user {} product {}", userId, productId, e);
            throw new DatabaseException("Failed to check existing review", e);
        }
    }

    @Override
    public boolean hasPurchasedProduct(Long buyerId, Long productId) throws DatabaseException {
        String sql = "SELECT 1 FROM orders o "
                   + "INNER JOIN order_items oi ON o.id = oi.order_id "
                   + "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED' LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking purchase history for user {} and product {}", buyerId, productId, e);
            throw new DatabaseException("Failed to check purchase history", e);
        }
    }

    @Override
    public Optional<Review> findById(Long id) throws DatabaseException {
        String sql = "SELECT id, product_id, user_id, rating, comment, created_at FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReview(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding review by id {}", id, e);
            throw new DatabaseException("Failed to find review by ID", e);
        }
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setProductId(rs.getLong("product_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
