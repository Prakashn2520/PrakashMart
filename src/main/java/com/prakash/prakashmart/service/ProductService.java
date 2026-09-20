package com.prakash.prakashmart.service;

import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.exception.AppException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for product management, filtering, searching, and inventory.
 */
public interface ProductService {

    /**
     * Retrieves a single product by ID.
     *
     * @param id Product ID
     * @return ProductDTO
     * @throws AppException if product not found
     */
    ProductDTO getProductById(Long id) throws AppException;

    /**
     * Searches and filters products with pagination (simple list).
     *
     * @param category Optional category filter
     * @param keyword  Optional keyword filter
     * @param page     Page number (1-based)
     * @param pageSize Page size
     * @return List of ProductDTOs
     * @throws AppException if query fails
     */
    List<ProductDTO> searchProducts(String category, String keyword, int page, int pageSize) throws AppException;

    /**
     * Searches, filters, and sorts products returning a full paginated result envelope.
     *
     * @param keyword     Optional keyword filter
     * @param category    Optional category filter
     * @param minPrice    Optional minimum price
     * @param maxPrice    Optional maximum price
     * @param inStockOnly If true, only in-stock items
     * @param page        Page number (1-based)
     * @param size        Page size
     * @param sortBy      Sort column
     * @param sortOrder   Sort order ("ASC" or "DESC")
     * @return PaginatedResult with page metadata and ProductDTO items
     * @throws AppException if query fails
     */
    PaginatedResult<ProductDTO> getProductsPaginated(String keyword, String category,
                                                    BigDecimal minPrice, BigDecimal maxPrice,
                                                    Boolean inStockOnly,
                                                    int page, int size,
                                                    String sortBy, String sortOrder) throws AppException;

    /**
     * Retrieves all products belonging to a seller.
     *
     * @param sellerId Seller user ID
     * @return List of ProductDTOs
     * @throws AppException if query fails
     */
    List<ProductDTO> getProductsBySeller(Long sellerId) throws AppException;

    /**
     * Creates a new product listing.
     *
     * @param dto Product details
     * @return Persisted ProductDTO
     * @throws AppException if validation fails or persistence error occurs
     */
    ProductDTO createProduct(ProductDTO dto) throws AppException;

    /**
     * Updates an existing product listing.
     * Only the seller who owns the product can update it.
     *
     * @param dto Product details
     * @throws AppException if validation fails or unauthorized
     */
    void updateProduct(ProductDTO dto) throws AppException;

    /**
     * Deletes a product listing.
     * Only the seller who owns the product can delete it.
     *
     * @param productId Product ID
     * @param sellerId  Seller ID for ownership verification
     * @throws AppException if unauthorized or not found
     */
    void deleteProduct(Long productId, Long sellerId) throws AppException;
}
