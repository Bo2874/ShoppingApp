package com.example.shoppingapp.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_details",
        foreignKeys = {
                @ForeignKey(
                        entity = Order.class,
                        parentColumns = "id",
                        childColumns = "orderId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("orderId"), @Index("productId")})
public class OrderDetail {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;

    public OrderDetail(int orderId, int productId, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
