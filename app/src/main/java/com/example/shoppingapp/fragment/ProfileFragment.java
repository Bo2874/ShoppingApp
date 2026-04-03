package com.example.shoppingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingapp.InvoiceActivity;
import com.example.shoppingapp.LoginActivity;
import com.example.shoppingapp.R;
import com.example.shoppingapp.SessionManager;
import com.example.shoppingapp.adapter.OrderHistoryAdapter;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private AppDatabase db;
    private LinearLayout layoutNotLoggedIn;
    private ScrollView layoutLoggedIn;
    private TextView tvProfileName, tvProfileUsername, tvProfilePhone, tvNoOrders;
    private OrderHistoryAdapter orderAdapter;
    private final List<Order> orders = new ArrayList<>();

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                updateUI();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        db = AppDatabase.getInstance(requireContext());

        layoutNotLoggedIn = view.findViewById(R.id.layoutNotLoggedIn);
        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        tvNoOrders = view.findViewById(R.id.tvNoOrders);

        view.findViewById(R.id.btnGoLogin).setOnClickListener(v ->
                loginLauncher.launch(new Intent(requireContext(), LoginActivity.class)));

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logout();
            updateUI();
        });

        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderHistoryAdapter(orders, order -> {
            Intent intent = new Intent(requireContext(), InvoiceActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });
        rvOrders.setAdapter(orderAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (sessionManager.isLoggedIn()) {
            layoutNotLoggedIn.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);
            tvProfileName.setText(sessionManager.getFullName());
            tvProfileUsername.setText("@" + sessionManager.getUsername());

            String phone = sessionManager.getPhone();
            if (phone != null && !phone.isEmpty()) {
                tvProfilePhone.setText(phone);
                tvProfilePhone.setVisibility(View.VISIBLE);
            }

            loadOrderHistory();
        } else {
            layoutNotLoggedIn.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }

    private void loadOrderHistory() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<Order> result = db.orderDao().getPaidOrders(sessionManager.getUserId());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    orders.clear();
                    orders.addAll(result);
                    orderAdapter.notifyDataSetChanged();
                    tvNoOrders.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
        });
    }
}
