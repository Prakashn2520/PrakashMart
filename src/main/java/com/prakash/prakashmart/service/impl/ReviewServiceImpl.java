package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dao.ReviewDAO;
import com.prakash.prakashmart.dao.UserDAO;
import com.prakash.prakashmart.dto.ReviewRequest;
import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.exception.*;
import com.prakash.prakashmart.model.Review;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.service.ReviewService;
import com.prakash.prakashmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for reviews, rating calculations, and purchase eligibility enforcement.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    public ReviewServiceImpl(ReviewDAO reviewDAO, ProductDAO productDAO, UserDAO userDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
    }

    @Override
    public ReviewResponse addReview(Long userId, ReviewRequest request) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID");
        }
        if (request == null) {
            throw new ValidationException("Review request cannot be empty");
        }

        ValidationUtil.validateId(request.getProductId(), "productId");
        ValidationUtil.validateRating(request.getRating());
        ValidationUtil.validateComment(request.getComment());

        // Verify product exists
        productDAO.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // Verify user exists
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Business rule: User cannot review twice
        if (reviewDAO.hasReviewed(userId, request.getProductId())) {
            throw new ValidationException("You have already submitted a review for this product");
        }

        // Business rule: Only buyers who have a DELIVERED order with this product can review
        boolean purchased = reviewDAO.hasPurchasedProduct(userId, request.getProductId());
        if (!purchased) {
            throw new AuthorizationException("Only verified purchasers of delivered orders can review this product");
        }

        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);

        Review saved = reviewDAO.create(review);
        logger.info("Review {} submitted by user {} for product {}", saved.getId(), userId, request.getProductId());

        return ReviewResponse.fromReview(saved, user.getName());
    }

    @Override
    public List<ReviewResponse> getProductReviews(Long productId) throws AppException {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID");
        }
        List<Review> reviews = reviewDAO.findByProduct(productId);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review r : reviews) {
            String userName = "Anonymous";
            Optional<User> u = userDAO.findById(r.getUserId());
            if (u.isPresent()) {
                userName = u.get().getName();
            }
            responses.add(ReviewResponse.fromReview(r, userName));
        }
        return responses;
    }

    @Override
    public double getAverageRating(Long productId) throws AppException {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID");
        }
        return reviewDAO.calculateAverageRating(productId);
    }

    @Override
    public boolean canUserReview(Long userId, Long productId) throws AppException {
        if (userId == null || userId <= 0 || productId == null || productId <= 0) {
            return false;
        }
        if (reviewDAO.hasReviewed(userId, productId)) {
            return false;
        }
        return reviewDAO.hasPurchasedProduct(userId, productId);
    }
}
