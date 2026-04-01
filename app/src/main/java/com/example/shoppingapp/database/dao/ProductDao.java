package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoppingapp.database.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    List<Product> searchProducts(String query);

    @Query("SELECT * FROM products LIMIT :limit")
    List<Product> getFeaturedProducts(int limit);
}
