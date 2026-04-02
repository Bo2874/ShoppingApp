package com.example.shoppingapp;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.example.shoppingapp.database.entity.Favorite;
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
    private String selectedSize = null;
    private boolean isFavorite = false;
    private ImageView btnFavorite;

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
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvBrand = findViewById(R.id.tvDetailBrand);
        TextView tvRating = findViewById(R.id.tvRating);
        TextView tvReviewCount = findViewById(R.id.tvReviewCount);
        LinearLayout layoutSizes = findViewById(R.id.layoutSizes);
        LinearLayout layoutRelated = findViewById(R.id.layoutRelatedProducts);
        RecyclerView rvRelated = findViewById(R.id.rvRelatedProducts);
        TextView tvTabDescription = findViewById(R.id.tvTabDescription);
        TextView tvTabReviews = findViewById(R.id.tvTabReviews);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddToCartIcon = findViewById(R.id.btnAddToCartIcon);
        View btnBuyNow = findViewById(R.id.btnBuyNow);

        btnBack.setOnClickListener(v -> finish());

        // Tab toggle
        if (tvTabDescription != null && tvTabReviews != null) {
            tvTabDescription.setOnClickListener(v -> {
                tvDetailDescriptionShow(tvDescription, true);
                tvTabDescription.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                tvTabDescription.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTabReviews.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
                tvTabReviews.setTypeface(null, android.graphics.Typeface.NORMAL);
            });
            tvTabReviews.setOnClickListener(v -> {
                tvDetailDescriptionShow(tvDescription, false);
                tvTabReviews.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                tvTabReviews.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTabDescription.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
                tvTabDescription.setTypeface(null, android.graphics.Typeface.NORMAL);
            });
        }

        // Add to cart button
        btnAddToCartIcon.setOnClickListener(v -> {
            if (product == null) return;
            if (selectedSize == null && product.getSizes() != null && !product.getSizes().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                promptLogin();
            } else {
                addToCart();
            }
        });

        // Buy now button
        btnBuyNow.setOnClickListener(v -> {
            if (product == null) return;
            if (selectedSize == null && product.getSizes() != null && !product.getSizes().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                promptLogin();
            } else {
                addToCartAndCheckout();
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

            if (sessionManager.isLoggedIn()) {
                isFavorite = db.favoriteDao().isFavorite(sessionManager.getUserId(), productId);
            }

            List<Product> relatedProducts = db.productDao().getRelatedProducts(
                    product.getCategoryId(), product.getId(), 4);

            runOnUiThread(() -> {
                tvName.setText(product.getName());
                tvDescription.setText(product.getDescription());
                if (tvBrand != null) tvBrand.setText(product.getBrand());
                if (tvRating != null) tvRating.setText(String.valueOf(product.getRating()));
                if (tvReviewCount != null) tvReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");

                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(formatter.format(product.getPrice()) + "đ");

                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivProduct);

                // Size selector
                if (layoutSizes != null && product.getSizes() != null && !product.getSizes().isEmpty()) {
                    String[] sizes = product.getSizes().split(",");
                    List<TextView> sizeViews = new ArrayList<>();
                    for (String size : sizes) {
                        String trimmed = size.trim();
                        TextView sv = new TextView(this);
                        int dp44 = dpToPx(44);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp44, dp44);
                        params.setMargins(0, 0, dpToPx(8), 0);
                        sv.setLayoutParams(params);
                        sv.setText(trimmed);
                        sv.setGravity(Gravity.CENTER);
                        sv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        sv.setBackgroundResource(R.drawable.bg_size_normal);
                        sv.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));

                        sv.setOnClickListener(click -> {
                            selectedSize = trimmed;
                            for (TextView s : sizeViews) {
                                s.setBackgroundResource(R.drawable.bg_size_normal);
                                s.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                            }
                            sv.setBackgroundResource(R.drawable.bg_size_selected);
                            sv.setTextColor(getResources().getColor(R.color.white, getTheme()));
                        });

                        sizeViews.add(sv);
                        layoutSizes.addView(sv);
                    }
                }

                // Favorite
                updateFavoriteIcon();

                // Related products
                if (!relatedProducts.isEmpty()) {
                    layoutRelated.setVisibility(View.VISIBLE);
                    rvRelated.setLayoutManager(new GridLayoutManager(this, 2));
                    rvRelated.setNestedScrollingEnabled(false);
                    rvRelated.setAdapter(new ProductAdapter(new ArrayList<>(relatedProducts), p -> {
                        Intent intent = new Intent(this, ProductDetailActivity.class);
                        intent.putExtra("productId", p.getId());
                        startActivity(intent);
                        finish();
                    }));
                }
            });
        });
    }

    private void tvDetailDescriptionShow(TextView tvDesc, boolean show) {
        if (tvDesc != null) tvDesc.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void promptLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để tiếp tục. Đăng nhập ngay?")
                .setPositiveButton("Đăng nhập", (d, w) -> loginLauncher.launch(new Intent(this, LoginActivity.class)))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateFavoriteIcon() {
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
    }

    private void toggleFavorite() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product == null) return;
        AppDatabase.databaseExecutor.execute(() -> {
            if (isFavorite) {
                db.favoriteDao().delete(sessionManager.getUserId(), product.getId());
                isFavorite = false;
            } else {
                db.favoriteDao().insert(new Favorite(sessionManager.getUserId(), product.getId()));
                isFavorite = true;
            }
            runOnUiThread(this::updateFavoriteIcon);
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
            boolean isUpdate = existing != null;
            if (existing != null) {
                db.orderDetailDao().setQuantity(existing.getId(), Math.min(existing.getQuantity() + 1, 99));
            } else {
                db.orderDetailDao().insert(new OrderDetail(orderId, product.getId(), 1, product.getPrice()));
            }

            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order updated = db.orderDao().getOrderById(orderId);
            updated.setTotalAmount(total);
            db.orderDao().update(updated);

            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? getString(R.string.added_to_cart_update) : "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setTitle("Thêm thành công!")
                        .setMessage("Bạn muốn tiếp tục mua sắm hay xem giỏ hàng?")
                        .setPositiveButton("Xem giỏ hàng", (d, w) -> {
                            Intent i = new Intent(this, MainActivity.class);
                            i.putExtra("openCart", true);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                            finish();
                        })
                        .setNegativeButton("Tiếp tục mua", null)
                        .show();
            });
        });
    }

    private void addToCartAndCheckout() {
        if (product == null) return;
        AppDatabase.databaseExecutor.execute(() -> {
            int userId = sessionManager.getUserId();

            // Tạo đơn hàng MỚI riêng cho "Mua ngay", không dùng giỏ hàng pending
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Order buyNowOrder = new Order(userId, date, 0, "BuyNow");
            int orderId = (int) db.orderDao().insert(buyNowOrder);

            db.orderDetailDao().insert(new OrderDetail(orderId, product.getId(), 1, product.getPrice()));

            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order updated = db.orderDao().getOrderById(orderId);
            updated.setTotalAmount(total);
            db.orderDao().update(updated);

            runOnUiThread(() -> {
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            });
        });
    }
}
