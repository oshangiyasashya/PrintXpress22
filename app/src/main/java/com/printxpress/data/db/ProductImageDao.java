package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.printxpress.data.entity.ProductImage;
import java.util.List;

@Dao
public interface ProductImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProductImage image);

    @Query("SELECT * FROM product_images WHERE productId = :productId ORDER BY sortOrder ASC")
    LiveData<List<ProductImage>> getImagesForProduct(long productId);

    @Query("DELETE FROM product_images WHERE id = :id")
    void delete(long id);
}
