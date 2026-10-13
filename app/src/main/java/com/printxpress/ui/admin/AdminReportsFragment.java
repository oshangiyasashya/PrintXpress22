package com.printxpress.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.Order;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminReportsFragment extends Fragment {

    private AdminViewModel viewModel;
    private TextView tvReportRevenueTotal, tvReportOrdersTotal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        tvReportRevenueTotal = view.findViewById(R.id.tvReportRevenueTotal);
        tvReportOrdersTotal = view.findViewById(R.id.tvReportOrdersTotal);

        MaterialButton btnExportCsv = view.findViewById(R.id.btnExportCsv);
        MaterialButton btnShareReport = view.findViewById(R.id.btnShareReport);

        btnExportCsv.setOnClickListener(v -> exportReportToCsv());
        btnShareReport.setOnClickListener(v -> shareReport());

        observeData();
    }

    private void observeData() {
        viewModel.getTotalDeliveredRevenue().observe(getViewLifecycleOwner(), rev -> {
            tvReportRevenueTotal.setText(String.format(Locale.getDefault(), "Total Sales: Rs. %,.2f", rev != null ? rev : 0.0));
        });

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            int deliveredCount = 0;
            if (orders != null) {
                for (Order o : orders) {
                    if ("DELIVERED".equalsIgnoreCase(o.status)) deliveredCount++;
                }
            }
            tvReportOrdersTotal.setText("Total Orders Delivered: " + deliveredCount);
        });
    }

    private void exportReportToCsv() {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "PrintXpress_Sales_Report_" + System.currentTimeMillis() + ".csv");
            FileWriter writer = new FileWriter(file);
            writer.append("Order ID,Status,Delivery Type,Payment,Total Price,Created At\n");

            if (viewModel.getAllOrders().getValue() != null) {
                for (Order o : viewModel.getAllOrders().getValue()) {
                    writer.append(String.valueOf(o.id)).append(",")
                            .append(o.status).append(",")
                            .append(o.deliveryType).append(",")
                            .append(o.paymentMethod).append(",")
                            .append(String.valueOf(o.totalPrice)).append(",")
                            .append(String.valueOf(o.createdAt)).append("\n");
                }
            }
            writer.flush();
            writer.close();

            Toast.makeText(getContext(), "CSV Exported to Downloads: " + file.getName(), Toast.LENGTH_LONG).show();
            viewModel.logActivity("EXPORT_REPORT_CSV", "Exported sales CSV: " + file.getName());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to export CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        String reportText = "PRINTXPRESS SALES SUMMARY REPORT\n" +
                "===============================\n" +
                tvReportRevenueTotal.getText().toString() + "\n" +
                tvReportOrdersTotal.getText().toString() + "\n" +
                "Generated on: " + DateFormat.getDateTimeInstance().format(new Date()) + "\n";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "PrintXpress Sales Analytics Report");
        intent.putExtra(Intent.EXTRA_TEXT, reportText);
        startActivity(Intent.createChooser(intent, "Share Sales Report"));
    }
}
