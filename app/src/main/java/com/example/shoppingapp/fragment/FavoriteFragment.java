package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.ProductDetailActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.SessionManager;
import com.example.shoppingapp.adapter.ProductAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteFragment extends Fragment {

    private AppDatabase db;
    private SessionManager sessionManager;
    private ProductAdapter adapter;
    private final List<Product> favorites = new ArrayList<>();
    private LinearLayout layoutFavoriteEmpty;
    private RecyclerView rvFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        layoutFavoriteEmpty = view.findViewById(R.id.layoutFavoriteEmpty);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new ProductAdapter(favorites, product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        rvFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (!sessionManager.isLoggedIn()) {
            showEmpty();
            return;
        }

        int userId = sessionManager.getUserId();
        AppDatabase.databaseExecutor.execute(() -> {
            List<Product> result = db.favoriteDao().getFavoriteProducts(userId);
            // Lấy danh sách ID để cập nhật icon trái tim đỏ
            List<Integer> favoriteIds = result.stream().map(Product::getId).collect(Collectors.toList());
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    favorites.clear();
                    favorites.addAll(result);
                    adapter.setFavoriteProductIds(favoriteIds);
                    adapter.notifyDataSetChanged();

                    if (favorites.isEmpty()) {
                        showEmpty();
                    } else {
                        layoutFavoriteEmpty.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void showEmpty() {
        layoutFavoriteEmpty.setVisibility(View.VISIBLE);
        rvFavorites.setVisibility(View.GONE);
    }
}
