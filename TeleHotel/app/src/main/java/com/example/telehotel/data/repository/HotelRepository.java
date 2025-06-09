package com.example.telehotel.data.repository;

import androidx.annotation.NonNull;

import com.example.telehotel.core.FirebaseUtil;
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
}