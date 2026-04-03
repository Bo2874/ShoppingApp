package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private ProductAdapter adapter;
    private final List<Product> products = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private AppDatabase db;
    private int categoryId = -1;
    private TextView tvEmptyResult;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        db = AppDatabase.getInstance(this);

        categoryId = getIntent().getIntExtra("categoryId", -1);
        String categoryName = getIntent().getStringExtra("categoryName");

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        tvEmptyResult = findViewById(R.id.tvEmptyResult);

        if (categoryName != null) {
            tvTitle.setText(categoryName);
        } else {
            tvTitle.setText("Tất cả sản phẩm");
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvProducts);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(products, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        // Pull to refresh
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.green_primary);
        swipeRefresh.setOnRefreshListener(this::loadProducts);

        // Search
        EditText etSearch = findViewById(R.id.etSearchProduct);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString().trim());
            }
        });

        loadProducts();
    }

    private void loadProducts() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<Product> result;
            if (categoryId > 0) {
                result = db.productDao().getProductsByCategory(categoryId);
            } else {
                result = db.productDao().getAllProducts();
            }
            runOnUiThread(() -> {
                allProducts = new ArrayList<>(result);
                products.clear();
                products.addAll(result);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                swipeRefresh.setRefreshing(false);
            });
        });
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            products.clear();
            products.addAll(allProducts);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        } else {
            // Local filter first
            String lowerQuery = query.toLowerCase();
            List<Product> filtered = new ArrayList<>();
            for (Product p : allProducts) {
                if (p.getName().toLowerCase().contains(lowerQuery)
                        || p.getDescription().toLowerCase().contains(lowerQuery)) {
                    filtered.add(p);
                }
            }

            if (filtered.isEmpty()) {
                // Fallback to DB search
                AppDatabase.databaseExecutor.execute(() -> {
                    List<Product> result = db.productDao().searchProducts(query);
                    runOnUiThread(() -> {
                        products.clear();
                        products.addAll(result);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    });
                });
            } else {
                products.clear();
                products.addAll(filtered);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }
        }
    }

    private void updateEmptyState() {
        tvEmptyResult.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
