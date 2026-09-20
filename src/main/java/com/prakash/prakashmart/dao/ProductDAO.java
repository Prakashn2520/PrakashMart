package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for product catalog, search, filtering, and inventory operations.
 */
public interface ProductDAO {

    /**
     * Creates / saves a new product.
     *
     * @param product Product entity to insert
     * @return Persisted Product with generated ID
     * @throws DatabaseException if a database error occurs
     */
    Product create(Product product) throws DatabaseException;

    /**
     * Alias / convenience for create to preserve backward compatibility.
     *
     * @param product Product entity to insert
     * @return Persisted Product with generated ID
     * @throws DatabaseException if a database error occurs
     */
    Product save(Product product) throws DatabaseException;

    /**
     * Updates an existing product listing.
     *
     * @param product Product entity to update
     * @throws DatabaseException if a database error occurs
     */
    void update(Product product) throws DatabaseException;

    /**
     * Deletes a product listing by ID.
     *
     * @param id Product ID
     * @throws DatabaseException if a database error occurs
     */
    void delete(Long id) throws DatabaseException;

    /**
     * Finds a product by its primary key.
     *
     * @param id Product ID
     * @return Optional containing the Product if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<Product> findById(Long id) throws DatabaseException;

    /**
     * Retrieves all products listed by a specific seller.
     *
     * @param sellerId Seller ID
     * @return List of products
     * @throws DatabaseException if a database error occurs
     */
    List<Product> findBySellerId(Long sellerId) throws DatabaseException;

    /**
     * Retrieves products listed by a seller (alias for findBySellerId).
     *
     * @param sellerId Seller ID
     * @return List of products
     * @throws DatabaseException if a database error occurs
     */
    List<Product> findBySeller(Long sellerId) throws DatabaseException;

    /**
     * Retrieves all products in a given category.
     *
     * @param category Category name
     * @return List of products
     * @throws DatabaseException if a database error occurs
     */
    List<Product> findByCategory(String category) throws DatabaseException;

    /**
     * Searches and filters products by category and keyword with offset/limit pagination.
     *
     * @param category Optional category filter
     * @param keyword  Optional keyword filter for name or description
     * @param offset   Pagination offset
     * @param limit    Page size limit
     * @return List of matching products
     * @throws DatabaseException if a database error occurs
     */
    List<Product> searchProducts(String category, String keyword, int offset, int limit) throws DatabaseException;

    /**
     * Searches, filters, and sorts products returning a full paginated result.
     *
     * @param keyword     Optional keyword filter
     * @param category    Optional category filter
     * @param minPrice    Optional minimum price
     * @param maxPrice    Optional maximum price
     * @param inStockOnly If true, only return items with stock_qty > 0
     * @param page        Page number (1-based)
     * @param size        Page size
     * @param sortBy      Field to sort by ("price", "name", "created_at", "stock_qty", "id")
     * @param sortOrder   Sort order ("ASC" or "DESC")
     * @return PaginatedResult containing page info and products
     * @throws DatabaseException if a database error occurs
     */
    PaginatedResult<Product> findPaginated(String keyword, String category,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Boolean inStockOnly,
                                          int page, int size,
                                          String sortBy, String sortOrder) throws DatabaseException;

    /**
     * Counts the total number of products matching filter criteria.
     *
     * @param keyword     Optional keyword filter
     * @param category    Optional category filter
     * @param minPrice    Optional minimum price
     * @param maxPrice    Optional maximum price
     * @param inStockOnly If true, count only in-stock products
     * @return Total count of matching products
     * @throws DatabaseException if a database error occurs
     */
    long countProducts(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly) throws DatabaseException;

    /**
     * Counts all active products.
     *
     * @return Total active product count
     * @throws DatabaseException if a database error occurs
     */
    long countAll() throws DatabaseException;

    /**
     * Updates product stock quantity.
     *
     * @param productId   Product ID
     * @param newStockQty New stock quantity
     * @throws DatabaseException if a database error occurs
     */
    void updateStock(Long productId, int newStockQty) throws DatabaseException;
}
