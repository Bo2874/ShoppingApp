package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.InvoiceActivity;
import com.example.shoppingapp.MainActivity;
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
    private LinearLayout layoutEmpty, layoutCartContent;
    private RecyclerView rvCart;

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

        tvTotal = view.findViewById(R.id.tvCartTotal);
        layoutEmpty = view.findViewById(R.id.layoutCartEmpty);
        layoutCartContent = view.findViewById(R.id.layoutCartContent);
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

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < items.size()) {
                    deleteItem(items.get(position));
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (dX < 0) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#F44336"));
                    RectF bg = new RectF(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    c.drawRect(bg, paint);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(40);
                    textPaint.setAntiAlias(true);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    float textY = itemView.getTop() + (itemView.getHeight() + textPaint.getTextSize()) / 2f;
                    c.drawText("Xóa", itemView.getRight() + dX / 2, textY, textPaint);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        btnCheckout.setOnClickListener(v -> checkout());

        // Start shopping button
        TextView btnStartShopping = view.findViewById(R.id.btnStartShopping);
        if (btnStartShopping != null) {
            btnStartShopping.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToTab(R.id.nav_home);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (!isAdded() || sessionManager == null || db == null) return;

        if (!sessionManager.isLoggedIn()) {
            showEmpty();
            return;
        }

        AppDatabase.databaseExecutor.execute(() -> {
            if (!isAdded()) return;

            Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
            if (order == null) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(this::showEmpty);
                }
                return;
            }

            List<OrderDetail> detailList = db.orderDetailDao().getOrderDetailsByOrderId(order.getId());
            if (detailList.isEmpty()) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(this::showEmpty);
                }
                return;
            }

            Map<Integer, Product> newProductMap = new HashMap<>();
            for (OrderDetail d : detailList) {
                Product p = db.productDao().getProductById(d.getProductId());
                if (p != null) newProductMap.put(p.getId(), p);
            }

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    productMap.clear();
                    productMap.putAll(newProductMap);
                    items.clear();
                    items.addAll(detailList);
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    layoutEmpty.setVisibility(View.GONE);
                    layoutCartContent.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateQuantity(OrderDetail item, int newQuantity) {
        if (!isAdded()) return;

        AppDatabase.databaseExecutor.execute(() -> {
            db.orderDetailDao().setQuantity(item.getId(), newQuantity);
            loadCart();
            if (getActivity() instanceof MainActivity) {
                getActivity().runOnUiThread(() -> ((MainActivity) getActivity()).updateCartBadge());
            }
        });
    }

    private void deleteItem(OrderDetail item) {
        if (item == null) return;

        AppDatabase.databaseExecutor.execute(() -> {
            db.orderDetailDao().deleteById(item.getId());
            loadCart();
            if (getActivity() instanceof MainActivity) {
                getActivity().runOnUiThread(() -> ((MainActivity) getActivity()).updateCartBadge());
            }
        });
    }

    private void updateTotal() {
        if (!isAdded()) return;
        double total = 0;
        for (OrderDetail item : items) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotal.setText(formatter.format(total) + "đ");
    }

    private void checkout() {
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total for dialog
        double total = 0;
        for (OrderDetail item : items) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String totalStr = formatter.format(total) + "đ";

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Tổng cộng: " + totalStr + "\nBạn có chắc muốn thanh toán?")
                .setPositiveButton("Thanh toán", (dialog, which) -> processCheckout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processCheckout() {
        AppDatabase.databaseExecutor.execute(() -> {
            Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
            if (order == null) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show());
                }
                return;
            }

            List<OrderDetail> details = db.orderDetailDao().getOrderDetailsByOrderId(order.getId());
            if (details.isEmpty()) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show());
                }
                return;
            }

            // Update total and set status to Paid
            double total = db.orderDetailDao().getTotalByOrderId(order.getId());
            order.setTotalAmount(total);
            order.setStatus("Paid");
            db.orderDao().update(order);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), InvoiceActivity.class);
                    intent.putExtra("orderId", order.getId());
                    startActivity(intent);

                    // Clear cart UI
                    items.clear();
                    adapter.notifyDataSetChanged();
                    showEmpty();

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge();
                    }
                });
            }
        });
    }

    private void showEmpty() {
        if (!isAdded()) return;
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutCartContent.setVisibility(View.GONE);
        tvTotal.setText("0đ");
    }
}
