package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.CartDao;
import com.printxpress.data.entity.CartItem;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class CartRepository {

    private final CartDao cartDao;
    private final ExecutorService executor;

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public CartRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        cartDao = db.cartDao();
        executor = AppDatabase.databaseWriteExecutor;
    }

    public void insert(CartItem item, ActionCallback callback) {
        executor.execute(() -> {
            try {
                cartDao.insert(item);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void update(CartItem item, ActionCallback callback) {
        executor.execute(() -> {
            try {
                cartDao.update(item);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void delete(CartItem item, ActionCallback callback) {
        executor.execute(() -> {
            try {
                cartDao.delete(item);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void clearCart(long userId, ActionCallback callback) {
        executor.execute(() -> {
            try {
                cartDao.clearCart(userId);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<List<CartItem>> getCartItemsForUser(long userId) {
        return cartDao.getCartItemsForUser(userId);
    }

    public LiveData<Integer> getCartCount(long userId) {
        return cartDao.getCartCount(userId);
    }
}
