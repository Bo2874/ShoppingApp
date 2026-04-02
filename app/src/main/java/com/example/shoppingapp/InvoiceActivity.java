package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        ImageButton btnBackToolbar = findViewById(R.id.btnBackToolbar);
        TextView tvInvoiceId = findViewById(R.id.tvInvoiceId);
        TextView tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        TextView tvInvoiceCustomer = findViewById(R.id.tvInvoiceCustomer);
        TextView tvInvoicePhone = findViewById(R.id.tvInvoicePhone);
        TextView tvInvoiceEmail = findViewById(R.id.tvInvoiceEmail);
        TextView tvInvoiceAddress = findViewById(R.id.tvInvoiceAddress);
        TextView tvInvoicePayment = findViewById(R.id.tvInvoicePayment);
        TextView tvInvoiceStatus = findViewById(R.id.tvInvoiceStatus);
        TextView tvInvoiceTotal = findViewById(R.id.tvInvoiceTotal);
        LinearLayout rowPhone = findViewById(R.id.rowPhone);
        LinearLayout rowEmail = findViewById(R.id.rowEmail);
        LinearLayout rowAddress = findViewById(R.id.rowAddress);
        LinearLayout rowPayment = findViewById(R.id.rowPayment);
        RecyclerView rv = findViewById(R.id.rvInvoiceItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView btnBackHome = findViewById(R.id.btnBackHome);
        TextView btnReorder = findViewById(R.id.btnReorder);

        btnBackToolbar.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnReorder.setOnClickListener(v -> reorder());

        // Load data
        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
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
                tvInvoiceId.setText("#" + order.getId());
                tvInvoiceDate.setText(order.getOrderDate());
                tvInvoiceCustomer.setText(user != null ? user.getFullName() : "N/A");
                tvInvoiceTotal.setText(formatter.format(order.getTotalAmount()) + "đ");

                // Status badge
                String statusText;
                switch (order.getStatus()) {
                    case "Paid":
                        statusText = "Đã thanh toán";
                        tvInvoiceStatus.setBackgroundResource(R.drawable.bg_status_badge);
                        break;
                    case "Delivering":
                        statusText = "Đang giao hàng";
                        tvInvoiceStatus.setBackgroundResource(R.drawable.bg_status_delivering);
                        break;
                    default:
                        statusText = order.getStatus();
                        tvInvoiceStatus.setBackgroundResource(R.drawable.bg_status_badge);
                }
                tvInvoiceStatus.setText(statusText);

                // Phone
                if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                    tvInvoicePhone.setText(user.getPhone());
                    rowPhone.setVisibility(View.VISIBLE);
                }

                // Email
                if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    tvInvoiceEmail.setText(user.getEmail());
                    rowEmail.setVisibility(View.VISIBLE);
                }

                // Address
                if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                    tvInvoiceAddress.setText(order.getAddress());
                    rowAddress.setVisibility(View.VISIBLE);
                }

                // Payment method
                if (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty()) {
                    String paymentLabel;
                    switch (order.getPaymentMethod()) {
                        case "COD": paymentLabel = "Thanh toán khi nhận hàng"; break;
                        case "BankTransfer": paymentLabel = "Chuyển khoản ngân hàng"; break;
                        case "EWallet": paymentLabel = "Ví điện tử"; break;
                        default: paymentLabel = order.getPaymentMethod();
                    }
                    tvInvoicePayment.setText(paymentLabel);
                    rowPayment.setVisibility(View.VISIBLE);
                }

                rv.setAdapter(new OrderDetailAdapter(details, productMap));
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

            Order pendingOrder = db.orderDao().getPendingOrder(userId);
            int newOrderId;
            if (pendingOrder == null) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                newOrderId = (int) db.orderDao().insert(new Order(userId, date, 0, "Pending"));
            } else {
                newOrderId = pendingOrder.getId();
            }

            for (OrderDetail old : oldDetails) {
                Product product = db.productDao().getProductById(old.getProductId());
                if (product == null) continue;

                OrderDetail existing = db.orderDetailDao().getOrderDetail(newOrderId, old.getProductId());
                if (existing != null) {
                    int newQty = Math.min(existing.getQuantity() + old.getQuantity(), 99);
                    db.orderDetailDao().setQuantity(existing.getId(), newQty);
                } else {
                    db.orderDetailDao().insert(new OrderDetail(newOrderId, old.getProductId(), old.getQuantity(), product.getPrice()));
                }
            }

            double total = db.orderDetailDao().getTotalByOrderId(newOrderId);
            Order updatedOrder = db.orderDao().getOrderById(newOrderId);
            updatedOrder.setTotalAmount(total);
            db.orderDao().update(updatedOrder);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("openCart", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
