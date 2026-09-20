package com.prakash.prakashmart.filter;

import com.prakash.prakashmart.dto.UserResponseDTO;
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
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
    }

    @Test
    @DisplayName("Should redirect unauthenticated user accessing web protected resource to /login")
    void testUnauthenticatedRedirectWeb() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/prakashmart/checkout");
        when(request.getContextPath()).thenReturn("/prakashmart");

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/prakashmart/login?redirect=/prakashmart/checkout");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED for unauthenticated API requests")
    void testUnauthenticatedApiReturns401() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/prakashmart/api/v1/buyer/orders");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authFilter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("UNAUTHENTICATED"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should forbid BUYER from accessing /admin endpoints")
    void testBuyerCannotAccessAdmin() throws IOException, ServletException {
        UserResponseDTO buyer = new UserResponseDTO(1L, "Buyer", "buyer@test.com", "BUYER", null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(buyer);
        when(request.getRequestURI()).thenReturn("/prakashmart/admin/dashboard");
        when(request.getContextPath()).thenReturn("/prakashmart");

        authFilter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendRedirect("/prakashmart/error/403");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should forbid BUYER from accessing /seller endpoints")
    void testBuyerCannotAccessSeller() throws IOException, ServletException {
        UserResponseDTO buyer = new UserResponseDTO(1L, "Buyer", "buyer@test.com", "BUYER", null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(buyer);
        when(request.getRequestURI()).thenReturn("/prakashmart/seller/products");
        when(request.getContextPath()).thenReturn("/prakashmart");

        authFilter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendRedirect("/prakashmart/error/403");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should forbid SELLER from accessing /admin endpoints")
    void testSellerCannotAccessAdmin() throws IOException, ServletException {
        UserResponseDTO seller = new UserResponseDTO(2L, "Seller", "seller@test.com", "SELLER", null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(seller);
        when(request.getRequestURI()).thenReturn("/prakashmart/admin/users");
        when(request.getContextPath()).thenReturn("/prakashmart");

        authFilter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendRedirect("/prakashmart/error/403");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow SELLER accessing /seller endpoints")
    void testSellerCanAccessSeller() throws IOException, ServletException {
        UserResponseDTO seller = new UserResponseDTO(2L, "Seller", "seller@test.com", "SELLER", null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(seller);
        when(request.getRequestURI()).thenReturn("/prakashmart/seller/dashboard");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow ADMIN accessing /admin and /seller endpoints")
    void testAdminCanAccessAll() throws IOException, ServletException {
        UserResponseDTO admin = new UserResponseDTO(3L, "Admin", "admin@test.com", "ADMIN", null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(admin);
        when(request.getRequestURI()).thenReturn("/prakashmart/admin/dashboard");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
