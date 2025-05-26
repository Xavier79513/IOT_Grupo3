package com.example.telehotel.core.network;
import com.example.telehotel.data.model.NominatimPlace;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface NominatimService {

    @GET("search")
    Call<List<NominatimPlace>> searchCities(
            @Query("city") String city,
            @Query("format") String format,
            @Query("limit") int limit
    );
}
