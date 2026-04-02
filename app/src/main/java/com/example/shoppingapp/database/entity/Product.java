package com.example.shoppingapp.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "products",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("categoryId"))
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String brand;
    private String description;
    private double price;
    private double originalPrice;
    private String imageUrl;
    private String unit;
    private int categoryId;
    private String sizes; // comma-separated: "38,39,40,41,42"
    private float rating;
    private int reviewCount;
    private int stockQuantity; // Trường số lượng tồn kho

    public Product(String name, String brand, String description, double price, String imageUrl, String unit, int categoryId) {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.unit = unit;
        this.categoryId = categoryId;
        this.originalPrice = 0;
        this.sizes = "38,39,40,41,42,43";
        this.rating = 4.5f;
        this.reviewCount = 0;
        this.stockQuantity = 50; // Mặc định là 50
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isOnSale() { return originalPrice > 0 && originalPrice > price; }
    public boolean isSoldOut() { return stockQuantity <= 0; }
}
