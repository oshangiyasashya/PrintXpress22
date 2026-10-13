package com.printxpress.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.printxpress.R;
import com.printxpress.data.entity.Order;
import com.printxpress.util.GenericAdapter;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {
    private OrdersViewModel viewModel;
    private GenericAdapter<Order> adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutOrders);
        RecyclerView rv = view.findViewById(R.id.rvOrders);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GenericAdapter<>(R.layout.item_order_card_full,
            (v, order) -> {
                TextView tvOrderNumber = v.findViewById(R.id.tvOrderNumber);
                TextView tvOrderBadgeStatus = v.findViewById(R.id.tvOrderBadgeStatus);
                TextView tvOrderTrackingCode = v.findViewById(R.id.tvOrderTrackingCode);
                TextView tvOrderTotalAmount = v.findViewById(R.id.tvOrderTotalAmount);
                TextView tvOrderTimestamp = v.findViewById(R.id.tvOrderTimestamp);
                TextView tvOrderDeliverySummary = v.findViewById(R.id.tvOrderDeliverySummary);

                if (tvOrderNumber != null) tvOrderNumber.setText("Order #" + order.id);
                if (tvOrderBadgeStatus != null) {
                    tvOrderBadgeStatus.setText(order.status);
                    if ("PENDING".equalsIgnoreCase(order.status)) {
                        tvOrderBadgeStatus.setBackgroundColor(android.graphics.Color.parseColor("#F59E0B"));
                    } else if ("PROCESSING".equalsIgnoreCase(order.status) || "PRINTING".equalsIgnoreCase(order.status)) {
                        tvOrderBadgeStatus.setBackgroundColor(android.graphics.Color.parseColor("#2563EB"));
                    } else if ("DELIVERED".equalsIgnoreCase(order.status) || "READY".equalsIgnoreCase(order.status) || "COMPLETED".equalsIgnoreCase(order.status)) {
                        tvOrderBadgeStatus.setBackgroundColor(android.graphics.Color.parseColor("#10B981"));
                    } else if ("CANCELLED".equalsIgnoreCase(order.status)) {
                        tvOrderBadgeStatus.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"));
                    }
                }
                if (tvOrderTrackingCode != null) {
                    tvOrderTrackingCode.setText("Tracking: " + (order.trackingNumber != null ? order.trackingNumber : "PX-" + order.id));
                }
                if (tvOrderTotalAmount != null) {
                    tvOrderTotalAmount.setText(String.format(Locale.getDefault(), "Rs. %,.2f", order.totalPrice));
                }
                if (tvOrderTimestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                    tvOrderTimestamp.setText(sdf.format(new Date(order.createdAt)));
                }
                if (tvOrderDeliverySummary != null) {
                    tvOrderDeliverySummary.setText("Type: " + (order.deliveryType != null ? order.deliveryType : "Standard Delivery"));
                }
            },
            order -> {
                Bundle bundle = new Bundle();
                bundle.putLong("orderId", order.id);
                Navigation.findNavController(view).navigate(R.id.orderDetailsFragment, bundle);
            }
        );
        rv.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Processing"));
        tabLayout.addTab(tabLayout.newTab().setText("Delivered"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentFilter = "ALL"; break;
                    case 1: currentFilter = "PROCESSING"; break;
                    case 2: currentFilter = "DELIVERED"; break;
                    case 3: currentFilter = "CANCELLED"; break;
                }
                filterOrders();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                allOrders = orders;
                filterOrders();
            }
        });

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders() {
        if (currentFilter.equals("ALL")) {
            adapter.setItems(allOrders);
            return;
        }
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if (currentFilter.equals("PROCESSING") && (o.status.equals("PENDING") || o.status.equals("PROCESSING") || o.status.equals("PRINTING"))) {
                filtered.add(o);
            } else if (currentFilter.equals("DELIVERED") && (o.status.equals("COMPLETED") || o.status.equals("DELIVERED"))) {
                filtered.add(o);
            } else if (currentFilter.equals("CANCELLED") && o.status.equals("CANCELLED")) {
                filtered.add(o);
            }
        }
        adapter.setItems(filtered);
    }
}
