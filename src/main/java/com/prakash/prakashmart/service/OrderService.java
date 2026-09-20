package com.prakash.prakashmart.service;

import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.model.OrderStatus;

import java.util.List;

/**
 * Service interface for order checkout, atomic transactions, status workflows, and role-based visibility.
 */
public interface OrderService {

    /**
     * Executes the atomic transactional checkout workflow:
     * 1. Load cart
     * 2. Validate cart is not empty
     * 3. Reload current product prices from database
     * 4. Validate current stock
     * 5. Calculate total on server
     * 6. Create order
     * 7. Create order items
     * 8. Reduce stock
     * 9. Clear cart
     * 10. Commit (or rollback everything on any failure)
     *
     * @param buyerId Buyer ID
     * @return Completed Order
     * @throws AppException if transaction fails or stock is exhausted
     */
    Order checkout(Long buyerId) throws AppException;

    /**
     * Retrieves an order by ID.
     *
     * @param orderId Order ID
     * @return Order
     * @throws AppException if order not found
     */
    Order getOrderById(Long orderId) throws AppException;

    /**
     * Retrieves an order by ID with authorization check:
     * Buyers can only view their own orders, sellers can view orders with their products, admins can view all.
     *
     * @param orderId Order ID
     * @param userId  Requesting user ID
     * @param role    Requesting user role ("BUYER", "SELLER", "ADMIN")
     * @return Order
     * @throws AppException if unauthorized or not found
     */
    Order getOrderById(Long orderId, Long userId, String role) throws AppException;

    /**
     * Retrieves all items in an order.
     *
     * @param orderId Order ID
     * @return List of OrderItems
     * @throws AppException if query fails
     */
    List<OrderItem> getOrderItems(Long orderId) throws AppException;

    /**
     * Retrieves order history for a buyer.
     *
     * @param buyerId Buyer ID
     * @return List of Orders
     * @throws AppException if query fails
     */
    List<Order> getOrdersByBuyer(Long buyerId) throws AppException;

    /**
     * Retrieves incoming orders containing products owned by a seller.
     *
     * @param sellerId Seller ID
     * @return List of Orders
     * @throws AppException if query fails
     */
    List<Order> getOrdersForSeller(Long sellerId) throws AppException;

    /**
     * Retrieves all orders for admin review.
     *
     * @return List of all Orders
     * @throws AppException if query fails
     */
    List<Order> getAllOrders() throws AppException;

    /**
     * Updates an order's status following valid state transitions:
     * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED (or CANCELLED).
     *
     * @param orderId   Order ID
     * @param newStatus New OrderStatus
     * @throws AppException if transition is invalid
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus) throws AppException;

    /**
     * Cancels an order if eligible (buyer or seller/admin; only when PENDING or CONFIRMED).
     * Restores product stock upon successful cancellation.
     *
     * @param orderId Order ID
     * @param userId  Requesting user ID
     * @param role    Requesting user role
     * @throws AppException if cancellation rules are violated
     */
    void cancelOrder(Long orderId, Long userId, String role) throws AppException;
}
