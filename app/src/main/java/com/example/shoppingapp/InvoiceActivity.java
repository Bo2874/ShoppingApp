package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.adapter.OrderDetailAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;
import com.example.shoppingapp.database.entity.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InvoiceActivity extends AppCompatActivity {

    private AppDatabase db;
    private SessionManager sessionManager;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);
        orderId = getIntent().getIntExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvInvoiceId = findViewById(R.id.tvInvoiceId);
        TextView tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        TextView tvInvoiceCustomer = findViewById(R.id.tvInvoiceCustomer);
        TextView tvInvoiceAddress = findViewById(R.id.tvInvoiceAddress);
        TextView tvInvoicePayment = findViewById(R.id.tvInvoicePayment);
        TextView tvInvoiceStatus = findViewById(R.id.tvInvoiceStatus);
        TextView tvInvoiceTotal = findViewById(R.id.tvInvoiceTotal);
        RecyclerView rv = findViewById(R.id.rvInvoiceItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView btnBackHome = findViewById(R.id.btnBackHome);
        TextView btnReorder = findViewById(R.id.btnReorder);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnReorder.setOnClickListener(v -> reorder());

        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            User user = db.userDao().getUserById(order.getUserId());
            List<OrderDetail> details = db.orderDetailDao().getOrderDetailsByOrderId(orderId);
            Map<Integer, Product> productMap = new HashMap<>();
            for (OrderDetail d : details) {
                Product p = db.productDao().getProductById(d.getProductId());
                if (p != null) productMap.put(p.getId(), p);
            }

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            runOnUiThread(() -> {
                tvInvoiceId.setText("Mã đơn hàng: #" + order.getId());
                tvInvoiceDate.setText("Ngày: " + order.getOrderDate());
                tvInvoiceCustomer.setText("Khách hàng: " + (user != null ? user.getFullName() : "N/A"));
                String statusText;
                switch (order.getStatus()) {
                    case "Paid": statusText = "Đã thanh toán"; break;
                    case "Delivering": statusText = "Đang giao hàng"; break;
                    default: statusText = order.getStatus();
                }
                tvInvoiceStatus.setText(statusText);
                tvInvoiceTotal.setText(formatter.format(order.getTotalAmount()) + "đ");

                // Show address if available
                if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                    tvInvoiceAddress.setText("Địa chỉ: " + order.getAddress());
                    tvInvoiceAddress.setVisibility(View.VISIBLE);
                }

                // Show payment method if available
                if (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty()) {
                    String paymentLabel;
                    switch (order.getPaymentMethod()) {
                        case "COD": paymentLabel = "Thanh toán khi nhận hàng"; break;
                        case "BankTransfer": paymentLabel = "Chuyển khoản ngân hàng"; break;
                        case "EWallet": paymentLabel = "Ví điện tử"; break;
                        default: paymentLabel = order.getPaymentMethod();
                    }
                    tvInvoicePayment.setText("Thanh toán: " + paymentLabel);
                    tvInvoicePayment.setVisibility(View.VISIBLE);
                }

                OrderDetailAdapter adapter = new OrderDetailAdapter(details, productMap);
                rv.setAdapter(adapter);
            });
        });
    }

    private void reorder() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseExecutor.execute(() -> {
            int userId = sessionManager.getUserId();
            List<OrderDetail> oldDetails = db.orderDetailDao().getOrderDetailsByOrderId(orderId);

            // Get or create pending order
            Order pendingOrder = db.orderDao().getPendingOrder(userId);
            int newOrderId;
            if (pendingOrder == null) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Order newOrder = new Order(userId, date, 0, "Pending");
                newOrderId = (int) db.orderDao().insert(newOrder);
            } else {
                newOrderId = pendingOrder.getId();
            }

            // Add items from old order
            for (OrderDetail old : oldDetails) {
                Product product = db.productDao().getProductById(old.getProductId());
                if (product == null) continue;

                OrderDetail existing = db.orderDetailDao().getOrderDetail(newOrderId, old.getProductId());
                if (existing != null) {
                    int newQty = Math.min(existing.getQuantity() + old.getQuantity(), 99);
                    db.orderDetailDao().setQuantity(existing.getId(), newQty);
                } else {
                    OrderDetail detail = new OrderDetail(newOrderId, old.getProductId(), old.getQuantity(), product.getPrice());
                    db.orderDetailDao().insert(detail);
                }
            }

            // Update total
            double total = db.orderDetailDao().getTotalByOrderId(newOrderId);
            Order updatedOrder = db.orderDao().getOrderById(newOrderId);
            updatedOrder.setTotalAmount(total);
            db.orderDao().update(updatedOrder);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
