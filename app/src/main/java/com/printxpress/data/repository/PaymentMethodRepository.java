package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.PaymentMethodDao;
import com.printxpress.data.entity.PaymentMethod;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PaymentMethodRepository {
    private final PaymentMethodDao paymentMethodDao;
    private final ExecutorService executor;

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public PaymentMethodRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        paymentMethodDao = db.paymentMethodDao();
        executor = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<PaymentMethod>> getPaymentMethodsByUser(long userId) {
        return paymentMethodDao.getPaymentMethodsByUser(userId);
    }

    public void insert(PaymentMethod paymentMethod, ActionCallback callback) {
        executor.execute(() -> {
            try {
                if (paymentMethod.isDefault) {
                    paymentMethodDao.resetDefaultPayments(paymentMethod.userId);
                }
                paymentMethodDao.insert(paymentMethod);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void delete(PaymentMethod paymentMethod, ActionCallback callback) {
        executor.execute(() -> {
            try {
                paymentMethodDao.delete(paymentMethod);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void setDefaultPayment(long userId, long paymentMethodId, ActionCallback callback) {
        executor.execute(() -> {
            try {
                paymentMethodDao.resetDefaultPayments(userId);
                paymentMethodDao.setDefaultPayment(paymentMethodId);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}
