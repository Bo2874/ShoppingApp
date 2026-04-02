package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.ProductListActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.adapter.CategoryAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private AppDatabase db;
    private CategoryAdapter adapter;
    private final List<Category> categories = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new CategoryAdapter(categories, category -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        EditText etSearch = view.findViewById(R.id.etCategorySearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterCategories(s.toString().trim());
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<Category> result = db.categoryDao().getAllCategories();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allCategories = new ArrayList<>(result);
                    categories.clear();
                    categories.addAll(result);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void filterCategories(String query) {
        categories.clear();
        if (query.isEmpty()) {
            categories.addAll(allCategories);
        } else {
            for (Category c : allCategories) {
                if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                    categories.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
