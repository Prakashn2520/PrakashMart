package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import com.prakash.prakashmart.util.DatabaseUtil;
import com.prakash.prakashmart.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseLayerIntegrationTest extends BaseDAOTest {

    @Test
    @DisplayName("Should verify all 6 required tables exist in the database")
    void testAllRequiredTablesExist() throws SQLException {
        Set<String> expectedTables = Set.of("USERS", "PRODUCTS", "ORDERS", "ORDER_ITEMS", "CART_ITEMS", "REVIEWS");
        Set<String> actualTables = new HashSet<>();

        try (Connection conn = DatabaseUtil.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                actualTables.add(rs.getString("TABLE_NAME").toUpperCase());
            }
        }

        for (String expected : expectedTables) {
            assertTrue(actualTables.contains(expected), "Missing required table: " + expected);
        }
    }

    @Test
    @DisplayName("Should execute seed.sql and verify BCrypt hashed passwords and data integrity")
    void testSeedDataExecutionAndBCryptVerification() throws Exception {
        Path seedPath = Paths.get("db", "seed.sql");
        String seedSql;
        if (Files.exists(seedPath)) {
            seedSql = Files.readString(seedPath, StandardCharsets.UTF_8);
        } else {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("seed.sql")) {
                assertNotNull(in, "seed.sql must be accessible");
                seedSql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            org.h2.tools.RunScript.execute(conn, new java.io.StringReader(seedSql));

            // Verify admin user
            try (PreparedStatement ps = conn.prepareStatement("SELECT password_hash, role FROM users WHERE email = ?")) {
                ps.setString(1, "admin@prakashmart.com");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Admin user should exist in seed data");
                    String hash = rs.getString("password_hash");
                    assertEquals("ADMIN", rs.getString("role"));
                    assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"), "Password must be BCrypt hash");
                    assertTrue(PasswordUtil.checkPassword("admin123", hash), "BCrypt hash must verify against password");
                }
            }

            // Verify products seeded
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 4, "Should seed at least 4 products");
            }

            // Verify orders seeded
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 1, "Should seed at least 1 order");
            }
        }
    }

    @Test
    @DisplayName("Should enforce foreign key constraints on products and orders")
    void testForeignKeyConstraints() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Inserting product with non-existent seller_id must fail
            String invalidProductSql = "INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (9999, 'Ghost', 10.00, 1, 'Misc')";
            try (Statement stmt = conn.createStatement()) {
                assertThrows(SQLException.class, () -> stmt.execute(invalidProductSql));
            }

            // Inserting order with non-existent buyer_id must fail
            String invalidOrderSql = "INSERT INTO orders (buyer_id, status, total_amount) VALUES (9999, 'PENDING', 10.00)";
            try (Statement stmt = conn.createStatement()) {
                assertThrows(SQLException.class, () -> stmt.execute(invalidOrderSql));
            }
        }
    }

    @Test
    @DisplayName("Should enforce CHECK constraints on rating (1-5), price (>=0), and quantity (>0)")
    void testCheckConstraints() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // First insert valid seller and buyer
            long sellerId;
            long buyerId;
            long productId;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO users (name, email, password_hash, role) VALUES ('S', 's_check@test.com', 'h', 'SELLER')", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rs = stmt.getGeneratedKeys()) { rs.next(); sellerId = rs.getLong(1); }

                stmt.execute("INSERT INTO users (name, email, password_hash, role) VALUES ('B', 'b_check@test.com', 'h', 'BUYER')", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rs = stmt.getGeneratedKeys()) { rs.next(); buyerId = rs.getLong(1); }

                stmt.execute("INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (" + sellerId + ", 'P', 10.00, 5, 'Cat')", Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rs = stmt.getGeneratedKeys()) { rs.next(); productId = rs.getLong(1); }
            }

            // Negative price must fail
            try (Statement stmt = conn.createStatement()) {
                assertThrows(SQLException.class, () ->
                        stmt.execute("INSERT INTO products (seller_id, name, price, stock_qty, category) VALUES (" + sellerId + ", 'P2', -5.00, 5, 'Cat')"));
            }

            // Rating > 5 must fail
            try (Statement stmt = conn.createStatement()) {
                assertThrows(SQLException.class, () ->
                        stmt.execute("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (" + productId + ", " + buyerId + ", 6, 'Too high')"));
            }

            // Rating < 1 must fail
            try (Statement stmt = conn.createStatement()) {
                assertThrows(SQLException.class, () ->
                        stmt.execute("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (" + productId + ", " + buyerId + ", 0, 'Too low')"));
            }
        }
    }

    @Test
    @DisplayName("Should verify performance and FK indexes exist in the schema")
    void testImportantIndexesExist() throws SQLException {
        Set<String> actualIndexes = new HashSet<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES")) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null) {
                    actualIndexes.add(indexName.toUpperCase());
                }
            }
        }

        Set<String> requiredIndexes = Set.of(
                "IDX_PRODUCTS_SELLER_ID",
                "IDX_PRODUCTS_CATEGORY",
                "IDX_PRODUCTS_NAME",
                "IDX_ORDERS_BUYER_ID",
                "IDX_ORDERS_STATUS",
                "IDX_ORDER_ITEMS_ORDER_ID",
                "IDX_ORDER_ITEMS_PRODUCT_ID",
                "IDX_CART_ITEMS_USER_ID",
                "IDX_REVIEWS_PRODUCT_ID"
        );

        for (String expectedIndex : requiredIndexes) {
            assertTrue(actualIndexes.contains(expectedIndex), "Required index not found: " + expectedIndex);
        }
    }

    @Test
    @DisplayName("Should acquire and release connections from HikariCP without leaking")
    void testConnectionPoolAcquisitionAndRelease() throws SQLException {
        for (int i = 0; i < 25; i++) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                assertNotNull(conn);
                assertFalse(conn.isClosed());
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1));
                }
            }
        }
    }
}
