package com.prakash.prakashmart.dto;

import java.io.Serializable;

/**
 * DTO for specifying an item in direct order requests.
 */
public class OrderItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
