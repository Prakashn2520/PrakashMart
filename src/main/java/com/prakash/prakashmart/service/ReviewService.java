package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dto.ReviewRequest;
import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.exception.AppException;

import java.util.List;

/**
 * Service interface for review submission, ratings, and purchase eligibility verification.
 */
public interface ReviewService {

    /**
     * Submits a new review for a product with full business validation:
     * - Rating between 1 and 5
     * - Non-blank comment under 2000 characters
     * - Buyer must have purchased product with a completed/DELIVERED order
     * - User cannot submit duplicate reviews for the same product
     *
     * @param userId  Submitting user ID
     * @param request Review request DTO
     * @return ReviewResponse with persisted details
     * @throws AppException if validation fails, unauthorized, or not eligible
     */
    ReviewResponse addReview(Long userId, ReviewRequest request) throws AppException;

    /**
     * Retrieves all reviews for a product.
     *
     * @param productId Product ID
     * @return List of ReviewResponses
     * @throws AppException if query fails
     */
    List<ReviewResponse> getProductReviews(Long productId) throws AppException;

    /**
     * Calculates the average rating for a product.
     *
     * @param productId Product ID
     * @return Average star rating
     * @throws AppException if query fails
     */
    double getAverageRating(Long productId) throws AppException;

    /**
     * Checks if a user is eligible to review a product:
     * - Has purchased and received the product (DELIVERED status)
     * - Has not yet reviewed the product
     *
     * @param userId    User ID
     * @param productId Product ID
     * @return true if eligible
     * @throws AppException if query fails
     */
    boolean canUserReview(Long userId, Long productId) throws AppException;
}
