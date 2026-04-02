package com.example.shoppingapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.Order;
import com.example.shoppingapp.fragment.CartFragment;
import com.example.shoppingapp.fragment.CategoryListFragment;
import com.example.shoppingapp.fragment.HomeFragment;
import com.example.shoppingapp.fragment.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_category) {
                fragment = new CategoryListFragment();
            } else if (itemId == R.id.nav_cart) {
                fragment = new CartFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                return false;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("openCart", false)) {
                bottomNav.setSelectedItemId(R.id.nav_cart);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("openCart", false)) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    public void switchToTab(int tabId) {
        bottomNav.setSelectedItemId(tabId);
    }

    public void updateCartBadge() {
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            bottomNav.removeBadge(R.id.nav_cart);
            return;
        }

        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Order order = db.orderDao().getPendingOrder(sessionManager.getUserId());
            int count = 0;
            if (order != null) {
                count = db.orderDetailDao().getItemCount(order.getId());
            }
            int finalCount = count;
            runOnUiThread(() -> {
                if (finalCount > 0) {
                    BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
                    badge.setNumber(finalCount);
                    badge.setBackgroundColor(getResources().getColor(R.color.red_price, getTheme()));
                } else {
                    bottomNav.removeBadge(R.id.nav_cart);
                }
            });
        });
    }
}
