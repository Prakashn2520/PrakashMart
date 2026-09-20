package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.model.OrderStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for orders and order items management.
 */
public interface OrderDAO {

    /**
     * Creates a new order along with its line items within an atomic transaction.
     *
     * @param order Order entity
     * @param items List of OrderItems
     * @return Persisted Order with ID set
     * @throws DatabaseException if creation fails
     */
    Order create(Order order, List<OrderItem> items) throws DatabaseException;

    /**
     * Finds an order by its ID.
     *
     * @param id Order ID
     * @return Optional containing the Order if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<Order> findById(Long id) throws DatabaseException;

    /**
     * Retrieves all orders placed by a specific buyer.
     *
     * @param buyerId Buyer user ID
     * @return List of orders
     * @throws DatabaseException if a database error occurs
     */
    List<Order> findByBuyer(Long buyerId) throws DatabaseException;

    /**
     * Alias for findByBuyer.
     *
     * @param buyerId Buyer user ID
     * @return List of orders
     * @throws DatabaseException if a database error occurs
     */
    List<Order> findByBuyerId(Long buyerId) throws DatabaseException;

    /**
     * Retrieves all incoming orders containing products owned by a specific seller.
     *
     * @param sellerId Seller user ID
     * @return List of orders
     * @throws DatabaseException if a database error occurs
     */
    List<Order> findBySeller(Long sellerId) throws DatabaseException;

    /**
     * Alias for findBySeller.
     *
     * @param sellerId Seller user ID
     * @return List of orders
     * @throws DatabaseException if a database error occurs
     */
    List<Order> findBySellerId(Long sellerId) throws DatabaseException;

    /**
     * Retrieves all orders in the system for admin review.
     *
     * @return List of all orders
     * @throws DatabaseException if a database error occurs
     */
    List<Order> findAll() throws DatabaseException;

    /**
     * Saves a new order within an existing transactional connection.
     *
     * @param conn  Active transactional database connection
     * @param order Order entity to persist
     * @return Persisted Order with generated ID
     * @throws DatabaseException if a database error occurs
     */
    Order save(Connection conn, Order order) throws DatabaseException;

    /**
     * Saves an order line item within an existing transactional connection.
     *
     * @param conn Active transactional database connection
     * @param item OrderItem entity to persist
     * @return Persisted OrderItem with generated ID
     * @throws DatabaseException if a database error occurs
     */
    OrderItem saveItem(Connection conn, OrderItem item) throws DatabaseException;

    /**
     * Retrieves line items belonging to an order.
     *
     * @param orderId Order ID
     * @return List of OrderItem entities
     * @throws DatabaseException if a database error occurs
     */
    List<OrderItem> findItemsByOrderId(Long orderId) throws DatabaseException;

    /**
     * Updates an order's status.
     *
     * @param orderId   Order ID
     * @param newStatus New OrderStatus
     * @throws DatabaseException if a database error occurs
     */
    void updateStatus(Long orderId, OrderStatus newStatus) throws DatabaseException;

    /**
     * Cancels an order if allowed (only when PENDING or CONFIRMED).
     *
     * @param orderId Order ID
     * @return true if successfully cancelled, false if not found or in un-cancellable state
     * @throws DatabaseException if a database error occurs
     */
    boolean cancel(Long orderId) throws DatabaseException;
}
