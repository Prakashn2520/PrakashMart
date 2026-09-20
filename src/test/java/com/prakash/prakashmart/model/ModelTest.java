package com.prakash.prakashmart.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("User entity should hold properties and NEVER expose passwordHash in toString()")
    void testUserModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        User user = new User(1L, "Prakash User", "prakash@test.com", "super_secret_bcrypt_hash", Role.SELLER, true, now);

        assertEquals(1L, user.getId());
        assertEquals("Prakash User", user.getName());
        assertEquals("prakash@test.com", user.getEmail());
        assertEquals("super_secret_bcrypt_hash", user.getPasswordHash());
        assertEquals(Role.SELLER, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());

        // Test equals & hashCode
        User user2 = new User(1L, "Prakash User", "prakash@test.com", "other_hash", Role.SELLER, true, now);
        assertEquals(user, user2);
        assertEquals(user.hashCode(), user2.hashCode());

        // SECURITY CHECK: toString must NOT leak password
        String toString = user.toString();
        assertFalse(toString.contains("super_secret_bcrypt_hash"), "User.toString() must never contain password hash");
        assertTrue(toString.contains("Prakash User"));
        assertTrue(toString.contains("prakash@test.com"));
    }

    @Test
    @DisplayName("Product entity should hold all attributes and have working equals/hashCode")
    void testProductModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Product p = new Product(10L, 2L, "Keyboard", "Mechanical", new BigDecimal("79.99"), 25, "Electronics", "img.jpg", true, now);

        assertEquals(10L, p.getId());
        assertEquals(2L, p.getSellerId());
        assertEquals("Keyboard", p.getName());
        assertEquals("Mechanical", p.getDescription());
        assertEquals(new BigDecimal("79.99"), p.getPrice());
        assertEquals(25, p.getStockQty());
        assertEquals("Electronics", p.getCategory());
        assertEquals("img.jpg", p.getImageUrl());
        assertTrue(p.getIsActive());
        assertEquals(now, p.getCreatedAt());

        Product p2 = new Product(10L, 2L, "Keyboard", "Mechanical", new BigDecimal("79.99"), 25, "Electronics", "img.jpg", true, now);
        assertEquals(p, p2);
        assertEquals(p.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("Order and OrderItem entities should hold correct values and calculate subtotals")
    void testOrderAndOrderItemModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Order order = new Order(100L, 5L, OrderStatus.CONFIRMED, new BigDecimal("150.00"), now);

        assertEquals(100L, order.getId());
        assertEquals(5L, order.getBuyerId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("150.00"), order.getTotalAmount());

        OrderItem item = new OrderItem(1L, 100L, 10L, 3, new BigDecimal("50.00"));
        assertEquals(1L, item.getId());
        assertEquals(100L, item.getOrderId());
        assertEquals(10L, item.getProductId());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("50.00"), item.getUnitPrice());
        assertEquals(0, new BigDecimal("150.00").compareTo(item.getSubtotal()));
    }

    @Test
    @DisplayName("CartItem and Review entities should hold appropriate attributes")
    void testCartItemAndReviewModel() {
        CartItem cartItem = new CartItem(1L, 5L, 10L, 2);
        assertEquals(1L, cartItem.getId());
        assertEquals(5L, cartItem.getUserId());
        assertEquals(10L, cartItem.getProductId());
        assertEquals(2, cartItem.getQuantity());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Review review = new Review(1L, 10L, 5L, 5, "Great product", now);
        assertEquals(1L, review.getId());
        assertEquals(10L, review.getProductId());
        assertEquals(5L, review.getUserId());
        assertEquals(5, review.getRating());
        assertEquals("Great product", review.getComment());
        assertEquals(now, review.getCreatedAt());
    }

    @Test
    @DisplayName("Role and OrderStatus enums should convert from string case-insensitively")
    void testEnums() {
        assertEquals(Role.BUYER, Role.fromString("BUYER"));
        assertEquals(Role.SELLER, Role.fromString("seller"));
        assertEquals(Role.ADMIN, Role.fromString("Admin"));
        assertNull(Role.fromString("invalid"));

        assertEquals(OrderStatus.PENDING, OrderStatus.fromString("PENDING"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.fromString("delivered"));
        assertNull(OrderStatus.fromString("unknown"));
    }
}
