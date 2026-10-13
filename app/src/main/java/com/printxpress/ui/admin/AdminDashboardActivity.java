package com.printxpress.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.printxpress.MainActivity;
import com.printxpress.R;
import com.printxpress.util.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AdminViewModel viewModel;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private long lastInteractionTime;
    private static final long INACTIVITY_TIMEOUT_MS = 15L * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdminOrStaff()) {
            Toast.makeText(this, "Unauthorized access: Session expired or invalid role", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        lastInteractionTime = System.currentTimeMillis();

        MaterialToolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        bottomNav = findViewById(R.id.adminBottomNav);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_home, R.string.nav_logout
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        updateNavHeader();

        enforceRolePermissions();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin_home) {
                replaceFragment(new AdminHomeFragment());
                return true;
            } else if (itemId == R.id.nav_admin_orders) {
                replaceFragment(new AdminOrdersFragment());
                return true;
            } else if (itemId == R.id.nav_admin_customers) {
                replaceFragment(new AdminCustomersFragment());
                return true;
            } else if (itemId == R.id.nav_admin_reports) {
                replaceFragment(new AdminReportsFragment());
                return true;
            }
            return false;
        });

        viewModel.getActionMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState == null) {
            replaceFragment(new AdminHomeFragment());
        }
    }

    private void enforceRolePermissions() {
        String role = sessionManager.getRole();
        if ("STAFF".equalsIgnoreCase(role)) {
            MenuItem activityLogItem = navigationView.getMenu().findItem(R.id.nav_admin_activity_log);
            if (activityLogItem != null) activityLogItem.setVisible(false);

            MenuItem catalogItem = navigationView.getMenu().findItem(R.id.nav_admin_catalog);
            if (catalogItem != null) catalogItem.setVisible(false);

            MenuItem reportsItem = navigationView.getMenu().findItem(R.id.nav_admin_reports);
            if (reportsItem != null) reportsItem.setVisible(false);
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvName = headerView.findViewById(R.id.tvHeaderAdminName);
            TextView tvEmail = headerView.findViewById(R.id.tvHeaderAdminEmail);
            TextView tvBadge = headerView.findViewById(R.id.tvAdminRoleBadge);

            if (tvName != null) tvName.setText(sessionManager.getUserName().isEmpty() ? "Admin Account" : sessionManager.getUserName());
            if (tvEmail != null) tvEmail.setText(sessionManager.getUserEmail());
            if (tvBadge != null) tvBadge.setText(sessionManager.getRole());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_home) {
            replaceFragment(new AdminHomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_home);
        } else if (id == R.id.nav_admin_orders) {
            replaceFragment(new AdminOrdersFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_orders);
        } else if (id == R.id.nav_admin_customers) {
            replaceFragment(new AdminCustomersFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_customers);
        } else if (id == R.id.nav_admin_catalog) {
            replaceFragment(new AdminCatalogFragment());
        } else if (id == R.id.nav_admin_marketing) {
            replaceFragment(new AdminMarketingFragment());
        } else if (id == R.id.nav_admin_reports) {
            replaceFragment(new AdminReportsFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_reports);
        } else if (id == R.id.nav_admin_activity_log) {
            replaceFragment(new AdminActivityLogFragment());
        } else if (id == R.id.nav_admin_logout) {
            viewModel.logout();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.adminFragmentContainer, fragment)
                .commit();
    }

    public void navigateToOrderDetail(long orderId) {
        replaceFragment(AdminOrderDetailFragment.newInstance(orderId));
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        long now = System.currentTimeMillis();
        if ((now - lastInteractionTime) > INACTIVITY_TIMEOUT_MS) {
            Toast.makeText(this, "Session expired due to 15 minutes of inactivity", Toast.LENGTH_LONG).show();
            viewModel.logout();
            redirectToLogin();
        } else {
            lastInteractionTime = now;
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
