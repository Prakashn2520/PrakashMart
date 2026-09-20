package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.Order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing an order in responses, including its line items.
 */
public class OrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long buyerId;
    private String buyerName;
    private String status;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    private List<OrderItemResponse> items = new ArrayList<>();

    public OrderResponse() {
    }

    public OrderResponse(Long id, Long buyerId, String buyerName, String status, BigDecimal totalAmount,
                         Timestamp createdAt, List<OrderItemResponse> items) {
        this.id = id;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.items = items != null ? items : new ArrayList<>();
    }

    public static OrderResponse fromOrder(Order order, List<?> items) {
        if (order == null) {
            return null;
        }
        List<OrderItemResponse> responseItems = new ArrayList<>();
        if (items != null) {
            for (Object obj : items) {
                if (obj instanceof OrderItemResponse) {
                    responseItems.add((OrderItemResponse) obj);
                } else if (obj instanceof com.prakash.prakashmart.model.OrderItem) {
                    responseItems.add(OrderItemResponse.fromOrderItem((com.prakash.prakashmart.model.OrderItem) obj));
                }
            }
        }
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                null,
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalAmount(),
                order.getCreatedAt(),
                responseItems
        );
    }

    public static OrderResponse fromOrder(Order order, String buyerName, List<OrderItemResponse> items) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                buyerName,
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", buyerId=" + buyerId +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
