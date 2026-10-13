package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.PaymentMethod;
import java.util.List;

@Dao
public interface PaymentMethodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PaymentMethod paymentMethod);

    @Update
    void update(PaymentMethod paymentMethod);

    @Delete
    void delete(PaymentMethod paymentMethod);

    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    LiveData<List<PaymentMethod>> getPaymentMethodsByUser(long userId);

    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    List<PaymentMethod> getPaymentMethodsByUserSync(long userId);

    @Query("SELECT COUNT(*) FROM payment_methods")
    int getPaymentMethodCountSync();

    @Query("UPDATE payment_methods SET isDefault = 0 WHERE userId = :userId")
    void resetDefaultPayments(long userId);

    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :paymentMethodId")
    void setDefaultPayment(long paymentMethodId);
}
