package com.example.shoppingapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREF_NAME = "SearchHistory";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 10;

    private final SharedPreferences prefs;

    public SearchHistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addQuery(String query) {
        if (query == null || query.trim().isEmpty()) return;
        query = query.trim();
        List<String> history = getHistory();
        history.remove(query); // Remove if exists
        history.add(0, query); // Add to top
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }
        prefs.edit().putString(KEY_HISTORY, String.join("|||", history)).apply();
    }

    public List<String> getHistory() {
        String saved = prefs.getString(KEY_HISTORY, "");
        if (saved.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(saved.split("\\|\\|\\|")));
    }

    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }
}
