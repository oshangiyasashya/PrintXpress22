package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.ProductOption;
import com.printxpress.data.entity.SampleDesign;
import java.util.List;

@Dao
public interface CatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertCategories(Category... categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertProducts(Product... products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Query("UPDATE products SET isActive = :isActive WHERE id = :productId")
    void setProductActive(long productId, boolean isActive);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOptions(ProductOption... options);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSampleDesigns(SampleDesign... designs);

    @Query("SELECT * FROM categories ORDER BY id ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<Category> getAllCategoriesSync();

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCountSync();

    @Query("SELECT COUNT(*) FROM products")
    int getProductCountSync();

    @Query("SELECT * FROM products ORDER BY id ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY id ASC")
    LiveData<List<Product>> getActiveProducts();

    @Query("SELECT * FROM products ORDER BY id ASC")
    List<Product> getAllProductsSync();

    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isActive = 1 ORDER BY id ASC")
    LiveData<List<Product>> searchProducts(String query);

    @Query("SELECT * FROM products WHERE categoryId = :catId AND isActive = 1")
    LiveData<List<Product>> getProductsByCategory(long catId);

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    LiveData<Product> getProductById(long productId);

    @Query("SELECT * FROM product_options WHERE productId = :productId")
    LiveData<List<ProductOption>> getOptionsByProduct(long productId);

    @Query("SELECT * FROM sample_designs WHERE categoryId = :categoryId")
    LiveData<List<SampleDesign>> getSamplesByCategory(long categoryId);

    @Query("SELECT * FROM sample_designs ORDER BY id ASC")
    LiveData<List<SampleDesign>> getAllSampleDesigns();
}
