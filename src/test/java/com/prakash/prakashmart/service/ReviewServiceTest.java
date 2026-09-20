package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dao.ReviewDAO;
import com.prakash.prakashmart.dao.UserDAO;
import com.prakash.prakashmart.dto.ReviewRequest;
import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ProductNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.model.Review;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private UserDAO userDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        this.reviewService = new ReviewServiceImpl(reviewDAO, productDAO, userDAO);
    }

    @Test
    @DisplayName("Should submit review when buyer has delivered purchase and hasn't reviewed yet")
    void testAddReviewSuccess() throws AppException {
        ReviewRequest request = new ReviewRequest(10L, 5, "Excellent quality!");
        Product product = new Product(10L, 1L, "Mouse", "Desc", new BigDecimal("25.00"), 50, "Electronics", null, true, null);
        User user = new User(2L, "Buyer Alice", "alice@test.com", "hash", Role.BUYER, null);

        when(productDAO.findById(10L)).thenReturn(Optional.of(product));
        when(userDAO.findById(2L)).thenReturn(Optional.of(user));
        when(reviewDAO.hasReviewed(2L, 10L)).thenReturn(false);
        when(reviewDAO.hasPurchasedProduct(2L, 10L)).thenReturn(true);

        Review saved = new Review(1L, 10L, 2L, 5, "Excellent quality!", null);
        when(reviewDAO.create(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.addReview(2L, request);
        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Excellent quality!", response.getComment());
        assertEquals("Buyer Alice", response.getUserName());
    }

    @Test
    @DisplayName("Should reject review submission if rating is out of 1-5 range")
    void testInvalidRating() {
        ReviewRequest requestZero = new ReviewRequest(10L, 0, "Bad");
        assertThrows(ValidationException.class, () -> reviewService.addReview(2L, requestZero));

        ReviewRequest requestSix = new ReviewRequest(10L, 6, "Too good");
        assertThrows(ValidationException.class, () -> reviewService.addReview(2L, requestSix));
    }

    @Test
    @DisplayName("Should reject review submission if user has not purchased the product with DELIVERED status")
    void testUserNotEligibleToReview() throws Exception {
        ReviewRequest request = new ReviewRequest(10L, 5, "Fake purchase");
        Product product = new Product(10L, 1L, "Mouse", "Desc", new BigDecimal("25.00"), 50, "Electronics", null, true, null);
        User user = new User(2L, "Buyer Alice", "alice@test.com", "hash", Role.BUYER, null);

        when(productDAO.findById(10L)).thenReturn(Optional.of(product));
        when(userDAO.findById(2L)).thenReturn(Optional.of(user));
        when(reviewDAO.hasReviewed(2L, 10L)).thenReturn(false);
        when(reviewDAO.hasPurchasedProduct(2L, 10L)).thenReturn(false); // not purchased!

        assertThrows(AuthorizationException.class, () -> reviewService.addReview(2L, request));
    }

    @Test
    @DisplayName("Should reject duplicate review if user has already reviewed the product")
    void testDuplicateReviewRejected() throws Exception {
        ReviewRequest request = new ReviewRequest(10L, 4, "Duplicate review");
        Product product = new Product(10L, 1L, "Mouse", "Desc", new BigDecimal("25.00"), 50, "Electronics", null, true, null);
        User user = new User(2L, "Buyer Alice", "alice@test.com", "hash", Role.BUYER, null);

        when(productDAO.findById(10L)).thenReturn(Optional.of(product));
        when(userDAO.findById(2L)).thenReturn(Optional.of(user));
        when(reviewDAO.hasReviewed(2L, 10L)).thenReturn(true); // already reviewed!

        assertThrows(ValidationException.class, () -> reviewService.addReview(2L, request));
    }

    @Test
    @DisplayName("Should calculate average rating and retrieve product reviews with reviewer names")
    void testGetReviewsAndAverageRating() throws AppException {
        when(reviewDAO.calculateAverageRating(10L)).thenReturn(4.5);
        double avg = reviewService.getAverageRating(10L);
        assertEquals(4.5, avg);

        Review r1 = new Review(1L, 10L, 2L, 5, "Good", null);
        when(reviewDAO.findByProduct(10L)).thenReturn(List.of(r1));
        when(userDAO.findById(2L)).thenReturn(Optional.of(new User(2L, "Alice", "alice@test.com", "hash", Role.BUYER, null)));

        List<ReviewResponse> list = reviewService.getProductReviews(10L);
        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getUserName());
    }
}
