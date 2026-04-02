package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shoppingapp.database.entity.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(Order order);

    @Update
    void update(Order order);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = 'Pending' LIMIT 1")
    Order getPendingOrder(int userId);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    Order getOrderById(int id);

    @Query("SELECT * FROM orders WHERE userId = :userId")
    List<Order> getOrdersByUser(int userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = 'Paid' ORDER BY orderDate DESC")
    List<Order> getPaidOrders(int userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status NOT IN ('Pending', 'BuyNow') ORDER BY orderDate DESC")
    List<Order> getCompletedOrders(int userId);

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = :status ORDER BY orderDate DESC")
    List<Order> getOrdersByStatus(int userId, String status);

    @Query("DELETE FROM orders WHERE id = :id")
    void deleteById(int id);
}
