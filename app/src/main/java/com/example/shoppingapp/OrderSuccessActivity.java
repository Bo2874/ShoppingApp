package com.example.shoppingapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        int orderId = getIntent().getIntExtra("orderId", -1);

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        TextView tvTotal = findViewById(R.id.tvTotal);
        TextView tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        View viewCircleBg = findViewById(R.id.viewCircleBg);
        View ivCheck = findViewById(R.id.ivCheck);
        TextView btnViewOrder = findViewById(R.id.btnViewOrder);
        TextView btnContinueShopping = findViewById(R.id.btnContinueShopping);

        // Animate success icon
        viewCircleBg.setScaleX(0f);
        viewCircleBg.setScaleY(0f);
        ivCheck.setAlpha(0f);

        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(viewCircleBg, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(viewCircleBg, "scaleY", 0f, 1f);
        scaleX.setInterpolator(new OvershootInterpolator(2f));
        scaleY.setInterpolator(new OvershootInterpolator(2f));
        scaleX.setDuration(600);
        scaleY.setDuration(600);

        ObjectAnimator checkFade = ObjectAnimator.ofFloat(ivCheck, "alpha", 0f, 1f);
        checkFade.setStartDelay(400);
        checkFade.setDuration(300);

        animSet.playTogether(scaleX, scaleY, checkFade);
        animSet.start();

        // Delivery estimate: 3-5 days from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        String fromDate = new SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 2);
        String toDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
        tvDeliveryDate.setText(fromDate + " - " + toDate);

        // Load order info
        if (orderId != -1) {
            AppDatabase db = AppDatabase.getInstance(this);
            AppDatabase.databaseExecutor.execute(() -> {
                Order order = db.orderDao().getOrderById(orderId);
                if (order == null) return;
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                runOnUiThread(() -> {
                    tvOrderId.setText("#" + order.getId());
                    tvTotal.setText(formatter.format(order.getTotalAmount()) + "đ");

                    String paymentLabel;
                    if (order.getPaymentMethod() == null) {
                        paymentLabel = "COD";
                    } else {
                        switch (order.getPaymentMethod()) {
                            case "BankTransfer": paymentLabel = "Chuyển khoản"; break;
                            case "EWallet": paymentLabel = "Ví điện tử"; break;
                            default: paymentLabel = "COD";
                        }
                    }
                    tvPaymentMethod.setText(paymentLabel);
                });
            });
        }

        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, InvoiceActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        // Go to main instead of back to checkout
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
