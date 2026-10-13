package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.OrderDao;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.OrderStatusHistory;
import java.util.List;

public class OrderRepository {
    private final AppDatabase db;
    private final OrderDao orderDao;

    public interface OrderPlacementCallback {
        void onSuccess(long orderId);
        void onError(String message);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public OrderRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        orderDao = db.orderDao();
    }

    public void placeOrder(Order order, List<OrderItem> items, OrderPlacementCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long orderId = db.runInTransaction(() -> {
                    long id = orderDao.insertOrder(order);
                    for (OrderItem item : items) {
                        item.orderId = id;
                    }
                    orderDao.insertOrderItems(items);

                    OrderStatusHistory initialHistory = new OrderStatusHistory(id, order.status, System.currentTimeMillis());
                    orderDao.insertStatusHistory(initialHistory);

                    // Insert confirmed notification
                    Notification notif = new Notification(
                            order.userId,
                            "Order Placed #" + id,
                            "Your print order " + order.trackingNumber + " has been successfully submitted.",
                            "ORDER",
                            false,
                            System.currentTimeMillis()
                    );
                    db.commonDao().insertNotification(notif);

                    return id;
                });

                callback.onSuccess(orderId);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<List<Order>> getAllOrders() {
        return orderDao.getAllOrders();
    }

    public LiveData<List<Order>> getOrdersByUser(long userId) {
        return orderDao.getOrdersByUser(userId);
    }

    public LiveData<List<Order>> searchOrders(long userId, String query) {
        return orderDao.searchOrders(userId, "%" + query + "%");
    }

    public LiveData<Order> getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    public LiveData<List<OrderItem>> getItemsForOrder(long orderId) {
        return orderDao.getItemsForOrder(orderId);
    }

    public LiveData<List<OrderStatusHistory>> getStatusHistory(long orderId) {
        return orderDao.getStatusHistory(orderId);
    }

    public void updateOrderStatus(long orderId, String newStatus, UpdateCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    orderDao.updateOrderStatus(orderId, newStatus);
                    OrderStatusHistory history = new OrderStatusHistory(orderId, newStatus, System.currentTimeMillis());
                    orderDao.insertStatusHistory(history);

                    Order order = orderDao.getOrderByIdSync(orderId);
                    if (order != null) {
                        Notification notif = new Notification(
                                order.userId,
                                "Order Status Update",
                                "Order #" + orderId + " is now " + newStatus,
                                "ORDER",
                                false,
                                System.currentTimeMillis()
                        );
                        db.commonDao().insertNotification(notif);
                    }
                });
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void cancelOrder(long orderId, String reason, UpdateCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    int rows = orderDao.cancelOrder(orderId, reason);
                    if (rows > 0) {
                        OrderStatusHistory history = new OrderStatusHistory(orderId, "CANCELLED", System.currentTimeMillis());
                        orderDao.insertStatusHistory(history);

                        Order order = orderDao.getOrderByIdSync(orderId);
                        if (order != null) {
                            Notification notif = new Notification(
                                    order.userId,
                                    "Order Cancelled",
                                    "Order #" + orderId + " has been cancelled. Reason: " + reason,
                                    "ORDER",
                                    false,
                                    System.currentTimeMillis()
                            );
                            db.commonDao().insertNotification(notif);
                        }
                    }
                });
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<Double> getTotalDeliveredRevenue() {
        return orderDao.getTotalDeliveredRevenue();
    }

    public LiveData<Integer> getPendingOrderCount() {
        return orderDao.getPendingOrderCount();
    }

    public LiveData<Integer> getActiveProcessingOrderCount() {
        return orderDao.getActiveProcessingOrderCount();
    }

    public void reorder(long orderId, long userId, UpdateCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<OrderItem> items = orderDao.getItemsForOrderSync(orderId);
                if (items != null && !items.isEmpty()) {
                    for (OrderItem item : items) {
                        CartItem ci = new CartItem(
                                userId,
                                item.productId,
                                "Reorder Item",
                                item.unitPrice,
                                item.quantity,
                                item.selectedOptions,
                                "custom",
                                item.designFilePath,
                                item.customText
                        );
                        db.cartDao().insert(ci);
                    }
                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onError("No items found to reorder.");
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}
