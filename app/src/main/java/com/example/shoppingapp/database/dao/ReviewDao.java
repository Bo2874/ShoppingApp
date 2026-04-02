package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoppingapp.database.entity.Review;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insert(Review review);

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY id DESC")
    List<Review> getReviewsByProduct(int productId);

    @Query("SELECT AVG(rating) FROM reviews WHERE productId = :productId")
    float getAverageRating(int productId);

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    int getReviewCount(int productId);
}
