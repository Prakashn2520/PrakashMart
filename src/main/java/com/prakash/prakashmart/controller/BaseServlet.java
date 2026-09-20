package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.ApiResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.*;
import com.prakash.prakashmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Base Servlet providing reusable helper methods for thin controller orchestration,
 * JSON serialization, standard response envelopes, session inspection, and centralized exception handling.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

    protected <T> T readJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && (contentType.toLowerCase().contains("application/x-www-form-urlencoded")
                || contentType.toLowerCase().contains("multipart/form-data"))) {
            return null;
        }
        BufferedReader reader;
        try {
            reader = request.getReader();
        } catch (Exception e) {
            return null;
        }
        if (reader == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString().trim();
        if (body.isEmpty() || (!body.startsWith("{") && !body.startsWith("["))) {
            return null;
        }
        return JsonUtil.fromJson(body, clazz);
    }

    protected void writeJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        writeJsonResponse(response, statusCode, "Operation completed successfully", data);
    }

    protected void writeJsonResponse(HttpServletResponse response, int statusCode, String message, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJson(ApiResponse.success(message, data)));
    }

    protected void writeJsonError(HttpServletResponse response, int statusCode, String errorCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJson(ApiResponse.error(errorCode, message)));
    }

    protected void handleException(HttpServletResponse response, Exception e, boolean isApi) throws IOException {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_SERVER_ERROR";
        String message = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";

        if (e instanceof ValidationException) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST; // 400
            errorCode = ((ValidationException) e).getErrorCode() != null ? ((ValidationException) e).getErrorCode() : "VALIDATION_ERROR";
        } else if (e instanceof AuthenticationException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED; // 401
            errorCode = "AUTHENTICATION_FAILED";
        } else if (e instanceof AuthorizationException) {
            statusCode = HttpServletResponse.SC_FORBIDDEN; // 403
            errorCode = "ACCESS_DENIED";
        } else if (e instanceof ResourceNotFoundException) {
            statusCode = HttpServletResponse.SC_NOT_FOUND; // 404
            errorCode = ((ResourceNotFoundException) e).getErrorCode() != null ? ((ResourceNotFoundException) e).getErrorCode() : "NOT_FOUND";
        } else if (e instanceof InsufficientStockException) {
            statusCode = HttpServletResponse.SC_CONFLICT; // 409
            errorCode = "INSUFFICIENT_STOCK";
        } else if (e instanceof OrderException) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST; // 400
            errorCode = ((OrderException) e).getErrorCode() != null ? ((OrderException) e).getErrorCode() : "ORDER_ERROR";
        } else if (e instanceof DatabaseException) {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
            errorCode = "DATABASE_ERROR";
        }

        logger.warn("Handling exception in controller [status={} code={}]: {}", statusCode, errorCode, message);

        if (isApi) {
            writeJsonError(response, statusCode, errorCode, message);
        } else {
            response.sendError(statusCode, message);
        }
    }

    protected UserResponseDTO getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserResponseDTO) session.getAttribute("currentUser") : null;
    }

    protected void forwardToJsp(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + jspPath).forward(request, response);
    }
}
