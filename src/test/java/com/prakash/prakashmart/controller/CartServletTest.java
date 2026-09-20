package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.service.CartService;
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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServletTest {

    @Mock
    private CartService cartService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private CartServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new CartServlet(cartService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/cart returns user cart items")
    void testGetCartApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/cart");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(3L);
        user.setEmail("buyer@example.com");
        when(session.getAttribute("currentUser")).thenReturn(user);

        CartItemDTO item = new CartItemDTO(10L, 1L, "Test Monitor", new BigDecimal("199.99"), 2, 10, new BigDecimal("399.98"));

        when(cartService.getCart(3L)).thenReturn(Collections.singletonList(item));
        when(cartService.calculateCartTotal(3L)).thenReturn(new BigDecimal("399.98"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Test Monitor"));
    }

    @Test
    @DisplayName("POST /api/cart/add adds product to cart")
    void testAddToCartApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/cart/add");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(3L);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = "{\"productId\":1,\"quantity\":2}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(cartService).addToCart(eq(3L), eq(1L), eq(2));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }

    @Test
    @DisplayName("POST /api/cart/remove removes item from cart")
    void testRemoveFromCartApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/cart/remove");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(3L);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = "{\"cartItemId\":10}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(cartService).removeFromCart(eq(10L));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }
}
