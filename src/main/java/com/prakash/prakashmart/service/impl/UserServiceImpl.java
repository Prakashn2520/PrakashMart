package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dao.UserDAO;
import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthenticationException;
import com.prakash.prakashmart.exception.ResourceNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.service.UserService;
import com.prakash.prakashmart.util.PasswordUtil;
import com.prakash.prakashmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for user authentication and management.
 */
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO request) throws AppException {
        if (request == null) {
            throw new ValidationException("Registration payload cannot be empty");
        }
        if (!ValidationUtil.isNotEmpty(request.getName())) {
            throw new ValidationException("name", "Name is required");
        }
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new ValidationException("email", "Valid email address is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters");
        }

        Role role = Role.fromString(request.getRole());
        if (role == null || role == Role.ADMIN) {
            role = Role.BUYER; // Default or fallback, admins only seeded
        }

        Optional<User> existing = userDAO.findByEmail(request.getEmail().trim().toLowerCase());
        if (existing.isPresent()) {
            throw new ValidationException("email", "Email is already registered");
        }

        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(hashedPassword);
        user.setRole(role);
        user.setIsActive(true);

        User savedUser = userDAO.save(user);
        logger.info("Registered new user: {} with role: {}", savedUser.getEmail(), savedUser.getRole());

        return toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO authenticate(LoginRequestDTO request) throws AppException {
        if (request == null || !ValidationUtil.isValidEmail(request.getEmail())
                || !ValidationUtil.isNotEmpty(request.getPassword())) {
            throw new ValidationException("Invalid login credentials provided");
        }

        Optional<User> userOpt = userDAO.findByEmail(request.getEmail().trim().toLowerCase());
        if (userOpt.isEmpty() || !PasswordUtil.checkPassword(request.getPassword(), userOpt.get().getPasswordHash())) {
            logger.warn("Authentication failed for email: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOpt.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AuthenticationException("Account is disabled");
        }

        logger.info("User authenticated successfully: {}", user.getEmail());
        return toResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserById(Long id) throws AppException {
        if (id == null || id <= 0) {
            throw new ValidationException("id", "Invalid user ID");
        }
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return toResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() throws AppException {
        List<User> users = userDAO.findAll();
        List<UserResponseDTO> dtos = new ArrayList<>();
        for (User u : users) {
            dtos.add(toResponseDTO(u));
        }
        return dtos;
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
