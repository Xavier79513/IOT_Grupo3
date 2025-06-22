// PrefsManager.java - Clase principal de gestión de preferencias
package com.example.telehotel.core.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.example.telehotel.data.model.*;

public class PrefsManager {
    private static final String PREFS_NAME = "hotel_search_prefs";
    private static final String USER_PREFS = "user_prefs";

    // Claves para búsqueda y reservas básicas
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

    // Claves para usuario y sesión
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Claves para datos adicionales
    private static final String KEY_RECENT_HOTELS = "recent_hotels";
    private static final String KEY_FAVORITE_HOTELS = "favorite_hotels";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_CURRENT_RESERVATION = "current_reservation";
    private static final String KEY_TAXI_REQUEST = "taxi_request";
    private static final String KEY_CHAT_MESSAGES = "chat_messages_";

    private SharedPreferences prefs;
    private SharedPreferences userPrefs;
    private Gson gson;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userPrefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // ============= MÉTODOS DE BÚSQUEDA Y RESERVAS BÁSICAS =============
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
        prefs.edit().putString(KEY_PEOPLE, people).apply();
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

    // ============= GESTIÓN DE SESIÓN DE USUARIO =============
    public void saveUserSession(String userId, String token, String role, String email, String name) {
        userPrefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_TOKEN, token)
                .putString(KEY_USER_ROLE, role)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public void clearUserSession() {
        userPrefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_TOKEN)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public boolean isUserLoggedIn() {
        return userPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return userPrefs.getString(KEY_USER_ID, null);
    }

    public String getUserToken() {
        return userPrefs.getString(KEY_USER_TOKEN, null);
    }

    public String getUserRole() {
        return userPrefs.getString(KEY_USER_ROLE, "CLIENTE");
    }

    public String getUserEmail() {
        return userPrefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return userPrefs.getString(KEY_USER_NAME, null);
    }

    // ============= GESTIÓN DE HOTELES RECIENTES Y FAVORITOS =============
    public void addRecentHotel(HotelInfo hotel) {
        List<HotelInfo> recentHotels = getRecentHotels();
        recentHotels.removeIf(h -> h.getId().equals(hotel.getId()));
        recentHotels.add(0, hotel);
        if (recentHotels.size() > 10) {
            recentHotels = recentHotels.subList(0, 10);
        }
        String json = gson.toJson(recentHotels);
        prefs.edit().putString(KEY_RECENT_HOTELS, json).apply();
    }

    public List<HotelInfo> getRecentHotels() {
        String json = prefs.getString(KEY_RECENT_HOTELS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<HotelInfo>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addFavoriteHotel(HotelInfo hotel) {
        List<HotelInfo> favorites = getFavoriteHotels();
        if (!favorites.contains(hotel)) {
            favorites.add(hotel);
            String json = gson.toJson(favorites);
            prefs.edit().putString(KEY_FAVORITE_HOTELS, json).apply();
        }
    }

    public void removeFavoriteHotel(String hotelId) {
        List<HotelInfo> favorites = getFavoriteHotels();
        favorites.removeIf(hotel -> hotel.getId().equals(hotelId));
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITE_HOTELS, json).apply();
    }

    public List<HotelInfo> getFavoriteHotels() {
        String json = prefs.getString(KEY_FAVORITE_HOTELS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<HotelInfo>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isHotelFavorite(String hotelId) {
        return getFavoriteHotels().stream().anyMatch(hotel -> hotel.getId().equals(hotelId));
    }

    // ============= HISTORIAL DE BÚSQUEDAS =============
    public void addSearchHistory(SearchHistory search) {
        List<SearchHistory> history = getSearchHistory();
        history.removeIf(s -> s.getLocation().equals(search.getLocation()) &&
                s.getStartDate() == search.getStartDate() &&
                s.getEndDate() == search.getEndDate());
        history.add(0, search);
        if (history.size() > 20) {
            history = history.subList(0, 20);
        }
        String json = gson.toJson(history);
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply();
    }

    public List<SearchHistory> getSearchHistory() {
        String json = prefs.getString(KEY_SEARCH_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<SearchHistory>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply();
    }

    // ============= GESTIÓN DE RESERVA ACTUAL =============
    public void saveCurrentReservation(ReservationInfo reservation) {
        String json = gson.toJson(reservation);
        prefs.edit().putString(KEY_CURRENT_RESERVATION, json).apply();
    }

    public ReservationInfo getCurrentReservation() {
        String json = prefs.getString(KEY_CURRENT_RESERVATION, null);
        if (json == null) return null;
        return gson.fromJson(json, ReservationInfo.class);
    }

    public void clearCurrentReservation() {
        prefs.edit().remove(KEY_CURRENT_RESERVATION).apply();
    }

    // ============= GESTIÓN DE SOLICITUDES DE TAXI =============
    public void saveTaxiRequest(TaxiRequest taxiRequest) {
        String json = gson.toJson(taxiRequest);
        prefs.edit().putString(KEY_TAXI_REQUEST, json).apply();
    }

    public TaxiRequest getTaxiRequest() {
        String json = prefs.getString(KEY_TAXI_REQUEST, null);
        if (json == null) return null;
        return gson.fromJson(json, TaxiRequest.class);
    }

    public void clearTaxiRequest() {
        prefs.edit().remove(KEY_TAXI_REQUEST).apply();
    }

    // ============= GESTIÓN DE MENSAJES DE CHAT =============
    public void saveChatMessages(String hotelId, List<ChatMessage> messages) {
        String json = gson.toJson(messages);
        prefs.edit().putString(KEY_CHAT_MESSAGES + hotelId, json).apply();
    }

    public List<ChatMessage> getChatMessages(String hotelId) {
        String json = prefs.getString(KEY_CHAT_MESSAGES + hotelId, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ChatMessage>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addChatMessage(String hotelId, ChatMessage message) {
        List<ChatMessage> messages = getChatMessages(hotelId);
        messages.add(message);
        saveChatMessages(hotelId, messages);
    }

    // ============= UTILIDADES =============
    public void clearAllData() {
        prefs.edit().clear().apply();
        userPrefs.edit().clear().apply();
    }

    public void clearSearchData() {
        prefs.edit()
                .remove(KEY_START_DATE)
                .remove(KEY_END_DATE)
                .remove(KEY_PEOPLE)
                .remove(KEY_HOTEL_ID)
                .remove(KEY_HOTEL_NAME)
                .remove(KEY_HOTEL_LOCATION)
                .remove(KEY_ROOM_TYPE)
                .remove(KEY_ROOM_DESCRIPTION)
                .remove(KEY_ROOM_NUMBER)
                .remove(KEY_ROOM_PRICE)
                .remove(KEY_ROOM_QUANTITY)
                .remove(KEY_TOTAL_DAYS)
                .remove(KEY_TOTAL_TAXES)
                .remove(KEY_TOTAL_PRICE)
                .apply();
    }
    // Claves adicionales
    private static final String KEY_LOCATION = "search_location";

    public void saveLocation(String location) {
        prefs.edit().putString(KEY_LOCATION, location).apply();
    }

    public String getLocation() {
        return prefs.getString(KEY_LOCATION, null);
    }

    public void saveStartDate(long startDate) {
        prefs.edit().putLong(KEY_START_DATE, startDate).apply();
    }

    public void saveEndDate(long endDate) {
        prefs.edit().putLong(KEY_END_DATE, endDate).apply();
    }
}