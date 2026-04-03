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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InvoiceActivity extends AppCompatActivity {

    private AppDatabase db;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        db = AppDatabase.getInstance(this);
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
        TextView tvInvoiceStatus = findViewById(R.id.tvInvoiceStatus);
        TextView tvInvoiceTotal = findViewById(R.id.tvInvoiceTotal);
        LinearLayout rowPhone = findViewById(R.id.rowPhone);
        RecyclerView rv = findViewById(R.id.rvInvoiceItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView btnBackHome = findViewById(R.id.btnBackHome);

        btnBackToolbar.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

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
                if ("Paid".equals(order.getStatus())) {
                    statusText = "Đã thanh toán";
                } else {
                    statusText = order.getStatus();
                }
                tvInvoiceStatus.setText(statusText);

                // Phone
                if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                    tvInvoicePhone.setText(user.getPhone());
                    rowPhone.setVisibility(View.VISIBLE);
                }

                rv.setAdapter(new OrderDetailAdapter(details, productMap));
            });
        });
    }
}
