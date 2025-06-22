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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HotelRepository {

    private static final String COLLECTION_NAME = "hoteles";

    private static CollectionReference getCollection() {
        return FirebaseUtil.getFirestore().collection(COLLECTION_NAME);
    }
    // Agregar al final de HotelRepository.java

    // Variables estáticas para cache inteligente
    private static List<String> cachedLocations = new ArrayList<>();
    private static List<LocationSearch> cachedDetailedLocations = new ArrayList<>();
    private static long lastLocationCacheTime = 0;
    private static final long CACHE_DURATION = 60000; // 1 minuto
    private static final List<Consumer<List<String>>> locationUpdateCallbacks = new ArrayList<>();

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
     * Busca hoteles por ciudad y país (compatible con ambos formatos)
     */


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

        // CAMBIO: Solo buscar que tenga AL MENOS UNA habitación con capacidad suficiente
        // Ignorar el número de habitaciones requeridas
        for (Habitacion habitacion : habitaciones) {
            if (isHabitacionSuitable(habitacion, adultos, ninos, fechaInicio, fechaFin)) {
                return true; // Con que haya UNA habitación que cumpla, el hotel está disponible
            }
        }

        return false;
    }

    /**
     * Verifica si una habitación es adecuada para los huéspedes especificados
     * CORREGIDO: Capacidad debe ser mayor o igual (no exacta)
     */
    /*private static boolean isHabitacionSuitable(Habitacion habitacion, int adultos, int ninos,
                                                long fechaInicio, long fechaFin) {
        if (habitacion == null || habitacion.getCapacidad() == null) {
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        Capacidad capacidad = habitacion.getCapacidad();

        // CORREGIDO: Verificar capacidad de adultos (debe ser MAYOR O IGUAL)
        if (capacidad.getAdultos() == null || capacidad.getAdultos() < adultos) {
            return false;
        }

        // CORREGIDO: Verificar capacidad de niños (debe ser MAYOR O IGUAL)
        if (capacidad.getNinos() == null || capacidad.getNinos() < ninos) {
            return false;
        }

        return true;
    }*/
    private static boolean isHabitacionSuitable(Habitacion habitacion, int adultos, int ninos,
                                                long fechaInicio, long fechaFin) {
        if (habitacion == null) {
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        // Usar la función robusta para obtener capacidad
        int[] capacidad = getHabitacionCapacity(habitacion);
        int capacidadAdultos = capacidad[0];
        int capacidadNinos = capacidad[1];

        // Log para debug
        Log.d("HotelRepository", String.format(
                "isHabitacionSuitable: [%d adultos, %d niños] vs requerido [%d adultos, %d niños]",
                capacidadAdultos, capacidadNinos, adultos, ninos
        ));

        // VERIFICACIÓN ESTRICTA: Debe tener AL MENOS la capacidad requerida
        return (capacidadAdultos >= adultos) && (capacidadNinos >= ninos);
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

    /**
     * Busca hoteles usando texto flexible (para búsquedas personalizadas)
     * Esta función permite buscar cuando el usuario escribe texto que no coincide
     * exactamente con el formato "Ciudad, País"
     */
    public static void searchHotelsByFlexibleText(@NonNull String searchText,
                                                  int adults,
                                                  int children,
                                                  int rooms,
                                                  @NonNull Consumer<List<Hotel>> onSuccess,
                                                  @NonNull Consumer<Exception> onError) {

        if (searchText == null || searchText.trim().isEmpty()) {
            onError.accept(new Exception("Texto de búsqueda vacío"));
            return;
        }

        String searchLower = searchText.toLowerCase().trim();

        getCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hotelesEncontrados = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            Hotel hotel = doc.toObject(Hotel.class);
                            if (hotel != null) {
                                hotel.setId(doc.getId());

                                // Verificar si el hotel coincide con la búsqueda flexible
                                if (matchesFlexibleSearch(hotel, searchLower)) {
                                    // Si se especificaron criterios de capacidad, verificarlos
                                    if (adults > 0 || children > 0 || rooms > 1) {
                                        if (isHotelAvailableForGuests(hotel, adults, children, rooms)) {
                                            hotelesEncontrados.add(hotel);
                                        }
                                    } else {
                                        // Sin criterios de capacidad, agregar todos los que coincidan
                                        hotelesEncontrados.add(hotel);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("HotelRepository", "Error procesando hotel: " + e.getMessage());
                        }
                    }

                    onSuccess.accept(hotelesEncontrados);
                })
                .addOnFailureListener(e -> onError.accept(e)); // ✅ CORREGIDO: Usar lambda en lugar de cast
    }

    /**
     * Verifica si un hotel coincide con la búsqueda flexible
     * Busca en múltiples campos: nombre, descripción, ubicación, servicios
     */
    private static boolean matchesFlexibleSearch(Hotel hotel, String searchLower) {
        if (hotel == null) return false;

        try {
            // Buscar en nombre del hotel
            if (hotel.getNombre() != null &&
                    hotel.getNombre().toLowerCase().contains(searchLower)) {
                return true;
            }

            // Buscar en descripción
            if (hotel.getDescripcion() != null &&
                    hotel.getDescripcion().toLowerCase().contains(searchLower)) {
                return true;
            }

            // Buscar en ubicación
            if (hotel.getUbicacion() != null) {
                Ubicacion ub = hotel.getUbicacion();

                // Dirección
                if (ub.getDireccion() != null &&
                        ub.getDireccion().toLowerCase().contains(searchLower)) {
                    return true;
                }

                // Ciudad
                if (ub.getCiudad() != null &&
                        ub.getCiudad().toLowerCase().contains(searchLower)) {
                    return true;
                }

                // País
                if (ub.getPais() != null &&
                        ub.getPais().toLowerCase().contains(searchLower)) {
                    return true;
                }
            }

            // Buscar en servicios
            if (hotel.getServicios() != null) {
                for (String servicio : hotel.getServicios()) {
                    if (servicio != null && servicio.toLowerCase().contains(searchLower)) {
                        return true;
                    }
                }
            }

            // Buscar en lugares cercanos (si es que existen)
            if (hotel.getLugaresCercanos() != null) {
                for (Object lugarObj : hotel.getLugaresCercanos()) {
                    if (lugarObj != null) {
                        String lugarStr = lugarObj.toString().toLowerCase();
                        if (lugarStr.contains(searchLower)) {
                            return true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("HotelRepository", "Error en matchesFlexibleSearch: " + e.getMessage());
        }

        return false;
    }

    private static boolean isHotelAvailableForGuests(Hotel hotel, int adults, int children, int rooms) {
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

        // CAMBIO: Solo buscar que tenga AL MENOS UNA habitación con capacidad suficiente
        // Ignorar el parámetro 'rooms'
        for (Habitacion habitacion : habitaciones) {
            if (isHabitacionSuitableForGuests(habitacion, adults, children)) {
                return true; // Con que haya UNA habitación que cumpla, el hotel está disponible
            }
        }

        return false;
    }

    private static boolean isHabitacionSuitableForGuests(Habitacion habitacion, int adults, int children) {
        if (habitacion == null) {
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        // Usar la función robusta para obtener capacidad
        int[] capacidad = getHabitacionCapacity(habitacion);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        // Log para debug
        Log.d("HotelRepository", String.format(
                "Verificando habitación: [%d adultos, %d niños] vs requerido [%d adultos, %d niños]",
                adultos, ninos, adults, children
        ));

        // VERIFICACIÓN ESTRICTA: Debe tener AL MENOS la capacidad requerida
        boolean cumple = (adultos >= adults) && (ninos >= children);

        if (!cumple) {
            Log.d("HotelRepository", String.format(
                    "Habitación RECHAZADA: No cumple capacidad mínima. Tiene [%d,%d], necesita [%d,%d]",
                    adultos, ninos, adults, children
            ));
        }

        return cumple;
    }


    /**
     * Mejora de la función searchHotelsWithCustomParams existente
     * Ahora también maneja búsquedas flexibles como fallback
     */
    /*public static void searchHotelsWithCustomParamsEnhanced(@NonNull android.content.Context context,
                                                            @NonNull String locationInput,
                                                            int adults,
                                                            int children,
                                                            int rooms,
                                                            @NonNull Consumer<List<Hotel>> onSuccess,
                                                            @NonNull Consumer<Exception> onError) {
        try {
            // Primero intentar búsqueda estándar (formato "Ciudad, País")
            if (locationInput.contains(",") && locationInput.split(",").length == 2) {
                // Usar función existente para formato estándar
                searchHotelsWithCustomParams(context, locationInput, adults, children, rooms,
                        hoteles -> {
                            if (!hoteles.isEmpty()) {
                                onSuccess.accept(hoteles);
                            } else {
                                // Si no hay resultados con búsqueda estándar, intentar búsqueda flexible
                                searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
                            }
                        },
                        error -> {
                            // Si falla la búsqueda estándar, intentar búsqueda flexible
                            searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
                        }
                );
            } else {
                // Input no tiene formato estándar, usar directamente búsqueda flexible
                searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
            }

        } catch (Exception e) {
            onError.accept(new Exception("Error en búsqueda mejorada: " + e.getMessage()));
        }
    }*/
    public static void searchHotelsWithCustomParamsEnhanced(@NonNull android.content.Context context,
                                                            @NonNull String locationInput,
                                                            int adults,
                                                            int children,
                                                            int rooms,
                                                            @NonNull Consumer<List<Hotel>> onSuccess,
                                                            @NonNull Consumer<Exception> onError) {
        try {
            Log.d("HotelRepository", "=== BÚSQUEDA ENHANCED ===");
            Log.d("HotelRepository", String.format("Ubicación: %s, Adultos: %d, Niños: %d", locationInput, adults, children));

            // Primero intentar búsqueda estándar (formato "Ciudad, País")
            if (locationInput.contains(",") && locationInput.split(",").length == 2) {
                Log.d("HotelRepository", "Usando formato estándar 'Ciudad, País'");

                // CAMBIO: Usar DIRECTAMENTE la función con colección separada
                searchHotelsWithExactCapacityFromDB(context, locationInput, adults, children, rooms,
                        hoteles -> {
                            Log.d("HotelRepository", "Búsqueda estándar completada: " + hoteles.size() + " hoteles");
                            if (!hoteles.isEmpty()) {
                                onSuccess.accept(hoteles);
                            } else {
                                // Si no hay resultados con búsqueda estándar, intentar búsqueda flexible
                                Log.d("HotelRepository", "Sin resultados en búsqueda estándar, intentando flexible...");
                                searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
                            }
                        },
                        error -> {
                            Log.e("HotelRepository", "Error en búsqueda estándar: " + error.getMessage());
                            // Si falla la búsqueda estándar, intentar búsqueda flexible
                            searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
                        }
                );
            } else {
                Log.d("HotelRepository", "Input no tiene formato estándar, usando búsqueda flexible");
                // Input no tiene formato estándar, usar directamente búsqueda flexible
                searchHotelsByFlexibleText(locationInput, adults, children, rooms, onSuccess, onError);
            }

        } catch (Exception e) {
            Log.e("HotelRepository", "Error en búsqueda mejorada: " + e.getMessage(), e);
            onError.accept(new Exception("Error en búsqueda mejorada: " + e.getMessage()));
        }
    }

    /**
     * Función auxiliar para verificar si una ubicación existe en formato estándar
     * Útil para determinar si mostrar el mensaje de "no se cuenta con la ubicación"
     */
    public static void checkIfLocationExists(@NonNull String locationInput,
                                             @NonNull Consumer<Boolean> onResult) {

        if (locationInput == null || locationInput.trim().isEmpty()) {
            onResult.accept(false);
            return;
        }

        // Si tiene formato "Ciudad, País", verificar directamente
        if (locationInput.contains(",") && locationInput.split(",").length == 2) {
            searchHotelsByLocationString(locationInput,
                    hoteles -> onResult.accept(!hoteles.isEmpty()),
                    error -> onResult.accept(false)
            );
        } else {
            // Para texto libre, verificar si coincide con alguna ubicación conocida
            getUniqueLocations(
                    ubicaciones -> {
                        String inputLower = locationInput.toLowerCase().trim();
                        boolean exists = ubicaciones.stream()
                                .anyMatch(ubicacion -> ubicacion.toLowerCase().contains(inputLower));
                        onResult.accept(exists);
                    },
                    error -> onResult.accept(false)
            );
        }
    }

    /**
     * Obtiene sugerencias de ubicaciones similares al texto ingresado
     * Útil para cuando no se encuentra una ubicación exacta
     */
    public static void getSimilarLocations(@NonNull String searchText,
                                           int maxSuggestions,
                                           @NonNull Consumer<List<String>> onSuccess,
                                           @NonNull Consumer<Exception> onError) {

        if (searchText == null || searchText.trim().isEmpty()) {
            onSuccess.accept(new ArrayList<>());
            return;
        }

        getUniqueLocations(
                ubicaciones -> {
                    List<String> sugerencias = new ArrayList<>();
                    String searchLower = searchText.toLowerCase().trim();

                    // Buscar ubicaciones que contengan parcialmente el texto
                    for (String ubicacion : ubicaciones) {
                        if (ubicacion.toLowerCase().contains(searchLower) ||
                                containsSimilarWords(ubicacion.toLowerCase(), searchLower)) {
                            sugerencias.add(ubicacion);

                            if (sugerencias.size() >= maxSuggestions) {
                                break;
                            }
                        }
                    }

                    onSuccess.accept(sugerencias);
                },
                onError
        );
    }

    /**
     * Verifica si dos strings contienen palabras similares
     * (implementación simple para sugerencias)
     */
    private static boolean containsSimilarWords(String location, String search) {
        String[] locationWords = location.split("[,\\s]+");
        String[] searchWords = search.split("\\s+");

        for (String searchWord : searchWords) {
            if (searchWord.length() >= 3) { // Solo palabras de 3+ caracteres
                for (String locationWord : locationWords) {
                    if (locationWord.contains(searchWord) || searchWord.contains(locationWord)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void searchHotelsWithExactCapacityFromDB(@NonNull android.content.Context context,
                                                           @NonNull String ubicacionCompleta,
                                                           int minAdults,        // CAMBIO: minAdults en lugar de exactAdults
                                                           int minChildren,      // CAMBIO: minChildren en lugar de exactChildren
                                                           int rooms,            // IGNORAR: Este parámetro ya no se usa
                                                           @NonNull Consumer<List<Hotel>> onSuccess,
                                                           @NonNull Consumer<Exception> onError) {
        try {
            com.example.telehotel.core.storage.PrefsManager prefsManager =
                    new com.example.telehotel.core.storage.PrefsManager(context);

            long fechaInicio = prefsManager.getStartDate();
            long fechaFin = prefsManager.getEndDate();

            if (fechaInicio == 0 || fechaFin == 0) {
                onError.accept(new Exception("Fechas no configuradas"));
                return;
            }

            // Primero buscar hoteles por ubicación
            searchHotelsByLocationString(ubicacionCompleta,
                    hoteles -> {
                        if (hoteles.isEmpty()) {
                            onSuccess.accept(new ArrayList<>());
                            return;
                        }

                        // CAMBIO: Usar la nueva función que ignora el número de habitaciones
                        buscarHabitacionesParaHoteles(hoteles, minAdults, minChildren, 1, onSuccess, onError);
                        // Nota: pasamos 1 como roomsNeeded pero la función lo ignora
                    },
                    onError
            );

        } catch (Exception e) {
            onError.accept(new Exception("Error en búsqueda de capacidad mínima: " + e.getMessage()));
        }
    }

    private static void buscarHabitacionesParaHoteles(List<Hotel> hoteles,
                                                      int requiredAdults,
                                                      int requiredChildren,
                                                      int roomsNeeded, // IGNORADO
                                                      Consumer<List<Hotel>> onSuccess,
                                                      Consumer<Exception> onError) {

        Log.d("HotelRepository", String.format(
                "=== BUSCANDO HABITACIONES ===\nRequerido: %d adultos, %d niños\nHoteles a verificar: %d",
                requiredAdults, requiredChildren, hoteles.size()
        ));

        List<Hotel> hotelesConHabitaciones = new ArrayList<>();
        AtomicInteger contador = new AtomicInteger(hoteles.size());

        for (Hotel hotel : hoteles) {
            Log.d("HotelRepository", "Verificando hotel: " + hotel.getNombre() + " (ID: " + hotel.getId() + ")");

            // 🔍 DEBUG: Buscar habitaciones para este hotel específico
            Log.d("HotelRepository", "=== CONSULTANDO FIREBASE ===");
            Log.d("HotelRepository", "Hotel ID a buscar: '" + hotel.getId() + "'");
            Log.d("HotelRepository", "Estado requerido: 'disponible'");

            FirebaseUtil.getFirestore()
                    .collection("habitaciones")
                    .whereEqualTo("hotelId", hotel.getId())
                    .whereEqualTo("estado", "disponible")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        try {
                            Log.d("HotelRepository", "📊 RESULTADO DE FIREBASE:");
                            Log.d("HotelRepository", "Documentos encontrados: " + querySnapshot.size());

                            // 🔍 DEBUG: Mostrar TODOS los documentos encontrados
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Log.d("HotelRepository", "--- Documento " + doc.getId() + " ---");
                                Log.d("HotelRepository", "hotelId: '" + doc.getString("hotelId") + "'");
                                Log.d("HotelRepository", "estado: '" + doc.getString("estado") + "'");
                                Log.d("HotelRepository", "numero: '" + doc.getString("numero") + "'");
                                Log.d("HotelRepository", "capacidadAdultos: " + doc.getLong("capacidadAdultos"));
                                Log.d("HotelRepository", "capacidadNinos: " + doc.getLong("capacidadNinos"));

                                // Verificar si es la habitación 500 específicamente
                                if ("500".equals(doc.getString("numero"))) {
                                    Log.d("HotelRepository", "🎯 ENCONTRADA HABITACIÓN 500!");
                                    Log.d("HotelRepository", "¿hotelId coincide? " + hotel.getId().equals(doc.getString("hotelId")));
                                    Log.d("HotelRepository", "¿estado correcto? " + "disponible".equals(doc.getString("estado")));
                                }
                            }

                            List<Habitacion> habitacionesHotel = new ArrayList<>();

                            Log.d("HotelRepository", "Habitaciones encontradas para hotel " + hotel.getNombre() + ": " + querySnapshot.size());

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Habitacion habitacion = doc.toObject(Habitacion.class);
                                if (habitacion != null) {
                                    habitacion.setId(doc.getId());
                                    habitacionesHotel.add(habitacion);

                                    // DEBUG: Mostrar datos de cada habitación
                                    debugHabitacionData(habitacion);
                                }
                            }

                            // Resto de tu código existente...
                            boolean tieneCapacidadSuficiente = hasAtLeastOneRoomWithCapacity(habitacionesHotel, requiredAdults, requiredChildren);

                            Log.d("HotelRepository", String.format(
                                    "Hotel %s: %s (habitaciones verificadas: %d)",
                                    hotel.getNombre(),
                                    tieneCapacidadSuficiente ? "✅ INCLUIDO" : "❌ RECHAZADO",
                                    habitacionesHotel.size()
                            ));

                            if (tieneCapacidadSuficiente) {
                                // Filtrar habitaciones antes de asignar al hotel
                                List<Habitacion> habitacionesFiltradas = new ArrayList<>();
                                for (Habitacion habitacion : habitacionesHotel) {
                                    if (hasMinimumCapacityFromHabitacion(habitacion, requiredAdults, requiredChildren)) {
                                        habitacionesFiltradas.add(habitacion);
                                    }
                                }

                                hotel.setHabitaciones(habitacionesFiltradas);

                                Log.d("HotelRepository", String.format(
                                        "Hotel %s: Habitaciones filtradas de %d a %d",
                                        hotel.getNombre(), habitacionesHotel.size(), habitacionesFiltradas.size()
                                ));

                                synchronized (hotelesConHabitaciones) {
                                    hotelesConHabitaciones.add(hotel);
                                }
                            }

                            // Verificar si terminamos de procesar todos los hoteles
                            if (contador.decrementAndGet() == 0) {
                                Log.d("HotelRepository", String.format(
                                        "=== RESULTADO FINAL ===\nHoteles que cumplen criterio: %d de %d",
                                        hotelesConHabitaciones.size(), hoteles.size()
                                ));

                                // Log final de habitaciones filtradas
                                for (Hotel hotelFinal : hotelesConHabitaciones) {
                                    if (hotelFinal.getHabitaciones() != null) {
                                        Log.d("HotelRepository", String.format(
                                                "Hotel final %s: %d habitaciones que cumplen criterio",
                                                hotelFinal.getNombre(), hotelFinal.getHabitaciones().size()
                                        ));
                                    }
                                }

                                onSuccess.accept(hotelesConHabitaciones);
                            }

                        } catch (Exception e) {
                            Log.e("HotelRepository", "Error procesando habitaciones para hotel: " + hotel.getId(), e);
                            if (contador.decrementAndGet() == 0) {
                                onSuccess.accept(hotelesConHabitaciones);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HotelRepository", "Error buscando habitaciones para hotel: " + hotel.getId(), e);
                        if (contador.decrementAndGet() == 0) {
                            onSuccess.accept(hotelesConHabitaciones);
                        }
                    });
        }
    }

    private static boolean hasAtLeastOneRoomWithCapacity(List<Habitacion> habitaciones,
                                                         int requiredAdults,
                                                         int requiredChildren) {
        if (habitaciones == null || habitaciones.isEmpty()) {
            Log.d("HotelRepository", "No hay habitaciones para verificar");
            return false;
        }

        Log.d("HotelRepository", String.format(
                "Verificando %d habitaciones para capacidad mínima: %d adultos, %d niños",
                habitaciones.size(), requiredAdults, requiredChildren
        ));

        for (Habitacion habitacion : habitaciones) {
            boolean cumple = hasMinimumCapacityFromHabitacion(habitacion, requiredAdults, requiredChildren);
            if (cumple) {
                Log.d("HotelRepository", "✅ Habitación " + habitacion.getNumero() + " cumple los requisitos");
                return true; // Con una habitación que cumpla es suficiente
            }
        }

        Log.d("HotelRepository", "❌ Ninguna habitación cumple los requisitos de capacidad");
        return false;
    }
    private static boolean hasMinimumCapacityFromHabitacion(Habitacion habitacion, int minAdults, int minChildren) {
        if (habitacion == null) {
            Log.d("HotelRepository", "Habitación es null");
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            Log.d("HotelRepository", "Habitación " + habitacion.getId() + " no está disponible: " + habitacion.getEstado());
            return false;
        }

        // Obtener capacidad real
        int[] capacidad = getHabitacionCapacity(habitacion);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        // Log para debug
        Log.d("HotelRepository", String.format(
                "Habitación %s: Capacidad real [%d adultos, %d niños] vs Requerido [%d adultos, %d niños]",
                habitacion.getNumero() != null ? habitacion.getNumero() : habitacion.getId(),
                adultos, ninos, minAdults, minChildren
        ));

        // VERIFICACIÓN ESTRICTA: Debe tener AL MENOS la capacidad requerida
        boolean adultosMinimos = adultos >= minAdults;
        boolean ninosMinimos = ninos >= minChildren;

        boolean cumple = adultosMinimos && ninosMinimos;

        Log.d("HotelRepository", String.format(
                "Habitación %s: Adultos OK=%b, Niños OK=%b, Cumple=%b",
                habitacion.getNumero() != null ? habitacion.getNumero() : habitacion.getId(),
                adultosMinimos, ninosMinimos, cumple
        ));

        return cumple;
    }



    /**
     * Verifica capacidad exacta desde lista de habitaciones
     */
    private static boolean hasExactCapacityRoomsFromList(List<Habitacion> habitaciones,
                                                         int exactAdults,
                                                         int exactChildren,
                                                         int roomsNeeded) {
        if (habitaciones == null || habitaciones.isEmpty()) {
            return false;
        }

        int habitacionesExactas = 0;
        for (Habitacion habitacion : habitaciones) {
            if (hasExactCapacityFromHabitacion(habitacion, exactAdults, exactChildren)) {
                habitacionesExactas++;
                if (habitacionesExactas >= roomsNeeded) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica capacidad exacta de una habitación específica usando los campos correctos
     */
    private static boolean hasExactCapacityFromHabitacion(Habitacion habitacion, int exactAdults, int exactChildren) {
        if (habitacion == null) {
            return false;
        }

        // Verificar que la habitación esté disponible
        if (!"disponible".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        // Obtener capacidad desde los campos específicos que vimos en Firebase
        Integer adultos = habitacion.getCapacidadAdultos();
        Integer ninos = habitacion.getCapacidadNinos();

        // Si no tiene estos campos, intentar con el objeto capacidad
        if (adultos == null && habitacion.getCapacidad() != null) {
            adultos = habitacion.getCapacidad().getAdultos();
        }
        if (ninos == null && habitacion.getCapacidad() != null) {
            ninos = habitacion.getCapacidad().getNinos();
        }

        // Valores por defecto si aún no hay datos
        if (adultos == null) adultos = 2;
        if (ninos == null) ninos = 0;

        // EXACTO: debe coincidir exactamente
        boolean adultosExactos = adultos.equals(exactAdults);
        boolean ninosExactos = ninos.equals(exactChildren);

        return adultosExactos && ninosExactos;
    }
    /**
     * Obtiene servicios de un hotel desde la colección separada de servicios
     */
    public static void getServiciosByHotelId(@NonNull String hotelId,
                                             @NonNull Consumer<List<String>> onSuccess,
                                             @NonNull Consumer<Exception> onError) {

        FirebaseUtil.getFirestore()
                .collection("servicios")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("disponible", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> servicios = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            // Obtener el nombre del servicio
                            String nombre = doc.getString("nombre");
                            String descripcion = doc.getString("descripcion");

                            if (nombre != null && !nombre.trim().isEmpty()) {
                                // Si hay descripción, agregarla
                                if (descripcion != null && !descripcion.trim().isEmpty()) {
                                    servicios.add(nombre + " - " + descripcion);
                                } else {
                                    servicios.add(nombre);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("HotelRepository", "Error procesando servicio: " + doc.getId(), e);
                        }
                    }

                    onSuccess.accept(servicios);
                })
                .addOnFailureListener(onError::accept);
    }
    /**
     * Obtiene habitaciones de un hotel específico desde la colección separada
     */
    public static void getHabitacionesByHotelId(@NonNull String hotelId,
                                                @NonNull Consumer<List<Habitacion>> onSuccess,
                                                @NonNull Consumer<Exception> onError) {

        FirebaseUtil.getFirestore()
                .collection("habitaciones")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("estado", "disponible")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Habitacion> habitaciones = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Habitacion habitacion = doc.toObject(Habitacion.class);
                            if (habitacion != null) {
                                habitacion.setId(doc.getId());
                                habitaciones.add(habitacion);
                            }
                        } catch (Exception e) {
                            Log.e("HotelRepository", "Error procesando habitación: " + doc.getId(), e);
                        }
                    }

                    onSuccess.accept(habitaciones);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Obtiene habitaciones filtradas por capacidad específica
     */
    /*public static void getHabitacionesByHotelIdAndCapacity(@NonNull String hotelId,
                                                           int adultos,
                                                           int ninos,
                                                           @NonNull Consumer<List<Habitacion>> onSuccess,
                                                           @NonNull Consumer<Exception> onError) {

        getHabitacionesByHotelId(hotelId,
                habitaciones -> {
                    // Filtrar por capacidad
                    List<Habitacion> habitacionesFiltradas = new ArrayList<>();

                    for (Habitacion habitacion : habitaciones) {
                        // Verificar si puede acomodar exactamente o al menos la capacidad requerida
                        if (puedeAcomodarCapacidad(habitacion, adultos, ninos)) {
                            habitacionesFiltradas.add(habitacion);
                        }
                    }

                    onSuccess.accept(habitacionesFiltradas);
                },
                onError
        );
    }*/

    /**
     * VERSIÓN CORREGIDA: Obtiene habitaciones filtradas por capacidad específica
     */
    public static void getHabitacionesByHotelIdAndCapacity(@NonNull String hotelId,
                                                           int adultos,
                                                           int ninos,
                                                           @NonNull Consumer<List<Habitacion>> onSuccess,
                                                           @NonNull Consumer<Exception> onError) {

        Log.d("HotelRepository", "=== FILTRO DE HABITACIONES ===");
        Log.d("HotelRepository", "Hotel ID: " + hotelId);
        Log.d("HotelRepository", "Buscando habitaciones para: " + adultos + "+ adultos, " + ninos + "+ niños");

        getHabitacionesByHotelId(hotelId,
                habitaciones -> {
                    Log.d("HotelRepository", "Habitaciones obtenidas de Firebase: " + habitaciones.size());

                    // Filtrar por capacidad CON LOGS DETALLADOS
                    List<Habitacion> habitacionesFiltradas = new ArrayList<>();

                    for (int i = 0; i < habitaciones.size(); i++) {
                        Habitacion habitacion = habitaciones.get(i);

                        // Obtener capacidad real usando función robusta
                        int[] capacidad = getHabitacionCapacity(habitacion);
                        int capacidadAdultos = capacidad[0];
                        int capacidadNinos = capacidad[1];

                        // Verificar criterio
                        boolean cumpleAdultos = capacidadAdultos >= adultos;
                        boolean cumpleNinos = capacidadNinos >= ninos;
                        boolean cumpleCriterio = cumpleAdultos && cumpleNinos;

                        Log.d("HotelRepository", String.format(
                                "Habitación %s: [%d adultos, %d niños] vs [%d+, %d+] → Adultos:%s, Niños:%s, Final:%s",
                                habitacion.getNumero(), capacidadAdultos, capacidadNinos,
                                adultos, ninos, cumpleAdultos ? "✅" : "❌",
                                cumpleNinos ? "✅" : "❌", cumpleCriterio ? "INCLUIDA" : "FILTRADA"
                        ));

                        if (cumpleCriterio) {
                            habitacionesFiltradas.add(habitacion);
                        }
                    }

                    Log.d("HotelRepository", "Resultado: " + habitaciones.size() + " → " + habitacionesFiltradas.size() + " habitaciones");
                    Log.d("HotelRepository", "===============================");

                    onSuccess.accept(habitacionesFiltradas);
                },
                onError
        );
    }


    private static boolean puedeAcomodarCapacidad(Habitacion habitacion, int adultosRequeridos, int ninosRequeridos) {
        Integer capacidadAdultos = habitacion.getCapacidadAdultos();
        Integer capacidadNinos = habitacion.getCapacidadNinos();

        if (capacidadAdultos == null || capacidadNinos == null) {
            return false;
        }

        // CORREGIDO: Puede acomodar AL MENOS la capacidad requerida (mayor o igual)
        return capacidadAdultos >= adultosRequeridos && capacidadNinos >= ninosRequeridos;
    }
    /**
     * FUNCIÓN ROBUSTA: Obtiene la capacidad real de una habitación
     * Verifica múltiples fuentes de datos
     */
    /*private static int[] getHabitacionCapacity(Habitacion habitacion) {
        if (habitacion == null) {
            return new int[]{0, 0}; // [adultos, niños]
        }

        Integer adultos = null;
        Integer ninos = null;

        // Estrategia 1: Intentar con campos directos
        if (habitacion.getCapacidadAdultos() != null) {
            adultos = habitacion.getCapacidadAdultos();
        }
        if (habitacion.getCapacidadNinos() != null) {
            ninos = habitacion.getCapacidadNinos();
        }

        // Estrategia 2: Intentar con objeto Capacidad
        if ((adultos == null || ninos == null) && habitacion.getCapacidad() != null) {
            if (adultos == null && habitacion.getCapacidad().getAdultos() != null) {
                adultos = habitacion.getCapacidad().getAdultos();
            }
            if (ninos == null && habitacion.getCapacidad().getNinos() != null) {
                ninos = habitacion.getCapacidad().getNinos();
            }
        }

        // Valores por defecto solo si es absolutamente necesario
        if (adultos == null) adultos = 0;
        if (ninos == null) ninos = 0;

        return new int[]{adultos, ninos};
    }*/
    /**
     * FUNCIÓN ROBUSTA ACTUALIZADA: Obtiene la capacidad real de una habitación
     */
    private static int[] getHabitacionCapacity(Habitacion habitacion) {
        if (habitacion == null) {
            Log.w("HotelRepository", "getHabitacionCapacity: habitacion es null");
            return new int[]{0, 0};
        }

        Integer adultos = null;
        Integer ninos = null;

        // Estrategia 1: Campos directos (más confiable)
        if (habitacion.getCapacidadAdultos() != null) {
            adultos = habitacion.getCapacidadAdultos();
            Log.d("HotelRepository", "Capacidad adultos desde campo directo: " + adultos);
        }
        if (habitacion.getCapacidadNinos() != null) {
            ninos = habitacion.getCapacidadNinos();
            Log.d("HotelRepository", "Capacidad niños desde campo directo: " + ninos);
        }

        // Estrategia 2: Objeto Capacidad (fallback)
        if ((adultos == null || ninos == null) && habitacion.getCapacidad() != null) {
            if (adultos == null && habitacion.getCapacidad().getAdultos() != null) {
                adultos = habitacion.getCapacidad().getAdultos();
                Log.d("HotelRepository", "Capacidad adultos desde objeto Capacidad: " + adultos);
            }
            if (ninos == null && habitacion.getCapacidad().getNinos() != null) {
                ninos = habitacion.getCapacidad().getNinos();
                Log.d("HotelRepository", "Capacidad niños desde objeto Capacidad: " + ninos);
            }
        }

        // Valores por defecto SOLO si no hay datos
        if (adultos == null) {
            adultos = 0;
            Log.w("HotelRepository", "Sin datos de capacidad adultos, usando 0");
        }
        if (ninos == null) {
            ninos = 0;
            Log.w("HotelRepository", "Sin datos de capacidad niños, usando 0");
        }

        Log.d("HotelRepository", "Capacidad final habitación " + habitacion.getNumero() +
                ": [" + adultos + " adultos, " + ninos + " niños]");

        return new int[]{adultos, ninos};
    }
    /**
     * FUNCIÓN ADICIONAL: Debug para verificar datos de habitación
     */
    private static void debugHabitacionData(Habitacion habitacion) {
        if (habitacion == null) {
            Log.d("HotelRepository", "DEBUG: Habitación es null");
            return;
        }

        Log.d("HotelRepository", "=== DEBUG HABITACIÓN ===");
        Log.d("HotelRepository", "ID: " + habitacion.getId());
        Log.d("HotelRepository", "Número: " + habitacion.getNumero());
        Log.d("HotelRepository", "Estado: " + habitacion.getEstado());

        // Verificar campos directos
        Log.d("HotelRepository", "CapacidadAdultos directo: " + habitacion.getCapacidadAdultos());
        Log.d("HotelRepository", "CapacidadNinos directo: " + habitacion.getCapacidadNinos());

        // Verificar objeto Capacidad
        if (habitacion.getCapacidad() != null) {
            Log.d("HotelRepository", "Capacidad.adultos: " + habitacion.getCapacidad().getAdultos());
            Log.d("HotelRepository", "Capacidad.ninos: " + habitacion.getCapacidad().getNinos());
        } else {
            Log.d("HotelRepository", "Objeto Capacidad es null");
        }

        // Capacidad final calculada
        int[] capacidad = getHabitacionCapacity(habitacion);
        Log.d("HotelRepository", "Capacidad FINAL: [" + capacidad[0] + " adultos, " + capacidad[1] + " niños]");
        Log.d("HotelRepository", "========================");
    }
    /**
     * NUEVA FUNCIÓN: Filtra las habitaciones de un hotel para mostrar solo las que cumplen criterio
     * Llama a esta función antes de enviar los hoteles al adapter
     */
    private static void filtrarHabitacionesQueNoComplean(List<Hotel> hoteles, int requiredAdults, int requiredChildren) {
        Log.d("HotelRepository", "=== FILTRANDO HABITACIONES QUE NO CUMPLEN ===");

        for (Hotel hotel : hoteles) {
            if (hotel.getHabitaciones() != null && !hotel.getHabitaciones().isEmpty()) {
                List<Habitacion> habitacionesOriginales = new ArrayList<>(hotel.getHabitaciones());
                List<Habitacion> habitacionesFiltradas = new ArrayList<>();

                Log.d("HotelRepository", String.format(
                        "Hotel %s: Filtrando %d habitaciones",
                        hotel.getNombre(), habitacionesOriginales.size()
                ));

                for (Habitacion habitacion : habitacionesOriginales) {
                    if (hasMinimumCapacityFromHabitacion(habitacion, requiredAdults, requiredChildren)) {
                        habitacionesFiltradas.add(habitacion);
                        Log.d("HotelRepository", String.format(
                                "✅ Habitación %s incluida", habitacion.getNumero()
                        ));
                    } else {
                        Log.d("HotelRepository", String.format(
                                "❌ Habitación %s filtrada (no cumple capacidad)", habitacion.getNumero()
                        ));
                    }
                }

                // Actualizar las habitaciones del hotel con solo las que cumplen
                hotel.setHabitaciones(habitacionesFiltradas);

                Log.d("HotelRepository", String.format(
                        "Hotel %s: %d habitaciones originales → %d habitaciones filtradas",
                        hotel.getNombre(), habitacionesOriginales.size(), habitacionesFiltradas.size()
                ));
            }
        }
    }




}
