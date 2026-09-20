package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServletTest {

    @Mock
    private ProductService productService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ProductServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProductServlet(productService);
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/products returns paginated products in standard JSON envelope")
    void testGetProductsPaginatedApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/products");
        when(request.getPathInfo()).thenReturn(null);

        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(new BigDecimal("999.99"));

        PaginatedResult<ProductDTO> result = PaginatedResult.of(
                Collections.singletonList(product), 1, 12, 1L);

        when(productService.getProductsPaginated(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(result);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Test Laptop"));
    }

    @Test
    @DisplayName("GET /api/products/1 returns single product")
    void testGetProductByIdApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/products");
        when(request.getPathInfo()).thenReturn("/1");

        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Gaming Mouse");
        product.setPrice(new BigDecimal("49.99"));

        when(productService.getProductById(1L)).thenReturn(product);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Gaming Mouse"));
    }

    @Test
    @DisplayName("POST /api/products creates product when seller authenticated")
    void testCreateProductAuthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/products");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO seller = new UserResponseDTO();
        seller.setId(5L);
        seller.setRole("SELLER");
        when(session.getAttribute("currentUser")).thenReturn(seller);

        String jsonInput = "{\"name\":\"Wireless Keyboard\",\"price\":79.99,\"stock\":20,\"category\":\"Electronics\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonInput)));

        ProductDTO created = new ProductDTO();
        created.setId(10L);
        created.setName("Wireless Keyboard");
        created.setSellerId(5L);
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(created);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Wireless Keyboard"));
    }

    @Test
    @DisplayName("POST /api/products returns 403 when user is buyer")
    void testCreateProductForbiddenForBuyer() throws Exception {
        when(request.getServletPath()).thenReturn("/api/products");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO buyer = new UserResponseDTO();
        buyer.setId(2L);
        buyer.setRole("BUYER");
        when(session.getAttribute("currentUser")).thenReturn(buyer);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("DELETE /api/products/1 deletes product for seller")
    void testDeleteProduct() throws Exception {
        when(request.getServletPath()).thenReturn("/api/products");
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO seller = new UserResponseDTO();
        seller.setId(5L);
        seller.setRole("SELLER");
        when(session.getAttribute("currentUser")).thenReturn(seller);

        servlet.doDelete(request, response);

        verify(productService).deleteProduct(eq(1L), eq(5L));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
    }
}
