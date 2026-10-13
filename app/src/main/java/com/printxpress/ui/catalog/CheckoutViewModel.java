package com.printxpress.ui.catalog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.Address;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.PaymentMethod;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.data.repository.OrderRepository;
import com.printxpress.data.repository.PaymentMethodRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class CheckoutViewModel extends AndroidViewModel {

    private final OrderRepository orderRepository;
    private final CommonRepository commonRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CartRepository cartRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<Long> orderPlacedId = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlacingOrder = new MutableLiveData<>(false);

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        commonRepository = new CommonRepository(application);
        paymentMethodRepository = new PaymentMethodRepository(application);
        cartRepository = new CartRepository(application);
        sessionManager = new SessionManager(application);
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<Long> getOrderPlacedId() {
        return orderPlacedId;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsPlacingOrder() {
        return isPlacingOrder;
    }

    public LiveData<List<Address>> getSavedAddresses() {
        return commonRepository.getAddressesByUser(sessionManager.getUserId());
    }

    public LiveData<List<PaymentMethod>> getSavedCards() {
        return paymentMethodRepository.getPaymentMethodsByUser(sessionManager.getUserId());
    }

    public void addAddress(Address address) {
        commonRepository.insertAddress(address, new CommonRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                // LiveData updates automatically
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Failed to save address: " + error);
            }
        });
    }

    public void placeOrder(Order order, List<OrderItem> items, boolean isFromCart) {
        isPlacingOrder.setValue(true);
        orderRepository.placeOrder(order, items, new OrderRepository.OrderPlacementCallback() {
            @Override
            public void onSuccess(long orderId) {
                if (isFromCart) {
                    cartRepository.clearCart(sessionManager.getUserId(), null);
                }
                isPlacingOrder.postValue(false);
                orderPlacedId.postValue(orderId);
            }

            @Override
            public void onError(String message) {
                isPlacingOrder.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
