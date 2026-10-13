package com.printxpress.ui.orders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.OrderStatusHistory;
import com.printxpress.data.repository.OrderRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailViewModel extends AndroidViewModel {

    private final OrderRepository orderRepository;

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<Order> getOrder(long orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public LiveData<List<OrderItem>> getOrderItems(long orderId) {
        return orderRepository.getItemsForOrder(orderId);
    }

    public LiveData<List<OrderStatusHistory>> getStatusHistory(long orderId) {
        return orderRepository.getStatusHistory(orderId);
    }

    public void cancelOrder(long orderId, String reason, OrderRepository.UpdateCallback callback) {
        orderRepository.cancelOrder(orderId, reason, callback);
    }

    public void updateOrderStatus(long orderId, String newStatus, OrderRepository.UpdateCallback callback) {
        orderRepository.updateOrderStatus(orderId, newStatus, callback);
    }

    public File generateInvoiceReceipt(Order order, List<OrderItem> items) {
        try {
            File invoicesDir = new File(getApplication().getFilesDir(), "invoices");
            if (!invoicesDir.exists()) invoicesDir.mkdirs();

            File invoiceFile = new File(invoicesDir, "Invoice_PX_" + order.id + ".txt");
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            sb.append("========================================\n");
            sb.append("          PRINTXPRESS STUDIO            \n");
            sb.append("     Colombo 03, Western Province       \n");
            sb.append("    Hotline: +94 11 2345678             \n");
            sb.append("========================================\n");
            sb.append("INVOICE RECEIPT\n");
            sb.append("Order #: ").append(order.id).append("\n");
            sb.append("Tracking: ").append(order.trackingNumber).append("\n");
            sb.append("Date: ").append(sdf.format(new Date(order.createdAt))).append("\n");
            sb.append("Status: ").append(order.status).append("\n");
            sb.append("Delivery Type: ").append(order.deliveryType).append("\n");
            sb.append("Payment: ").append(order.paymentMethod).append(" (").append(order.paymentStatus).append(")\n");
            sb.append("----------------------------------------\n");
            sb.append("ITEMS:\n");

            if (items != null) {
                for (OrderItem it : items) {
                    sb.append("• Item #").append(it.productId)
                            .append(" | Qty: ").append(it.quantity)
                            .append(" | Rs. ").append(String.format(Locale.US, "%,.2f", it.unitPrice))
                            .append(" = Rs. ").append(String.format(Locale.US, "%,.2f", it.unitPrice * it.quantity))
                            .append("\n  Specs: ").append(it.selectedOptions)
                            .append("\n");
                }
            }

            sb.append("----------------------------------------\n");
            if (order.discountAmount > 0) {
                sb.append("Discount (Promo ").append(order.promoCode).append("): -Rs. ")
                        .append(String.format(Locale.US, "%,.2f", order.discountAmount)).append("\n");
            }
            sb.append("TOTAL PAID / DUE: Rs. ").append(String.format(Locale.US, "%,.2f", order.totalPrice)).append("\n");
            sb.append("========================================\n");
            sb.append("Thank you for choosing PrintXpress!\n");

            try (FileOutputStream fos = new FileOutputStream(invoiceFile)) {
                fos.write(sb.toString().getBytes());
                fos.flush();
            }
            return invoiceFile;
        } catch (Exception e) {
            return null;
        }
    }
}
