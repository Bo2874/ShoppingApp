package com.example.shoppingapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shoppingapp.database.entity.OrderDetail;

import java.util.List;

@Dao
public interface OrderDetailDao {
    @Insert
    void insert(OrderDetail orderDetail);

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    List<OrderDetail> getOrderDetailsByOrderId(int orderId);

    @Query("SELECT SUM(quantity * unitPrice) FROM order_details WHERE orderId = :orderId")
    double getTotalByOrderId(int orderId);

    @Query("SELECT * FROM order_details WHERE orderId = :orderId AND productId = :productId LIMIT 1")
    OrderDetail getOrderDetail(int orderId, int productId);

    @Query("UPDATE order_details SET quantity = quantity + :quantity WHERE id = :id")
    void updateQuantity(int id, int quantity);

    @Query("UPDATE order_details SET quantity = :quantity WHERE id = :id")
    void setQuantity(int id, int quantity);

    @Query("DELETE FROM order_details WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM order_details WHERE orderId = :orderId")
    int getItemCount(int orderId);
}
