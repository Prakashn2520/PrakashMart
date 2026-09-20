package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServletTest {

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private OrderServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new OrderServlet(orderService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getServletPath()).thenReturn("/api/orders");
    }

    @Test
    @DisplayName("GET /api/orders returns user orders")
    void testGetOrdersListApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/orders");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(7L);
        user.setRole("BUYER");
        when(session.getAttribute("currentUser")).thenReturn(user);

        Order order = new Order();
        order.setId(101L);
        order.setBuyerId(7L);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        when(orderService.getOrdersByBuyer(7L)).thenReturn(Collections.singletonList(order));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("101"));
    }

    @Test
    @DisplayName("POST /api/orders performs checkout")
    void testCheckoutApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/orders");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(7L);
        user.setRole("BUYER");
        when(session.getAttribute("currentUser")).thenReturn(user);

        Order order = new Order();
        order.setId(200L);
        order.setBuyerId(7L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("299.99"));

        when(orderService.checkout(7L)).thenReturn(order);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("200"));
    }

    @Test
    @DisplayName("POST /api/orders/200/cancel cancels order")
    void testCancelOrderApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/orders/200/cancel");
        when(request.getPathInfo()).thenReturn("/200/cancel");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(7L);
        user.setRole("BUYER");
        when(session.getAttribute("currentUser")).thenReturn(user);

        servlet.doPost(request, response);

        verify(orderService).cancelOrder(eq(200L), eq(7L), eq("BUYER"));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }
}
