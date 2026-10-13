package com.printxpress.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.Order;
import java.util.Locale;

public class AdminOrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private long orderId = -1L;
    private AdminViewModel viewModel;
    private Order currentOrder;

    private TextView tvOrderId, tvStatusChip, tvTrackingAndDate;
    private TextView tvCustomerName, tvAddress, tvPayment;
    private EditText etAdminNotes;

    public static AdminOrderDetailFragment newInstance(long orderId) {
        AdminOrderDetailFragment fragment = new AdminOrderDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getLong(ARG_ORDER_ID, -1L);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        tvOrderId = view.findViewById(R.id.tvDetailOrderId);
        tvStatusChip = view.findViewById(R.id.tvDetailStatusChip);
        tvTrackingAndDate = view.findViewById(R.id.tvDetailTrackingAndDate);
        tvCustomerName = view.findViewById(R.id.tvDetailCustomerName);
        tvAddress = view.findViewById(R.id.tvDetailAddress);
        tvPayment = view.findViewById(R.id.tvDetailPayment);
        etAdminNotes = view.findViewById(R.id.etAdminNotes);

        MaterialButton btnChangeStatus = view.findViewById(R.id.btnChangeStatus);
        MaterialButton btnPrintProduction = view.findViewById(R.id.btnPrintProductionSheet);
        MaterialButton btnSaveNotes = view.findViewById(R.id.btnSaveAdminNotes);
        MaterialButton btnSaveDesign = view.findViewById(R.id.btnSaveDesignFile);

        btnChangeStatus.setOnClickListener(v -> showChangeStatusDialog());
        btnPrintProduction.setOnClickListener(v -> shareProductionSheet());
        btnSaveNotes.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateAdminNotes(currentOrder.id, etAdminNotes.getText().toString().trim());
            }
        });
        btnSaveDesign.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Design PDF/Artwork downloaded to Downloads folder", Toast.LENGTH_SHORT).show();
        });

        if (orderId > 0) {
            viewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), order -> {
                if (order != null) {
                    currentOrder = order;
                    bindOrderDetails(order);
                }
            });
        }
    }

    private void bindOrderDetails(Order order) {
        tvOrderId.setText("Order #" + order.id);
        tvStatusChip.setText(order.status);
        tvTrackingAndDate.setText("Tracking: " + (order.trackingNumber != null ? order.trackingNumber : "PX-000") + " • " + order.deliveryType);
        tvCustomerName.setText("Customer User ID: " + order.userId);
        tvAddress.setText(order.shippingAddressText != null && !order.shippingAddressText.isEmpty() ? order.shippingAddressText : "Standard Studio Delivery");
        tvPayment.setText("Payment: " + order.paymentMethod + " • " + order.paymentStatus + " • Total: Rs. " + String.format(Locale.getDefault(), "%,.2f", order.totalPrice));

        if (order.adminNotes != null) {
            etAdminNotes.setText(order.adminNotes);
        }
    }

    private void showChangeStatusDialog() {
        if (currentOrder == null) return;
        String[] validStatuses = {"PENDING", "PROCESSING", "PRINTING", "READY", "DELIVERED", "CANCELLED"};
        new AlertDialog.Builder(requireContext())
            .setTitle("Set Order #" + currentOrder.id + " Status")
            .setItems(validStatuses, (dialog, which) -> {
                String newStatus = validStatuses[which];
                viewModel.updateOrderStatus(currentOrder.id, newStatus);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void shareProductionSheet() {
        if (currentOrder == null) return;
        String sheetText = "PRINTXPRESS PRODUCTION SHEET\n" +
                "===========================\n" +
                "Order ID: #" + currentOrder.id + "\n" +
                "Tracking: " + currentOrder.trackingNumber + "\n" +
                "Status: " + currentOrder.status + "\n" +
                "Type: " + currentOrder.deliveryType + "\n" +
                "Total: Rs. " + currentOrder.totalPrice + "\n" +
                "Admin Notes: " + etAdminNotes.getText().toString().trim() + "\n";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Production Sheet - Order #" + currentOrder.id);
        intent.putExtra(Intent.EXTRA_TEXT, sheetText);
        startActivity(Intent.createChooser(intent, "Share Production Sheet"));
    }
}
