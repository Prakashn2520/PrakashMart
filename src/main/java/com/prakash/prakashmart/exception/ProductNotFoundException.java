package com.prakash.prakashmart.exception;

/**
 * Specific exception thrown when a requested product does not exist.
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId, "PRODUCT_NOT_FOUND");
        this.productId = productId;
    }

    public ProductNotFoundException(String message) {
        super(message, "PRODUCT_NOT_FOUND");
        this.productId = null;
    }

    public Long getProductId() {
        return productId;
    }
}
