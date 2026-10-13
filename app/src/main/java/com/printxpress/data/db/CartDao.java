package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.CartItem;
import java.util.List;

@Dao
public interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CartItem item);

    @Update
    void update(CartItem item);

    @Delete
    void delete(CartItem item);

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    void clearCart(long userId);

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    LiveData<List<CartItem>> getCartItemsForUser(long userId);

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    List<CartItem> getCartItemsSync(long userId);

    @Query("SELECT COUNT(*) FROM cart_items")
    int getCartCountSync();

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    LiveData<Integer> getCartCount(long userId);
}
