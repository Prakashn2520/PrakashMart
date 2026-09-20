package com.prakash.prakashmart.dao.impl;

import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JDBC implementation of ProductDAO using PreparedStatement and HikariCP connection pool.
 */
public class ProductDAOImpl implements ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "id", "name", "price", "stock_qty", "category", "created_at"
    );

    @Override
    public Product create(Product product) throws DatabaseException {
        return save(product);
    }

    @Override
    public Product save(Product product) throws DatabaseException {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty() != null ? product.getStockQty() : 0);
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());
            ps.setBoolean(8, product.getIsActive() != null ? product.getIsActive() : true);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            logger.error("Error saving product", e);
            throw new DatabaseException("Failed to save product", e);
        }
    }

    @Override
    public void update(Product product) throws DatabaseException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ?, is_active = ? "
                   + "WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.getIsActive() != null ? product.getIsActive() : true);
            ps.setLong(8, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating product {}", product.getId(), e);
            throw new DatabaseException("Failed to update product", e);
        }
    }

    @Override
    public void delete(Long id) throws DatabaseException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting product {}", id, e);
            throw new DatabaseException("Failed to delete product", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) throws DatabaseException {
        String sql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at "
                   + "FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding product by id {}", id, e);
            throw new DatabaseException("Failed to find product by ID", e);
        }
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) throws DatabaseException {
        return findBySeller(sellerId);
    }

    @Override
    public List<Product> findBySeller(Long sellerId) throws DatabaseException {
        String sql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at "
                   + "FROM products WHERE seller_id = ? ORDER BY id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding products by seller {}", sellerId, e);
            throw new DatabaseException("Failed to find products by seller", e);
        }
    }

    @Override
    public List<Product> findByCategory(String category) throws DatabaseException {
        String sql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at "
                   + "FROM products WHERE LOWER(category) = LOWER(?) AND is_active = TRUE ORDER BY id DESC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category != null ? category.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding products by category {}", category, e);
            throw new DatabaseException("Failed to find products by category", e);
        }
    }

    @Override
    public List<Product> searchProducts(String category, String keyword, int offset, int limit) throws DatabaseException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at "
                + "FROM products WHERE is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND LOWER(category) = LOWER(?) ");
            params.add(category.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)) ");
            String kwParam = "%" + keyword.trim() + "%";
            params.add(kwParam);
            params.add(kwParam);
        }

        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            throw new DatabaseException("Failed to search products", e);
        }
    }

    @Override
    public PaginatedResult<Product> findPaginated(String keyword, String category,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  Boolean inStockOnly,
                                                  int page, int size,
                                                  String sortBy, String sortOrder) throws DatabaseException {
        int safePage = Math.max(1, page);
        int safeSize = size > 0 ? size : 20;
        int offset = (safePage - 1) * safeSize;

        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder(" WHERE is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.append("AND (LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (category != null && !category.trim().isEmpty()) {
            whereClause.append("AND LOWER(category) = LOWER(?) ");
            params.add(category.trim());
        }

        if (minPrice != null) {
            whereClause.append("AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            whereClause.append("AND price <= ? ");
            params.add(maxPrice);
        }

        if (Boolean.TRUE.equals(inStockOnly)) {
            whereClause.append("AND stock_qty > 0 ");
        }

        // Count total results
        String countSql = "SELECT COUNT(*) FROM products" + whereClause.toString();
        long totalResults = 0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalResults = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting products for pagination", e);
            throw new DatabaseException("Failed to count products", e);
        }

        // Safe sort column and direction
        String safeSortCol = "id";
        if (sortBy != null && ALLOWED_SORT_COLUMNS.contains(sortBy.toLowerCase())) {
            safeSortCol = sortBy.toLowerCase();
        }
        String safeOrder = "DESC";
        if (sortOrder != null && ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder))) {
            safeOrder = sortOrder.toUpperCase();
        }

        // Fetch paginated data
        String dataSql = "SELECT id, seller_id, name, description, price, stock_qty, category, image_url, is_active, created_at "
                       + "FROM products" + whereClause.toString()
                       + "ORDER BY " + safeSortCol + " " + safeOrder + " LIMIT ? OFFSET ?";

        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(dataSql)) {
            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }
            ps.setInt(paramIndex++, safeSize);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching paginated products", e);
            throw new DatabaseException("Failed to fetch paginated products", e);
        }

        return PaginatedResult.of(products, safePage, safeSize, totalResults);
    }

    @Override
    public long countProducts(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly) throws DatabaseException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND LOWER(category) = LOWER(?) ");
            params.add(category.trim());
        }

        if (minPrice != null) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }

        if (Boolean.TRUE.equals(inStockOnly)) {
            sql.append("AND stock_qty > 0 ");
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting products", e);
            throw new DatabaseException("Failed to count products", e);
        }
    }

    @Override
    public long countAll() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = TRUE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting all products", e);
            throw new DatabaseException("Failed to count all products", e);
        }
    }

    @Override
    public void updateStock(Long productId, int newStockQty) throws DatabaseException {
        String sql = "UPDATE products SET stock_qty = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStockQty);
            ps.setLong(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating stock for product {}", productId, e);
            throw new DatabaseException("Failed to update stock", e);
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setIsActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
