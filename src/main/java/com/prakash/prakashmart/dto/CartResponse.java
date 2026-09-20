package com.prakash.prakashmart.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing full cart overview including total amount and item count.
 */
public class CartResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalCount = 0;

    public CartResponse() {
    }

    public CartResponse(List<CartItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }

    public CartResponse(List<? extends CartItemResponse> items, BigDecimal totalAmount) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
        this.totalCount = this.items.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
    }

    public void calculateTotals() {
        this.totalAmount = BigDecimal.ZERO;
        this.totalCount = 0;
        if (items != null) {
            for (CartItemResponse item : items) {
                if (item.getSubtotal() != null) {
                    this.totalAmount = this.totalAmount.add(item.getSubtotal());
                }
                this.totalCount += item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
