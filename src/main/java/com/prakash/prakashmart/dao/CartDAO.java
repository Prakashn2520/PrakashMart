package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user cart persistence operations.
 */
public interface CartDAO {

    /**
     * Adds an item to the user's cart or increments quantity if already present.
     *
     * @param userId    User ID
     * @param productId Product ID
     * @param quantity  Quantity to add
     * @throws DatabaseException if a database error occurs
     */
    void add(Long userId, Long productId, int quantity) throws DatabaseException;

    /**
     * Alias for add.
     *
     * @param userId    User ID
     * @param productId Product ID
     * @param quantity  Quantity to add
     * @throws DatabaseException if a database error occurs
     */
    void addItem(Long userId, Long productId, int quantity) throws DatabaseException;

    /**
     * Updates quantity of a cart item.
     *
     * @param cartItemId  Cart item ID
     * @param newQuantity New quantity
     * @throws DatabaseException if a database error occurs
     */
    void update(Long cartItemId, int newQuantity) throws DatabaseException;

    /**
     * Alias for update.
     *
     * @param cartItemId  Cart item ID
     * @param newQuantity New quantity
     * @throws DatabaseException if a database error occurs
     */
    void updateQuantity(Long cartItemId, int newQuantity) throws DatabaseException;

    /**
     * Removes an item from the cart.
     *
     * @param cartItemId Cart item ID
     * @throws DatabaseException if a database error occurs
     */
    void remove(Long cartItemId) throws DatabaseException;

    /**
     * Alias for remove.
     *
     * @param cartItemId Cart item ID
     * @throws DatabaseException if a database error occurs
     */
    void removeItem(Long cartItemId) throws DatabaseException;

    /**
     * Clears all items in a user's cart.
     *
     * @param userId User ID
     * @throws DatabaseException if a database error occurs
     */
    void clear(Long userId) throws DatabaseException;

    /**
     * Clears all items in a user's cart, optionally within an existing transaction.
     *
     * @param conn   Active database connection (can be null if autocommit)
     * @param userId User ID
     * @throws DatabaseException if a database error occurs
     */
    void clearCart(Connection conn, Long userId) throws DatabaseException;

    /**
     * Finds cart items for a user as raw CartItem entities.
     *
     * @param userId User ID
     * @return List of CartItems
     * @throws DatabaseException if a database error occurs
     */
    List<CartItem> findByUser(Long userId) throws DatabaseException;

    /**
     * Retrieves rich cart item DTOs with product information and calculated totals for a user.
     *
     * @param userId User ID
     * @return List of CartItemDTOs
     * @throws DatabaseException if a database error occurs
     */
    List<CartItemDTO> getCartByUserId(Long userId) throws DatabaseException;

    /**
     * Finds a specific cart item by primary key ID.
     *
     * @param cartItemId Cart item ID
     * @return Optional containing the CartItem if present
     * @throws DatabaseException if a database error occurs
     */
    Optional<CartItem> findById(Long cartItemId) throws DatabaseException;

    /**
     * Finds a specific cart item by user ID and product ID.
     *
     * @param userId    User ID
     * @param productId Product ID
     * @return Optional containing the CartItem if present
     * @throws DatabaseException if a database error occurs
     */
    Optional<CartItem> findItem(Long userId, Long productId) throws DatabaseException;
}
