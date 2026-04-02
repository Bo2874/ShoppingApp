package com.example.shoppingapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.shoppingapp.R;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<OrderDetail> items;
    private final Map<Integer, Product> productMap;
    private final CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(OrderDetail item, int newQuantity);
        void onItemDeleted(OrderDetail item);
    }

    public CartAdapter(List<OrderDetail> items, Map<Integer, Product> productMap, CartItemListener listener) {
        this.items = items;
        this.productMap = productMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (position < 0 || position >= items.size()) {
                return;
            }
            
            OrderDetail item = items.get(position);
            if (item == null) {
                return;
            }
            
            Product product = productMap.get(item.getProductId());
            Context context = holder.itemView.getContext();
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (product != null) {
                holder.tvName.setText(product.getName());
                holder.tvPrice.setText(formatter.format(product.getPrice()) + "đ/" + product.getUnit());
                
                // Logic load ảnh nội bộ cho Cart
                Object imageSource = product.getImageUrl();
                if (product.getImageUrl() != null && product.getImageUrl().startsWith("res://drawable/")) {
                    String resName = product.getImageUrl().replace("res://drawable/", "");
                    int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
                    if (resId != 0) imageSource = resId;
                }

                Glide.with(context)
                        .load(imageSource)
                        .transform(new CenterCrop(), new RoundedCorners(12))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(holder.ivProduct);
            }

            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
            double subtotal = item.getQuantity() * item.getUnitPrice();
            holder.tvSubtotal.setText(formatter.format(subtotal) + "đ");

            holder.btnMinus.setOnClickListener(v -> {
                try {
                    if (item != null && item.getQuantity() > 1 && listener != null) {
                        listener.onQuantityChanged(item, item.getQuantity() - 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.btnPlus.setOnClickListener(v -> {
                try {
                    if (item != null && listener != null) {
                        listener.onQuantityChanged(item, item.getQuantity() + 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                try {
                    if (item != null && listener != null) {
                        listener.onItemDeleted(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvSubtotal;
        TextView btnMinus, btnPlus;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivCartProduct);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnDelete = itemView.findViewById(R.id.btnCartDelete);
        }
    }
}
