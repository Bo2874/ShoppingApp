package com.example.shoppingapp.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.shoppingapp.R;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_LIST = 1;

    private final List<Product> products;
    private final OnProductClickListener listener;
    private int viewType = VIEW_TYPE_GRID;
    private Set<Integer> favoriteProductIds = new HashSet<>();

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
        notifyDataSetChanged();
    }

    public void setFavoriteProductIds(List<Integer> ids) {
        this.favoriteProductIds = new HashSet<>(ids);
        notifyDataSetChanged();
    }

    public int getViewType() {
        return viewType;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_LIST ? R.layout.item_product_list : R.layout.item_product_grid;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());

        // Brand
        if (holder.tvBrand != null) {
            holder.tvBrand.setText(product.getBrand());
        }

        // Price formatted Vietnamese locale
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(product.getPrice()) + "đ");

        // Sale badge and original price
        if (product.isOnSale()) {
            if (holder.tvOriginalPrice != null) {
                holder.tvOriginalPrice.setVisibility(View.VISIBLE);
                holder.tvOriginalPrice.setText(formatter.format(product.getOriginalPrice()) + "đ");
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            if (holder.tvSaleBadge != null) {
                int percent = (int) ((1 - product.getPrice() / product.getOriginalPrice()) * 100);
                holder.tvSaleBadge.setText("-" + percent + "%");
                holder.tvSaleBadge.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.tvOriginalPrice != null) holder.tvOriginalPrice.setVisibility(View.GONE);
            if (holder.tvSaleBadge != null) holder.tvSaleBadge.setVisibility(View.GONE);
        }

        // Rating
        if (holder.tvRating != null) {
            holder.tvRating.setText(String.valueOf(product.getRating()));
        }

        // Description (list view only)
        if (holder.tvDescription != null) {
            holder.tvDescription.setText(product.getDescription());
        }

        // Image
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .transform(new CenterCrop(), new RoundedCorners(16))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.ivProduct);

        // Favorite Heart Icon
        if (holder.ivHeart != null) {
            if (favoriteProductIds.contains(product.getId())) {
                holder.ivHeart.setImageResource(R.drawable.ic_heart_filled);
            } else {
                holder.ivHeart.setImageResource(R.drawable.ic_heart_outline);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        ImageView ivHeart;
        TextView tvName, tvPrice, tvOriginalPrice, tvSaleBadge, tvDescription, tvBrand, tvRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvSaleBadge = itemView.findViewById(R.id.tvSaleBadge);
            tvDescription = itemView.findViewById(R.id.tvProductDescription);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivHeart = itemView.findViewById(R.id.ivHeart);
        }
    }
}
