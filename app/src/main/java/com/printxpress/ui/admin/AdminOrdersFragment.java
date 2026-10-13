package com.printxpress.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.printxpress.R;
import com.printxpress.data.entity.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersFragment extends Fragment {

    private AdminViewModel viewModel;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmptyOrders, layoutBulkBar;
    private TextView tvBulkSelectedCount;
    private EditText etOrderSearch;
    private ChipGroup chipGroupStatus;

    private List<Order> allOrdersList = new ArrayList<>();
    private final List<Long> selectedOrderIds = new ArrayList<>();
    private String currentStatusFilter = "ALL";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        rvOrders = view.findViewById(R.id.rvAdminOrders);
        layoutEmptyOrders = view.findViewById(R.id.layoutEmptyOrders);
        layoutBulkBar = view.findViewById(R.id.layoutBulkBar);
        tvBulkSelectedCount = view.findViewById(R.id.tvBulkSelectedCount);
        etOrderSearch = view.findViewById(R.id.etOrderSearch);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        MaterialButton btnBulkUpdate = view.findViewById(R.id.btnBulkUpdateStatus);
        btnBulkUpdate.setOnClickListener(v -> showBulkUpdateDialog());

        etOrderSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                filterAndDisplayOrders();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPending) currentStatusFilter = "PENDING";
            else if (checkedId == R.id.chipProcessing) currentStatusFilter = "PROCESSING";
            else if (checkedId == R.id.chipPrinting) currentStatusFilter = "PRINTING";
            else if (checkedId == R.id.chipReady) currentStatusFilter = "READY";
            else if (checkedId == R.id.chipCompleted) currentStatusFilter = "DELIVERED";
            else if (checkedId == R.id.chipCancelled) currentStatusFilter = "CANCELLED";
            else currentStatusFilter = "ALL";

            filterAndDisplayOrders();
        });

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                allOrdersList = orders;
                filterAndDisplayOrders();
            }
        });
    }

    private void filterAndDisplayOrders() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrdersList) {
            boolean matchesStatus = "ALL".equals(currentStatusFilter) || currentStatusFilter.equalsIgnoreCase(o.status);
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    String.valueOf(o.id).contains(currentSearchQuery) ||
                    (o.trackingNumber != null && o.trackingNumber.toLowerCase(Locale.ROOT).contains(currentSearchQuery));

            if (matchesStatus && matchesSearch) {
                filtered.add(o);
            }
        }

        if (filtered.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmptyOrders.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmptyOrders.setVisibility(View.GONE);
            rvOrders.setAdapter(new OrdersAdapter(filtered));
        }
    }

    private void showBulkUpdateDialog() {
        if (selectedOrderIds.isEmpty()) return;
        String[] statuses = {"PENDING", "PROCESSING", "PRINTING", "READY", "DELIVERED", "CANCELLED"};
        new AlertDialog.Builder(requireContext())
            .setTitle("Bulk Update " + selectedOrderIds.size() + " Orders")
            .setItems(statuses, (dialog, which) -> {
                String newStatus = statuses[which];
                viewModel.updateBulkOrderStatus(selectedOrderIds, newStatus);
                selectedOrderIds.clear();
                updateBulkBarVisibility();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateBulkBarVisibility() {
        if (!selectedOrderIds.isEmpty()) {
            layoutBulkBar.setVisibility(View.VISIBLE);
            tvBulkSelectedCount.setText(selectedOrderIds.size() + " orders selected");
        } else {
            layoutBulkBar.setVisibility(View.GONE);
        }
    }

    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private final List<Order> items;

        public OrdersAdapter(List<Order> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = items.get(position);
            holder.tvOrderId.setText("Order #" + order.id);
            holder.tvTracking.setText((order.trackingNumber != null ? order.trackingNumber : "PX-000") + " • " + order.deliveryType);
            holder.tvStatusChip.setText(order.status);
            holder.tvCustomerName.setText("Customer #" + order.userId);
            holder.tvOrderDetails.setText(order.notes != null && !order.notes.isEmpty() ? order.notes : "Standard Print Order");
            holder.tvTotal.setText(String.format(Locale.getDefault(), "Rs. %,.2f", order.totalPrice));

            int chipColor = 0xFF2563EB;
            if ("PENDING".equalsIgnoreCase(order.status)) chipColor = 0xFFD97706;
            else if ("PROCESSING".equalsIgnoreCase(order.status)) chipColor = 0xFFD97706;
            else if ("PRINTING".equalsIgnoreCase(order.status)) chipColor = 0xFF7C3AED;
            else if ("READY".equalsIgnoreCase(order.status)) chipColor = 0xFF0284C7;
            else if ("DELIVERED".equalsIgnoreCase(order.status) || "COMPLETED".equalsIgnoreCase(order.status)) chipColor = 0xFF16A34A;
            else if ("CANCELLED".equalsIgnoreCase(order.status)) chipColor = 0xFFDC2626;

            holder.tvStatusChip.setBackgroundColor(chipColor);

            holder.cbSelect.setChecked(selectedOrderIds.contains(order.id));
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedOrderIds.contains(order.id)) selectedOrderIds.add(order.id);
                } else {
                    selectedOrderIds.remove(order.id);
                }
                updateBulkBarVisibility();
            });

            holder.itemView.setOnClickListener(v -> {
                if (getActivity() instanceof AdminDashboardActivity) {
                    ((AdminDashboardActivity) getActivity()).navigateToOrderDetail(order.id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvTracking, tvStatusChip, tvCustomerName, tvOrderDetails, tvTotal;
            CheckBox cbSelect;

            ViewHolder(View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvAdminOrderId);
                tvTracking = itemView.findViewById(R.id.tvAdminOrderTracking);
                tvStatusChip = itemView.findViewById(R.id.tvAdminOrderStatusChip);
                tvCustomerName = itemView.findViewById(R.id.tvAdminCustomerName);
                tvOrderDetails = itemView.findViewById(R.id.tvAdminOrderDetails);
                tvTotal = itemView.findViewById(R.id.tvAdminOrderTotal);
                cbSelect = itemView.findViewById(R.id.cbSelectOrder);
            }
        }
    }
}
