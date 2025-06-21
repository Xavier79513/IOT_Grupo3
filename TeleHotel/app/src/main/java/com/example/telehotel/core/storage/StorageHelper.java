// StorageHelper.java - Utilidades adicionales para manejo de storage
package com.example.telehotel.core.storage;

import android.content.Context;
import android.util.Log;

import com.example.telehotel.data.model.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageHelper {
    private PrefsManager prefsManager;
    private Context context;

    public StorageHelper(Context context) {
        this.context = context;
        this.prefsManager = new PrefsManager(context);
    }

    // ============= MÉTODOS DE BÚSQUEDA RÁPIDA =============

    /**
     * Guarda una búsqueda completa con todos los datos
     */
    public void saveCompleteSearch(String location, long startDate, long endDate,
                                   int adults, int children, int rooms) {
        // Guardar fechas
        prefsManager.saveDateRange(startDate, endDate);

        // Guardar string de personas
        String peopleString = buildPeopleString(adults, children, rooms);
        prefsManager.savePeopleString(peopleString);

        // Agregar al historial
        SearchHistory searchHistory = new SearchHistory(location, startDate, endDate,
                adults, children, rooms);
        prefsManager.addSearchHistory(searchHistory);
    }

    /**
     * Construye el string descriptivo de personas y habitaciones
     */
    private String buildPeopleString(int adults, int children, int rooms) {
        StringBuilder sb = new StringBuilder();
        sb.append(adults).append(" adulto");
        if (adults > 1) sb.append("s");

        if (children > 0) {
            sb.append(", ").append(children).append(" niño");
            if (children > 1) sb.append("s");
        }

        sb.append(", ").append(rooms).append(" habitación");
        if (rooms > 1) sb.append("es");

        return sb.toString();
    }

    /**
     * Obtiene las búsquedas recientes (últimos 7 días)
     */
    public List<SearchHistory> getRecentSearches() {
        List<SearchHistory> allSearches = prefsManager.getSearchHistory();
        List<SearchHistory> recentSearches = new ArrayList<>();

        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        for (SearchHistory search : allSearches) {
            if (search.getTimestamp() > sevenDaysAgo) {
                recentSearches.add(search);
            }
        }

        return recentSearches;
    }

    // ============= MÉTODOS DE RESERVAS =============

    /**
     * Crea una nueva reserva con todos los datos actuales
     */
    public ReservationInfo createReservationFromCurrentData() {
        ReservationInfo reservation = new ReservationInfo();

        // ID único de reserva
        reservation.setReservationId("RES_" + System.currentTimeMillis());

        // Datos del hotel
        reservation.setHotelId(prefsManager.getHotelId());
        reservation.setHotelName(prefsManager.getHotelName());
        reservation.setHotelLocation(prefsManager.getHotelLocation());

        // Datos de la habitación
        reservation.setRoomType(prefsManager.getRoomType());
        reservation.setRoomDescription(prefsManager.getRoomDescription());
        reservation.setRoomNumber(prefsManager.getRoomNumber());
        reservation.setRoomPrice(prefsManager.getRoomPrice());

        // Fechas
        reservation.setStartDate(prefsManager.getStartDate());
        reservation.setEndDate(prefsManager.getEndDate());

        // Totales
        reservation.setTaxes(prefsManager.getTotalTaxes());
        reservation.setTotalPrice(prefsManager.getTotalPrice());

        // Estado inicial
        reservation.setStatus(ReservationInfo.STATUS_PENDING);

        return reservation;
    }

    /**
     * Verifica si hay una reserva activa
     */
    public boolean hasActiveReservation() {
        ReservationInfo reservation = prefsManager.getCurrentReservation();
        return reservation != null && reservation.isActive();
    }

    /**
     * Obtiene información resumida de la reserva actual
     */
    public String getCurrentReservationSummary() {
        ReservationInfo reservation = prefsManager.getCurrentReservation();
        if (reservation == null) return null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String checkIn = sdf.format(new Date(reservation.getStartDate()));
        String checkOut = sdf.format(new Date(reservation.getEndDate()));

        return String.format("%s - %s a %s (%d noches)",
                reservation.getHotelName(),
                checkIn,
                checkOut,
                reservation.getStayDuration());
    }

    // ============= MÉTODOS DE TAXI =============

    /**
     * Verifica si el usuario puede solicitar taxi gratuito
     */
    public boolean canRequestFreeTaxi(double minimumAmount) {
        float totalPrice = prefsManager.getTotalPrice();
        return totalPrice >= minimumAmount;
    }

    /**
     * Crea una solicitud de taxi
     */
    public TaxiRequest createTaxiRequest(String airport) {
        ReservationInfo currentReservation = prefsManager.getCurrentReservation();
        if (currentReservation == null) return null;

        TaxiRequest taxiRequest = new TaxiRequest();
        taxiRequest.setRequestId("TAXI_" + System.currentTimeMillis());
        taxiRequest.setReservationId(currentReservation.getReservationId());
        taxiRequest.setHotelId(currentReservation.getHotelId());
        taxiRequest.setHotelName(currentReservation.getHotelName());
        taxiRequest.setAirport(airport);

        return taxiRequest;
    }

    /**
     * Verifica si hay una solicitud de taxi activa
     */
    public boolean hasActiveTaxiRequest() {
        TaxiRequest taxiRequest = prefsManager.getTaxiRequest();
        return taxiRequest != null && taxiRequest.isActive();
    }

    // ============= MÉTODOS DE CHAT =============

    /**
     * Envía un mensaje de chat
     */
    public void sendChatMessage(String hotelId, String message) {
        String userId = prefsManager.getUserId();
        String userName = prefsManager.getUserName();

        ChatMessage chatMessage = new ChatMessage(userId, userName,
                ChatMessage.ROLE_CLIENT,
                message, hotelId);

        prefsManager.addChatMessage(hotelId, chatMessage);
    }

    /**
     * Obtiene mensajes no leídos para un hotel
     */
    public List<ChatMessage> getUnreadMessages(String hotelId) {
        List<ChatMessage> allMessages = prefsManager.getChatMessages(hotelId);
        List<ChatMessage> unreadMessages = new ArrayList<>();
        String currentUserId = prefsManager.getUserId();

        for (ChatMessage message : allMessages) {
            // Solo contar como no leídos los mensajes que no son del usuario actual
            if (!message.getSenderId().equals(currentUserId) && !message.isRead()) {
                unreadMessages.add(message);
            }
        }

        return unreadMessages;
    }

    /**
     * Marca todos los mensajes como leídos para un hotel
     */
    public void markAllMessagesAsRead(String hotelId) {
        List<ChatMessage> messages = prefsManager.getChatMessages(hotelId);
        boolean hasChanges = false;

        for (ChatMessage message : messages) {
            if (!message.isRead()) {
                message.setRead(true);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            prefsManager.saveChatMessages(hotelId, messages);
        }
    }

    /**
     * Obtiene el número total de mensajes no leídos
     */
    public int getTotalUnreadMessagesCount() {
        // Para obtener el total, necesitaríamos iterar sobre todos los hoteles
        // Por simplicidad, retornamos el count del hotel actual si existe
        String currentHotelId = prefsManager.getHotelId();
        if (currentHotelId != null) {
            return getUnreadMessages(currentHotelId).size();
        }
        return 0;
    }

    // ============= MÉTODOS DE VALIDACIÓN =============

    /**
     * Valida si los datos de búsqueda están completos
     */
    public boolean isSearchDataComplete() {
        return prefsManager.getStartDate() > 0 &&
                prefsManager.getEndDate() > 0 &&
                prefsManager.getPeopleString() != null;
    }

    /**
     * Valida si los datos del hotel están completos
     */
    public boolean isHotelDataComplete() {
        return prefsManager.getHotelId() != null &&
                prefsManager.getHotelName() != null;
    }

    /**
     * Valida si los datos de la habitación están completos
     */
    public boolean isRoomDataComplete() {
        return prefsManager.getRoomType() != null &&
                prefsManager.getRoomPrice() > 0;
    }

    /**
     * Valida si el usuario está autenticado
     */
    public boolean isUserAuthenticated() {
        return prefsManager.isUserLoggedIn() &&
                prefsManager.getUserId() != null &&
                prefsManager.getUserToken() != null;
    }

    // ============= MÉTODOS DE LIMPIEZA =============

    /**
     * Limpia datos temporales de búsqueda manteniendo el historial
     */
    public void clearTemporarySearchData() {
        prefsManager.clearSearchData();
    }

    /**
     * Limpia datos específicos de la sesión actual
     */
    public void clearCurrentSessionData() {
        prefsManager.clearCurrentReservation();
        prefsManager.clearTaxiRequest();
        prefsManager.clearSearchData();
    }

    /**
     * Limpia todo excepto datos de usuario y favoritos
     */
    public void clearAllExceptUserData() {
        prefsManager.clearCurrentReservation();
        prefsManager.clearTaxiRequest();
        prefsManager.clearSearchData();
        prefsManager.clearSearchHistory();
        // No limpiamos favoritos ni datos de usuario
    }

    // ============= MÉTODOS DE EXPORTACIÓN/IMPORTACIÓN =============

    /**
     * Exporta datos de búsqueda como string JSON
     */
    public String exportSearchData() {
        SearchDataExport export = new SearchDataExport();
        export.startDate = prefsManager.getStartDate();
        export.endDate = prefsManager.getEndDate();
        export.peopleString = prefsManager.getPeopleString();
        export.hotelId = prefsManager.getHotelId();
        export.hotelName = prefsManager.getHotelName();
        export.hotelLocation = prefsManager.getHotelLocation();

        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.toJson(export);
    }

    /**
     * Importa datos de búsqueda desde string JSON
     */
    public boolean importSearchData(String jsonData) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            SearchDataExport export = gson.fromJson(jsonData, SearchDataExport.class);

            prefsManager.saveDateRange(export.startDate, export.endDate);
            prefsManager.savePeopleString(export.peopleString);
            prefsManager.saveHotel(export.hotelId, export.hotelName, export.hotelLocation);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============= MÉTODOS DE ESTADÍSTICAS =============

    /**
     * Obtiene estadísticas del usuario
     */
    public UserStats getUserStats() {
        UserStats stats = new UserStats();

        // Hoteles favoritos
        stats.favoriteHotelsCount = prefsManager.getFavoriteHotels().size();

        // Búsquedas realizadas
        stats.totalSearches = prefsManager.getSearchHistory().size();

        // Búsquedas recientes (últimos 7 días)
        stats.recentSearches = getRecentSearches().size();

        // Reserva actual
        stats.hasActiveReservation = hasActiveReservation();

        // Solicitud de taxi activa
        stats.hasActiveTaxiRequest = hasActiveTaxiRequest();

        return stats;
    }

    // ============= CLASES AUXILIARES =============

    private static class SearchDataExport {
        public long startDate;
        public long endDate;
        public String peopleString;
        public String hotelId;
        public String hotelName;
        public String hotelLocation;
    }

    public static class UserStats {
        public int favoriteHotelsCount;
        public int totalSearches;
        public int recentSearches;
        public boolean hasActiveReservation;
        public boolean hasActiveTaxiRequest;

        @Override
        public String toString() {
            return "UserStats{" +
                    "favoriteHotelsCount=" + favoriteHotelsCount +
                    ", totalSearches=" + totalSearches +
                    ", recentSearches=" + recentSearches +
                    ", hasActiveReservation=" + hasActiveReservation +
                    ", hasActiveTaxiRequest=" + hasActiveTaxiRequest +
                    '}';
        }
    }

    // ============= MÉTODOS DE UTILIDAD PARA FECHAS =============

    /**
     * Formatea una fecha en formato legible
     */
    public String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Formatea una fecha con hora
     */
    public String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Calcula la diferencia en días entre dos fechas
     */
    public int calculateDaysDifference(long startDate, long endDate) {
        return (int) ((endDate - startDate) / (1000 * 60 * 60 * 24));
    }


    /**
     * Verifica si una fecha está en el futuro
     */
    public boolean isFutureDate(long timestamp) {
        return timestamp > System.currentTimeMillis();
    }

    /**
     * Verifica si una fecha está en el pasado
     */
    public boolean isPastDate(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }
}