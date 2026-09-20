package com.prakash.prakashmart.exception;

/**
 * Thrown when an order or cart operation cannot be fulfilled due to insufficient product inventory.
 */
public class InsufficientStockException extends AppException {

    private final Long productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(Long productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d",
                productId, requestedQuantity, availableStock), "INSUFFICIENT_STOCK");
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public InsufficientStockException(String message) {
        super(message, "INSUFFICIENT_STOCK");
        this.productId = null;
        this.requestedQuantity = 0;
        this.availableStock = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
