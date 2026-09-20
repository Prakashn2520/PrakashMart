package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ProductNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.service.impl.ProductServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        this.productService = new ProductServiceImpl(productDAO);
    }

    @Test
    @DisplayName("Should create product successfully with valid details")
    void testCreateProductSuccess() throws AppException {
        ProductDTO dto = new ProductDTO(null, 1L, "Laptop", "High performance", new BigDecimal("999.99"), 10, "Electronics", "http://img");

        Product saved = new Product(100L, 1L, "Laptop", "High performance", new BigDecimal("999.99"), 10, "Electronics", "http://img", true, null);
        when(productDAO.create(any(Product.class))).thenReturn(saved);

        ProductDTO created = productService.createProduct(dto);
        assertNotNull(created.getId());
        assertEquals("Laptop", created.getName());
        assertEquals(0, new BigDecimal("999.99").compareTo(created.getPrice()));
    }

    @Test
    @DisplayName("Should validate product price, stock quantity, name, and category")
    void testCreateProductValidationFailures() {
        // Null payload
        assertThrows(ValidationException.class, () -> productService.createProduct(null));

        // Negative price
        ProductDTO badPrice = new ProductDTO(null, 1L, "Laptop", "Desc", new BigDecimal("-10.00"), 10, "Electronics", null);
        assertThrows(ValidationException.class, () -> productService.createProduct(badPrice));

        // Negative stock
        ProductDTO badStock = new ProductDTO(null, 1L, "Laptop", "Desc", new BigDecimal("100.00"), -5, "Electronics", null);
        assertThrows(ValidationException.class, () -> productService.createProduct(badStock));

        // Blank name
        ProductDTO badName = new ProductDTO(null, 1L, "", "Desc", new BigDecimal("100.00"), 5, "Electronics", null);
        assertThrows(ValidationException.class, () -> productService.createProduct(badName));

        // Blank category
        ProductDTO badCat = new ProductDTO(null, 1L, "Laptop", "Desc", new BigDecimal("100.00"), 5, "   ", null);
        assertThrows(ValidationException.class, () -> productService.createProduct(badCat));
    }

    @Test
    @DisplayName("Seller can update only their own product")
    void testSellerCanOnlyUpdateOwnProduct() throws AppException {
        Product existing = new Product(100L, 1L, "Old Name", "Desc", new BigDecimal("50.00"), 5, "Cat", null, true, null);
        when(productDAO.findById(100L)).thenReturn(Optional.of(existing));

        // Updating by another seller (sellerId 2) must throw AuthorizationException
        ProductDTO updateByOther = new ProductDTO(100L, 2L, "New Name", "Desc", new BigDecimal("60.00"), 5, "Cat", null);
        assertThrows(AuthorizationException.class, () -> productService.updateProduct(updateByOther));

        // Updating by owning seller (sellerId 1) succeeds
        ProductDTO updateByOwner = new ProductDTO(100L, 1L, "New Name", "Desc", new BigDecimal("60.00"), 5, "Cat", null);
        productService.updateProduct(updateByOwner);
        verify(productDAO).update(existing);
    }

    @Test
    @DisplayName("Seller can delete only their own product")
    void testSellerCanOnlyDeleteOwnProduct() throws AppException {
        Product existing = new Product(100L, 1L, "Item", "Desc", new BigDecimal("50.00"), 5, "Cat", null, true, null);
        when(productDAO.findById(100L)).thenReturn(Optional.of(existing));

        // Delete by another seller (sellerId 2) must throw AuthorizationException
        assertThrows(AuthorizationException.class, () -> productService.deleteProduct(100L, 2L));

        // Delete by owner succeeds
        productService.deleteProduct(100L, 1L);
        verify(productDAO).delete(100L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException for missing product")
    void testProductNotFound() throws Exception {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("Should pass validated pagination, sorting, and filter parameters to DAO")
    void testGetProductsPaginated() throws AppException {
        PaginatedResult<Product> entityResult = new PaginatedResult<>(1, 10, 1, 1, List.of(
                new Product(1L, 1L, "Book", "Desc", new BigDecimal("20.00"), 5, "Books", null, true, null)
        ));

        when(productDAO.findPaginated(eq("book"), eq("Books"), any(), any(), eq(true), eq(1), eq(10), eq("price"), eq("ASC")))
                .thenReturn(entityResult);

        PaginatedResult<ProductDTO> result = productService.getProductsPaginated(
                "book", "Books", new BigDecimal("10.00"), new BigDecimal("50.00"), true, 1, 10, "price", "ASC"
        );

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getData().size());
        assertEquals("Book", result.getData().get(0).getName());
    }
}
