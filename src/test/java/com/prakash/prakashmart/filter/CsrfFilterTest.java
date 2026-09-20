package com.prakash.prakashmart.filter;

import com.prakash.prakashmart.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsrfFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private CsrfFilter csrfFilter;

    @BeforeEach
    void setUp() {
        csrfFilter = new CsrfFilter();
    }

    @Test
    @DisplayName("Should pass safe GET requests and set security headers")
    void testSafeGetPassesAndSetsHeaders() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/products");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(CsrfFilter.CSRF_SESSION_ATTR)).thenReturn("test-token-123");

        csrfFilter.doFilter(request, response, chain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(request).setAttribute("csrfToken", "test-token-123");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should pass POST request when valid CSRF token is provided in header")
    void testValidCsrfTokenInHeaderPasses() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(CsrfFilter.CSRF_SESSION_ATTR)).thenReturn("valid-csrf-token");
        when(request.getHeader(CsrfFilter.CSRF_HEADER_NAME)).thenReturn("valid-csrf-token");

        csrfFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reject POST request when CSRF token is missing or invalid")
    void testInvalidCsrfTokenRejects() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/cart/add");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(CsrfFilter.CSRF_SESSION_ATTR)).thenReturn("valid-csrf-token");
        when(request.getHeader(CsrfFilter.CSRF_HEADER_NAME)).thenReturn(null);
        when(request.getParameter(CsrfFilter.CSRF_PARAM_NAME)).thenReturn("tampered-token");

        csrfFilter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
        verify(chain, never()).doFilter(request, response);
    }
}
