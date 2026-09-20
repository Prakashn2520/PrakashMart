package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession oldSession;

    @Mock
    private HttpSession newSession;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        this.authService = new AuthServiceImpl(userService);
    }

    @Test
    @DisplayName("Should delegate register to UserService")
    void testRegister() throws AppException {
        RegisterRequestDTO dto = new RegisterRequestDTO("Alice", "alice@example.com", "password123", "BUYER");
        UserResponseDTO expected = new UserResponseDTO(1L, "Alice", "alice@example.com", "BUYER", null);
        when(userService.register(dto)).thenReturn(expected);

        UserResponseDTO actual = authService.register(dto);
        assertEquals(expected, actual);
        verify(userService).register(dto);
    }

    @Test
    @DisplayName("Should delegate login to UserService")
    void testLogin() throws AppException {
        LoginRequestDTO dto = new LoginRequestDTO("alice@example.com", "password123");
        UserResponseDTO expected = new UserResponseDTO(1L, "Alice", "alice@example.com", "BUYER", null);
        when(userService.authenticate(dto)).thenReturn(expected);

        UserResponseDTO actual = authService.login(dto);
        assertEquals(expected, actual);
        verify(userService).authenticate(dto);
    }

    @Test
    @DisplayName("Should invalidate old session and create fresh secure session with user")
    void testCreateSecureSession() {
        UserResponseDTO user = new UserResponseDTO(1L, "Alice", "alice@example.com", "BUYER", null);

        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);

        HttpSession session = authService.createSecureSession(request, user);

        verify(oldSession).invalidate(); // Session fixation prevention
        verify(newSession).setAttribute(AuthServiceImpl.SESSION_USER_KEY, user);
        assertEquals(newSession, session);
    }

    @Test
    @DisplayName("Should invalidate session on logout")
    void testLogout() {
        when(request.getSession(false)).thenReturn(oldSession);

        authService.logout(request);

        verify(oldSession).invalidate();
    }
}
