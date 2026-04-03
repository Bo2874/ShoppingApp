package com.example.shoppingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_category) {
                fragment = new CategoryListFragment();
            } else if (itemId == R.id.nav_cart) {
                fragment = new CartFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("openCart", false)) {
                bottomNavigation.setSelectedItemId(R.id.nav_cart);
            } else {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("openCart", false)) {
            switchToTab(R.id.nav_cart);
            intent.removeExtra("openCart");
        }
    }

    public void switchToTab(int tabId) {
        bottomNavigation.setSelectedItemId(tabId);
    }

    public void updateCartBadge() {
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            bottomNavigation.removeBadge(R.id.nav_cart);
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseExecutor.execute(() -> {
            Order pendingOrder = db.orderDao().getPendingOrder(sessionManager.getUserId());
            int count = 0;
            if (pendingOrder != null) {
                count = db.orderDetailDao().getItemCount(pendingOrder.getId());
            }
            int finalCount = count;
            runOnUiThread(() -> {
                if (finalCount > 0) {
                    BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_cart);
                    badge.setNumber(finalCount);
                    badge.setVisible(true);
                } else {
                    bottomNavigation.removeBadge(R.id.nav_cart);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }
}
