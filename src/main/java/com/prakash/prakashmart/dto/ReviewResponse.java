package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.Review;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO for returning reviews to clients.
 */
public class ReviewResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id, Long productId, Long userId, String userName,
                          Integer rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static ReviewResponse fromReview(Review review) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    public static ReviewResponse fromReview(Review review, String userName) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                userName,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ReviewResponse{" +
                "id=" + id +
                ", productId=" + productId +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
