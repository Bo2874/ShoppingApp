package com.example.shoppingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    private final List<Category> categories = new ArrayList<>();
    private RecyclerView.Adapter<?> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvCategories);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new RecyclerView.Adapter<CatViewHolder>() {
            @NonNull
            @Override
            public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_category_grid, parent, false);
                return new CatViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
                Category cat = categories.get(position);
                Context context = holder.itemView.getContext();
                holder.tvName.setText(cat.getName());
                holder.tvDesc.setText(cat.getDescription());

                // Xử lý load ảnh từ resource nội bộ res://drawable/
                Object imageSource = cat.getImageUrl();
                if (cat.getImageUrl() != null && cat.getImageUrl().startsWith("res://drawable/")) {
                    String resName = cat.getImageUrl().replace("res://drawable/", "");
                    int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
                    if (resId != 0) imageSource = resId;
                }

                Glide.with(context)
                        .load(imageSource)
                        .placeholder(R.drawable.ic_category)
                        .error(R.drawable.ic_category)
                        .into(holder.ivImage);

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(CategoryListActivity.this, ProductListActivity.class);
                    intent.putExtra("categoryId", cat.getId());
                    intent.putExtra("categoryName", cat.getName());
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return categories.size();
            }
        };
        rv.setAdapter(adapter);

        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseExecutor.execute(() -> {
            List<Category> result = db.categoryDao().getAllCategories();
            runOnUiThread(() -> {
                categories.clear();
                categories.addAll(result);
                adapter.notifyDataSetChanged();
            });
        });
    }

    static class CatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDesc;

        CatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCategoryImage);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDesc = itemView.findViewById(R.id.tvCategoryDesc);
        }
    }
}
