package com.example.shoppingapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppingapp.R;
import com.example.shoppingapp.database.entity.Category;

import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public HomeCategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        Context context = holder.itemView.getContext();
        holder.tvName.setText(category.getName());

        // Logic load ảnh từ resource string
        Object imageSource = category.getImageUrl();
        if (category.getImageUrl() != null && category.getImageUrl().startsWith("res://drawable/")) {
            String resName = category.getImageUrl().replace("res://drawable/", "");
            int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
            if (resId != 0) imageSource = resId;
        }

        Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.ic_category)
                .error(R.drawable.ic_category)
                .into(holder.ivIcon);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_category_chip_selected);
            holder.tvName.setTextColor(context.getResources().getColor(R.color.white, null));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_category_chip);
            holder.tvName.setTextColor(context.getResources().getColor(R.color.text_primary, null));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPosition;
                notifyDataSetChanged();
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
