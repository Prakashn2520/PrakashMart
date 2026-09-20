package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.Product;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Backward-compatible alias for ProductResponse.
 */
public class ProductDTO extends ProductResponse {
    private static final long serialVersionUID = 1L;

    public ProductDTO() {
        super();
    }

    public ProductDTO(Long id, Long sellerId, String sellerName, String name, String description,
                      BigDecimal price, Integer stockQty, String category, String imageUrl,
                      Boolean isActive, Timestamp createdAt) {
        super(id, sellerId, sellerName, name, description, price, stockQty, category, imageUrl, isActive, createdAt);
    }

    public ProductDTO(Long id, Long sellerId, String name, String description,
                      BigDecimal price, Integer stockQty, String category, String imageUrl) {
        super(id, sellerId, name, description, price, stockQty, category, imageUrl);
    }

    public static ProductDTO fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDTO(
                product.getId(),
                product.getSellerId(),
                null,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQty(),
                product.getCategory(),
                product.getImageUrl(),
                product.getIsActive(),
                product.getCreatedAt()
        );
    }

    public Integer getStock() {
        return getStockQty();
    }

    public void setStock(Integer stock) {
        setStockQty(stock);
    }
}
