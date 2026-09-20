package com.prakash.prakashmart.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for checkout / order placement requests.
 */
public class OrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<OrderItemRequest> items = new ArrayList<>();
    private String shippingAddress;
    private String paymentMethod;

    public OrderRequest() {
    }

    public OrderRequest(List<OrderItemRequest> items, String shippingAddress, String paymentMethod) {
        this.items = items != null ? items : new ArrayList<>();
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
