package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;

import java.util.List;

/**
 * Service interface for user authentication, registration, and administration.
 */
public interface UserService {

    /**
     * Registers a new user with BCrypt hashed password.
     *
     * @param request Registration payload
     * @return UserResponseDTO without password hash
     * @throws AppException if validation fails or email already registered
     */
    UserResponseDTO register(RegisterRequestDTO request) throws AppException;

    /**
     * Authenticates a user against stored BCrypt credentials.
     *
     * @param request Login credentials
     * @return Authenticated UserResponseDTO
     * @throws AppException if credentials are invalid
     */
    UserResponseDTO authenticate(LoginRequestDTO request) throws AppException;

    /**
     * Retrieves a user by ID.
     *
     * @param id User ID
     * @return UserResponseDTO
     * @throws AppException if not found
     */
    UserResponseDTO getUserById(Long id) throws AppException;

    /**
     * Retrieves all registered users for admin dashboard.
     *
     * @return List of UserResponseDTOs
     * @throws AppException if query fails
     */
    List<UserResponseDTO> getAllUsers() throws AppException;
}
