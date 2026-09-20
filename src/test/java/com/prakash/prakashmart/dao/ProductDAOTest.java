package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest extends BaseDAOTest {

    private ProductDAO productDAO;
    private UserDAO userDAO;
    private Long sellerId;

    @BeforeEach
    void setUp() throws DatabaseException {
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();

        // Create a seller
        User seller = new User();
        seller.setName("Gadget Store");
        seller.setEmail("seller@gadgets.com");
        seller.setPasswordHash("sellerhash");
        seller.setRole(Role.SELLER);
        User savedSeller = userDAO.save(seller);
        this.sellerId = savedSeller.getId();
    }

    @Test
    @DisplayName("Should save, find by ID, update, and delete product")
    void testProductCrudLifecycle() throws DatabaseException {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Mechanical Keyboard");
        product.setDescription("RGB gaming keyboard");
        product.setPrice(new BigDecimal("79.99"));
        product.setStockQty(20);
        product.setCategory("Electronics");
        product.setImageUrl("https://example.com/keyboard.png");

        Product saved = productDAO.create(product);
        assertNotNull(saved.getId());

        Optional<Product> found = productDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Mechanical Keyboard", found.get().getName());
        assertEquals(0, new BigDecimal("79.99").compareTo(found.get().getPrice()));
        assertEquals(20, found.get().getStockQty());

        // Update product
        product.setName("Mechanical Keyboard Pro");
        product.setPrice(new BigDecimal("89.99"));
        productDAO.update(product);

        Optional<Product> updated = productDAO.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Mechanical Keyboard Pro", updated.get().getName());
        assertEquals(0, new BigDecimal("89.99").compareTo(updated.get().getPrice()));

        // Delete product
        productDAO.delete(saved.getId());
        Optional<Product> deleted = productDAO.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Should find products by seller ID and update stock")
    void testFindBySellerIdAndUpdateStock() throws DatabaseException {
        Product p1 = new Product(null, sellerId, "Headphones", "Noise cancelling", new BigDecimal("120.00"), 10, "Electronics", null, null);
        Product p2 = new Product(null, sellerId, "Mouse", "Wireless mouse", new BigDecimal("25.00"), 30, "Electronics", null, null);
        productDAO.save(p1);
        productDAO.save(p2);

        List<Product> sellerProducts = productDAO.findBySellerId(sellerId);
        assertEquals(2, sellerProducts.size());

        List<Product> sellerProductsAlias = productDAO.findBySeller(sellerId);
        assertEquals(2, sellerProductsAlias.size());

        productDAO.updateStock(p1.getId(), 5);
        Optional<Product> updated = productDAO.findById(p1.getId());
        assertTrue(updated.isPresent());
        assertEquals(5, updated.get().getStockQty());
    }

    @Test
    @DisplayName("Should find products by category")
    void testFindByCategory() throws DatabaseException {
        productDAO.save(new Product(null, sellerId, "Book A", "Desc", new BigDecimal("15.00"), 10, "Books", null, null));
        productDAO.save(new Product(null, sellerId, "Book B", "Desc", new BigDecimal("25.00"), 5, "Books", null, null));
        productDAO.save(new Product(null, sellerId, "Gadget A", "Desc", new BigDecimal("50.00"), 2, "Electronics", null, null));

        List<Product> books = productDAO.findByCategory("Books");
        assertEquals(2, books.size());

        List<Product> empty = productDAO.findByCategory("NonExistent");
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("Should search products with category and keyword filter")
    void testSearchProducts() throws DatabaseException {
        productDAO.save(new Product(null, sellerId, "Java 17 in Action", "Comprehensive guide to Java", new BigDecimal("45.00"), 50, "Books", null, null));
        productDAO.save(new Product(null, sellerId, "Clean Architecture", "Architecture principles", new BigDecimal("35.00"), 40, "Books", null, null));
        productDAO.save(new Product(null, sellerId, "Gaming Headset", "Surround sound headset", new BigDecimal("60.00"), 15, "Electronics", null, null));

        // Filter by category
        List<Product> books = productDAO.searchProducts("Books", null, 0, 10);
        assertEquals(2, books.size());

        // Filter by keyword
        List<Product> javaSearch = productDAO.searchProducts(null, "Java", 0, 10);
        assertEquals(1, javaSearch.size());
        assertEquals("Java 17 in Action", javaSearch.get(0).getName());

        // Filter by both
        List<Product> combined = productDAO.searchProducts("Books", "Clean", 0, 10);
        assertEquals(1, combined.size());
        assertEquals("Clean Architecture", combined.get(0).getName());
    }

    @Test
    @DisplayName("Should support full pagination, sorting, and count")
    void testPaginationAndSorting() throws DatabaseException {
        // Create 25 products
        for (int i = 1; i <= 25; i++) {
            productDAO.save(new Product(null, sellerId, String.format("Product %02d", i),
                    "Description for product " + i,
                    new BigDecimal(i * 10),
                    i % 2 == 0 ? 10 : 0, // Alternate in-stock and out-of-stock
                    i % 2 == 0 ? "Electronics" : "Books",
                    null, null));
        }

        // Total active products count
        long totalAll = productDAO.countAll();
        assertEquals(25, totalAll);

        // Paginate page 1 with size 10
        PaginatedResult<Product> page1 = productDAO.findPaginated(null, null, null, null, null, 1, 10, "id", "ASC");
        assertEquals(1, page1.getCurrentPage());
        assertEquals(10, page1.getPageSize());
        assertEquals(25, page1.getTotalResults());
        assertEquals(3, page1.getTotalPages());
        assertEquals(10, page1.getData().size());
        assertEquals("Product 01", page1.getData().get(0).getName());

        // Paginate page 3 (remaining 5)
        PaginatedResult<Product> page3 = productDAO.findPaginated(null, null, null, null, null, 3, 10, "id", "ASC");
        assertEquals(3, page3.getCurrentPage());
        assertEquals(5, page3.getData().size());
        assertEquals("Product 25", page3.getData().get(4).getName());

        // Sort by price descending
        PaginatedResult<Product> sortedByPriceDesc = productDAO.findPaginated(null, null, null, null, null, 1, 5, "price", "DESC");
        assertEquals(0, new BigDecimal("250").compareTo(sortedByPriceDesc.getData().get(0).getPrice()));

        // Filter by inStockOnly
        PaginatedResult<Product> inStockPage = productDAO.findPaginated(null, null, null, null, true, 1, 50, "id", "ASC");
        assertEquals(12, inStockPage.getTotalResults());
        for (Product p : inStockPage.getData()) {
            assertTrue(p.getStockQty() > 0);
        }

        // Filter by price range
        PaginatedResult<Product> priceRangePage = productDAO.findPaginated(null, null, new BigDecimal("50"), new BigDecimal("100"), null, 1, 50, "price", "ASC");
        assertEquals(6, priceRangePage.getTotalResults()); // 50, 60, 70, 80, 90, 100

        // Count products matching filter
        long countElectronics = productDAO.countProducts(null, "Electronics", null, null, null);
        assertEquals(12, countElectronics);
    }

    @Test
    @DisplayName("Should handle invalid IDs, empty results, and SQL injection safety")
    void testInvalidIdsAndSqlInjectionSafety() throws DatabaseException {
        // Invalid ID search
        Optional<Product> nonExistent = productDAO.findById(999999L);
        assertFalse(nonExistent.isPresent());

        // Empty search results
        PaginatedResult<Product> emptyResult = productDAO.findPaginated("NON_EXISTENT_KEYWORD_XYZ", null, null, null, null, 1, 10, "id", "ASC");
        assertEquals(0, emptyResult.getTotalResults());
        assertTrue(emptyResult.getData().isEmpty());

        // SQL injection attempts in search keyword, category, and sortBy
        String injectionKeyword = "'; DROP TABLE products; --";
        PaginatedResult<Product> safeResult = productDAO.findPaginated(injectionKeyword, injectionKeyword, null, null, null, 1, 10, "name; DROP TABLE products; --", "ASC; SELECT 1;");
        assertNotNull(safeResult);
        assertEquals(0, safeResult.getTotalResults());

        // Verify products table was not dropped!
        long count = productDAO.countAll();
        assertTrue(count >= 0);
    }
}
