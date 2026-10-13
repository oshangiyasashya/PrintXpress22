package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.CatalogDao;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.ProductOption;
import com.printxpress.data.entity.SampleDesign;
import java.util.List;

public class CatalogRepository {
    private final CatalogDao catalogDao;

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public CatalogRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        catalogDao = db.catalogDao();
    }

    public LiveData<List<Category>> getAllCategories() {
        return catalogDao.getAllCategories();
    }

    public LiveData<List<Product>> getProductsByCategory(long categoryId) {
        return catalogDao.getProductsByCategory(categoryId);
    }

    public LiveData<Product> getProductById(long productId) {
        return catalogDao.getProductById(productId);
    }

    public LiveData<List<ProductOption>> getOptionsByProduct(long productId) {
        return catalogDao.getOptionsByProduct(productId);
    }

    public LiveData<List<SampleDesign>> getSamplesByCategory(long categoryId) {
        return catalogDao.getSamplesByCategory(categoryId);
    }

    public LiveData<List<SampleDesign>> getAllSampleDesigns() {
        return catalogDao.getAllSampleDesigns();
    }

    public LiveData<List<Product>> getAllProducts() {
        return catalogDao.getAllProducts();
    }

    public LiveData<List<Product>> getActiveProducts() {
        return catalogDao.getActiveProducts();
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return catalogDao.searchProducts(query);
    }

    public void insertProduct(Product product, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                catalogDao.insertProduct(product);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void updateProduct(Product product, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                catalogDao.updateProduct(product);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void setProductActive(long productId, boolean isActive, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                catalogDao.setProductActive(productId, isActive);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void insertSampleDesign(SampleDesign design, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                catalogDao.insertSampleDesigns(design);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void insertCategory(Category category, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                catalogDao.insertCategory(category);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}