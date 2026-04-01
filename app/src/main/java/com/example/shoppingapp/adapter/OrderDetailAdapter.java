package com.example.shoppingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.R;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private final List<OrderDetail> orderDetails;
    private final Map<Integer, Product> productMap;

    public OrderDetailAdapter(List<OrderDetail> orderDetails, Map<Integer, Product> productMap) {
        this.orderDetails = orderDetails;
        this.productMap = productMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = orderDetails.get(position);
        Product product = productMap.get(detail.getProductId());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        String productName = product != null ? product.getName() : "Sản phẩm #" + detail.getProductId();
        holder.tvProductName.setText(productName);
        holder.tvQuantity.setText("SL: " + detail.getQuantity());
        holder.tvUnitPrice.setText(formatter.format(detail.getUnitPrice()) + "đ");
        double subtotal = detail.getQuantity() * detail.getUnitPrice();
        holder.tvSubtotal.setText(formatter.format(subtotal) + "đ");
    }

    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvUnitPrice, tvSubtotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvDetailProductName);
            tvQuantity = itemView.findViewById(R.id.tvDetailQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvDetailUnitPrice);
            tvSubtotal = itemView.findViewById(R.id.tvDetailSubtotal);
        }
    }
}
