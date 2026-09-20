package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.model.Review;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    @DisplayName("ApiResponse should format success and error envelopes properly")
    void testApiResponseEnvelope() {
        ApiResponse<String> successResp = ApiResponse.success("Operation completed", "test-data");
        assertTrue(successResp.isSuccess());
        assertEquals("Operation completed", successResp.getMessage());
        assertEquals("test-data", successResp.getData());
        assertNull(successResp.getErrorCode());

        ApiResponse<Void> errorResp = ApiResponse.error("PRODUCT_NOT_FOUND", "Product not found");
        assertFalse(errorResp.isSuccess());
        assertEquals("Product not found", errorResp.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", errorResp.getErrorCode());
        assertNotNull(errorResp.getError());
        assertEquals("PRODUCT_NOT_FOUND", errorResp.getError().getCode());
        assertEquals("Product not found", errorResp.getError().getMessage());
    }

    @Test
    @DisplayName("UserResponse and UserResponseDTO must NEVER expose password")
    void testUserResponseSecurity() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        User user = new User(1L, "Prakash", "prakash@test.com", "secret_hash", Role.SELLER, true, now);

        UserResponse userResponse = UserResponse.fromUser(user);
        assertNotNull(userResponse);
        assertEquals(1L, userResponse.getId());
        assertEquals("Prakash", userResponse.getName());
        assertEquals("prakash@test.com", userResponse.getEmail());
        assertEquals("SELLER", userResponse.getRole());
        assertTrue(userResponse.getIsActive());

        // Verify toString does not leak password
        assertFalse(userResponse.toString().contains("secret_hash"));

        UserResponseDTO userResponseDTO = UserResponseDTO.fromUser(user);
        assertEquals(userResponse.getId(), userResponseDTO.getId());
    }

    @Test
    @DisplayName("ProductResponse, CartResponse, OrderResponse, and ReviewResponse mapping")
    void testDtoMappings() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Product product = new Product(10L, 2L, "Keyboard", "Desc", new BigDecimal("50.00"), 10, "Electronics", "img.png", true, now);
        ProductResponse prodResp = ProductResponse.fromProduct(product, "Seller Name");
        assertEquals("Seller Name", prodResp.getSellerName());
        assertEquals("Keyboard", prodResp.getName());

        // CartResponse total calculation
        CartItemResponse item1 = new CartItemResponse(1L, 10L, "Keyboard", new BigDecimal("50.00"), 2, 10, new BigDecimal("100.00"), "img.png");
        CartItemResponse item2 = new CartItemResponse(2L, 11L, "Mouse", new BigDecimal("25.00"), 1, 5, new BigDecimal("25.00"), "img.png");
        CartResponse cartResponse = new CartResponse(List.of(item1, item2));
        assertEquals(0, new BigDecimal("125.00").compareTo(cartResponse.getTotalAmount()));
        assertEquals(3, cartResponse.getTotalCount());

        // OrderResponse mapping
        Order order = new Order(100L, 1L, OrderStatus.PENDING, new BigDecimal("125.00"), now);
        OrderItem orderItem = new OrderItem(1L, 100L, 10L, 2, new BigDecimal("50.00"));
        OrderItemResponse orderItemResp = OrderItemResponse.fromOrderItem(orderItem, "Keyboard");
        OrderResponse orderResp = OrderResponse.fromOrder(order, "Buyer Name", List.of(orderItemResp));
        assertEquals("Buyer Name", orderResp.getBuyerName());
        assertEquals(1, orderResp.getItems().size());
        assertEquals(0, new BigDecimal("100.00").compareTo(orderResp.getItems().get(0).getSubtotal()));

        // ReviewResponse mapping
        Review review = new Review(1L, 10L, 1L, 5, "Great keyboard", now);
        ReviewResponse reviewResp = ReviewResponse.fromReview(review, "Buyer Name");
        assertEquals("Buyer Name", reviewResp.getUserName());
        assertEquals(5, reviewResp.getRating());
    }
}
