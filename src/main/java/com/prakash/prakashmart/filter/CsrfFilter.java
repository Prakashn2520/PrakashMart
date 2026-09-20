package com.prakash.prakashmart.filter;

import com.prakash.prakashmart.util.SecurityUtil;

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
import java.util.Set;

/**
 * Filter providing CSRF token generation and validation for state-changing HTTP methods (POST, PUT, DELETE).
 * Also adds fundamental security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection).
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    public static final String CSRF_SESSION_ATTR = "csrfToken";
    public static final String CSRF_PARAM_NAME = "_csrf";
    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Add standard security headers on all responses
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        HttpSession session = httpRequest.getSession(true);
        String sessionCsrf = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionCsrf == null) {
            sessionCsrf = SecurityUtil.generateCsrfToken();
            session.setAttribute(CSRF_SESSION_ATTR, sessionCsrf);
        }
        // Expose token to request scope so JSP views can inject it into forms easily
        httpRequest.setAttribute("csrfToken", sessionCsrf);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();

        // Validate CSRF on state-changing requests (except login/register endpoints which have their own rate/session handling)
        if (!SAFE_METHODS.contains(method.toUpperCase())) {
            boolean isAuthEndpoint = uri.endsWith("/login") || uri.endsWith("/register") || uri.contains("/api/v1/auth/");
            if (!isAuthEndpoint) {
                String reqToken = httpRequest.getHeader(CSRF_HEADER_NAME);
                if (reqToken == null || reqToken.trim().isEmpty()) {
                    reqToken = httpRequest.getParameter(CSRF_PARAM_NAME);
                }

                if (!SecurityUtil.verifyCsrfToken(sessionCsrf, reqToken)) {
                    if (uri.contains("/api/")) {
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.setContentType("application/json;charset=UTF-8");
                        httpResponse.getWriter().write("{\"success\":false,\"error\":{\"code\":\"CSRF_INVALID\",\"message\":\"Invalid or missing CSRF token\"}}");
                    } else {
                        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
                    }
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
