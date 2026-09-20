package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dao.impl.OrderDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.ReviewDAOImpl;
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

class ReviewDAOTest extends BaseDAOTest {

    private ReviewDAO reviewDAO;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;

    private Long buyerId;
    private Long sellerId;
    private Long productId;

    @BeforeEach
    void setUp() throws DatabaseException {
        this.reviewDAO = new ReviewDAOImpl();
        this.orderDAO = new OrderDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();

        User seller = userDAO.save(new User(null, "Rev Seller", "revseller@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.save(new User(null, "Rev Buyer", "revbuyer@test.com", "hash", Role.BUYER, null));
        this.sellerId = seller.getId();
        this.buyerId = buyer.getId();

        Product product = productDAO.save(new Product(null, seller.getId(), "Smart Watch", "Fitness watch", new BigDecimal("199.99"), 20, "Electronics", null, null));
        this.productId = product.getId();
    }

    @Test
    @DisplayName("Should create review and retrieve by product ID")
    void testSaveAndFindReviews() throws DatabaseException {
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyerId);
        review.setRating(5);
        review.setComment("Amazing smartwatch, highly recommended!");

        Review saved = reviewDAO.create(review);
        assertNotNull(saved.getId());

        List<Review> reviews = reviewDAO.findByProduct(productId);
        assertEquals(1, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
        assertEquals("Amazing smartwatch, highly recommended!", reviews.get(0).getComment());

        Optional<Review> byId = reviewDAO.findById(saved.getId());
        assertTrue(byId.isPresent());
        assertEquals(5, byId.get().getRating());
    }

    @Test
    @DisplayName("Should calculate average rating correctly")
    void testCalculateAverageRating() throws DatabaseException {
        // Average with 0 reviews
        assertEquals(0.0, reviewDAO.calculateAverageRating(productId));

        // User 1 review: 5
        reviewDAO.create(new Review(null, productId, buyerId, 5, "Great", null));

        // Create another buyer
        User buyer2 = userDAO.save(new User(null, "Buyer 2", "buyer2@test.com", "hash", Role.BUYER, null));
        reviewDAO.create(new Review(null, productId, buyer2.getId(), 3, "Average", null));

        double avg = reviewDAO.calculateAverageRating(productId);
        assertEquals(4.0, avg, 0.001);
    }

    @Test
    @DisplayName("Should verify existing review where required and enforce unique constraint")
    void testHasReviewedAndUniqueConstraint() throws DatabaseException {
        assertFalse(reviewDAO.hasReviewed(buyerId, productId));

        reviewDAO.create(new Review(null, productId, buyerId, 4, "Nice", null));

        assertTrue(reviewDAO.hasReviewed(buyerId, productId));

        // Duplicate review by same user on same product must fail constraint
        Review duplicate = new Review(null, productId, buyerId, 5, "Again", null);
        assertThrows(DatabaseException.class, () -> reviewDAO.create(duplicate));
    }

    @Test
    @DisplayName("Should verify purchase history only for DELIVERED orders")
    void testHasPurchasedProduct() throws Exception {
        // Initially buyer has not purchased
        assertFalse(reviewDAO.hasPurchasedProduct(buyerId, productId));

        // Create a DELIVERED order
        try (Connection conn = DatabaseUtil.getConnection()) {
            Order order = new Order(null, buyerId, OrderStatus.DELIVERED, new BigDecimal("199.99"), null);
            Order savedOrder = orderDAO.save(conn, order);
            orderDAO.saveItem(conn, new OrderItem(null, savedOrder.getId(), productId, 1, new BigDecimal("199.99")));
        }

        // Now buyer should show as verified purchaser
        assertTrue(reviewDAO.hasPurchasedProduct(buyerId, productId));
    }

    @Test
    @DisplayName("Should return empty list for product with no reviews and empty optional for invalid ID")
    void testEmptyReviewsAndInvalidId() throws DatabaseException {
        List<Review> emptyList = reviewDAO.findByProduct(999999L);
        assertTrue(emptyList.isEmpty());

        Optional<Review> emptyReview = reviewDAO.findById(999999L);
        assertFalse(emptyReview.isPresent());
    }
}
