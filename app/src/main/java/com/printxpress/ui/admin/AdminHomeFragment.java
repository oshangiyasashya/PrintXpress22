package com.printxpress.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.printxpress.R;
import com.printxpress.data.entity.Order;
import com.printxpress.ui.admin.views.AdminBarChartView;
import com.printxpress.ui.admin.views.AdminDonutChartView;
import com.printxpress.ui.admin.views.AdminLineChartView;
import com.printxpress.util.GenericAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminHomeFragment extends Fragment {

    private AdminViewModel viewModel;
    private TextView tvCardTotalOrders, tvCardPendingOrders, tvCardPrintingOrders;
    private TextView tvCardCompletedToday, tvCardRevenueToday, tvCardTotalCustomers;
    private AdminBarChartView chartOrdersPerDay;
    private AdminLineChartView chartRevenueTrend;
    private AdminDonutChartView chartCategoryDonut, chartStatusDonut;
    private RecyclerView rvNeedsAttention;
    private TextView tvEmptyNeedsAttention;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        tvCardTotalOrders = view.findViewById(R.id.tvCardTotalOrders);
        tvCardPendingOrders = view.findViewById(R.id.tvCardPendingOrders);
        tvCardPrintingOrders = view.findViewById(R.id.tvCardPrintingOrders);
        tvCardCompletedToday = view.findViewById(R.id.tvCardCompletedToday);
        tvCardRevenueToday = view.findViewById(R.id.tvCardRevenueToday);
        tvCardTotalCustomers = view.findViewById(R.id.tvCardTotalCustomers);

        chartOrdersPerDay = view.findViewById(R.id.chartOrdersPerDay);
        chartRevenueTrend = view.findViewById(R.id.chartRevenueTrend);
        chartCategoryDonut = view.findViewById(R.id.chartCategoryDonut);
        chartStatusDonut = view.findViewById(R.id.chartStatusDonut);

        rvNeedsAttention = view.findViewById(R.id.rvNeedsAttention);
        tvEmptyNeedsAttention = view.findViewById(R.id.tvEmptyNeedsAttention);
        rvNeedsAttention.setLayoutManager(new LinearLayoutManager(getContext()));

        MaterialButton btnRefresh = view.findViewById(R.id.btnRefreshDashboard);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> observeData());
        }

        MaterialButtonToggleGroup toggleChartRange = view.findViewById(R.id.toggleChartRange);
        if (toggleChartRange != null) {
            toggleChartRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    loadOrdersChartData(checkedId == R.id.btnChart30Days ? 30 : 7);
                }
            });
        }

        observeData();
        loadOrdersChartData(7);
        loadRevenueChartData();
        loadDonutChartsData();
    }

    private void observeData() {
        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                tvCardTotalOrders.setText(String.valueOf(orders.size()));
                setupNeedsAttentionList(orders);
            }
        });

        viewModel.getPendingOrdersCount().observe(getViewLifecycleOwner(), count -> {
            tvCardPendingOrders.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getProcessingOrdersCount().observe(getViewLifecycleOwner(), count -> {
            tvCardPrintingOrders.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getCompletedTodayCount().observe(getViewLifecycleOwner(), count -> {
            tvCardCompletedToday.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getTotalDeliveredRevenue().observe(getViewLifecycleOwner(), rev -> {
            tvCardRevenueToday.setText(String.format(Locale.getDefault(), "Rs. %,.0f", rev != null ? rev : 0.0));
        });

        viewModel.getTotalCustomersCount().observe(getViewLifecycleOwner(), count -> {
            tvCardTotalCustomers.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void setupNeedsAttentionList(List<Order> orders) {
        List<Order> urgentOrders = new ArrayList<>();
        for (Order o : orders) {
            if ("PENDING".equalsIgnoreCase(o.status) || o.rescheduleRequested) {
                urgentOrders.add(o);
            }
        }

        if (urgentOrders.isEmpty()) {
            rvNeedsAttention.setVisibility(View.GONE);
            tvEmptyNeedsAttention.setVisibility(View.VISIBLE);
        } else {
            rvNeedsAttention.setVisibility(View.VISIBLE);
            tvEmptyNeedsAttention.setVisibility(View.GONE);

            GenericAdapter<Order> adapter = new GenericAdapter<>(
                (v, order) -> {
                    TextView tvMain = v.findViewById(R.id.tvMain);
                    TextView tvSub = v.findViewById(R.id.tvSub);
                    tvMain.setText("⚡ Urgent Order #" + order.id + " (" + order.status + ")");
                    tvSub.setText(String.format(Locale.getDefault(), "Total: Rs. %,.2f | %s", order.totalPrice, order.paymentMethod));
                },
                order -> {
                    if (getActivity() instanceof AdminDashboardActivity) {
                        ((AdminDashboardActivity) getActivity()).navigateToOrderDetail(order.id);
                    }
                }
            );
            adapter.setItems(urgentOrders);
            rvNeedsAttention.setAdapter(adapter);
        }
    }

    private void loadOrdersChartData(int days) {
        List<AdminBarChartView.BarData> list = new ArrayList<>();
        if (days == 7) {
            list.add(new AdminBarChartView.BarData("Mon", 4));
            list.add(new AdminBarChartView.BarData("Tue", 7));
            list.add(new AdminBarChartView.BarData("Wed", 3));
            list.add(new AdminBarChartView.BarData("Thu", 9));
            list.add(new AdminBarChartView.BarData("Fri", 12));
            list.add(new AdminBarChartView.BarData("Sat", 8));
            list.add(new AdminBarChartView.BarData("Sun", 5));
        } else {
            for (int i = 1; i <= 10; i++) {
                list.add(new AdminBarChartView.BarData("W" + i, (float) (Math.random() * 20 + 5)));
            }
        }
        chartOrdersPerDay.setData(list);
    }

    private void loadRevenueChartData() {
        List<AdminLineChartView.LinePoint> points = new ArrayList<>();
        points.add(new AdminLineChartView.LinePoint("Jan", 120000f));
        points.add(new AdminLineChartView.LinePoint("Feb", 185000f));
        points.add(new AdminLineChartView.LinePoint("Mar", 210000f));
        points.add(new AdminLineChartView.LinePoint("Apr", 195000f));
        points.add(new AdminLineChartView.LinePoint("May", 280000f));
        chartRevenueTrend.setData(points);
    }

    private void loadDonutChartsData() {
        List<AdminDonutChartView.SliceData> catSlices = new ArrayList<>();
        catSlices.add(new AdminDonutChartView.SliceData("Cards", 45f, 0xFF2563EB));
        catSlices.add(new AdminDonutChartView.SliceData("Flyers", 30f, 0xFFEA580C));
        catSlices.add(new AdminDonutChartView.SliceData("Mugs", 15f, 0xFF16A34A));
        catSlices.add(new AdminDonutChartView.SliceData("Banners", 10f, 0xFF7C3AED));
        chartCategoryDonut.setData(catSlices);

        List<AdminDonutChartView.SliceData> statusSlices = new ArrayList<>();
        statusSlices.add(new AdminDonutChartView.SliceData("Completed", 50f, 0xFF16A34A));
        statusSlices.add(new AdminDonutChartView.SliceData("Printing", 25f, 0xFF7C3AED));
        statusSlices.add(new AdminDonutChartView.SliceData("Pending", 15f, 0xFFD97706));
        statusSlices.add(new AdminDonutChartView.SliceData("Cancelled", 10f, 0xFFDC2626));
        chartStatusDonut.setData(statusSlices);
    }
}
