package com.printxpress.ui.catalog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.repository.CatalogRepository;
import java.util.List;

public class ProductsViewModel extends AndroidViewModel {

    private final CatalogRepository catalogRepository;

    public ProductsViewModel(@NonNull Application application) {
        super(application);
        catalogRepository = new CatalogRepository(application);
    }

    public LiveData<List<Category>> getCategories() {
        return catalogRepository.getAllCategories();
    }

    public LiveData<List<Product>> getAllProducts() {
        return catalogRepository.getActiveProducts();
    }

    public LiveData<List<Product>> getProductsByCategory(long categoryId) {
        if (categoryId <= 0) {
            return catalogRepository.getActiveProducts();
        }
        return catalogRepository.getProductsByCategory(categoryId);
    }

    public LiveData<List<Product>> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return catalogRepository.getActiveProducts();
        }
        return catalogRepository.searchProducts(query.trim());
    }

    public LiveData<List<SampleDesign>> getAllSampleDesigns() {
        return catalogRepository.getAllSampleDesigns();
    }

    public LiveData<List<SampleDesign>> getSamplesByCategory(long categoryId) {
        if (categoryId <= 0) {
            return catalogRepository.getAllSampleDesigns();
        }
        return catalogRepository.getSamplesByCategory(categoryId);
    }
}
