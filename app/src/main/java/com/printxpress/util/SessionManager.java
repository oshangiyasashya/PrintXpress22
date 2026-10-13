package com.printxpress.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Hardened Session Manager.
 * Stores user session metadata, active role, timestamps, and session expiration.
 */
public class SessionManager {

    private static final String PREF_NAME = "PrintXpressSession";
    private static final String KEY_USER_ID = "logged_in_user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ROLE = "logged_in_role";
    private static final String KEY_USER_EMAIL = "logged_in_email";
    private static final String KEY_USER_NAME = "logged_in_name";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    // 30 days session lifetime in milliseconds
    private static final long SESSION_LIFETIME_MS = 30L * 24 * 60 * 60 * 1000;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createLoginSession(long userId) {
        createLoginSession(userId, "CUSTOMER", "", "");
    }

    public void createLoginSession(long userId, String role, String email, String name) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role != null ? role : "CUSTOMER")
                .putString(KEY_USER_EMAIL, email != null ? email : "")
                .putString(KEY_USER_NAME, name != null ? name : "")
                .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public boolean isLoggedIn() {
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }
        if (isSessionExpired()) {
            logout();
            return false;
        }
        return true;
    }

    public boolean isSessionExpired() {
        long loginTime = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L);
        if (loginTime == 0L) return false;
        return (System.currentTimeMillis() - loginTime) > SESSION_LIFETIME_MS;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "CUSTOMER");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public boolean isAdminOrStaff() {
        String role = getRole();
        return "SUPER_ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean("is_first_launch", true);
    }

    public void setFirstLaunchCompleted() {
        prefs.edit().putBoolean("is_first_launch", false).apply();
    }

    public void saveRememberedEmail(String email) {
        prefs.edit().putString(KEY_REMEMBERED_EMAIL, email != null ? email : "").apply();
    }

    public String getRememberedEmail() {
        return prefs.getString(KEY_REMEMBERED_EMAIL, "");
    }

    public void logout() {
        String remembered = getRememberedEmail();
        prefs.edit().clear().apply();
        if (!remembered.isEmpty()) {
            saveRememberedEmail(remembered);
        }
    }
}