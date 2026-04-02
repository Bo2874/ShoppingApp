package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.CategoryListActivity;
import com.example.shoppingapp.MainActivity;
import com.example.shoppingapp.ProductDetailActivity;
import com.example.shoppingapp.ProductListActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.SearchHistoryManager;
import com.example.shoppingapp.SessionManager;
import com.example.shoppingapp.adapter.HomeCategoryAdapter;
import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Category;
import com.example.shoppingapp.database.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private AppDatabase db;
    private SessionManager sessionManager;
    private ProductAdapter productAdapter;
    private HomeCategoryAdapter categoryAdapter;
    private final List<Product> products = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private SearchHistoryManager searchHistoryManager;
    private LinearLayout layoutSearchHistory;
    private LinearLayout layoutSearchHistoryTags;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());
        searchHistoryManager = new SearchHistoryManager(requireContext());

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

        // See all buttons
        view.findViewById(R.id.tvSeeAllCategories).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryListActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.tvSeeAllProducts).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            startActivity(intent);
        });

        // Search history
        layoutSearchHistory = view.findViewById(R.id.layoutSearchHistory);
        layoutSearchHistoryTags = view.findViewById(R.id.layoutSearchHistoryTags);
        TextView tvClearHistory = view.findViewById(R.id.tvClearHistory);
        tvClearHistory.setOnClickListener(v -> {
            searchHistoryManager.clearHistory();
            layoutSearchHistory.setVisibility(View.GONE);
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
                    showSearchHistory();
                } else {
                    layoutSearchHistory.setVisibility(View.GONE);
                    searchProducts(query);
                }
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchHistoryManager.addQuery(query);
                }
                return true;
            }
            return false;
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etSearch.getText().toString().trim().isEmpty()) {
                showSearchHistory();
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
        loadFavorites(); // Tải lại danh sách yêu thích để cập nhật icon trái tim
    }

    private void loadFavorites() {
        if (!sessionManager.isLoggedIn()) {
            productAdapter.setFavoriteProductIds(new ArrayList<>());
            return;
        }
        AppDatabase.databaseExecutor.execute(() -> {
            List<Integer> favoriteIds = db.favoriteDao().getFavoriteProductIds(sessionManager.getUserId());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> productAdapter.setFavoriteProductIds(favoriteIds));
            }
        });
    }

    private void showSearchHistory() {
        List<String> history = searchHistoryManager.getHistory();
        if (history.isEmpty()) {
            layoutSearchHistory.setVisibility(View.GONE);
            return;
        }

        layoutSearchHistoryTags.removeAllViews();
        EditText etSearch = requireView().findViewById(R.id.etSearch);

        for (String query : history) {
            TextView tag = new TextView(requireContext());
            tag.setText(query);
            tag.setTextSize(13);
            tag.setBackgroundResource(R.drawable.bg_search_history_item);
            tag.setPadding(24, 12, 24, 12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 8);
            tag.setLayoutParams(params);
            tag.setOnClickListener(v -> {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
            });
            layoutSearchHistoryTags.addView(tag);
        }

        layoutSearchHistory.setVisibility(View.VISIBLE);
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
            List<Product> result = db.productDao().searchProductsAdvanced(query);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> productAdapter.updateList(result));
            }
        });
    }
}
