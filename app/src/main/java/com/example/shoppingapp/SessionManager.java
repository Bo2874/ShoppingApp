package com.example.shoppingapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "ShoppingAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(int userId, String username, String fullName, String email, String phone) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email != null ? email : "");
        editor.putString(KEY_PHONE, phone != null ? phone : "");
        editor.apply();
    }

    public boolean isLoggedIn() { return prefs.getBoolean(KEY_IS_LOGGED_IN, false); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getUserFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getPhone() { return prefs.getString(KEY_PHONE, ""); }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
