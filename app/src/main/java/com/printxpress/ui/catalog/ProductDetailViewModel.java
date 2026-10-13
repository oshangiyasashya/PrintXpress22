package com.printxpress.ui.catalog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.ProductOption;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.data.repository.CatalogRepository;
import com.printxpress.data.repository.WishlistRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class ProductDetailViewModel extends AndroidViewModel {

    private final CatalogRepository catalogRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartAddSuccess = new MutableLiveData<>();

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
        catalogRepository = new CatalogRepository(application);
        cartRepository = new CartRepository(application);
        wishlistRepository = new WishlistRepository(application);
        sessionManager = new SessionManager(application);
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<Boolean> getCartAddSuccess() {
        return cartAddSuccess;
    }

    public LiveData<Product> getProduct(long productId) {
        return catalogRepository.getProductById(productId);
    }

    public LiveData<List<ProductOption>> getProductOptions(long productId) {
        return catalogRepository.getOptionsByProduct(productId);
    }

    public LiveData<Boolean> isWishlisted(long productId) {
        return wishlistRepository.isWishlisted(sessionManager.getUserId(), productId);
    }

    public void toggleWishlist(long productId) {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            actionMessage.setValue("Please log in to add to wishlist.");
            return;
        }
        wishlistRepository.toggleWishlist(userId, productId, new WishlistRepository.ActionCallback() {
            @Override
            public void onSuccess(boolean isNowWishlisted) {
                actionMessage.postValue(isNowWishlisted ? "Added to Wishlist ❤️" : "Removed from Wishlist");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Wishlist error: " + error);
            }
        });
    }

    public void addToCart(Product product, int quantity, String selectedOptionsText, double optionPriceDelta) {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            actionMessage.setValue("Please log in to add to cart.");
            return;
        }

        double unitPrice = product.basePrice + optionPriceDelta;
        String specs = selectedOptionsText.isEmpty()
                ? (product.material + " | " + product.sizes)
                : selectedOptionsText;

        CartItem item = new CartItem(
                userId,
                product.id,
                product.name,
                unitPrice,
                Math.max(1, quantity),
                specs,
                "custom",
                "",
                product.name + " (" + specs + ")"
        );

        cartRepository.insert(item, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                cartAddSuccess.postValue(true);
                actionMessage.postValue("Added to Cart!");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Failed to add to cart: " + error);
            }
        });
    }
}
