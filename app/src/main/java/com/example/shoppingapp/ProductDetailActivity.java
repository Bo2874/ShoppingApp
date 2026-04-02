package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.adapter.ReviewAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Favorite;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;
import com.example.shoppingapp.database.entity.Review;

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
    private ProductAdapter relatedAdapter;
    private ReviewAdapter reviewAdapter;
    private final List<Review> reviews = new ArrayList<>();
    private RecyclerView rvReviews;
    private LinearLayout layoutReviewList;
    private int pendingLoginAction = 0;

    private static final int ACTION_NONE = 0;
    private static final int ACTION_ADD_TO_CART = 1;
    private static final int ACTION_BUY_NOW = 2;
    private static final int ACTION_ADD_REVIEW = 3;
    private static final int ACTION_TOGGLE_FAVORITE = 4;

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && sessionManager.isLoggedIn()) {
                    if (pendingLoginAction == ACTION_BUY_NOW) {
                        if (ensureSizeSelected()) {
                            addToCartAndCheckout();
                        }
                    } else if (pendingLoginAction == ACTION_ADD_REVIEW) {
                        showReviewDialog();
                    } else if (pendingLoginAction == ACTION_TOGGLE_FAVORITE) {
                        toggleFavorite();
                    } else {
                        if (ensureSizeSelected()) {
                            addToCart();
                        }
                    }
                }
                pendingLoginAction = ACTION_NONE;
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
        TextView tvStock = findViewById(R.id.tvDetailStock); // Cần thêm vào XML
        LinearLayout layoutSizes = findViewById(R.id.layoutSizes);
        LinearLayout layoutRelated = findViewById(R.id.layoutRelatedProducts);
        RecyclerView rvRelated = findViewById(R.id.rvRelatedProducts);
        TextView tvTabDescription = findViewById(R.id.tvTabDescription);
        TextView tvTabReviews = findViewById(R.id.tvTabReviews);
        layoutReviewList = findViewById(R.id.layoutReviewList); // Cần thêm vào XML

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddToCartIcon = findViewById(R.id.btnAddToCartIcon);
        View btnBuyNow = findViewById(R.id.btnBuyNow);
        btnFavorite = findViewById(R.id.btnFavorite);
        Button btnAddReview = findViewById(R.id.btnAddReview); // Cần thêm vào XML

        btnBack.setOnClickListener(v -> finish());
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }

        // Tab toggle
        if (tvTabDescription != null && tvTabReviews != null) {
            tvTabDescription.setOnClickListener(v -> {
                tvDetailDescriptionShow(tvDescription, true);
                layoutReviewList.setVisibility(View.GONE);
                tvTabDescription.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                tvTabDescription.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTabReviews.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
                tvTabReviews.setTypeface(null, android.graphics.Typeface.NORMAL);
            });
            tvTabReviews.setOnClickListener(v -> {
                tvDetailDescriptionShow(tvDescription, false);
                layoutReviewList.setVisibility(View.VISIBLE);
                tvTabReviews.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                tvTabReviews.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTabDescription.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
                tvTabDescription.setTypeface(null, android.graphics.Typeface.NORMAL);
                loadReviews();
            });
        }

        btnAddReview.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                promptLogin(ACTION_ADD_REVIEW);
            } else {
                showReviewDialog();
            }
        });

        // Add to cart button
        btnAddToCartIcon.setOnClickListener(v -> {
            if (product == null) return;
            if (product.isSoldOut()) {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ensureSizeSelected()) {
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                promptLogin(ACTION_ADD_TO_CART);
            } else {
                addToCart();
            }
        });

        btnBuyNow.setOnClickListener(v -> {
            if (product == null) return;
            if (product.isSoldOut()) {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ensureSizeSelected()) {
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                promptLogin(ACTION_BUY_NOW);
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

            List<Integer> favoriteIds = new ArrayList<>();
            if (sessionManager.isLoggedIn()) {
                isFavorite = db.favoriteDao().isFavorite(sessionManager.getUserId(), productId);
                favoriteIds = db.favoriteDao().getFavoriteProductIds(sessionManager.getUserId());
            } else {
                isFavorite = false;
            }

            List<Product> relatedProducts = db.productDao().getRelatedProducts(
                    product.getCategoryId(), product.getId(), 4);

            List<Integer> finalFavoriteIds = favoriteIds;
            runOnUiThread(() -> {
                updateFavoriteIcon();
                tvName.setText(product.getName());
                tvDescription.setText(product.getDescription());
                if (tvBrand != null) tvBrand.setText(product.getBrand());
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));
                tvReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");
                
                if (tvStock != null) {
                    tvStock.setText("Kho: " + product.getStockQuantity());
                    tvStock.setTextColor(product.isSoldOut() ? 
                            getResources().getColor(R.color.red_price, getTheme()) : 
                            getResources().getColor(R.color.gray_text, getTheme()));
                }

                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(formatter.format(product.getPrice()) + "đ");

                Object imageSource = product.getImageUrl();
                if (product.getImageUrl() != null && product.getImageUrl().startsWith("res://drawable/")) {
                    String resName = product.getImageUrl().replace("res://drawable/", "");
                    int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                    if (resId != 0) imageSource = resId;
                }

                Glide.with(this).load(imageSource).into(ivProduct);

                // Size selector
                if (layoutSizes != null && product.getSizes() != null && !product.getSizes().isEmpty()) {
                    layoutSizes.removeAllViews();
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
                        sv.setBackgroundResource(R.drawable.bg_size_normal);
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

                // Related products
                if (!relatedProducts.isEmpty()) {
                    layoutRelated.setVisibility(View.VISIBLE);
                    rvRelated.setLayoutManager(new GridLayoutManager(this, 2));
                    relatedAdapter = new ProductAdapter(new ArrayList<>(relatedProducts), p -> {
                        Intent intent = new Intent(this, ProductDetailActivity.class);
                        intent.putExtra("productId", p.getId());
                        startActivity(intent);
                        finish();
                    });
                    relatedAdapter.setFavoriteProductIds(finalFavoriteIds);
                    rvRelated.setAdapter(relatedAdapter);
                }

                // Load review list ngay khi dữ liệu sản phẩm sẵn sàng
                loadReviews();
            });
        });

        rvReviews = findViewById(R.id.rvReviews); // Cần thêm vào XML
        rvReviews.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rvReviews.setNestedScrollingEnabled(false);
        rvReviews.setHasFixedSize(false);
        reviewAdapter = new ReviewAdapter(reviews);
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        if (product == null) return;
        AppDatabase.databaseExecutor.execute(() -> {
            List<Review> result = db.reviewDao().getReviewsByProduct(product.getId());
            runOnUiThread(() -> {
                reviews.clear();
                reviews.addAll(result);
                reviewAdapter.notifyDataSetChanged();
                rvReviews.requestLayout();

                // Đồng bộ lại count hiển thị theo dữ liệu thật
                TextView tvReviewCount = findViewById(R.id.tvReviewCount);
                if (tvReviewCount != null) {
                    tvReviewCount.setText("(" + reviews.size() + " đánh giá)");
                }
            });
        });
    }

    private void showReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rbReview);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);

        new AlertDialog.Builder(this)
                .setTitle("Đánh giá sản phẩm")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();
                    if (rating == 0) {
                        Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveReview(rating, comment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveReview(float rating, String comment) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        AppDatabase.databaseExecutor.execute(() -> {
            Review review = new Review(sessionManager.getUserId(), product.getId(), rating, comment, date, sessionManager.getUserFullName());
            db.reviewDao().insert(review);
            
            // Cập nhật rating trung bình cho sản phẩm
            float avg = db.reviewDao().getAverageRating(product.getId());
            int count = db.reviewDao().getReviewCount(product.getId());
            product.setRating(avg);
            product.setReviewCount(count);
            db.productDao().update(product);

            runOnUiThread(() -> {
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                loadReviews();
                // Update UI rating
                ((TextView)findViewById(R.id.tvRating)).setText(String.format(Locale.getDefault(), "%.1f", avg));
                ((TextView)findViewById(R.id.tvReviewCount)).setText("(" + count + " đánh giá)");
            });
        });
    }

    private void tvDetailDescriptionShow(TextView tvDesc, boolean show) {
        if (tvDesc != null) tvDesc.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void promptLogin(int action) {
        pendingLoginAction = action;
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để tiếp tục. Đăng nhập ngay?")
                .setPositiveButton("Đăng nhập", (d, w) -> loginLauncher.launch(new Intent(this, LoginActivity.class)))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateFavoriteIcon() {
        if (btnFavorite == null) return;
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void toggleFavorite() {
        if (!sessionManager.isLoggedIn()) {
            promptLogin(ACTION_TOGGLE_FAVORITE);
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
            runOnUiThread(() -> {
                updateFavoriteIcon();
                Toast.makeText(this, isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
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
                if (existing.getQuantity() + 1 > product.getStockQuantity()) {
                    runOnUiThread(() -> Toast.makeText(this, "Không thể thêm quá số lượng tồn kho!", Toast.LENGTH_SHORT).show());
                    return;
                }
                db.orderDetailDao().setQuantity(existing.getId(), existing.getQuantity() + 1);
            } else {
                db.orderDetailDao().insert(new OrderDetail(orderId, product.getId(), 1, product.getPrice()));
            }

            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order updated = db.orderDao().getOrderById(orderId);
            updated.setTotalAmount(total);
            db.orderDao().update(updated);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setTitle("Thêm thành công!")
                        .setMessage("Bạn muốn xem giỏ hàng ngay?")
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

    private boolean ensureSizeSelected() {
        if (selectedSize == null || selectedSize.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size trước khi tiếp tục", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (product == null || !sessionManager.isLoggedIn()) {
            return;
        }
        AppDatabase.databaseExecutor.execute(() -> {
            boolean favorite = db.favoriteDao().isFavorite(sessionManager.getUserId(), product.getId());
            runOnUiThread(() -> {
                isFavorite = favorite;
                updateFavoriteIcon();
            });
        });
    }
}
