package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.CheckoutActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.SessionManager;
import com.example.shoppingapp.adapter.CartAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.database.entity.OrderDetail;
import com.example.shoppingapp.database.entity.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {

    private AppDatabase db;
    private SessionManager sessionManager;
    private CartAdapter adapter;
    private final List<OrderDetail> items = new ArrayList<>();
    private final Map<Integer, Product> productMap = new HashMap<>();
    private TextView tvTotal;
    private LinearLayout layoutEmpty;
    private RecyclerView rvCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            db = AppDatabase.getInstance(requireContext());
            sessionManager = new SessionManager(requireContext());

            tvTotal = view.findViewById(R.id.tvCartTotal);
            layoutEmpty = view.findViewById(R.id.layoutCartEmpty);
            rvCart = view.findViewById(R.id.rvCartItems);
            TextView btnCheckout = view.findViewById(R.id.btnCheckout);

            rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new CartAdapter(items, productMap, new CartAdapter.CartItemListener() {
                @Override
                public void onQuantityChanged(OrderDetail item, int newQuantity) {
                    updateQuantity(item, newQuantity);
                }

                @Override
                public void onItemDeleted(OrderDetail item) {
                    deleteItem(item);
                }
            });
            rvCart.setAdapter(adapter);

            btnCheckout.setOnClickListener(v -> {
                if (items == null || items.isEmpty()) {
                    Toast.makeText(requireContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    AppDatabase.databaseExecutor.execute(() -> {
                        try {
                            Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
                            if (order != null && isAdded()) {
                                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                                intent.putExtra("orderId", order.getId());
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (!isAdded()) {
            return;
        }
        if (sessionManager == null || db == null) {
            return;
        }
         
         if (!sessionManager.isLoggedIn()) {
             showEmpty();
             return;
         }

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                if (!isAdded()) {
                    return;
                }
                
                Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
                if (order == null) {
                    if (isAdded()) {
                        getActivity().runOnUiThread(this::showEmpty);
                    }
                    return;
                }

                List<OrderDetail> detailList = db.orderDetailDao().getOrderDetailsByOrderId(order.getId());
                if (detailList.isEmpty()) {
                    if (isAdded()) {
                        getActivity().runOnUiThread(this::showEmpty);
                    }
                    return;
                }

                for (OrderDetail d : detailList) {
                    Product p = db.productDao().getProductById(d.getProductId());
                    if (p != null) productMap.put(p.getId(), p);
                }

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            items.clear();
                            items.addAll(detailList);
                            adapter.notifyDataSetChanged();
                            updateTotalUI();
                            layoutEmpty.setVisibility(View.GONE);
                            rvCart.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateQuantity(OrderDetail item, int newQuantity) {
        if (!isAdded()) {
            return;
        }
        
        try {
            Product p = productMap.get(item.getProductId());
            if (p != null && newQuantity > p.getStockQuantity()) {
                Toast.makeText(requireContext(), "Chỉ còn " + p.getStockQuantity() + " sản phẩm trong kho", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseExecutor.execute(() -> {
                try {
                    if (item != null) {
                        db.orderDetailDao().setQuantity(item.getId(), newQuantity);
                        loadCart();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(OrderDetail item) {
        if (item == null) return;
        
        AppDatabase.databaseExecutor.execute(() -> {
            try {
                db.orderDetailDao().deleteById(item.getId());
                loadCart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTotalUI() {
        try {
            if (!isAdded()) {
                return;
            }
            
            double total = 0;
            if (items != null) {
                for (OrderDetail item : items) {
                    if (item != null) {
                        total += item.getQuantity() * item.getUnitPrice();
                    }
                }
            }
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            if (tvTotal != null) {
                tvTotal.setText(formatter.format(total) + "đ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEmpty() {
        try {
            if (!isAdded()) {
                return;
            }
            
            if (layoutEmpty != null && rvCart != null && tvTotal != null) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvCart.setVisibility(View.GONE);
                tvTotal.setText("0đ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
