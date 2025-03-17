// Review.java
package com.example.save_food.models;

public class Review {
    private String productId;
    private String userId;
    private Long ratingType;
    private long timestamp;
    private String review;

    public Review() { }

    public Review(String productId, String userId, Long ratingType, long timestamp, String review) {
        this.productId = productId;
        this.userId = userId;
        this.ratingType = ratingType;
        this.timestamp = timestamp;
        this.review = review;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Long getRatingType() {
        return ratingType;
    }
    public void setRatingType(Long ratingType) {
        this.ratingType = ratingType;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
}
