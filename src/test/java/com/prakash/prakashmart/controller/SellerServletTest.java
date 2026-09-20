package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.service.ProductService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServletTest {

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

    private SellerServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new SellerServlet(productService, orderService);
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/seller/dashboard returns seller metrics")
    void testSellerDashboardApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/seller/dashboard");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO seller = new UserResponseDTO();
        seller.setId(4L);
        seller.setRole("SELLER");
        when(session.getAttribute("currentUser")).thenReturn(seller);

        ProductDTO product = new ProductDTO();
        product.setId(10L);
        product.setStock(2);
        when(productService.getProductsBySeller(4L)).thenReturn(Collections.singletonList(product));

        Order order = new Order();
        order.setId(100L);
        order.setTotalAmount(new BigDecimal("250.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderService.getOrdersForSeller(4L)).thenReturn(Collections.singletonList(order));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"totalProducts\":1"));
        assertTrue(json.contains("\"totalOrders\":1"));
    }

    @Test
    @DisplayName("POST /api/seller/orders/status updates order status")
    void testUpdateOrderStatus() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/seller/orders/100/status");
        when(request.getPathInfo()).thenReturn("/100/status");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO seller = new UserResponseDTO();
        seller.setId(4L);
        seller.setRole("SELLER");
        when(session.getAttribute("currentUser")).thenReturn(seller);

        String json = "{\"status\":\"SHIPPED\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(orderService).updateOrderStatus(eq(100L), eq(OrderStatus.SHIPPED));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }

    @Test
    @DisplayName("Reject non-seller user with 403 Forbidden")
    void testRejectBuyerFromSellerServlet() throws Exception {
        when(request.getRequestURI()).thenReturn("/prakashmart/api/seller/dashboard");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO buyer = new UserResponseDTO();
        buyer.setId(2L);
        buyer.setRole("BUYER");
        when(session.getAttribute("currentUser")).thenReturn(buyer);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("ACCESS_DENIED"));
    }
}
