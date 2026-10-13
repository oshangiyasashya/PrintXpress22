package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.WishlistItem;
import java.util.List;

@Dao
public interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WishlistItem item);

    @Query("DELETE FROM wishlist WHERE userId = :userId AND productId = :productId")
    int delete(long userId, long productId);

    @Query("SELECT p.* FROM products p INNER JOIN wishlist w ON p.id = w.productId WHERE w.userId = :userId ORDER BY w.addedAt DESC")
    LiveData<List<Product>> getWishlistProducts(long userId);

    @Query("SELECT COUNT(*) > 0 FROM wishlist WHERE userId = :userId AND productId = :productId")
    LiveData<Boolean> isWishlisted(long userId, long productId);

    @Query("SELECT COUNT(*) FROM wishlist WHERE userId = :userId")
    LiveData<Integer> getWishlistCount(long userId);
}
