package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ProductNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for product operations, authorization checks, and inventory management.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductDAO productDAO;

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public ProductDTO getProductById(Long id) throws AppException {
        if (id == null || id <= 0) {
            throw new ValidationException("id", "Invalid product ID");
        }
        Product p = productDAO.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toDTO(p);
    }

    @Override
    public List<ProductDTO> searchProducts(String category, String keyword, int page, int pageSize) throws AppException {
        int safePage = ValidationUtil.validatePage(page);
        int safeLimit = ValidationUtil.validateLimit(pageSize);
        int offset = (safePage - 1) * safeLimit;

        List<Product> products = productDAO.searchProducts(category, keyword, offset, safeLimit);
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product p : products) {
            dtos.add(toDTO(p));
        }
        return dtos;
    }

    @Override
    public PaginatedResult<ProductDTO> getProductsPaginated(String keyword, String category,
                                                            BigDecimal minPrice, BigDecimal maxPrice,
                                                            Boolean inStockOnly,
                                                            int page, int size,
                                                            String sortBy, String sortOrder) throws AppException {
        int safePage = ValidationUtil.validatePage(page);
        int safeSize = ValidationUtil.validateLimit(size);

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("minPrice", "Minimum price cannot be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("maxPrice", "Maximum price cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ValidationException("price", "Minimum price cannot exceed maximum price");
        }

        PaginatedResult<Product> entityResult = productDAO.findPaginated(
                ValidationUtil.sanitizeSearchKeyword(keyword),
                category != null ? category.trim() : null,
                minPrice,
                maxPrice,
                inStockOnly,
                safePage,
                safeSize,
                sortBy,
                sortOrder
        );

        List<ProductDTO> dtos = new ArrayList<>();
        for (Product p : entityResult.getData()) {
            dtos.add(toDTO(p));
        }

        return new PaginatedResult<>(
                entityResult.getCurrentPage(),
                entityResult.getPageSize(),
                entityResult.getTotalResults(),
                entityResult.getTotalPages(),
                dtos
        );
    }

    @Override
    public List<ProductDTO> getProductsBySeller(Long sellerId) throws AppException {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID");
        }
        List<Product> products = productDAO.findBySellerId(sellerId);
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product p : products) {
            dtos.add(toDTO(p));
        }
        return dtos;
    }

    @Override
    public ProductDTO createProduct(ProductDTO dto) throws AppException {
        if (dto == null) {
            throw new ValidationException("Product payload cannot be empty");
        }
        if (dto.getSellerId() == null || dto.getSellerId() <= 0) {
            throw new ValidationException("sellerId", "Valid seller ID is required");
        }
        ValidationUtil.validateProductName(dto.getName());
        ValidationUtil.validatePrice(dto.getPrice());
        ValidationUtil.validateStockQuantity(dto.getStockQty());
        ValidationUtil.validateCategory(dto.getCategory());
        ValidationUtil.validateProductDescription(dto.getDescription());

        Product p = new Product();
        p.setSellerId(dto.getSellerId());
        p.setName(dto.getName().trim());
        p.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        p.setPrice(dto.getPrice());
        p.setStockQty(dto.getStockQty());
        p.setCategory(dto.getCategory().trim());
        p.setImageUrl(dto.getImageUrl());
        p.setIsActive(true);

        Product saved = productDAO.create(p);
        logger.info("Product created: {} by seller: {}", saved.getId(), saved.getSellerId());
        return toDTO(saved);
    }

    @Override
    public void updateProduct(ProductDTO dto) throws AppException {
        if (dto == null || dto.getId() == null) {
            throw new ValidationException("Product ID is required for update");
        }
        Product existing = productDAO.findById(dto.getId())
                .orElseThrow(() -> new ProductNotFoundException(dto.getId()));

        // Seller can only manage their own products
        if (dto.getSellerId() != null && !existing.getSellerId().equals(dto.getSellerId())) {
            throw new AuthorizationException("Unauthorized to modify this product listing");
        }

        ValidationUtil.validateProductName(dto.getName());
        ValidationUtil.validatePrice(dto.getPrice());
        ValidationUtil.validateStockQuantity(dto.getStockQty());
        ValidationUtil.validateCategory(dto.getCategory());
        ValidationUtil.validateProductDescription(dto.getDescription());

        existing.setName(dto.getName().trim());
        existing.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        existing.setPrice(dto.getPrice());
        existing.setStockQty(dto.getStockQty());
        existing.setCategory(dto.getCategory().trim());
        existing.setImageUrl(dto.getImageUrl());

        productDAO.update(existing);
        logger.info("Product updated: {}", existing.getId());
    }

    @Override
    public void deleteProduct(Long productId, Long sellerId) throws AppException {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID");
        }
        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Seller can only manage their own products
        if (sellerId != null && !existing.getSellerId().equals(sellerId)) {
            throw new AuthorizationException("Unauthorized to delete this product listing");
        }

        productDAO.delete(productId);
        logger.info("Product deleted: {}", productId);
    }

    private ProductDTO toDTO(Product p) {
        return new ProductDTO(
                p.getId(),
                p.getSellerId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQty(),
                p.getCategory(),
                p.getImageUrl()
        );
    }
}
