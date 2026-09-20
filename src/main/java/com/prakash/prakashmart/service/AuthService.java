package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Service interface for authentication, registration, session management, and logout.
 */
public interface AuthService {

    /**
     * Registers a new user with BCrypt hashed password and validation checks.
     *
     * @param request Registration payload
     * @return UserResponseDTO without password hash
     * @throws AppException if validation fails or email already exists
     */
    UserResponseDTO register(RegisterRequestDTO request) throws AppException;

    /**
     * Authenticates user against BCrypt password hash.
     *
     * @param request Login credentials
     * @return Authenticated UserResponseDTO
     * @throws AppException if credentials are invalid
     */
    UserResponseDTO login(LoginRequestDTO request) throws AppException;

    /**
     * Creates a new secure session for the user, invalidating any pre-existing session
     * to prevent session fixation attacks.
     *
     * @param request Current HTTP request
     * @param user    Authenticated user details
     * @return The freshly created HTTP session
     */
    HttpSession createSecureSession(HttpServletRequest request, UserResponseDTO user);

    /**
     * Invalidates the current user session.
     *
     * @param request Current HTTP request
     */
    void logout(HttpServletRequest request);
}
