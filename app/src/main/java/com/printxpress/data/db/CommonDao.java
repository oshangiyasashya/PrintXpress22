package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.Address;
import com.printxpress.data.entity.Faq;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SavedDesign;
import java.util.List;

@Dao
public interface CommonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFaqs(Faq... faqs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPromotions(Promotion... promos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPromotion(Promotion promo);

    @Update
    void updatePromotion(Promotion promo);

    @Query("DELETE FROM promotions WHERE id = :id")
    void deletePromotion(long id);

    @Query("SELECT * FROM faqs")
    LiveData<List<Faq>> getAllFaqs();

    @Query("SELECT COUNT(*) FROM faqs")
    int getFaqCountSync();

    @Query("SELECT COUNT(*) FROM promotions")
    int getPromotionCountSync();

    @Query("SELECT * FROM promotions ORDER BY id DESC")
    LiveData<List<Promotion>> getAllPromotions();

    @Query("SELECT COUNT(*) FROM addresses")
    int getAddressCountSync();

    @Query("SELECT COUNT(*) FROM notifications")
    int getNotificationCountSync();

    @Query("SELECT COUNT(*) FROM saved_designs")
    int getSavedDesignCountSync();

    @Query("SELECT * FROM promotions WHERE :now BETWEEN validFrom AND validTo")
    LiveData<List<Promotion>> getActivePromotions(long now);

    @Query("SELECT * FROM promotions WHERE UPPER(title) = UPPER(:title) LIMIT 1")
    Promotion getPromotionByTitleSync(String title);

    @Insert
    long insertAddress(Address address);

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    LiveData<List<Address>> getAddressesByUser(long userId);

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    List<Address> getAddressesByUserSync(long userId);

    @Query("SELECT * FROM addresses WHERE id = :addressId LIMIT 1")
    Address getAddressByIdSync(long addressId);

    @Query("DELETE FROM addresses WHERE id = :addressId")
    void deleteAddress(long addressId);

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    void resetDefaultAddresses(long userId);

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId")
    void setDefaultAddress(long addressId);

    @Insert
    long insertNotification(Notification notification);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Notification>> getNotificationsByUser(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    LiveData<Integer> getUnreadNotificationCount(long userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isDelivered = 0")
    List<Notification> getUndeliveredNotificationsSync(long userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markNotificationRead(long id);

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllNotificationsRead(long userId);

    @Query("UPDATE notifications SET isDelivered = 1 WHERE userId = :userId")
    void markNotificationsDeliveredSync(long userId);

    @Query("DELETE FROM notifications WHERE id = :id")
    void deleteNotification(long id);

    @Insert
    long insertSavedDesign(SavedDesign design);

    @Query("SELECT * FROM saved_designs WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<SavedDesign>> getSavedDesigns(long userId);

    @Query("SELECT * FROM saved_designs WHERE id = :designId LIMIT 1")
    LiveData<SavedDesign> getSavedDesignById(long designId);

    @Query("SELECT * FROM saved_designs WHERE id = :designId LIMIT 1")
    SavedDesign getSavedDesignByIdSync(long designId);

    @Query("UPDATE saved_designs SET name = :name, thumbnailPath = :thumbnail, designJson = :json WHERE id = :id")
    void updateSavedDesign(long id, String name, String thumbnail, String json);

    @Query("DELETE FROM saved_designs WHERE id = :designId")
    void deleteSavedDesign(long designId);
}