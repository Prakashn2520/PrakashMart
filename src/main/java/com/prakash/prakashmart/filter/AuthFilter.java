package com.prakash.prakashmart.filter;

import com.prakash.prakashmart.dto.ApiResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter enforcing authentication and Role-Based Access Control (RBAC).
 *
 * Rules:
 * - Unauthenticated users are rejected or redirected to /login.
 * - BUYER: cannot access /seller/*, /admin/*, /api/v1/seller/*, /api/v1/admin/*.
 * - SELLER: cannot access /admin/*, /api/v1/admin/*.
 * - ADMIN: has administrative access.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/seller/*", "/admin/*", "/buyer/*", "/checkout", "/checkout/*",
        "/api/seller/*", "/api/admin/*", "/api/buyer/*", "/api/checkout/*",
        "/api/v1/seller/*", "/api/v1/admin/*", "/api/v1/buyer/*", "/api/v1/checkout/*"
})
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        UserResponseDTO currentUser = (session != null) ? (UserResponseDTO) session.getAttribute("currentUser") : null;
        String uri = httpRequest.getRequestURI();
        boolean isApi = uri.contains("/api/");

        if (currentUser == null) {
            logger.warn("Unauthorized access attempt to {}", uri);
            if (isApi) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(JsonUtil.toJson(ApiResponse.error("UNAUTHENTICATED", "Authentication required")));
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + uri);
            }
            return;
        }

        Role role = Role.fromString(currentUser.getRole());

        // RBAC validation: Admin endpoints
        if ((uri.contains("/admin/") || uri.endsWith("/admin")) && role != Role.ADMIN) {
            logger.warn("Forbidden admin access attempt by user: {} role: {}", currentUser.getEmail(), role);
            sendForbidden(httpResponse, isApi, httpRequest.getContextPath());
            return;
        }

        // RBAC validation: Seller endpoints
        if ((uri.contains("/seller/") || uri.endsWith("/seller")) && role != Role.SELLER && role != Role.ADMIN) {
            logger.warn("Forbidden seller access attempt by user: {} role: {}", currentUser.getEmail(), role);
            sendForbidden(httpResponse, isApi, httpRequest.getContextPath());
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletResponse response, boolean isApi, String contextPath) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        if (isApi) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Access denied for this role")));
        } else {
            response.sendRedirect(contextPath + "/error/403");
        }
    }

    @Override
    public void destroy() {
    }
}
