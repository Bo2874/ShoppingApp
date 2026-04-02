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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.CheckoutActivity;
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

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < cartItems.size()) {
                    OrderDetail item = cartItems.get(position);
                    deleteItem(item, position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (dX < 0) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#D32F2F"));
                    float cornerRadius = 12 * recyclerView.getContext().getResources().getDisplayMetrics().density;
                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                    // Draw "Xoa" text
                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(14 * recyclerView.getContext().getResources().getDisplayMetrics().scaledDensity);
                    textPaint.setAntiAlias(true);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    float textX = itemView.getRight() - 50 * recyclerView.getContext().getResources().getDisplayMetrics().density;
                    float textY = itemView.getTop() + (itemView.getHeight() + textPaint.getTextSize()) / 2f;
                    c.drawText("Xóa", textX, textY, textPaint);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rv);

        view.findViewById(R.id.btnCheckout).setOnClickListener(v -> checkout());

        view.findViewById(R.id.btnStartShopping).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_home);
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
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge();
                    }
                });
            }
        });
    }

    @Override
    public void onItemDeleted(OrderDetail item) {
        int position = cartItems.indexOf(item);
        if (position >= 0) {
            deleteItem(item, position);
        }
    }

    private void deleteItem(OrderDetail item, int position) {
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
                getActivity().runOnUiThread(() -> {
                    loadCart();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge();
                    }
                });
            }
        });
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

        // Navigate to CheckoutActivity with orderId
        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }
}
