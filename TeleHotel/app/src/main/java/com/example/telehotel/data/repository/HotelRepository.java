package com.example.telehotel.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.AvailabilityStats;
import com.example.telehotel.data.model.Capacidad;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.LocationSearch;
import com.example.telehotel.data.model.Ubicacion;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class HotelRepository {

    private static final String COLLECTION_NAME = "hoteles";

    private static CollectionReference getCollection() {
        return FirebaseUtil.getFirestore().collection(COLLECTION_NAME);
    }

    public static void getAllHotels(@NonNull Consumer<List<Hotel>> onSuccess,
                                    @NonNull Consumer<Exception> onError) {

        getCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId());
                            hoteles.add(hotel);
                        }
                    }
                    onSuccess.accept(hoteles);
                })
                .addOnFailureListener(onError::accept);
    }

    public static void getHotelById(@NonNull String hotelDocumentId,
                                    @NonNull Consumer<Hotel> onSuccess,
                                    @NonNull Consumer<Exception> onError) {

        getCollection()
                .document(hotelDocumentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId());
                            onSuccess.accept(hotel);
                        } else {
                            onError.accept(new Exception("Hotel mapeado como null"));
                        }
                    } else {
                        onError.accept(new Exception("No existe hotel con ID: " + hotelDocumentId));
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    public static void getHotelByIdPorCampo(@NonNull String idHotel,
                                            @NonNull Consumer<Hotel> onSuccess,
                                            @NonNull Consumer<Exception> onError) {
        getCollection()
                .whereEqualTo("id", idHotel)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId()); // Guarda el ID real del documento Firestore
                            onSuccess.accept(hotel);
                        } else {
                            onError.accept(new Exception("Hotel mapeado como null"));
                        }
                    } else {
                        onError.accept(new Exception("No se encontró hotel con idHotel: " + idHotel));
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Obtiene todas las ubicaciones únicas (ciudad, país) de los hoteles
     * @param onSuccess Callback que recibe la lista de ubicaciones únicas
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void getUniqueLocations(@NonNull Consumer<List<String>> onSuccess,
                                          @NonNull Consumer<Exception> onError) {
        getCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> ubicacionesUnicas = new HashSet<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null && hotel.getUbicacion() != null) {
                            Ubicacion ubicacion = hotel.getUbicacion();
                            String ciudad = ubicacion.getCiudad();
                            String pais = ubicacion.getPais();

                            // Solo agregar si tanto ciudad como país no son nulos
                            if (ciudad != null && !ciudad.trim().isEmpty() &&
                                    pais != null && !pais.trim().isEmpty()) {
                                String ubicacionCompleta = ciudad.trim() + ", " + pais.trim();
                                ubicacionesUnicas.add(ubicacionCompleta);
                            }
                        }
                    }

                    // Convertir Set a List y ordenar alfabéticamente
                    List<String> ubicacionesList = new ArrayList<>(ubicacionesUnicas);
                    ubicacionesList.sort(String::compareToIgnoreCase);

                    onSuccess.accept(ubicacionesList);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Obtiene ubicaciones únicas con información detallada (incluyendo contador de hoteles)
     * @param onSuccess Callback que recibe la lista de LocationSearch
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void getUniqueLocationsWithDetails(@NonNull Consumer<List<LocationSearch>> onSuccess,
                                                     @NonNull Consumer<Exception> onError) {
        getCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Usar un Map para contar hoteles por ubicación
                    Map<String, LocationSearch> ubicacionesMap = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null && hotel.getUbicacion() != null) {
                            Ubicacion ubicacion = hotel.getUbicacion();
                            String ciudad = ubicacion.getCiudad();
                            String pais = ubicacion.getPais();

                            // Solo procesar si tanto ciudad como país no son nulos
                            if (ciudad != null && !ciudad.trim().isEmpty() &&
                                    pais != null && !pais.trim().isEmpty()) {

                                String key = ciudad.trim() + "," + pais.trim();

                                if (ubicacionesMap.containsKey(key)) {
                                    ubicacionesMap.get(key).incrementHotelCount();
                                } else {
                                    LocationSearch locationSearch = new LocationSearch(
                                            ciudad.trim(), pais.trim(), 1
                                    );
                                    ubicacionesMap.put(key, locationSearch);
                                }
                            }
                        }
                    }

                    // Convertir Map a List y ordenar por ciudad
                    List<LocationSearch> ubicacionesList = new ArrayList<>(ubicacionesMap.values());
                    ubicacionesList.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));

                    onSuccess.accept(ubicacionesList);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Busca hoteles por ciudad y país
     * @param ciudad La ciudad a buscar
     * @param pais El país a buscar
     * @param onSuccess Callback que recibe la lista de hoteles encontrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsByLocation(@NonNull String ciudad,
                                              @NonNull String pais,
                                              @NonNull Consumer<List<Hotel>> onSuccess,
                                              @NonNull Consumer<Exception> onError) {
        getCollection()
                .whereEqualTo("ubicacion.ciudad", ciudad.trim())
                .whereEqualTo("ubicacion.pais", pais.trim())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId());
                            hoteles.add(hotel);
                        }
                    }
                    onSuccess.accept(hoteles);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Busca hoteles por una ubicación completa (formato: "Ciudad, País")
     * @param ubicacionCompleta La ubicación en formato "Ciudad, País"
     * @param onSuccess Callback que recibe la lista de hoteles encontrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsByLocationString(@NonNull String ubicacionCompleta,
                                                    @NonNull Consumer<List<Hotel>> onSuccess,
                                                    @NonNull Consumer<Exception> onError) {
        String[] partes = ubicacionCompleta.split(",");
        if (partes.length == 2) {
            String ciudad = partes[0].trim();
            String pais = partes[1].trim();
            searchHotelsByLocation(ciudad, pais, onSuccess, onError);
        } else {
            onError.accept(new Exception("Formato de ubicación inválido. Use: 'Ciudad, País'"));
        }
    }

    /**
     * Busca hoteles con filtros avanzados basados en parámetros de búsqueda
     * @param ciudad La ciudad a buscar
     * @param pais El país a buscar
     * @param fechaInicio Fecha de inicio de la estadía (timestamp)
     * @param fechaFin Fecha de fin de la estadía (timestamp)
     * @param adultos Número mínimo de adultos que debe soportar
     * @param ninos Número mínimo de niños que debe soportar
     * @param habitacionesRequeridas Número mínimo de habitaciones requeridas
     * @param onSuccess Callback que recibe la lista de hoteles filtrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsWithFilters(@NonNull String ciudad,
                                               @NonNull String pais,
                                               long fechaInicio,
                                               long fechaFin,
                                               int adultos,
                                               int ninos,
                                               int habitacionesRequeridas,
                                               @NonNull Consumer<List<Hotel>> onSuccess,
                                               @NonNull Consumer<Exception> onError) {

        // Primero buscar hoteles por ubicación
        searchHotelsByLocation(ciudad, pais,
                hotelesEnUbicacion -> {
                    try {
                        // Filtrar hoteles basándose en los criterios
                        List<Hotel> hotelesFiltrados = new ArrayList<>();

                        for (Hotel hotel : hotelesEnUbicacion) {
                            if (isHotelAvailable(hotel, fechaInicio, fechaFin, adultos, ninos, habitacionesRequeridas)) {
                                hotelesFiltrados.add(hotel);
                            }
                        }

                        // Ordenar por capacidad y disponibilidad (opcional)
                        hotelesFiltrados.sort((h1, h2) -> {
                            int score1 = calculateHotelScore(h1, adultos, ninos, habitacionesRequeridas);
                            int score2 = calculateHotelScore(h2, adultos, ninos, habitacionesRequeridas);
                            return Integer.compare(score2, score1); // Orden descendente
                        });

                        onSuccess.accept(hotelesFiltrados);

                    } catch (Exception e) {
                        onError.accept(e);
                    }
                },
                onError
        );
    }

    /**
     * Busca hoteles con filtros usando string de ubicación completa
     * @param ubicacionCompleta La ubicación en formato "Ciudad, País"
     * @param fechaInicio Fecha de inicio de la estadía (timestamp)
     * @param fechaFin Fecha de fin de la estadía (timestamp)
     * @param adultos Número mínimo de adultos que debe soportar
     * @param ninos Número mínimo de niños que debe soportar
     * @param habitacionesRequeridas Número mínimo de habitaciones requeridas
     * @param onSuccess Callback que recibe la lista de hoteles filtrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsWithFiltersString(@NonNull String ubicacionCompleta,
                                                     long fechaInicio,
                                                     long fechaFin,
                                                     int adultos,
                                                     int ninos,
                                                     int habitacionesRequeridas,
                                                     @NonNull Consumer<List<Hotel>> onSuccess,
                                                     @NonNull Consumer<Exception> onError) {

        String[] partes = ubicacionCompleta.split(",");
        if (partes.length == 2) {
            String ciudad = partes[0].trim();
            String pais = partes[1].trim();
            searchHotelsWithFilters(ciudad, pais, fechaInicio, fechaFin,
                    adultos, ninos, habitacionesRequeridas, onSuccess, onError);
        } else {
            onError.accept(new Exception("Formato de ubicación inválido. Use: 'Ciudad, País'"));
        }
    }

    /**
     * Verifica si un hotel está disponible según los criterios especificados
     */
    private static boolean isHotelAvailable(Hotel hotel, long fechaInicio, long fechaFin,
                                            int adultos, int ninos, int habitacionesRequeridas) {

        // Verificar que el hotel permita reservas
        if (hotel.getPermiteReservas() == null || !hotel.getPermiteReservas()) {
            return false;
        }

        // Verificar que el hotel esté activo
        if (!"activo".equalsIgnoreCase(hotel.getEstado())) {
            return false;
        }

        // Verificar disponibilidad de habitaciones
        List<Habitacion> habitaciones = hotel.getHabitaciones();
        if (habitaciones == null || habitaciones.isEmpty()) {
            return false;
        }

        // Contar habitaciones que pueden acomodar los huéspedes requeridos
        int habitacionesDisponibles = 0;
        for (Habitacion habitacion : habitaciones) {
            if (isHabitacionSuitable(habitacion, adultos, ninos, fechaInicio, fechaFin)) {
                habitacionesDisponibles++;
                if (habitacionesDisponibles >= habitacionesRequeridas) {
                    return true; // Encontramos suficientes habitaciones
                }
            }
        }

        return false;
    }

    /**
     * Verifica si una habitación es adecuada para los huéspedes especificados
     */
    private static boolean isHabitacionSuitable(Habitacion habitacion, int adultos, int ninos,
                                                long fechaInicio, long fechaFin) {
        if (habitacion == null || habitacion.getCapacidad() == null) {
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        Capacidad capacidad = habitacion.getCapacidad();

        // Verificar capacidad de adultos (debe ser mayor o igual)
        if (capacidad.getAdultos() == null || capacidad.getAdultos() < adultos) {
            return false;
        }

        // Verificar capacidad de niños (debe ser mayor o igual)
        if (capacidad.getNinos() == null || capacidad.getNinos() < ninos) {
            return false;
        }

        // TODO: Aquí se puede agregar lógica para verificar disponibilidad por fechas
        // Por ejemplo, consultar una colección de reservas para verificar si la habitación
        // está libre en las fechas especificadas

        return true;
    }

    /**
     * Busca hoteles utilizando parámetros guardados en PrefsManager
     * @param context Contexto para acceder al PrefsManager
     * @param ubicacionCompleta La ubicación en formato "Ciudad, País"
     * @param onSuccess Callback que recibe la lista de hoteles filtrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsFromPrefs(@NonNull android.content.Context context,
                                             @NonNull String ubicacionCompleta,
                                             @NonNull Consumer<List<Hotel>> onSuccess,
                                             @NonNull Consumer<Exception> onError) {
        try {
            // Obtener parámetros del PrefsManager
            com.example.telehotel.core.storage.PrefsManager prefsManager =
                    new com.example.telehotel.core.storage.PrefsManager(context);

            long fechaInicio = prefsManager.getStartDate();
            long fechaFin = prefsManager.getEndDate();

            if (fechaInicio == 0 || fechaFin == 0) {
                onError.accept(new Exception("Fechas no configuradas. Por favor selecciona las fechas de estadía."));
                return;
            }

            // Parsear string de personas para obtener valores
            String peopleString = prefsManager.getPeopleString();
            SearchParams params = parseSearchParams(peopleString);

            // Realizar búsqueda con filtros
            searchHotelsWithFiltersString(ubicacionCompleta, fechaInicio, fechaFin,
                    params.adults, params.children, params.rooms,
                    onSuccess, onError);

        } catch (Exception e) {
            onError.accept(new Exception("Error obteniendo parámetros de búsqueda: " + e.getMessage()));
        }
    }

    /**
     * Busca hoteles con parámetros específicos
     * @param context Contexto para acceder al PrefsManager
     * @param ubicacionCompleta La ubicación en formato "Ciudad, País"
     * @param adults Número de adultos
     * @param children Número de niños
     * @param rooms Número de habitaciones
     * @param onSuccess Callback que recibe la lista de hoteles filtrados
     * @param onError Callback que recibe la excepción en caso de error
     */
    public static void searchHotelsWithCustomParams(@NonNull android.content.Context context,
                                                    @NonNull String ubicacionCompleta,
                                                    int adults,
                                                    int children,
                                                    int rooms,
                                                    @NonNull Consumer<List<Hotel>> onSuccess,
                                                    @NonNull Consumer<Exception> onError) {
        try {
            // Obtener fechas del PrefsManager
            com.example.telehotel.core.storage.PrefsManager prefsManager =
                    new com.example.telehotel.core.storage.PrefsManager(context);

            long fechaInicio = prefsManager.getStartDate();
            long fechaFin = prefsManager.getEndDate();

            if (fechaInicio == 0 || fechaFin == 0) {
                onError.accept(new Exception("Fechas no configuradas. Por favor selecciona las fechas de estadía."));
                return;
            }

            // Realizar búsqueda con parámetros personalizados
            searchHotelsWithFiltersString(ubicacionCompleta, fechaInicio, fechaFin,
                    adults, children, rooms, onSuccess, onError);

        } catch (Exception e) {
            onError.accept(new Exception("Error en búsqueda personalizada: " + e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas de disponibilidad para una ubicación
     * @param ubicacionCompleta La ubicación en formato "Ciudad, País"
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @param onSuccess Callback con estadísticas
     * @param onError Callback de error
     */
    public static void getAvailabilityStats(@NonNull String ubicacionCompleta,
                                            long fechaInicio,
                                            long fechaFin,
                                            @NonNull Consumer<AvailabilityStats> onSuccess,
                                            @NonNull Consumer<Exception> onError) {

        searchHotelsByLocationString(ubicacionCompleta,
                hoteles -> {
                    try {
                        AvailabilityStats stats = new AvailabilityStats();
                        stats.totalHoteles = hoteles.size();
                        stats.hotelesActivos = 0;
                        stats.habitacionesDisponibles = 0;
                        stats.capacidadMaximaAdultos = 0;
                        stats.capacidadMaximaNinos = 0;

                        for (Hotel hotel : hoteles) {
                            if ("activo".equalsIgnoreCase(hotel.getEstado()) &&
                                    hotel.getPermiteReservas() != null && hotel.getPermiteReservas()) {
                                stats.hotelesActivos++;

                                if (hotel.getHabitaciones() != null) {
                                    for (Habitacion habitacion : hotel.getHabitaciones()) {
                                        if ("disponible".equalsIgnoreCase(habitacion.getEstado())) {
                                            stats.habitacionesDisponibles++;

                                            if (habitacion.getCapacidad() != null) {
                                                if (habitacion.getCapacidad().getAdultos() != null) {
                                                    stats.capacidadMaximaAdultos = Math.max(
                                                            stats.capacidadMaximaAdultos,
                                                            habitacion.getCapacidad().getAdultos()
                                                    );
                                                }
                                                if (habitacion.getCapacidad().getNinos() != null) {
                                                    stats.capacidadMaximaNinos = Math.max(
                                                            stats.capacidadMaximaNinos,
                                                            habitacion.getCapacidad().getNinos()
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        onSuccess.accept(stats);

                    } catch (Exception e) {
                        onError.accept(e);
                    }
                },
                onError
        );
    }

    /**
     * Clase auxiliar para parámetros de búsqueda
     */
    private static class SearchParams {
        int adults = 2;
        int children = 0;
        int rooms = 1;

        SearchParams(int adults, int children, int rooms) {
            this.adults = adults;
            this.children = children;
            this.rooms = rooms;
        }
    }

    /**
     * Parsea el string de personas del PrefsManager
     * Formato esperado: "2 habitaciones · 3 adultos · 1 niño"
     */
    private static SearchParams parseSearchParams(String peopleString) {
        int adults = 2;
        int children = 0;
        int rooms = 1;

        if (peopleString != null && !peopleString.trim().isEmpty()) {
            try {
                String[] parts = peopleString.split(" · ");

                for (String part : parts) {
                    part = part.trim().toLowerCase();

                    if (part.contains("habitación")) {
                        String[] roomParts = part.split(" ");
                        if (roomParts.length > 0) {
                            rooms = Integer.parseInt(roomParts[0]);
                        }
                    } else if (part.contains("adulto")) {
                        String[] adultParts = part.split(" ");
                        if (adultParts.length > 0) {
                            adults = Integer.parseInt(adultParts[0]);
                        }
                    } else if (part.contains("niño")) {
                        String[] childParts = part.split(" ");
                        if (childParts.length > 0) {
                            children = Integer.parseInt(childParts[0]);
                        }
                    }
                }
            } catch (Exception e) {
                // Si hay error parseando, usar valores por defecto
                Log.w("HotelRepository", "Error parseando string de personas: " + peopleString + ". Usando valores por defecto.");
            }
        }

        return new SearchParams(adults, children, rooms);
    }

    /**
     * Calcula un puntaje para el hotel basado en qué tan bien coincide con los criterios
     */
    private static int calculateHotelScore(Hotel hotel, int adultos, int ninos, int habitacionesRequeridas) {
        int score = 0;

        if (hotel.getHabitaciones() != null) {
            for (Habitacion habitacion : hotel.getHabitaciones()) {
                if (habitacion.getCapacidad() != null) {
                    Capacidad cap = habitacion.getCapacidad();

                    // Puntos por capacidad exacta o superior
                    if (cap.getAdultos() != null && cap.getAdultos() >= adultos) {
                        score += 10;
                        if (cap.getAdultos().equals(adultos)) {
                            score += 5; // Bonus por coincidencia exacta
                        }
                    }

                    if (cap.getNinos() != null && cap.getNinos() >= ninos) {
                        score += 10;
                        if (cap.getNinos().equals(ninos)) {
                            score += 5; // Bonus por coincidencia exacta
                        }
                    }
                }
            }

            // Bonus por tener suficientes habitaciones
            if (hotel.getHabitaciones().size() >= habitacionesRequeridas) {
                score += 20;
            }
        }

        return score;
    }
}

