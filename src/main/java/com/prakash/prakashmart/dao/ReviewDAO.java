package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for product reviews and ratings.
 */
public interface ReviewDAO {

    /**
     * Creates a new review.
     *
     * @param review Review entity to insert
     * @return Persisted Review with generated ID
     * @throws DatabaseException if a database error occurs
     */
    Review create(Review review) throws DatabaseException;

    /**
     * Saves a new review (alias for create).
     *
     * @param review Review entity to insert
     * @return Persisted Review with generated ID
     * @throws DatabaseException if a database error occurs
     */
    Review save(Review review) throws DatabaseException;

    /**
     * Retrieves all reviews submitted for a given product.
     *
     * @param productId Product ID
     * @return List of Review entities
     * @throws DatabaseException if a database error occurs
     */
    List<Review> findByProduct(Long productId) throws DatabaseException;

    /**
     * Retrieves all reviews submitted for a given product (alias for findByProduct).
     *
     * @param productId Product ID
     * @return List of Review entities
     * @throws DatabaseException if a database error occurs
     */
    List<Review> findByProductId(Long productId) throws DatabaseException;

    /**
     * Calculates the average star rating for a given product.
     *
     * @param productId Product ID
     * @return Average rating (0.0 if no reviews exist)
     * @throws DatabaseException if a database error occurs
     */
    double calculateAverageRating(Long productId) throws DatabaseException;

    /**
     * Checks if a user has already reviewed a given product.
     *
     * @param userId    User ID
     * @param productId Product ID
     * @return true if user has already reviewed this product
     * @throws DatabaseException if a database error occurs
     */
    boolean hasReviewed(Long userId, Long productId) throws DatabaseException;

    /**
     * Checks if a buyer has purchased the product (required before allowing a review).
     *
     * @param buyerId   Buyer ID
     * @param productId Product ID
     * @return true if buyer has a completed order containing this product
     * @throws DatabaseException if a database error occurs
     */
    boolean hasPurchasedProduct(Long buyerId, Long productId) throws DatabaseException;

    /**
     * Finds a review by its primary key.
     *
     * @param id Review ID
     * @return Optional containing Review if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<Review> findById(Long id) throws DatabaseException;
}
