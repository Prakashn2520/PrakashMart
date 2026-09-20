package com.prakash.prakashmart.model;

/**
 * Lifecycle states of an order in PrakashMart.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null) {
            return null;
        }
        String clean = statusStr.trim().toUpperCase();
        if ("PROCESSING".equals(clean)) {
            return CONFIRMED;
        }
        try {
            return OrderStatus.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
