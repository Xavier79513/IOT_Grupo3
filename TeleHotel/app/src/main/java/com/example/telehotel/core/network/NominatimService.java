package com.example.telehotel.core.network;

import com.example.telehotel.data.model.NominatimPlace;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimService {

    /**
     * Búsqueda básica (método original)
     */
    @GET("search")
    Call<List<NominatimPlace>> searchCities(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit
    );

    /**
     * Búsqueda avanzada con más parámetros
     */
    @GET("search")
    Call<List<NominatimPlace>> searchCities(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("class") String placeClass,
            @Query("addressdetails") int addressDetails,
            @Query("accept-language") String language
    );

    /**
     * Búsqueda con filtro por país
     */
    @GET("search")
    Call<List<NominatimPlace>> searchCitiesInCountry(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("countrycodes") String countryCodes,
            @Query("addressdetails") int addressDetails
    );

    /**
     * Búsqueda por coordenadas (geocodificación inversa)
     */
    @GET("reverse")
    Call<NominatimPlace> reverseGeocode(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails
    );
}