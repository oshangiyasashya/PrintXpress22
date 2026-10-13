package com.printxpress.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.printxpress.data.db.AppDatabase;
import com.printxpress.data.db.UserDao;
import com.printxpress.data.db.UserSettingsDao;
import com.printxpress.data.entity.User;
import com.printxpress.data.entity.UserSettings;
import com.printxpress.util.PasswordUtil;
import java.util.List;

public class UserRepository {
    private final UserDao userDao;
    private final UserSettingsDao userSettingsDao;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        userSettingsDao = db.userSettingsDao();
    }

    public void registerUser(User user, AuthCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                user.id = userDao.insert(user);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError("Email or phone number already registered.");
            }
        });
    }

    public void authenticateUser(String email, String plainPassword, AuthCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = userDao.getUserByEmailSync(email);
                if (user == null) {
                    callback.onError("No account found with this email or phone.");
                    return;
                }

                if (user.isBlocked) {
                    callback.onError("Account has been blocked. Please contact support.");
                    return;
                }

                long now = System.currentTimeMillis();
                if (now < user.lockoutUntil) {
                    long secondsLeft = (user.lockoutUntil - now) / 1000;
                    if (secondsLeft > 60) {
                        long mins = (secondsLeft + 59) / 60;
                        callback.onError("Account temporarily locked. Try again in " + mins + " minute(s).");
                    } else {
                        callback.onError("Account temporarily locked. Try again in " + secondsLeft + "s.");
                    }
                    return;
                }

                if (PasswordUtil.verifyPassword(plainPassword, user.salt, user.passwordHash)) {
                    // Reset failed attempts on successful login
                    userDao.updateLoginAttempts(user.email, 0, 0L);

                    // Upgrade legacy SHA-256 hash to PBKDF2 automatically
                    if (PasswordUtil.isLegacyHash(user.passwordHash)) {
                        String newSalt = PasswordUtil.generateSalt();
                        String newHash = PasswordUtil.hashPassword(plainPassword, newSalt);
                        userDao.updatePasswordById(user.id, newHash, newSalt, "PBKDF2");
                        user.salt = newSalt;
                        user.passwordHash = newHash;
                        user.hashAlgorithm = "PBKDF2";
                    }

                    callback.onSuccess(user);
                } else {
                    int newAttempts = user.failedLoginAttempts + 1;
                    long lockoutMs = 0L;
                    String message;

                    if (newAttempts >= 10) {
                        lockoutMs = 24L * 60 * 60 * 1000; // 24 hours
                        message = "Account locked for 24 hours due to 10 failed login attempts.";
                    } else if (newAttempts >= 7) {
                        lockoutMs = 15L * 60 * 1000; // 15 mins
                        message = "Account locked for 15 minutes due to multiple failed attempts.";
                    } else if (newAttempts >= 5) {
                        lockoutMs = 5L * 60 * 1000; // 5 mins
                        message = "Account locked for 5 minutes due to 5 failed attempts.";
                    } else {
                        message = "Invalid password (" + newAttempts + "/5 attempts).";
                    }

                    userDao.updateLoginAttempts(user.email, newAttempts, lockoutMs > 0 ? (now + lockoutMs) : 0L);
                    callback.onError(message);
                }
            } catch (Exception e) {
                callback.onError("Authentication error: " + e.getMessage());
            }
        });
    }

    public void resetPassword(String email, String newPassword, AuthCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = userDao.getUserByEmailSync(email);
                if (user != null) {
                    String salt = PasswordUtil.generateSalt();
                    String hash = PasswordUtil.hashPassword(newPassword, salt);
                    int rows = userDao.updatePasswordByEmail(email, hash, salt);
                    if (rows > 0) {
                        user.passwordHash = hash;
                        user.salt = salt;
                        user.hashAlgorithm = "PBKDF2";
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Failed to update password.");
                    }
                } else {
                    callback.onError("Email address not found.");
                }
            } catch (Exception e) {
                callback.onError("Error resetting password: " + e.getMessage());
            }
        });
    }

    public void updateProfile(User user, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                userDao.update(user);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void softDeleteUser(long userId, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                userDao.softDeleteUser(userId);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void setBlockedStatus(long userId, boolean blocked, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                userDao.updateUserBlockedStatus(userId, blocked);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public LiveData<User> getUserById(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public LiveData<UserSettings> getUserSettings(long userId) {
        return userSettingsDao.getSettings(userId);
    }

    public void saveUserSettings(UserSettings settings, ActionCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                userSettingsDao.insertOrUpdate(settings);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}
