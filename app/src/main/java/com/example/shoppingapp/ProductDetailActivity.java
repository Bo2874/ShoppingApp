package com.example.shoppingapp;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private AppDatabase db;
    private SessionManager sessionManager;
    private Product product;
    private int quantity = 1;
    private TextView tvQuantity;

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && sessionManager.isLoggedIn()) {
                    addToCart();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        int productId = getIntent().getIntExtra("productId", -1);

        if (productId == -1) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivProduct = findViewById(R.id.ivProductDetail);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        TextView tvSaleBadge = findViewById(R.id.tvSaleBadge);
        tvQuantity = findViewById(R.id.tvQuantity);
        TextView btnMinus = findViewById(R.id.btnMinus);
        TextView btnPlus = findViewById(R.id.btnPlus);
        TextView btnAddToCart = findViewById(R.id.btnAddToCart);
        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout layoutRelated = findViewById(R.id.layoutRelatedProducts);
        RecyclerView rvRelated = findViewById(R.id.rvRelatedProducts);

        btnBack.setOnClickListener(v -> finish());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity >= 99) {
                Toast.makeText(this, "Số lượng tối đa là 99", Toast.LENGTH_SHORT).show();
                return;
            }
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            if (product == null) {
                Toast.makeText(this, "Đang tải thông tin sản phẩm...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (quantity < 1) {
                Toast.makeText(this, "Vui lòng chọn số lượng hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu đăng nhập")
                        .setMessage("Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng. Đăng nhập ngay?")
                        .setPositiveButton("Đăng nhập", (dialog, which) ->
                                loginLauncher.launch(new Intent(this, LoginActivity.class)))
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                addToCart();
            }
        });

        // Load product
        AppDatabase.databaseExecutor.execute(() -> {
            product = db.productDao().getProductById(productId);
            if (product == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            Category category = db.categoryDao().getCategoryById(product.getCategoryId());

            // Load related products
            List<Product> relatedProducts = db.productDao().getRelatedProducts(
                    product.getCategoryId(), product.getId(), 4);

            runOnUiThread(() -> {
                tvName.setText(product.getName());
                tvDescription.setText(product.getDescription());
                tvCategory.setText(category != null ? category.getName() : "");
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(formatter.format(product.getPrice()) + "đ/" + product.getUnit());

                // Show sale price
                if (product.isOnSale()) {
                    tvOriginalPrice.setVisibility(View.VISIBLE);
                    tvOriginalPrice.setText(formatter.format(product.getOriginalPrice()) + "đ");
                    tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    int percent = (int) ((1 - product.getPrice() / product.getOriginalPrice()) * 100);
                    tvSaleBadge.setText("GIẢM " + percent + "%");
                    tvSaleBadge.setVisibility(View.VISIBLE);
                }

                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivProduct);

                // Related products
                if (!relatedProducts.isEmpty()) {
                    layoutRelated.setVisibility(View.VISIBLE);
                    List<Product> relatedList = new ArrayList<>(relatedProducts);
                    rvRelated.setLayoutManager(new GridLayoutManager(this, 2));
                    rvRelated.setNestedScrollingEnabled(false);
                    ProductAdapter relatedAdapter = new ProductAdapter(relatedList, p -> {
                        Intent intent = new Intent(this, ProductDetailActivity.class);
                        intent.putExtra("productId", p.getId());
                        startActivity(intent);
                        finish();
                    });
                    rvRelated.setAdapter(relatedAdapter);
                }
            });
        });
    }

    private void addToCart() {
        if (product == null) return;

        AppDatabase.databaseExecutor.execute(() -> {
            int userId = sessionManager.getUserId();

            // Get or create pending order
            Order order = db.orderDao().getPendingOrder(userId);
            int orderId;
            if (order == null) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Order newOrder = new Order(userId, date, 0, "Pending");
                orderId = (int) db.orderDao().insert(newOrder);
            } else {
                orderId = order.getId();
            }

            // Check if product already in order - ADD quantity instead of replacing
            OrderDetail existing = db.orderDetailDao().getOrderDetail(orderId, product.getId());
            boolean isUpdate = existing != null;
            if (existing != null) {
                int newQty = Math.min(existing.getQuantity() + quantity, 99);
                db.orderDetailDao().setQuantity(existing.getId(), newQty);
            } else {
                OrderDetail detail = new OrderDetail(orderId, product.getId(), quantity, product.getPrice());
                db.orderDetailDao().insert(detail);
            }

            // Update order total
            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order updatedOrder = db.orderDao().getOrderById(orderId);
            updatedOrder.setTotalAmount(total);
            db.orderDao().update(updatedOrder);

            runOnUiThread(() -> {
                String message = isUpdate
                        ? getString(R.string.added_to_cart_update)
                        : "Đã thêm " + product.getName() + " vào giỏ hàng!";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                new AlertDialog.Builder(this)
                        .setTitle("Thêm thành công!")
                        .setMessage("Bạn muốn tiếp tục mua sắm hay xem giỏ hàng?")
                        .setPositiveButton("Xem giỏ hàng", (dialog, which) -> finish())
                        .setNegativeButton("Tiếp tục mua", (dialog, which) -> finish())
                        .show();
            });
        });
    }
}
