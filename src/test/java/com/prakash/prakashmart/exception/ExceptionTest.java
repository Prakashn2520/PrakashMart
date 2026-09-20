package com.prakash.prakashmart.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    @DisplayName("AppException should carry message and default/custom errorCode")
    void testAppException() {
        AppException ex1 = new AppException("Generic error");
        assertEquals("Generic error", ex1.getMessage());
        assertEquals("APP_ERROR", ex1.getErrorCode());

        AppException ex2 = new AppException("Custom error", "CUSTOM_CODE");
        assertEquals("Custom error", ex2.getMessage());
        assertEquals("CUSTOM_CODE", ex2.getErrorCode());

        RuntimeException cause = new RuntimeException("root cause");
        AppException ex3 = new AppException("With cause", "CUSTOM_CODE", cause);
        assertEquals(cause, ex3.getCause());
    }

    @Test
    @DisplayName("ValidationException should carry field and VALIDATION_ERROR code")
    void testValidationException() {
        ValidationException ex = new ValidationException("email", "Invalid email");
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertEquals("email", ex.getField());
        assertEquals("Invalid email", ex.getMessage());
    }

    @Test
    @DisplayName("AuthenticationException and AuthorizationException should have appropriate errorCodes")
    void testAuthExceptions() {
        AuthenticationException authEx = new AuthenticationException("Bad credentials");
        assertEquals("AUTHENTICATION_FAILED", authEx.getErrorCode());

        AuthorizationException authzEx = new AuthorizationException("Not an admin");
        assertEquals("ACCESS_DENIED", authzEx.getErrorCode());
    }

    @Test
    @DisplayName("ResourceNotFoundException and ProductNotFoundException should carry resource details")
    void testNotFoundExceptions() {
        ResourceNotFoundException rnf = new ResourceNotFoundException("Item not found");
        assertEquals("RESOURCE_NOT_FOUND", rnf.getErrorCode());

        ProductNotFoundException pnf = new ProductNotFoundException(42L);
        assertEquals("PRODUCT_NOT_FOUND", pnf.getErrorCode());
        assertEquals(42L, pnf.getProductId());
        assertTrue(pnf.getMessage().contains("42"));
    }

    @Test
    @DisplayName("InsufficientStockException, OrderException, and DatabaseException should have proper codes")
    void testDomainExceptions() {
        InsufficientStockException stockEx = new InsufficientStockException(10L, 5, 2);
        assertEquals("INSUFFICIENT_STOCK", stockEx.getErrorCode());
        assertEquals(10L, stockEx.getProductId());
        assertEquals(5, stockEx.getRequestedQuantity());
        assertEquals(2, stockEx.getAvailableStock());

        OrderException orderEx = new OrderException("Order cancelled");
        assertEquals("ORDER_ERROR", orderEx.getErrorCode());

        DatabaseException dbEx = new DatabaseException("Connection timeout");
        assertEquals("DATABASE_ERROR", dbEx.getErrorCode());
    }
}
