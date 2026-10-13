package com.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.printxpress.data.entity.Order;
import com.printxpress.ui.admin.AdminDashboardActivity;
import com.printxpress.ui.orders.OrdersViewModel;
import com.printxpress.util.SessionManager;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;
    private NavigationView navigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNav = findViewById(R.id.bottom_nav);
        navigationView = findViewById(R.id.navigation_view);

        setSupportActionBar(topAppBar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        SessionManager session = new SessionManager(this);
        androidx.navigation.NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        if (session.isLoggedIn()) {
            navGraph.setStartDestination(R.id.catalogFragment);
        } else {
            navGraph.setStartDestination(R.id.loginFragment);
        }
        navController.setGraph(navGraph);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.catalogFragment) {
                navController.popBackStack(R.id.catalogFragment, false);
                return true;
            } else if (id == R.id.productsFragment) {
                navController.navigate(R.id.productsFragment);
                return true;
            } else if (id == R.id.ordersFragment) {
                navController.navigate(R.id.ordersFragment);
                return true;
            } else if (id == R.id.profileFragment) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, topAppBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                new SessionManager(this).logout();
                androidx.navigation.NavGraph logoutGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
                logoutGraph.setStartDestination(R.id.loginFragment);
                navController.setGraph(logoutGraph);
                return true;
            } else if (id == R.id.infoSupport) {
                navController.navigate(R.id.infoFragment);
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_cart) {
                navController.navigate(R.id.cartFragment);
                return true;
            } else if (itemId == R.id.action_notifications) {
                navController.navigate(R.id.notificationsFragment);
                return true;
            }
            return false;
        });

        if (session.isLoggedIn()) {
            String role = session.getRole();
            if ("SUPER_ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role)) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            OrdersViewModel ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
            ordersViewModel.getOrders().observe(this, orders -> {
                if (orders != null && bottomNav != null) {
                    int activeCount = 0;
                    for (Order o : orders) {
                        if ("PENDING".equalsIgnoreCase(o.status) || "PROCESSING".equalsIgnoreCase(o.status) || "PRINTING".equalsIgnoreCase(o.status)) {
                            activeCount++;
                        }
                    }
                    BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.ordersFragment);
                    if (activeCount > 0) {
                        badge.setVisible(true);
                        badge.setNumber(activeCount);
                    } else {
                        badge.setVisible(false);
                    }
                }
            });
        }

        handleIncomingIntent(getIntent());

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            if (destId == R.id.loginFragment || destId == R.id.registerFragment) {
                topAppBar.setVisibility(View.GONE);
                bottomNav.setVisibility(View.GONE);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                topAppBar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.VISIBLE);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String navigateTo = intent.getStringExtra("navigateTo");
        if ("checkout".equals(navigateTo)) {
            Bundle bundle = new Bundle();
            bundle.putLong("productId", intent.getLongExtra("productId", 1L));
            bundle.putDouble("basePrice", intent.getDoubleExtra("basePrice", 2500.0));
            bundle.putInt("quantity", intent.getIntExtra("quantity", 1));
            bundle.putString("designData", intent.getStringExtra("designData"));
            navController.navigate(R.id.checkoutFragment, bundle);
        } else if ("order".equals(navigateTo)) {
            long orderId = intent.getLongExtra("orderId", -1L);
            if (orderId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong("orderId", orderId);
                navController.navigate(R.id.orderDetailsFragment, bundle);
            } else {
                navController.navigate(R.id.ordersFragment);
            }
        } else if ("notifications".equals(navigateTo)) {
            navController.navigate(R.id.notificationsFragment);
        } else if ("catalog".equals(navigateTo)) {
            navController.navigate(R.id.catalogFragment);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp();
    }
}
