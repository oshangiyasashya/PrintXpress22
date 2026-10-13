package com.printxpress.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Product;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.data.repository.WishlistRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class WishlistViewModel extends AndroidViewModel {

    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public WishlistViewModel(@NonNull Application application) {
        super(application);
        wishlistRepository = new WishlistRepository(application);
        cartRepository = new CartRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<List<Product>> getWishlistProducts() {
        return wishlistRepository.getWishlistProducts(sessionManager.getUserId());
    }

    public void removeFromWishlist(long productId) {
        wishlistRepository.toggleWishlist(sessionManager.getUserId(), productId, new WishlistRepository.ActionCallback() {
            @Override
            public void onSuccess(boolean isNowWishlisted) {
                actionMessage.postValue("Removed from Wishlist");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void moveToCart(Product product) {
        long userId = sessionManager.getUserId();
        CartItem item = new CartItem(
                userId,
                product.id,
                product.name,
                product.basePrice,
                100,
                product.material + " | " + product.sizes,
                "wishlist",
                "",
                product.name
        );
        cartRepository.insert(item, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                wishlistRepository.toggleWishlist(userId, product.id, null);
                actionMessage.postValue("Moved to Cart! 🛒");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Could not move to cart: " + error);
            }
        });
    }
}
