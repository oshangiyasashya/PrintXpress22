package com.printxpress.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.User;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.PasswordUtil;
import com.printxpress.util.SessionManager;
import com.printxpress.util.ValidationUtil;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<User> authSuccessUser = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<User> getAuthSuccessUser() {
        return authSuccessUser;
    }

    public void login(String emailOrPhone, String password) {
        if (!ValidationUtil.isValidEmail(emailOrPhone) && !ValidationUtil.isValidPhone(emailOrPhone)) {
            errorMessage.setValue("Please enter a valid email or Sri Lankan phone number (07XXXXXXXX).");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Please enter your password.");
            return;
        }

        isLoading.setValue(true);
        userRepository.authenticateUser(emailOrPhone.trim(), password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                sessionManager.createLoginSession(user.id, user.role, user.email, user.fullName);
                authSuccessUser.postValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void register(String name, String email, String phone, String password, String confirmPassword) {
        String cleanName = ValidationUtil.sanitize(name);
        if (cleanName.isEmpty() || cleanName.length() < 2) {
            errorMessage.setValue("Please enter your full name.");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errorMessage.setValue("Please enter a valid email address.");
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            errorMessage.setValue("Please enter a valid Sri Lankan mobile number (e.g. 0771234567).");
            return;
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            errorMessage.setValue("Password must be at least 6 characters with both letters and numbers.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match.");
            return;
        }

        isLoading.setValue(true);
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        User newUser = new User(cleanName, email.trim().toLowerCase(), phone.trim(), hash, salt, System.currentTimeMillis());

        userRepository.registerUser(newUser, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                sessionManager.createLoginSession(user.id, user.role, user.email, user.fullName);
                authSuccessUser.postValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void resetPassword(String email, String newPassword, String confirmPassword) {
        if (!ValidationUtil.isValidEmail(email)) {
            errorMessage.setValue("Please enter a valid email address.");
            return;
        }
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            errorMessage.setValue("New password must be at least 6 characters with letters and numbers.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match.");
            return;
        }

        isLoading.setValue(true);
        userRepository.resetPassword(email.trim().toLowerCase(), newPassword, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                errorMessage.postValue("Password reset successful! Please log in.");
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}
