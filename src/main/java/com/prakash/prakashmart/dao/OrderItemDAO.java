package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.OrderItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for order item persistence operations.
 */
public interface OrderItemDAO {

    /**
     * Saves an order item using an active transactional connection.
     *
     * @param conn Active database connection
     * @param item OrderItem to save
     * @return Saved OrderItem with generated ID
     * @throws DatabaseException if a database access error occurs
     */
    OrderItem save(Connection conn, OrderItem item) throws DatabaseException;

    /**
     * Finds an order item by its primary key ID.
     *
     * @param id Order item ID
     * @return Optional containing the OrderItem if found
     * @throws DatabaseException if a database access error occurs
     */
    Optional<OrderItem> findById(Long id) throws DatabaseException;

    /**
     * Finds all order items associated with an order ID.
     *
     * @param orderId Order ID
     * @return List of order items
     * @throws DatabaseException if a database access error occurs
     */
    List<OrderItem> findByOrderId(Long orderId) throws DatabaseException;

    /**
     * Finds all order items associated with an order ID using an active connection.
     *
     * @param conn Active database connection
     * @param orderId Order ID
     * @return List of order items
     * @throws DatabaseException if a database access error occurs
     */
    List<OrderItem> findByOrderId(Connection conn, Long orderId) throws DatabaseException;
}
