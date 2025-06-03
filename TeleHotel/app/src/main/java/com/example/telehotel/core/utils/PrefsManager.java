package com.example.telehotel.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "hotel_search_prefs";

    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_PEOPLE = "people_string";
    private static final String KEY_HOTEL_ID = "hotel_id";
    private static final String KEY_HOTEL_NAME = "hotel_name";
    private static final String KEY_HOTEL_LOCATION = "hotel_location";

    private static final String KEY_ROOM_TYPE = "room_type";
    private static final String KEY_ROOM_DESCRIPTION = "room_description";
    private static final String KEY_ROOM_NUMBER = "room_number";
    private static final String KEY_ROOM_PRICE = "room_price";
    private static final String KEY_ROOM_QUANTITY = "room_quantity";

    private static final String KEY_TOTAL_DAYS = "total_days";
    private static final String KEY_TOTAL_TAXES = "total_taxes";
    private static final String KEY_TOTAL_PRICE = "total_price";
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

    public void saveHotel(String id, String name, String location) {
        prefs.edit()
                .putString(KEY_HOTEL_ID, id)
                .putString(KEY_HOTEL_NAME, name)
                .putString(KEY_HOTEL_LOCATION, location)
                .apply();
    }

    public String getHotelId() {
        return prefs.getString(KEY_HOTEL_ID, null);
    }

    public String getHotelName() {
        return prefs.getString(KEY_HOTEL_NAME, null);
    }

    public String getHotelLocation() {
        return prefs.getString(KEY_HOTEL_LOCATION, null);
    }

    // Guardar habitación
    public void saveRoom(String type, String description, String number, double price, int quantity) {
        prefs.edit()
                .putString(KEY_ROOM_TYPE, type)
                .putString(KEY_ROOM_DESCRIPTION, description)
                .putString(KEY_ROOM_NUMBER, number)
                .putFloat(KEY_ROOM_PRICE, (float) price)
                .putInt(KEY_ROOM_QUANTITY, quantity)
                .apply();
    }

    public String getRoomType() {
        return prefs.getString(KEY_ROOM_TYPE, null);
    }

    public String getRoomDescription() {
        return prefs.getString(KEY_ROOM_DESCRIPTION, null);
    }

    public String getRoomNumber() {
        return prefs.getString(KEY_ROOM_NUMBER, null);
    }

    public float getRoomPrice() {
        return prefs.getFloat(KEY_ROOM_PRICE, 0);
    }

    public int getRoomQuantity() {
        return prefs.getInt(KEY_ROOM_QUANTITY, 1);
    }

    // Guardar totales
    public void saveTotals(int totalDays, double taxes, double totalPrice) {
        prefs.edit()
                .putInt(KEY_TOTAL_DAYS, totalDays)
                .putFloat(KEY_TOTAL_TAXES, (float) taxes)
                .putFloat(KEY_TOTAL_PRICE, (float) totalPrice)
                .apply();
    }

    public int getTotalDays() {
        return prefs.getInt(KEY_TOTAL_DAYS, 0);
    }

    public float getTotalTaxes() {
        return prefs.getFloat(KEY_TOTAL_TAXES, 0);
    }

    public float getTotalPrice() {
        return prefs.getFloat(KEY_TOTAL_PRICE, 0);
    }
}
