package com.printxpress.ui.catalog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.data.repository.WishlistRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository cartRepository;
    private final CommonRepository commonRepository;
    private final WishlistRepository wishlistRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    private final MutableLiveData<Promotion> appliedPromotion = new MutableLiveData<>(null);

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartRepository = new CartRepository(application);
        commonRepository = new CommonRepository(application);
        wishlistRepository = new WishlistRepository(application);
        sessionManager = new SessionManager(application);
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<Promotion> getAppliedPromotion() {
        return appliedPromotion;
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartRepository.getCartItemsForUser(sessionManager.getUserId());
    }

    public LiveData<Integer> getCartCount() {
        return cartRepository.getCartCount(sessionManager.getUserId());
    }

    public void updateQuantity(CartItem item, int newQty) {
        if (newQty <= 0) return;
        item.quantity = newQty;
        cartRepository.update(item, null);
    }

    public void deleteItem(CartItem item) {
        cartRepository.delete(item, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Item removed from cart");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error removing item: " + error);
            }
        });
    }

    public void clearCart() {
        cartRepository.clearCart(sessionManager.getUserId(), new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Cart cleared");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error clearing cart: " + error);
            }
        });
    }

    public void moveToWishlist(CartItem item) {
        long userId = sessionManager.getUserId();
        wishlistRepository.toggleWishlist(userId, item.productId, new WishlistRepository.ActionCallback() {
            @Override
            public void onSuccess(boolean isNowWishlisted) {
                cartRepository.delete(item, null);
                actionMessage.postValue("Saved to Wishlist!");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error saving to wishlist: " + error);
            }
        });
    }

    public void applyPromoCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            appliedPromotion.setValue(null);
            actionMessage.setValue("Promo code cleared.");
            return;
        }

        commonRepository.getPromotionByTitle(code.trim(), new CommonRepository.PromoCallback() {
            @Override
            public void onFound(Promotion promo) {
                long now = System.currentTimeMillis();
                if (now >= promo.validFrom && now <= promo.validTo) {
                    appliedPromotion.postValue(promo);
                    actionMessage.postValue("Promo code applied: " + (int) promo.discountPercent + "% OFF! 🎉");
                } else {
                    actionMessage.postValue("This promo code has expired.");
                }
            }

            @Override
            public void onNotFound() {
                actionMessage.postValue("Invalid promo code.");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Error validating promo code: " + error);
            }
        });
    }
}
