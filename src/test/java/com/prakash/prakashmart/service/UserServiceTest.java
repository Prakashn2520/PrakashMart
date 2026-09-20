package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dao.UserDAO;
import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthenticationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.service.impl.UserServiceImpl;
import com.prakash.prakashmart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        this.userService = new UserServiceImpl(userDAO);
    }

    @Test
    @DisplayName("Should register new user successfully with hashed password")
    void testRegisterSuccess() throws AppException {
        RegisterRequestDTO dto = new RegisterRequestDTO("Alice Buyer", "alice@example.com", "password123", "BUYER");

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setName("Alice Buyer");
        savedUser.setEmail("alice@example.com");
        savedUser.setPasswordHash("hashed_pwd");
        savedUser.setRole(Role.BUYER);

        when(userDAO.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO result = userService.register(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("BUYER", result.getRole());
        verify(userDAO).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void testRegisterDuplicateEmail() throws AppException {
        RegisterRequestDTO dto = new RegisterRequestDTO("Bob", "bob@example.com", "password123", "BUYER");

        when(userDAO.findByEmail("bob@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ValidationException.class, () -> userService.register(dto));
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void testAuthenticateSuccess() throws AppException {
        String plainPassword = "validPassword123";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        User user = new User();
        user.setId(5L);
        user.setName("Charlie");
        user.setEmail("charlie@example.com");
        user.setPasswordHash(hashed);
        user.setRole(Role.SELLER);

        when(userDAO.findByEmail("charlie@example.com")).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.authenticate(new LoginRequestDTO("charlie@example.com", plainPassword));

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("SELLER", response.getRole());
    }

    @Test
    @DisplayName("Should throw AuthenticationException on incorrect password")
    void testAuthenticateInvalidPassword() throws AppException {
        String hashed = PasswordUtil.hashPassword("correctPassword");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash(hashed);

        when(userDAO.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                userService.authenticate(new LoginRequestDTO("user@example.com", "wrongPassword")));
    }
}
