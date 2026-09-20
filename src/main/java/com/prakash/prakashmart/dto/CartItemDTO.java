package com.prakash.prakashmart.dto;

import java.math.BigDecimal;

/**
 * Backward-compatible alias for CartItemResponse.
 */
public class CartItemDTO extends CartItemResponse {
    private static final long serialVersionUID = 1L;

    public CartItemDTO() {
        super();
    }

    public CartItemDTO(Long cartItemId, Long productId, String productName, BigDecimal unitPrice,
                       Integer quantity, Integer availableStock, BigDecimal subtotal) {
        super(cartItemId, productId, productName, unitPrice, quantity, availableStock, subtotal, null);
    }

    public CartItemDTO(Long cartItemId, Long productId, String productName, BigDecimal unitPrice,
                       Integer quantity, Integer availableStock, BigDecimal subtotal, String imageUrl) {
        super(cartItemId, productId, productName, unitPrice, quantity, availableStock, subtotal, imageUrl);
    }
}
