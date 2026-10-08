package com.prakash.prakashmart;

import com.prakash.prakashmart.dao.BaseDAOTest;
import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dao.UserDAO;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test verifying the complete Edit Product feature lifecycle against H2 Database:
 * UI / Service -> DAO -> H2 Database -> UI / Retrieval
 */
public class EditProductIntegrationTest extends BaseDAOTest {

    private ProductDAO productDAO;
    private UserDAO userDAO;
    private ProductService productService;

    private Long seller1Id;
    private Long seller2Id;

    @BeforeEach
    void setUp() throws Exception {
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.productService = new ProductServiceImpl(productDAO);

        // Seed Seller 1
        User seller1 = new User(null, "Artisan Crafts", "artisan@prakashmart.com", "hash123", Role.SELLER, true, null);
        seller1 = userDAO.save(seller1);
        seller1Id = seller1.getId();

        // Seed Seller 2
        User seller2 = new User(null, "Tech Store", "tech@prakashmart.com", "hash456", Role.SELLER, true, null);
        seller2 = userDAO.save(seller2);
        seller2Id = seller2.getId();
    }

    @Test
    @DisplayName("Complete Flow: Add product -> Load from H2 -> Edit fields -> Save changes -> Verify H2 DB updated with same ID")
    void testCompleteEditProductFlow() throws AppException {
        // 1. Existing database record: ID: auto-generated, Name: Laptop, Price: 50000.00, Stock: 10
        ProductDTO newProduct = new ProductDTO(null, seller1Id, "Laptop", "High performance workstation",
                new BigDecimal("50000.00"), 10, "Electronics", "https://example.com/laptop.jpg");
        ProductDTO created = productService.createProduct(newProduct);
        Long productId = created.getId();
        assertNotNull(productId);

        // 2. Load existing product data from DATABASE
        ProductDTO loaded = productService.getProductById(productId);
        assertEquals(productId, loaded.getId());
        assertEquals("Laptop", loaded.getName());
        assertEquals(0, new BigDecimal("50000.00").compareTo(loaded.getPrice()));
        assertEquals(10, loaded.getStock());
        assertEquals("Electronics", loaded.getCategory());
        assertEquals("High performance workstation", loaded.getDescription());
        assertEquals("https://example.com/laptop.jpg", loaded.getImageUrl());

        // 3. Seller changes fields: Price -> 55000.00, Stock -> 8, Name -> "Laptop Pro", Category -> "Electronics"
        ProductDTO editRequest = new ProductDTO(productId, seller1Id, "Laptop Pro", "Updated workstation details",
                new BigDecimal("55000.00"), 8, "Electronics", "https://example.com/laptop-pro.jpg");

        // 4. Save changes (Executes UPDATE in H2 Database)
        productService.updateProduct(editRequest);

        // 5. Directly verify the H2 Database contains the new values
        Optional<Product> directFromDb = productDAO.findById(productId);
        assertTrue(directFromDb.isPresent());
        Product dbProduct = directFromDb.get();

        // The SAME product ID must remain
        assertEquals(productId, dbProduct.getId());
        assertEquals("Laptop Pro", dbProduct.getName());
        assertEquals("Updated workstation details", dbProduct.getDescription());
        assertEquals(0, new BigDecimal("55000.00").compareTo(dbProduct.getPrice()));
        assertEquals(8, dbProduct.getStockQty());
        assertEquals("Electronics", dbProduct.getCategory());
        assertEquals("https://example.com/laptop-pro.jpg", dbProduct.getImageUrl());

        // 6. Verify retrieval through service / UI layer reflects updated database values
        ProductDTO refreshed = productService.getProductById(productId);
        assertEquals(productId, refreshed.getId());
        assertEquals("Laptop Pro", refreshed.getName());
        assertEquals(0, new BigDecimal("55000.00").compareTo(refreshed.getPrice()));
        assertEquals(8, refreshed.getStock());
    }

