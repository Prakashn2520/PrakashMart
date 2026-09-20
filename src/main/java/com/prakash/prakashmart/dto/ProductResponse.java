package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DTO for returning product details to clients.
 */
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private Boolean isActive;
    private Timestamp createdAt;

    public ProductResponse() {
    }

    public ProductResponse(Long id, Long sellerId, String sellerName, String name, String description,
                           BigDecimal price, Integer stockQty, String category, String imageUrl,
                           Boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public ProductResponse(Long id, Long sellerId, String name, String description,
                           BigDecimal price, Integer stockQty, String category, String imageUrl) {
        this(id, sellerId, null, name, description, price, stockQty, category, imageUrl, true, null);
    }

    public static ProductResponse fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
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

    public static ProductResponse fromProduct(Product product, String sellerName) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                sellerName,
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", category='" + category + '\'' +
                '}';
    }
}
