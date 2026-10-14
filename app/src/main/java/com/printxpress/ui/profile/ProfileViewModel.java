package com.printxpress.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.User;
import com.printxpress.data.entity.UserSettings;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.PasswordUtil;
import com.printxpress.util.SessionManager;
import com.printxpress.util.ValidationUtil;

import com.printxpress.data.repository.PaymentMethodRepository;
import com.printxpress.data.entity.PaymentMethod;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutTrigger = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        paymentMethodRepository = new PaymentMethodRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<Boolean> getLogoutTrigger() {
        return logoutTrigger;
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getUserById(sessionManager.getUserId());
    }

    public LiveData<UserSettings> getUserSettings() {
        return userRepository.getUserSettings(sessionManager.getUserId());
    }

    public void saveUserSettings(UserSettings settings) {
        userRepository.saveUserSettings(settings, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Settings saved");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to save settings: " + message);
            }
        });
    }

    public void updateProfile(User user, String newName, String newPhone) {
        String cleanName = ValidationUtil.sanitize(newName);
        if (cleanName.length() < 2) {
            actionMessage.setValue("Name cannot be empty.");
            return;
        }
        if (!ValidationUtil.isValidPhone(newPhone)) {
            actionMessage.setValue("Invalid Sri Lankan phone number (07XXXXXXXX).");
            return;
        }

        user.fullName = cleanName;
        user.phone = newPhone.trim();
        userRepository.updateProfile(user, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Profile updated successfully!");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Error: " + message);
            }
        });
    }

    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword) {
        if (!PasswordUtil.verifyPassword(currentPassword, user.salt, user.passwordHash)) {
            actionMessage.setValue("Current password is incorrect.");
            return;
        }
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            actionMessage.setValue("New password must be at least 6 characters with letters and numbers.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            actionMessage.setValue("New passwords do not match.");
            return;
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        user.salt = newSalt;
        user.passwordHash = newHash;
        user.hashAlgorithm = "PBKDF2";

        userRepository.updateProfile(user, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Password changed successfully!");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Error changing password: " + message);
            }
        });
    }

    public void updateAvatarPath(User user, String absolutePath) {
        user.profileImageUri = absolutePath;
        userRepository.updateProfile(user, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Profile photo updated!");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to update photo: " + message);
            }
        });
    }

    public void removeAvatar(User user) {
        user.profileImageUri = "";
        userRepository.updateProfile(user, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Profile photo removed!");
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to remove photo: " + message);
            }
        });
    }

    public void deleteAccount(long userId) {
        userRepository.softDeleteUser(userId, new UserRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                sessionManager.logout();
                logoutTrigger.postValue(true);
            }

            @Override
            public void onError(String message) {
                actionMessage.postValue("Failed to delete account: " + message);
            }
        });
    }

    public LiveData<List<PaymentMethod>> getPaymentMethods() {
        return paymentMethodRepository.getPaymentMethodsByUser(sessionManager.getUserId());
    }

    public void setDefaultPayment(long cardId) {
        paymentMethodRepository.setDefaultPayment(sessionManager.getUserId(), cardId, new PaymentMethodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Set as default card");
            }
            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void deletePayment(PaymentMethod card) {
        paymentMethodRepository.delete(card, new PaymentMethodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Card deleted");
            }
            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void addPayment(PaymentMethod card) {
        paymentMethodRepository.insert(card, new PaymentMethodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.postValue("Card added successfully!");
            }
            @Override
            public void onError(String error) {
                actionMessage.postValue("Error: " + error);
            }
        });
    }

    public void logout() {
        sessionManager.logout();
        logoutTrigger.setValue(true);
    }
}
