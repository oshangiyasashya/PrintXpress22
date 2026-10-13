package com.printxpress.ui.orders;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Order;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.data.repository.OrderRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class OrdersViewModel extends AndroidViewModel {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        cartRepository = new CartRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<List<Order>> getOrders() {
        return orderRepository.getOrdersByUser(sessionManager.getUserId());
    }

    public LiveData<List<Order>> searchOrders(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getOrders();
        }
        return orderRepository.searchOrders(sessionManager.getUserId(), query.trim());
    }

    public void cancelOrder(long orderId, String reason) {
        orderRepository.cancelOrder(orderId, reason, new OrderRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Order #" + orderId + " has been cancelled.");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Could not cancel order: " + message);
            }
        });
    }

    public void reorder(long orderId) {
        long userId = sessionManager.getUserId();
        orderRepository.reorder(orderId, userId, new OrderRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Items added back to your Cart! 🛒");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Reorder failed: " + message);
            }
        });
    }
}
