package com.prakash.prakashmart.dto;

import java.io.Serializable;

/**
 * DTO for submitting a product review and rating.
 */
public class ReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer rating;
    private String comment;

    public ReviewRequest() {
    }

    public ReviewRequest(Long productId, Integer rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ReviewRequest{" +
                "productId=" + productId +
                ", rating=" + rating +
                '}';
    }
}
