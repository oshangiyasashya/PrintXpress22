package com.printxpress.ui.admin;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.entity.AdminActivityLog;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.entity.SupportQuery;
import com.printxpress.data.entity.User;
import com.printxpress.data.repository.CatalogRepository;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.data.repository.OrderRepository;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.PasswordUtil;
import com.printxpress.util.SessionManager;
import java.util.List;

public class AdminViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final OrderRepository orderRepository;
    private final CatalogRepository catalogRepository;
    private final UserRepository userRepository;
    private final CommonRepository commonRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public AdminViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        orderRepository = new OrderRepository(application);
        catalogRepository = new CatalogRepository(application);
        userRepository = new UserRepository(application);
        commonRepository = new CommonRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    // Activity Audit Log
    public void logActivity(String actionName, String details) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long adminId = sessionManager.getUserId();
                String adminName = sessionManager.getUserName();
                if (adminName.isEmpty()) adminName = "Admin #" + adminId;
                AdminActivityLog log = new AdminActivityLog(adminId, adminName, actionName, details, System.currentTimeMillis());
                db.activityLogDao().insert(log);
            } catch (Exception ignored) {
            }
        });
    }

    public LiveData<List<AdminActivityLog>> getAllActivityLogs() {
        return db.activityLogDao().getAllLogs();
    }

    // Orders
    public LiveData<List<Order>> getAllOrders() {
        return orderRepository.getAllOrders();
    }

    public LiveData<List<Order>> searchOrders(String query) {
        return db.orderDao().searchAllOrders(query);
    }

    public LiveData<Order> getOrderById(long orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public void updateOrderStatus(long orderId, String status) {
        orderRepository.updateOrderStatus(orderId, status, new OrderRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Order #" + orderId + " updated to " + status);
                logActivity("UPDATE_ORDER_STATUS", "Order #" + orderId + " set to " + status);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Status update failed: " + message);
            }
        });
    }

    public void updateBulkOrderStatus(List<Long> orderIds, String status) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.orderDao().updateBulkOrderStatus(orderIds, status);
                logActivity("BULK_ORDER_UPDATE", "Updated " + orderIds.size() + " orders to " + status);
                actionMessage.postValue("Updated " + orderIds.size() + " orders to " + status);
            } catch (Exception e) {
                actionMessage.postValue("Bulk update failed: " + e.getMessage());
            }
        });
    }

    public void updateAdminNotes(long orderId, String notes) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.orderDao().updateAdminNotes(orderId, notes);
                logActivity("UPDATE_ADMIN_NOTES", "Updated notes for Order #" + orderId);
                actionMessage.postValue("Notes saved for Order #" + orderId);
            } catch (Exception e) {
                actionMessage.postValue("Error saving notes: " + e.getMessage());
            }
        });
    }

    // Products & Catalog
    public LiveData<List<Product>> getAllProducts() {
        return catalogRepository.getAllProducts();
    }

    public void toggleProductActive(long productId, boolean isActive) {
        catalogRepository.setProductActive(productId, isActive, new CatalogRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Product status updated");
                logActivity("TOGGLE_PRODUCT_ACTIVE", "Product #" + productId + " set active=" + isActive);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Error: " + message);
            }
        });
    }

    public void addProduct(Product product) {
        catalogRepository.insertProduct(product, new CatalogRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Product added successfully!");
                logActivity("ADD_PRODUCT", "Added product: " + product.name);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to add product: " + message);
            }
        });
    }

    public void addSampleTemplate(SampleDesign design) {
        catalogRepository.insertSampleDesign(design, new CatalogRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Template added successfully!");
                logActivity("ADD_TEMPLATE", "Added template: " + design.title);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to add template: " + message);
            }
        });
    }

    public LiveData<List<Category>> getAllCategories() {
        return catalogRepository.getAllCategories();
    }

    public void addCategory(Category category) {
        catalogRepository.insertCategory(category, new CatalogRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Category added successfully!");
                logActivity("ADD_CATEGORY", "Added category: " + category.name);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to add category: " + message);
            }
        });
    }

    public LiveData<List<SampleDesign>> getAllSampleTemplates() {
        return catalogRepository.getAllSampleDesigns();
    }

    // Users & Customers
    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public LiveData<List<User>> searchUsers(String query) {
        return db.userDao().searchUsers(query);
    }

    public LiveData<Integer> getTotalCustomersCount() {
        return db.userDao().getTotalCustomersCount();
    }

    public LiveData<Integer> getNewCustomersCount() {
        long startOfWeek = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        return db.userDao().getNewCustomersCount(startOfWeek);
    }

    public void setUserBlocked(long userId, boolean isBlocked) {
        userRepository.setBlockedStatus(userId, isBlocked, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue(isBlocked ? "User account blocked" : "User account unblocked");
                logActivity("TOGGLE_USER_BLOCK", "User #" + userId + " blocked=" + isBlocked);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Error: " + message);
            }
        });
    }

    public void resetUserPassword(long userId, String newPassword) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hashPassword(newPassword, salt);
                db.userDao().updatePasswordById(userId, hash, salt, "PBKDF2");
                logActivity("RESET_USER_PASSWORD", "Reset password for User #" + userId);
                actionMessage.postValue("Password reset successfully for User #" + userId);
            } catch (Exception e) {
                actionMessage.postValue("Reset failed: " + e.getMessage());
            }
        });
    }

    public void softDeleteUser(long userId) {
        userRepository.softDeleteUser(userId, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("User deleted");
                logActivity("DELETE_USER", "Soft deleted User #" + userId);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Delete failed: " + message);
            }
        });
    }

    public void createAdminAccount(String name, String email, String phone, String password, String role) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        User newAdmin = new User(name, email, phone, hash, salt, System.currentTimeMillis(), role);
        userRepository.registerUser(newAdmin, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                actionMessage.postValue("Staff/Admin created: " + user.fullName);
                logActivity("CREATE_ADMIN_ACCOUNT", "Created " + role + ": " + user.email);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Error: " + message);
            }
        });
    }

    // Promotions & Notifications
    public LiveData<List<Promotion>> getAllPromotions() {
        return commonRepository.getAllPromotions();
    }

    public void addPromotion(Promotion promo) {
        commonRepository.insertPromotion(promo, new CommonRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Promotion published!");
                logActivity("ADD_PROMOTION", "Published promo: " + promo.title);
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void deletePromotion(long promoId) {
        commonRepository.deletePromotion(promoId, new CommonRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Promotion deleted");
                logActivity("DELETE_PROMOTION", "Deleted promo #" + promoId);
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void sendBroadcastNotification(String title, String message, Long targetUserId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                if (targetUserId != null && targetUserId > 0) {
                    Notification notif = new Notification(targetUserId, title, message, "PROMO", false, System.currentTimeMillis());
                    db.commonDao().insertNotification(notif);
                    logActivity("SEND_NOTIFICATION", "Sent direct notif to User #" + targetUserId);
                } else {
                    List<User> users = db.userDao().getUserByIdSync(1) != null ? db.userDao().getAllUsers().getValue() : null;
                    if (users != null) {
                        for (User u : users) {
                            if ("CUSTOMER".equalsIgnoreCase(u.role)) {
                                Notification notif = new Notification(u.id, title, message, "PROMO", false, System.currentTimeMillis());
                                db.commonDao().insertNotification(notif);
                            }
                        }
                    }
                    logActivity("BROADCAST_NOTIFICATION", "Sent broadcast: " + title);
                }
                actionMessage.postValue("Notification sent successfully!");
            } catch (Exception e) {
                actionMessage.postValue("Failed to send notification: " + e.getMessage());
            }
        });
    }

    // Support Queries
    public LiveData<List<SupportQuery>> getAllSupportTickets() {
        return commonRepository.getAllSupportQueries();
    }

    public LiveData<Integer> getUnansweredTicketsCount() {
        return db.supportQueryDao().getUnansweredTicketsCount();
    }

    public LiveData<List<SupportQuery>> getUnansweredTickets() {
        return db.supportQueryDao().getUnansweredTickets();
    }

    public void replyToTicket(long ticketId, String reply) {
        commonRepository.replyToSupportQuery(ticketId, reply, new CommonRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Reply sent to customer!");
                logActivity("REPLY_SUPPORT_TICKET", "Replied to ticket #" + ticketId);
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    // Metrics
    public LiveData<Double> getTotalDeliveredRevenue() {
        return orderRepository.getTotalDeliveredRevenue();
    }

    public LiveData<Integer> getPendingOrdersCount() {
        return orderRepository.getPendingOrderCount();
    }

    public LiveData<Integer> getProcessingOrdersCount() {
        return orderRepository.getActiveProcessingOrderCount();
    }

    public LiveData<Integer> getCompletedTodayCount() {
        long startOfDay = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000L));
        return db.orderDao().getCompletedTodayCount(startOfDay);
    }

    public void logout() {
        logActivity("ADMIN_LOGOUT", "Admin logged out");
        sessionManager.logout();
    }
}
