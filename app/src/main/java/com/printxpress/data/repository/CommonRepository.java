package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.CommonDao;
import com.printxpress.data.db.SupportQueryDao;
import com.printxpress.data.entity.Address;
import com.printxpress.data.entity.Faq;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.data.entity.SupportQuery;
import java.util.List;

public class CommonRepository {
    private final CommonDao commonDao;
    private final SupportQueryDao supportQueryDao;

    public interface InsertCallback {
        void onSuccess(long id);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface PromoCallback {
        void onFound(Promotion promo);
        void onNotFound();
        void onError(String error);
    }

    public CommonRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        commonDao = db.commonDao();
        supportQueryDao = db.supportQueryDao();
    }

    public LiveData<List<Faq>> getAllFaqs() {
        return commonDao.getAllFaqs();
    }

    public LiveData<List<Promotion>> getActivePromotions(long now) {
        return commonDao.getActivePromotions(now);
    }

    public LiveData<List<Promotion>> getAllPromotions() {
        return commonDao.getAllPromotions();
    }

    public void getPromotionByTitle(String title, PromoCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Promotion promo = commonDao.getPromotionByTitleSync(title);
                if (promo != null) {
                    if (callback != null) callback.onFound(promo);
                } else {
                    if (callback != null) callback.onNotFound();
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void insertPromotion(Promotion promo, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                commonDao.insertPromotion(promo);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updatePromotion(Promotion promo, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                commonDao.updatePromotion(promo);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deletePromotion(long id, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                commonDao.deletePromotion(id);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void insertAddress(Address address, InsertCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                if (address.isDefault) {
                    commonDao.resetDefaultAddresses(address.userId);
                }
                long id = commonDao.insertAddress(address);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void deleteAddress(long addressId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.deleteAddress(addressId);
            if (onComplete != null) onComplete.run();
        });
    }

    public void setDefaultAddress(long userId, long addressId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.resetDefaultAddresses(userId);
            commonDao.setDefaultAddress(addressId);
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Address>> getAddressesByUser(long userId) {
        return commonDao.getAddressesByUser(userId);
    }

    public void insertNotification(Notification notification) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.insertNotification(notification);
        });
    }

    public LiveData<List<Notification>> getNotificationsByUser(long userId) {
        return commonDao.getNotificationsByUser(userId);
    }

    public LiveData<Integer> getUnreadNotificationCount(long userId) {
        return commonDao.getUnreadNotificationCount(userId);
    }

    public void markNotificationRead(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.markNotificationRead(id);
        });
    }

    public void markAllNotificationsRead(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.markAllNotificationsRead(userId);
        });
    }

    public void deleteNotification(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.deleteNotification(id);
        });
    }

    public void insertSavedDesign(SavedDesign design, InsertCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long id = commonDao.insertSavedDesign(design);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updateSavedDesign(long id, String name, String thumbnail, String json, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                commonDao.updateSavedDesign(id, name, thumbnail, json);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteSavedDesign(long designId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commonDao.deleteSavedDesign(designId);
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<SavedDesign>> getSavedDesigns(long userId) {
        return commonDao.getSavedDesigns(userId);
    }

    public LiveData<SavedDesign> getSavedDesignById(long id) {
        return commonDao.getSavedDesignById(id);
    }

    public void insertSupportQuery(SupportQuery query, InsertCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long id = supportQueryDao.insert(query);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<List<SupportQuery>> getSupportQueriesByUser(long userId) {
        return supportQueryDao.getQueriesByUser(userId);
    }

    public LiveData<List<SupportQuery>> getSupportQueriesByUserAsc(long userId) {
        return supportQueryDao.getQueriesByUserAsc(userId);
    }

    public LiveData<List<SupportQuery>> getAllSupportQueries() {
        return supportQueryDao.getAllQueries();
    }

    public void replyToSupportQuery(long queryId, String reply, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                supportQueryDao.replyToQuery(queryId, reply, System.currentTimeMillis());
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}
