package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shoppingapp.database.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<Product> searchProducts(String query);

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<Product> searchProductsAdvanced(String query);

    @Query("SELECT * FROM products LIMIT :limit")
    List<Product> getFeaturedProducts(int limit);

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND id != :excludeId LIMIT :limit")
    List<Product> getRelatedProducts(int categoryId, int excludeId, int limit);

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId")
    void reduceStock(int productId, int quantity);
}
