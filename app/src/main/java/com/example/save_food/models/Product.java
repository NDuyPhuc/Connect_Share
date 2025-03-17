// Product.java
package com.example.save_food.models;

public class Product {
    private String productId;
    private String title;
    private String imageLink;
    private String status;
    private String userId;
    private int ratingType;


    public Product(String productId, String title, String imageLink, String status, String userId) {
        this.productId = productId;
        this.title = title;
        this.imageLink = imageLink;
        this.status = status;
        this.userId = userId;
        this.ratingType = 0;

    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getImageLink() {
        return imageLink;
    }
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getRatingType() {
        return ratingType;
    }
    public void setRatingType(int ratingType) {
        this.ratingType = ratingType;
    }



}
