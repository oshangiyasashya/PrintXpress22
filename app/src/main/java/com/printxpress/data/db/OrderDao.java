package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.OrderStatusHistory;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insertOrder(Order order);

    @Insert
    void insertOrderItems(List<OrderItem> items);

    @Insert
    void insertStatusHistory(OrderStatusHistory history);

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllOrders();

    @Query("SELECT COUNT(*) FROM orders")
    int getOrderCountSync();

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByUser(long userId);

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    LiveData<Order> getOrderById(long orderId);

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    Order getOrderByIdSync(long orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    LiveData<List<OrderItem>> getItemsForOrder(long orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItem> getItemsForOrderSync(long orderId);

    @Query("SELECT * FROM order_status_history WHERE orderId = :orderId ORDER BY timestamp ASC")
    LiveData<List<OrderStatusHistory>> getStatusHistory(long orderId);

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    void updateOrderStatus(long orderId, String status);

    @Query("UPDATE orders SET status = 'CANCELLED', cancelReason = :reason WHERE id = :orderId AND status = 'PENDING'")
    int cancelOrder(long orderId, String reason);

    @Query("SELECT * FROM orders WHERE userId = :userId AND (CAST(id AS TEXT) LIKE :query OR trackingNumber LIKE :query) ORDER BY createdAt DESC")
    LiveData<List<Order>> searchOrders(long userId, String query);

    @Query("SELECT SUM(totalPrice) FROM orders WHERE status = 'DELIVERED'")
    LiveData<Double> getTotalDeliveredRevenue();

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'")
    LiveData<Integer> getPendingOrderCount();

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PROCESSING' OR status = 'PRINTING'")
    LiveData<Integer> getActiveProcessingOrderCount();

    @Query("UPDATE orders SET adminNotes = :notes WHERE id = :orderId")
    void updateAdminNotes(long orderId, String notes);

    @Query("UPDATE orders SET status = :status WHERE id IN (:orderIds)")
    void updateBulkOrderStatus(List<Long> orderIds, String status);

    @Query("SELECT * FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    LiveData<List<Order>> getOrdersByDateRange(long startTime, long endTime);

    @Query("SELECT COALESCE(SUM(totalPrice), 0.0) FROM orders WHERE status = 'DELIVERED' AND createdAt >= :startTime AND createdAt <= :endTime")
    LiveData<Double> getRevenueByDateRange(long startTime, long endTime);

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED' AND createdAt >= :startOfDay")
    LiveData<Integer> getCompletedTodayCount(long startOfDay);

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime")
    LiveData<Integer> getOrdersCountByDateRange(long startTime, long endTime);

    @Query("UPDATE orders SET cancelApproved = :approved, status = CASE WHEN :approved THEN 'CANCELLED' ELSE status END WHERE id = :orderId")
    void updateCancelApproval(long orderId, boolean approved);

    @Query("UPDATE orders SET rescheduleRequested = 0, scheduledDate = CASE WHEN :approved THEN :newDate ELSE scheduledDate END WHERE id = :orderId")
    void updateReschedule(long orderId, boolean approved, long newDate);

    @Query("SELECT * FROM orders WHERE CAST(id AS TEXT) LIKE '%' || :query || '%' OR trackingNumber LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    LiveData<List<Order>> searchAllOrders(String query);
}
