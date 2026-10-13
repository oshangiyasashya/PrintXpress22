package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.WishlistDao;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.WishlistItem;
import java.util.List;

public class WishlistRepository {

    public interface ActionCallback {
        void onSuccess(boolean isNowWishlisted);
        void onError(String error);
    }

    private final WishlistDao wishlistDao;

    public WishlistRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.wishlistDao = db.wishlistDao();
    }

    public LiveData<List<Product>> getWishlistProducts(long userId) {
        return wishlistDao.getWishlistProducts(userId);
    }

    public LiveData<Boolean> isWishlisted(long userId, long productId) {
        return wishlistDao.isWishlisted(userId, productId);
    }

    public LiveData<Integer> getWishlistCount(long userId) {
        return wishlistDao.getWishlistCount(userId);
    }

    public void toggleWishlist(long userId, long productId, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int deleted = wishlistDao.delete(userId, productId);
                if (deleted > 0) {
                    if (callback != null) callback.onSuccess(false);
                } else {
                    wishlistDao.insert(new WishlistItem(userId, productId, System.currentTimeMillis()));
                    if (callback != null) callback.onSuccess(true);
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}
