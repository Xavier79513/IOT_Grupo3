package com.example.telehotel.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "hotel_search_prefs";

    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_PEOPLE = "people_string";

    private SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveDateRange(long startDate, long endDate) {
        prefs.edit()
                .putLong(KEY_START_DATE, startDate)
                .putLong(KEY_END_DATE, endDate)
                .apply();
    }

    public long getStartDate() {
        return prefs.getLong(KEY_START_DATE, 0);
    }

    public long getEndDate() {
        return prefs.getLong(KEY_END_DATE, 0);
    }

    public void savePeopleString(String people) {
        prefs.edit()
                .putString(KEY_PEOPLE, people)
                .apply();
    }

    public String getPeopleString() {
        return prefs.getString(KEY_PEOPLE, null);
    }
}
