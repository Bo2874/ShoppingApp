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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.InvoiceActivity;
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

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private AppDatabase db;
    private SessionManager sessionManager;
    private CartAdapter adapter;
    private final List<OrderDetail> cartItems = new ArrayList<>();
    private final Map<Integer, Product> productMap = new HashMap<>();
    private TextView tvCartTotal;
    private LinearLayout layoutCartEmpty, layoutCartContent;
    private int orderId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvCartTotal = view.findViewById(R.id.tvCartTotal);
        layoutCartEmpty = view.findViewById(R.id.layoutCartEmpty);
        layoutCartContent = view.findViewById(R.id.layoutCartContent);

        RecyclerView rv = view.findViewById(R.id.rvCartItems);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartItems, productMap, this);
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnCheckout).setOnClickListener(v -> checkout());

        view.findViewById(R.id.btnStartShopping).setOnClickListener(v -> {
            if (getActivity() instanceof com.example.shoppingapp.MainActivity) {
                ((com.example.shoppingapp.MainActivity) getActivity()).switchToTab(R.id.nav_home);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }

        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
            if (order == null) {
                if (getActivity() != null) getActivity().runOnUiThread(this::showEmptyState);
                return;
            }

            orderId = order.getId();
            List<OrderDetail> details = db.orderDetailDao().getOrderDetailsByOrderId(orderId);
            Map<Integer, Product> pMap = new HashMap<>();
            for (OrderDetail d : details) {
                Product p = db.productDao().getProductById(d.getProductId());
                if (p != null) pMap.put(p.getId(), p);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    cartItems.clear();
                    cartItems.addAll(details);
                    productMap.clear();
                    productMap.putAll(pMap);
                    adapter.notifyDataSetChanged();

                    if (cartItems.isEmpty()) {
                        showEmptyState();
                    } else {
                        showCartContent();
                        updateTotal(order.getTotalAmount());
                    }
                });
            }
        });
    }

    private void showEmptyState() {
        layoutCartEmpty.setVisibility(View.VISIBLE);
        layoutCartContent.setVisibility(View.GONE);
    }

    private void showCartContent() {
        layoutCartEmpty.setVisibility(View.GONE);
        layoutCartContent.setVisibility(View.VISIBLE);
    }

    private void updateTotal(double total) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvCartTotal.setText(formatter.format(total) + "đ");
    }

    @Override
    public void onQuantityChanged(OrderDetail item, int newQuantity) {
        if (newQuantity < 1) return;
        AppDatabase.databaseExecutor.execute(() -> {
            db.orderDetailDao().setQuantity(item.getId(), newQuantity);
            double total = db.orderDetailDao().getTotalByOrderId(orderId);
            Order order = db.orderDao().getOrderById(orderId);
            order.setTotalAmount(total);
            db.orderDao().update(order);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    item.setQuantity(newQuantity);
                    adapter.notifyDataSetChanged();
                    updateTotal(total);
                });
            }
        });
    }

    @Override
    public void onItemDeleted(OrderDetail item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseExecutor.execute(() -> {
                        db.orderDetailDao().deleteById(item.getId());
                        int remainingCount = db.orderDetailDao().getItemCount(orderId);
                        if (remainingCount == 0) {
                            db.orderDao().deleteById(orderId);
                            orderId = -1;
                        } else {
                            double total = db.orderDetailDao().getTotalByOrderId(orderId);
                            Order order = db.orderDao().getOrderById(orderId);
                            order.setTotalAmount(total);
                            db.orderDao().update(order);
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(this::loadCart);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void checkout() {
        if (orderId == -1 || cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        double currentTotal = 0;
        for (OrderDetail item : cartItems) {
            currentTotal += item.getQuantity() * item.getUnitPrice();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Tổng tiền: " + formatter.format(currentTotal) + "đ\n\nBạn có chắc muốn thanh toán đơn hàng này?")
                .setPositiveButton("Thanh toán", (dialog, which) -> processCheckout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processCheckout() {
        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order == null) return;
            order.setStatus("Paid");
            db.orderDao().update(order);

            int completedOrderId = orderId;
            orderId = -1;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), InvoiceActivity.class);
                    intent.putExtra("orderId", completedOrderId);
                    startActivity(intent);
                    loadCart();
                });
            }
        });
    }
}
