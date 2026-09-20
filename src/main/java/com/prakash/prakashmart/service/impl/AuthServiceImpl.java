package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.service.AuthService;
import com.prakash.prakashmart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Implementation of AuthService delegating authentication/registration to UserService
 * and managing secure session lifecycles for servlets and filters.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    public static final String SESSION_USER_KEY = "currentUser";

    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO request) throws AppException {
        return userService.register(request);
    }

    @Override
    public UserResponseDTO login(LoginRequestDTO request) throws AppException {
        return userService.authenticate(request);
    }

    @Override
    public HttpSession createSecureSession(HttpServletRequest request, UserResponseDTO user) {
        if (request == null) {
            return null;
        }

        // Prevent Session Fixation: invalidate existing session if present
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            try {
                oldSession.invalidate();
            } catch (IllegalStateException ignored) {
            }
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, user);
        logger.info("Secure session established for user: {}", user != null ? user.getEmail() : "anonymous");
        return session;
    }

    @Override
    public void logout(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.info("Session successfully invalidated upon logout");
        }
    }
}
