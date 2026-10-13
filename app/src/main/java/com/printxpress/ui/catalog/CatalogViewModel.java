package com.printxpress.ui.catalog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.entity.User;
import com.printxpress.data.repository.CatalogRepository;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.data.repository.OrderRepository;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private final CatalogRepository catalogRepository;
    private final CommonRepository commonRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SessionManager sessionManager;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        catalogRepository = new CatalogRepository(application);
        commonRepository = new CommonRepository(application);
        userRepository = new UserRepository(application);
        orderRepository = new OrderRepository(application);
        sessionManager = new SessionManager(application);
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getUserById(sessionManager.getUserId());
    }

    public LiveData<List<Category>> getCategories() {
        return catalogRepository.getAllCategories();
    }

    public LiveData<List<Promotion>> getActivePromotions() {
        return commonRepository.getActivePromotions(System.currentTimeMillis());
    }

    public LiveData<List<Product>> getPopularProducts() {
        return catalogRepository.getActiveProducts();
    }

    public LiveData<List<SampleDesign>> getSampleDesigns() {
        return catalogRepository.getAllSampleDesigns();
    }

    public LiveData<List<Order>> getRecentOrders() {
        return orderRepository.getOrdersByUser(sessionManager.getUserId());
    }

    public LiveData<Integer> getUnreadNotificationCount() {
        return commonRepository.getUnreadNotificationCount(sessionManager.getUserId());
    }
}
