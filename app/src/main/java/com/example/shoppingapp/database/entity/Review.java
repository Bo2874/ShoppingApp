package com.example.shoppingapp.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews",
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("userId"), @Index("productId")})
public class Review {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private int productId;
    private float rating;
    private String comment;
    private String date;
    private String userName;

    public Review(int userId, int productId, float rating, String comment, String date, String userName) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.userName = userName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
