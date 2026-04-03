package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

        // Views
        ImageView ivProduct = findViewById(R.id.ivProductDetail);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        tvQuantity = findViewById(R.id.tvQuantity);
        TextView btnMinus = findViewById(R.id.btnMinus);
        TextView btnPlus = findViewById(R.id.btnPlus);
        LinearLayout layoutRelated = findViewById(R.id.layoutRelatedProducts);
        RecyclerView rvRelated = findViewById(R.id.rvRelatedProducts);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);

        btnBack.setOnClickListener(v -> finish());

        // Quantity selector
        if (tvQuantity != null) {
            tvQuantity.setText(String.valueOf(quantity));
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    tvQuantity.setText(String.valueOf(quantity));
                }
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                if (quantity < 99) {
                    quantity++;
                    tvQuantity.setText(String.valueOf(quantity));
                }
            });
        }

        // Add to cart
        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                if (product == null) return;
                if (!sessionManager.isLoggedIn()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Yêu cầu đăng nhập")
                            .setMessage("Bạn cần đăng nhập để thêm vào giỏ hàng. Đăng nhập ngay?")
                            .setPositiveButton("Đăng nhập", (d, w) ->
                                    loginLauncher.launch(new Intent(this, LoginActivity.class)))
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    addToCart();
                }
            });
        }

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

            // Load category name
            Category category = db.categoryDao().getCategoryById(product.getCategoryId());
            String categoryName = category != null ? category.getName() : "";

            // Load related products
            List<Product> relatedProducts = db.productDao().getRelatedProducts(
                    product.getCategoryId(), product.getId(), 4);

            runOnUiThread(() -> {
                tvName.setText(product.getName());
                tvDescription.setText(product.getDescription());

                if (tvCategory != null) {
                    tvCategory.setText(categoryName);
                }

                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(formatter.format(product.getPrice()) + "đ/" + product.getUnit());

                // Load image
                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivProduct);

                // Related products
                if (!relatedProducts.isEmpty() && layoutRelated != null && rvRelated != null) {
                    layoutRelated.setVisibility(android.view.View.VISIBLE);
                    rvRelated.setLayoutManager(new GridLayoutManager(this, 2));
                    ProductAdapter relatedAdapter = new ProductAdapter(new ArrayList<>(relatedProducts), p -> {
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
            Order order = db.orderDao().getPendingOrder(userId);
            int orderId;
            if (order == null) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                orderId = (int) db.orderDao().insert(new Order(userId, date, 0, "Pending"));
            } else {
                orderId = order.getId();
            }

            OrderDetail existing = db.orderDetailDao().getOrderDetail(orderId, product.getId());
            if (existing != null) {
                db.orderDetailDao().addQuantity(existing.getId(), quantity);
            } else {
                db.orderDetailDao().insert(new OrderDetail(orderId, product.getId(), quantity, product.getPrice()));
            }

            // Update order total
            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order updated = db.orderDao().getOrderById(orderId);
            if (updated != null) {
                updated.setTotalAmount(total);
                db.orderDao().update(updated);
            }

            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Thêm thành công!")
                        .setMessage("Đã thêm " + quantity + " " + product.getName() + " vào giỏ hàng")
                        .setPositiveButton("Xem giỏ hàng", (d, w) -> {
                            Intent i = new Intent(this, MainActivity.class);
                            i.putExtra("openCart", true);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                            finish();
                        })
                        .setNegativeButton("Tiếp tục chọn", null)
                        .show();
            });
        });
    }
}
