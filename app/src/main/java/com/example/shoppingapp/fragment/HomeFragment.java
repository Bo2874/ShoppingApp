package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.MainActivity;
import com.example.shoppingapp.ProductDetailActivity;
import com.example.shoppingapp.ProductListActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.adapter.HomeCategoryAdapter;
import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private AppDatabase db;
    private ProductAdapter productAdapter;
    private HomeCategoryAdapter categoryAdapter;
    private final List<Product> products = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        // Categories horizontal list
        RecyclerView rvCategories = view.findViewById(R.id.rvHomeCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new HomeCategoryAdapter(categories, category -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);

        // Products grid
        RecyclerView rvProducts = view.findViewById(R.id.rvHomeProducts);
        rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProductAdapter(products, product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        rvProducts.setAdapter(productAdapter);

        // See all categories -> switch to category tab
        TextView tvSeeAllCategories = view.findViewById(R.id.tvSeeAllCategories);
        tvSeeAllCategories.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_category);
            }
        });

        // See all products
        TextView tvSeeAllProducts = view.findViewById(R.id.tvSeeAllProducts);
        tvSeeAllProducts.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProductListActivity.class));
        });

        // Search
        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadData();
                } else {
                    searchProducts(query);
                }
            }
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateCartBadge();
        }
    }

    private void loadData() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<Category> catResult = db.categoryDao().getAllCategories();
            List<Product> prodResult = db.productDao().getFeaturedProducts(8);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    categories.clear();
                    categories.addAll(catResult);
                    categoryAdapter.notifyDataSetChanged();

                    products.clear();
                    products.addAll(prodResult);
                    productAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void searchProducts(String query) {
        AppDatabase.databaseExecutor.execute(() -> {
            List<Product> result = db.productDao().searchProducts(query);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> productAdapter.updateList(result));
            }
        });
    }
}
