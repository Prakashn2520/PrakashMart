package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServletTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private AdminServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminServlet(userService, productService, orderService);
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/admin/dashboard returns admin platform stats")
    void testAdminDashboardApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/admin/dashboard");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO admin = new UserResponseDTO();
        admin.setId(1L);
        admin.setRole("ADMIN");
        when(session.getAttribute("currentUser")).thenReturn(admin);

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(admin));
        when(productService.getProductsPaginated(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(PaginatedResult.of(Collections.emptyList(), 1, 10, 0L));
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"totalUsers\":1"));
    }

    @Test
    @DisplayName("GET /api/admin/users returns registered users")
    void testGetAdminUsersApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/admin/users");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO admin = new UserResponseDTO();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");
        when(session.getAttribute("currentUser")).thenReturn(admin);

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(admin));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("admin@example.com"));
    }

    @Test
    @DisplayName("Reject non-admin seller with 403 Forbidden")
    void testRejectSellerFromAdminServlet() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/admin/dashboard");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO seller = new UserResponseDTO();
        seller.setId(4L);
        seller.setRole("SELLER");
        when(session.getAttribute("currentUser")).thenReturn(seller);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("ACCESS_DENIED"));
    }
}
