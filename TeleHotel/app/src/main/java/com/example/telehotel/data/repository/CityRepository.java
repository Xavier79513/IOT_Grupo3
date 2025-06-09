package com.example.telehotel.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.telehotel.core.network.NominatimService;
import com.example.telehotel.core.network.NominatimRetrofitClient;
import com.example.telehotel.data.model.NominatimPlace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CityRepository {
    private static final String TAG = "CityRepository";

    public static void searchCities(@NonNull String query,
                                    @NonNull Consumer<List<NominatimPlace>> onSuccess,
                                    @NonNull Consumer<Throwable> onError) {

        // Validar entrada
        if (query.trim().isEmpty()) {
            onSuccess.accept(new ArrayList<>());
            return;
        }

        Log.d(TAG, "Buscando ciudades para: " + query);

        try {
            NominatimService service = NominatimRetrofitClient.getService();

            // Mejorar los parámetros de búsqueda
            Call<List<NominatimPlace>> call = service.searchCities(
                    query.trim(),
                    "json",
                    8,  // Aumentar límite de resultados
                    "city", // Especificar tipos de lugares
                    1, // Addressdetails
                    "es" // Idioma español
            );

            call.enqueue(new Callback<List<NominatimPlace>>() {
                @Override
                public void onResponse(Call<List<NominatimPlace>> call, Response<List<NominatimPlace>> response) {
                    Log.d(TAG, "Respuesta recibida. Código: " + response.code());

                    if (response.isSuccessful()) {
                        List<NominatimPlace> places = response.body();
                        if (places != null) {
                            Log.d(TAG, "Encontrados " + places.size() + " lugares");

                            // Filtrar y procesar resultados
                            List<NominatimPlace> filteredPlaces = filterAndProcessPlaces(places);
                            onSuccess.accept(filteredPlaces);
                        } else {
                            Log.w(TAG, "Respuesta exitosa pero body es null");
                            onSuccess.accept(new ArrayList<>());
                        }
                    } else {
                        // Manejo detallado de errores HTTP
                        String errorMessage = getDetailedErrorMessage(response);
                        Log.e(TAG, "Error HTTP: " + errorMessage);
                        onError.accept(new Exception(errorMessage));
                    }
                }

                @Override
                public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                    Log.e(TAG, "Error de red: " + t.getMessage(), t);

                    // Proporcionar mensaje de error más específico
                    String errorMessage = getNetworkErrorMessage(t);
                    onError.accept(new Exception(errorMessage));
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al crear la llamada: " + e.getMessage(), e);
            onError.accept(new Exception("Error interno del servicio"));
        }
    }

    /**
     * Filtra y procesa los lugares para mejorar la calidad de los resultados
     */
    private static List<NominatimPlace> filterAndProcessPlaces(List<NominatimPlace> places) {
        List<NominatimPlace> filtered = new ArrayList<>();

        for (NominatimPlace place : places) {
            // Validar que el lugar tenga información mínima requerida
            if (isValidPlace(place)) {
                // Mejorar el display name si es necesario
                improveDisplayName(place);
                filtered.add(place);
            }
        }

        return filtered;
    }

    /**
     * Valida que un lugar tenga la información mínima requerida
     */
    private static boolean isValidPlace(NominatimPlace place) {
        return place != null &&
                place.getDisplayName() != null &&
                !place.getDisplayName().trim().isEmpty() &&
                place.getLat() != null &&
                place.getLon() != null;
    }

    /**
     * Mejora el display name del lugar para mejor presentación
     */
    private static void improveDisplayName(NominatimPlace place) {
        String displayName = place.getDisplayName();

        // Simplificar nombres muy largos
        if (displayName.length() > 60) {
            String[] parts = displayName.split(",");
            if (parts.length > 2) {
                // Tomar las primeras 2-3 partes más relevantes
                StringBuilder simplified = new StringBuilder();
                for (int i = 0; i < Math.min(3, parts.length); i++) {
                    if (i > 0) simplified.append(", ");
                    simplified.append(parts[i].trim());
                }
                place.setDisplayName(simplified.toString());
            }
        }
    }

    /**
     * Genera un mensaje de error detallado basado en la respuesta HTTP
     */
    private static String getDetailedErrorMessage(Response<?> response) {
        switch (response.code()) {
            case 400:
                return "Búsqueda inválida. Verifica el término de búsqueda.";
            case 403:
                return "Acceso denegado al servicio de búsqueda.";
            case 429:
                return "Demasiadas búsquedas. Intenta más tarde.";
            case 500:
                return "Error en el servidor de búsqueda.";
            case 503:
                return "Servicio de búsqueda temporalmente no disponible.";
            default:
                return "Error de búsqueda (Código: " + response.code() + ")";
        }
    }

    /**
     * Genera un mensaje de error específico basado en el tipo de error de red
     */
    private static String getNetworkErrorMessage(Throwable t) {
        String message = t.getMessage();

        if (message != null) {
            if (message.contains("timeout")) {
                return "Tiempo de espera agotado. Verifica tu conexión.";
            } else if (message.contains("Unable to resolve host")) {
                return "Sin conexión a internet.";
            } else if (message.contains("SSLException")) {
                return "Error de seguridad en la conexión.";
            }
        }

        return "Error de conexión. Verifica tu internet.";
    }

    /**
     * Método alternativo con fallback a búsqueda local
     */
    public static void searchCitiesWithFallback(@NonNull String query,
                                                @NonNull Consumer<List<NominatimPlace>> onSuccess,
                                                @NonNull Consumer<Throwable> onError) {

        // Intentar búsqueda normal primero
        searchCities(query, onSuccess, (error) -> {
            Log.w(TAG, "Búsqueda principal falló, intentando fallback");

            // Fallback a búsqueda con parámetros simplificados
            searchCitiesSimple(query, onSuccess, onError);
        });
    }

    /**
     * Búsqueda simplificada como fallback
     */
    private static void searchCitiesSimple(@NonNull String query,
                                           @NonNull Consumer<List<NominatimPlace>> onSuccess,
                                           @NonNull Consumer<Throwable> onError) {
        try {
            NominatimService service = NominatimRetrofitClient.getService();
            Call<List<NominatimPlace>> call = service.searchCities(query, "json", 5);

            call.enqueue(new Callback<List<NominatimPlace>>() {
                @Override
                public void onResponse(Call<List<NominatimPlace>> call, Response<List<NominatimPlace>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        onSuccess.accept(response.body());
                    } else {
                        // Si también falla, usar datos locales
                        onSuccess.accept(getLocalCityFallback(query));
                    }
                }

                @Override
                public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                    // Último recurso: datos locales
                    onSuccess.accept(getLocalCityFallback(query));
                }
            });
        } catch (Exception e) {
            onSuccess.accept(getLocalCityFallback(query));
        }
    }

    /**
     * Fallback local con ciudades predefinidas
     */
    private static List<NominatimPlace> getLocalCityFallback(String query) {
        List<NominatimPlace> localCities = new ArrayList<>();
        String queryLower = query.toLowerCase();

        // Lista de ciudades populares como fallback
        String[][] cities = {
                {"Lima", "Lima, Perú", "-12.0464", "-77.0428"},
                {"Cusco", "Cusco, Perú", "-13.5319", "-71.9675"},
                {"Arequipa", "Arequipa, Perú", "-16.4040", "-71.5440"},
                {"Trujillo", "Trujillo, Perú", "-8.1116", "-79.0292"},
                {"Chiclayo", "Chiclayo, Perú", "-6.7714", "-79.8371"},
                {"Piura", "Piura, Perú", "-5.1945", "-80.6328"},
                {"Iquitos", "Iquitos, Perú", "-3.7437", "-73.2516"},
                {"Huancayo", "Huancayo, Perú", "-12.0653", "-75.2049"},
                {"Cajamarca", "Cajamarca, Perú", "-7.1611", "-78.5137"},
                {"Pucallpa", "Pucallpa, Perú", "-8.3791", "-74.5539"}
        };

        for (String[] city : cities) {
            if (city[0].toLowerCase().contains(queryLower) ||
                    city[1].toLowerCase().contains(queryLower)) {

                NominatimPlace place = new NominatimPlace();
                place.setDisplayName(city[1]);
                place.setLat(city[2]);
                place.setLon(city[3]);
                localCities.add(place);
            }
        }

        Log.d(TAG, "Usando fallback local, encontradas " + localCities.size() + " ciudades");
        return localCities;
    }
}