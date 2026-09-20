package com.prakash.prakashmart.exception;

/**
 * Thrown when an order operation (creation, checkout, status change, cancellation) fails.
 */
public class OrderException extends AppException {

    public OrderException(String message) {
        super(message, "ORDER_ERROR");
    }

    public OrderException(String message, String errorCode) {
        super(message, errorCode);
    }

    public OrderException(String message, Throwable cause) {
        super(message, "ORDER_ERROR", cause);
    }
}
