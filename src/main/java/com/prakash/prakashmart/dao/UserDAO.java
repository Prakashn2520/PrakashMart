package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user persistence operations.
 */
public interface UserDAO {

    /**
     * Creates a new user entity in the database.
     *
     * @param user User entity to persist
     * @return The persisted User with generated ID
     * @throws DatabaseException if a database access error occurs
     */
    User create(User user) throws DatabaseException;

    /**
     * Saves a new user entity to the database (alias for create).
     *
     * @param user User entity to persist
     * @return The persisted User with generated ID
     * @throws DatabaseException if a database access error occurs
     */
    User save(User user) throws DatabaseException;

    /**
     * Finds a user by unique ID.
     *
     * @param id User ID
     * @return Optional containing User if found, or empty Optional
     * @throws DatabaseException if a database access error occurs
     */
    Optional<User> findById(Long id) throws DatabaseException;

    /**
     * Finds a user by unique email address.
     *
     * @param email User email
     * @return Optional containing User if found, or empty Optional
     * @throws DatabaseException if a database access error occurs
     */
    Optional<User> findByEmail(String email) throws DatabaseException;

    /**
     * Updates an existing user record.
     *
     * @param user User entity to update
     * @throws DatabaseException if a database access error occurs
     */
    void update(User user) throws DatabaseException;

    /**
     * Retrieves all registered users (for admin moderation).
     *
     * @return List of all User entities
     * @throws DatabaseException if a database access error occurs
     */
    List<User> findAll() throws DatabaseException;
}
