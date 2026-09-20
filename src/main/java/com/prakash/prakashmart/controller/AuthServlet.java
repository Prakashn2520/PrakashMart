package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.LoginRequestDTO;
import com.prakash.prakashmart.dto.RegisterRequestDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.service.AuthService;
import com.prakash.prakashmart.service.UserService;
import com.prakash.prakashmart.service.impl.AuthServiceImpl;
import com.prakash.prakashmart.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller handling user registration, authentication, session regeneration, and logout.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout", "/api/v1/auth/*"})
public class AuthServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private AuthService authService;

    @Override
    public void init() {
        UserService userService = new UserServiceImpl(new UserDAOImpl());
        this.authService = new AuthServiceImpl(userService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/login".equals(path)) {
            forwardToJsp(req, resp, "auth/login.jsp");
        } else if ("/register".equals(path)) {
            forwardToJsp(req, resp, "auth/register.jsp");
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();

        boolean isApi = "/api/v1/auth".equals(servletPath);

        try {
            if ("/login".equals(servletPath) || (isApi && "/login".equals(pathInfo))) {
                handleLogin(req, resp, isApi);
            } else if ("/register".equals(servletPath) || (isApi && "/register".equals(pathInfo))) {
                handleRegister(req, resp, isApi);
            } else {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (AppException e) {
            logger.warn("Authentication operation failed: {}", e.getMessage());
            if (isApi) {
                writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "AUTH_ERROR", e.getMessage());
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                try {
                    forwardToJsp(req, resp, "/login".equals(servletPath) ? "auth/login.jsp" : "auth/register.jsp");
                } catch (ServletException se) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp, boolean isApi)
            throws IOException, AppException {
        LoginRequestDTO dto;
        if (isApi) {
            dto = readJsonBody(req, LoginRequestDTO.class);
        } else {
            dto = new LoginRequestDTO(req.getParameter("email"), req.getParameter("password"));
        }

        UserResponseDTO user = authService.login(dto);

        // Establish secure session with fixation protection
        authService.createSecureSession(req, user);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, user);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp, boolean isApi)
            throws IOException, AppException {
        RegisterRequestDTO dto;
        if (isApi) {
            dto = readJsonBody(req, RegisterRequestDTO.class);
        } else {
            dto = new RegisterRequestDTO(
                    req.getParameter("name"),
                    req.getParameter("email"),
                    req.getParameter("password"),
                    req.getParameter("role")
            );
        }

        UserResponseDTO user = authService.register(dto);

        // Establish secure session
        authService.createSecureSession(req, user);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_CREATED, user);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        authService.logout(req);
        resp.sendRedirect(req.getContextPath() + "/login?loggedOut=true");
    }
}
