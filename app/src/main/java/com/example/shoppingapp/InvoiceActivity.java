package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        AppDatabase db = AppDatabase.getInstance(this);
        int orderId = getIntent().getIntExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvInvoiceId = findViewById(R.id.tvInvoiceId);
        TextView tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        TextView tvInvoiceCustomer = findViewById(R.id.tvInvoiceCustomer);
        TextView tvInvoiceStatus = findViewById(R.id.tvInvoiceStatus);
        TextView tvInvoiceTotal = findViewById(R.id.tvInvoiceTotal);
        RecyclerView rv = findViewById(R.id.rvInvoiceItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView btnBackHome = findViewById(R.id.btnBackHome);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

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
                tvInvoiceStatus.setText(order.getStatus().equals("Paid") ? "Đã thanh toán" : order.getStatus());
                tvInvoiceTotal.setText(formatter.format(order.getTotalAmount()) + "đ");

                OrderDetailAdapter adapter = new OrderDetailAdapter(details, productMap);
                rv.setAdapter(adapter);
            });
        });
    }
}
