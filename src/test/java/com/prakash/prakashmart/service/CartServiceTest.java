package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dao.CartDAO;
import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.InsufficientStockException;
import com.prakash.prakashmart.exception.ProductNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.CartItem;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        this.cartService = new CartServiceImpl(cartDAO, productDAO);
    }

    @Test
    @DisplayName("Should add product to cart with valid stock and calculate price from server")
    void testAddToCartSuccess() throws AppException {
        Product product = new Product(10L, 1L, "Mouse", "Optical", new BigDecimal("25.00"), 50, "Electronics", null, true, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));
        when(cartDAO.findItem(1L, 10L)).thenReturn(Optional.empty());

        cartService.addToCart(1L, 10L, 2);

        verify(cartDAO).add(1L, 10L, 2);
    }

    @Test
    @DisplayName("Should reject adding non-existent product")
    void testAddToCartProductNotFound() throws Exception {
        when(productDAO.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> cartService.addToCart(1L, 99L, 1));
    }

    @Test
    @DisplayName("Should reject quantity greater than available stock")
    void testAddToCartInsufficientStock() throws Exception {
        Product product = new Product(10L, 1L, "Mouse", "Optical", new BigDecimal("25.00"), 3, "Electronics", null, true, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));
        when(cartDAO.findItem(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(1L, 10L, 5));
    }

    @Test
    @DisplayName("Should reject invalid and negative quantities")
    void testAddToCartInvalidQuantity() {
        assertThrows(ValidationException.class, () -> cartService.addToCart(1L, 10L, 0));
        assertThrows(ValidationException.class, () -> cartService.addToCart(1L, 10L, -1));
    }

    @Test
    @DisplayName("Server calculates prices dynamically and never trusts client totals")
    void testServerCalculatesCartPrices() throws AppException {
        CartItemDTO item1 = new CartItemDTO(1L, 10L, "Mouse", new BigDecimal("1.00"), 2, 50, new BigDecimal("2.00"));
        when(cartDAO.getCartByUserId(1L)).thenReturn(List.of(item1));

        // Real price in DB is 25.00, not 1.00
        Product realProduct = new Product(10L, 1L, "Mouse", "Desc", new BigDecimal("25.00"), 50, "Cat", null, true, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(realProduct));

        List<CartItemDTO> cart = cartService.getCart(1L);
        assertEquals(1, cart.size());
        assertEquals(0, new BigDecimal("25.00").compareTo(cart.get(0).getUnitPrice()));
        assertEquals(0, new BigDecimal("50.00").compareTo(cart.get(0).getSubtotal()));

        BigDecimal total = cartService.calculateCartTotal(1L);
        assertEquals(0, new BigDecimal("50.00").compareTo(total));
    }

    @Test
    @DisplayName("Should update quantity or remove item if quantity becomes zero")
    void testUpdateQuantity() throws AppException {
        CartItem cartItem = new CartItem(100L, 1L, 10L, 2);
        when(cartDAO.findById(100L)).thenReturn(Optional.of(cartItem));

        Product product = new Product(10L, 1L, "Mouse", "Desc", new BigDecimal("25.00"), 50, "Cat", null, true, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));

        // Valid update
        cartService.updateQuantity(100L, 4);
        verify(cartDAO).update(100L, 4);

        // Update to 0 removes the item
        cartService.updateQuantity(100L, 0);
        verify(cartDAO).remove(100L);
    }
}
