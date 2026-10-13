package com.printxpress.ui.orders;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.printxpress.R;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.repository.OrderRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsFragment extends Fragment {
    private OrderDetailViewModel viewModel;
    private long orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        if (getArguments() != null) {
            orderId = getArguments().getLong("orderId", -1);
        }

        TextView tvOrderId = view.findViewById(R.id.tvOrderId);
        TextView tvOrderTracking = view.findViewById(R.id.tvOrderTracking);
        TextView tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        TextView tvOrderDate = view.findViewById(R.id.tvOrderDate);
        TextView tvOrderShippingAddress = view.findViewById(R.id.tvOrderShippingAddress);
        TextView tvOrderPaymentDetails = view.findViewById(R.id.tvOrderPaymentDetails);
        TextView tvOrderItems = view.findViewById(R.id.tvOrderItems);
        Button btnCancelOrder = view.findViewById(R.id.btnCancelOrder);
        Button btnReschedule = view.findViewById(R.id.btnReschedule);

        viewModel.getOrder(orderId).observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                tvOrderId.setText("Order #" + order.id);
                if (tvOrderTracking != null) {
                    tvOrderTracking.setText(order.trackingNumber != null ? order.trackingNumber : "PX-" + order.id);
                }
                tvOrderStatus.setText("Status: " + order.status);

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                tvOrderDate.setText("Placed on: " + sdf.format(new Date(order.createdAt)));

                if (tvOrderShippingAddress != null) {
                    String addr = !TextUtils.isEmpty(order.shippingAddressText) ? order.shippingAddressText : (order.deliveryType != null ? order.deliveryType : "Standard");
                    tvOrderShippingAddress.setText("Delivery: " + order.deliveryType + "\nAddress: " + addr);
                }

                if (tvOrderPaymentDetails != null) {
                    String payMethod = order.paymentMethod != null ? order.paymentMethod : "COD";
                    String payStatus = order.paymentStatus != null ? order.paymentStatus : "PENDING";
                    tvOrderPaymentDetails.setText(String.format(Locale.getDefault(), "Payment: %s (%s) | Total: Rs. %,.2f", payMethod, payStatus, order.totalPrice));
                }

                boolean canModify = "PENDING".equalsIgnoreCase(order.status) || "PLACED".equalsIgnoreCase(order.status);
                btnCancelOrder.setEnabled(canModify);
                btnReschedule.setEnabled(canModify);
                if (!canModify) {
                    btnCancelOrder.setAlpha(0.5f);
                    btnReschedule.setAlpha(0.5f);
                }

                btnCancelOrder.setOnClickListener(v -> {
                    if (canModify) {
                        new AlertDialog.Builder(requireContext())
                            .setTitle("Cancel Order")
                            .setMessage("Are you sure you want to cancel Order #" + order.id + "?")
                            .setPositiveButton("Yes, Cancel", (d, w) -> cancelOrder())
                            .setNegativeButton("No", null)
                            .show();
                    } else {
                        Toast.makeText(getContext(), "Cannot cancel order once printing has started.", Toast.LENGTH_SHORT).show();
                    }
                });

                btnReschedule.setOnClickListener(v -> {
                    if (canModify) {
                        Toast.makeText(getContext(), "Delivery rescheduled for tomorrow morning.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Cannot reschedule order once production has started.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        viewModel.getOrderItems(orderId).observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    sb.append("Item ").append(i + 1).append(":\n")
                      .append("• Quantity: ").append(item.quantity).append(" units\n")
                      .append(String.format(Locale.getDefault(), "• Unit Price: Rs. %,.2f\n", item.unitPrice));
                    if (!TextUtils.isEmpty(item.customText)) {
                        sb.append("• Specifications: ").append(item.customText).append("\n");
                    }
                    if (i < items.size() - 1) sb.append("\n");
                }
                tvOrderItems.setText(sb.toString());
            } else {
                tvOrderItems.setText("Standard Custom Print Specification");
            }
        });
    }

    private void cancelOrder() {
        viewModel.cancelOrder(orderId, "Customer requested cancellation", new OrderRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Order cancelled successfully", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