    @Test
    @DisplayName("Security: Seller must NOT be able to edit another seller's product (Access Denied)")
    void testSecurityUnauthorizedSellerCannotEdit() throws AppException {
        // Seller 1 creates a product
        ProductDTO product = new ProductDTO(null, seller1Id, "Silk Scarf", "Handmade silk",
                new BigDecimal("45.00"), 15, "Clothing", null);
        ProductDTO created = productService.createProduct(product);
        Long productId = created.getId();

        // Seller 2 attempts to edit Seller 1's product
        ProductDTO unauthorizedEdit = new ProductDTO(productId, seller2Id, "Hacked Scarf", "Modified desc",
                new BigDecimal("10.00"), 50, "Clothing", null);

        assertThrows(AuthorizationException.class, () -> productService.updateProduct(unauthorizedEdit));

        // Verify H2 Database record was NOT modified
        Optional<Product> unmodified = productDAO.findById(productId);
        assertTrue(unmodified.isPresent());
        assertEquals("Silk Scarf", unmodified.get().getName());
        assertEquals(0, new BigDecimal("45.00").compareTo(unmodified.get().getPrice()));
        assertEquals(15, unmodified.get().getStockQty());
    }

    @Test
    @DisplayName("Validation: Try invalid price (zero or negative) -> Error and database must NOT change")
    void testInvalidPriceRejectedAndDatabaseUnchanged() throws AppException {
        ProductDTO product = new ProductDTO(null, seller1Id, "Ceramic Vase", "Handmade ceramic",
                new BigDecimal("75.00"), 5, "Home", null);
        ProductDTO created = productService.createProduct(product);
        Long productId = created.getId();

        // Try 0 price
        ProductDTO zeroPrice = new ProductDTO(productId, seller1Id, "Ceramic Vase", "Handmade ceramic",
                BigDecimal.ZERO, 5, "Home", null);
        assertThrows(ValidationException.class, () -> productService.updateProduct(zeroPrice));

        // Try negative price
        ProductDTO negPrice = new ProductDTO(productId, seller1Id, "Ceramic Vase", "Handmade ceramic",
                new BigDecimal("-25.00"), 5, "Home", null);
        assertThrows(ValidationException.class, () -> productService.updateProduct(negPrice));

        // Verify database is unchanged
        Product p = productDAO.findById(productId).orElseThrow();
        assertEquals(0, new BigDecimal("75.00").compareTo(p.getPrice()));
    }

    @Test
    @DisplayName("Validation: Try invalid stock (negative) -> Error and database must NOT change")
    void testInvalidStockRejectedAndDatabaseUnchanged() throws AppException {
        ProductDTO product = new ProductDTO(null, seller1Id, "Coffee Beans", "Roasted coffee",
                new BigDecimal("18.00"), 20, "Home", null);
        ProductDTO created = productService.createProduct(product);
        Long productId = created.getId();

        // Try negative stock
        ProductDTO negStock = new ProductDTO(productId, seller1Id, "Coffee Beans", "Roasted coffee",
                new BigDecimal("18.00"), -5, "Home", null);
        assertThrows(ValidationException.class, () -> productService.updateProduct(negStock));

        // Verify database is unchanged
        Product p = productDAO.findById(productId).orElseThrow();
        assertEquals(20, p.getStockQty());
    }

    @Test
    @DisplayName("Verify Add Product and Delete Product still work seamlessly")
    void testAddAndDeleteProductStillWork() throws AppException {
        // Add product
        ProductDTO product = new ProductDTO(null, seller1Id, "Wool Blanket", "Warm wool",
                new BigDecimal("85.00"), 12, "Home", null);
        ProductDTO created = productService.createProduct(product);
        assertNotNull(created.getId());

        // Verify added in DB
        assertTrue(productDAO.findById(created.getId()).isPresent());

        // Delete product
        productService.deleteProduct(created.getId(), seller1Id);

        // Verify deleted from DB
        assertFalse(productDAO.findById(created.getId()).isPresent());
    }
}
