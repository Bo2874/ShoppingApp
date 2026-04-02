package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoppingapp.database.entity.Favorite;
import com.example.shoppingapp.database.entity.Product;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    void delete(int userId, int productId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND productId = :productId)")
    boolean isFavorite(int userId, int productId);

    @Query("SELECT p.* FROM products p INNER JOIN favorites f ON p.id = f.productId WHERE f.userId = :userId")
    List<Product> getFavoriteProducts(int userId);

    @Query("SELECT productId FROM favorites WHERE userId = :userId")
    List<Integer> getFavoriteProductIds(int userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    int getFavoriteCount(int userId);
}
