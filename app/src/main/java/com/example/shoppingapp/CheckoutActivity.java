package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.adapter.OrderDetailAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private AppDatabase db;
    private int orderId;
    private EditText etAddress;
    private RadioGroup rgPaymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = AppDatabase.getInstance(this);
        orderId = getIntent().getIntExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etAddress = findViewById(R.id.etAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        TextView tvTotal = findViewById(R.id.tvCheckoutTotal);
        RecyclerView rvItems = findViewById(R.id.rvCheckoutItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setNestedScrollingEnabled(false);

        TextView btnConfirm = findViewById(R.id.btnConfirmOrder);
        btnConfirm.setOnClickListener(v -> confirmOrder());

        // Load order details
        AppDatabase.databaseExecutor.execute(() -> {
            List<OrderDetail> details = db.orderDetailDao().getOrderDetailsByOrderId(orderId);
            Map<Integer, Product> productMap = new HashMap<>();
            for (OrderDetail d : details) {
                Product p = db.productDao().getProductById(d.getProductId());
                if (p != null) productMap.put(p.getId(), p);
            }

            Order order = db.orderDao().getOrderById(orderId);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            runOnUiThread(() -> {
                if (order != null) {
                    tvTotal.setText(formatter.format(order.getTotalAmount()) + "đ");
                }
                OrderDetailAdapter adapter = new OrderDetailAdapter(new ArrayList<>(details), productMap);
                rvItems.setAdapter(adapter);
            });
        });
    }

    private void confirmOrder() {
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            etAddress.requestFocus();
            return;
        }

        String paymentMethod;
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCOD) {
            paymentMethod = "COD";
        } else if (selectedId == R.id.rbBank) {
            paymentMethod = "BankTransfer";
        } else if (selectedId == R.id.rbEWallet) {
            paymentMethod = "EWallet";
        } else {
            paymentMethod = "COD";
        }

        // Quy trình thanh toán luôn, không qua xác nhận đặt hàng nữa theo yêu cầu
        processOrder(address, paymentMethod);
    }

    private void processOrder(String address, String paymentMethod) {
        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order == null) return;
            
            // Trạng thái luôn là "Paid" sau khi thanh toán
            order.setStatus("Paid");
            order.setAddress(address);
            order.setPaymentMethod(paymentMethod);
            db.orderDao().update(order);

            runOnUiThread(() -> {
                Intent intent = new Intent(this, OrderSuccessActivity.class);
                intent.putExtra("orderId", orderId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }
}
