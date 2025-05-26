package com.example.telehotel.data.repository;
import androidx.annotation.NonNull;

import com.example.telehotel.core.network.NominatimService;
import com.example.telehotel.core.network.NominatimRetrofitClient;
import com.example.telehotel.data.model.NominatimPlace;

import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CityRepository {

    public static void searchCities(@NonNull String query,
                                    @NonNull Consumer<List<NominatimPlace>> onSuccess,
                                    @NonNull Consumer<Throwable> onError) {
        NominatimService service = NominatimRetrofitClient.getService();
        Call<List<NominatimPlace>> call = service.searchCities(query, "json", 5);

        call.enqueue(new Callback<List<NominatimPlace>>() {
            @Override
            public void onResponse(Call<List<NominatimPlace>> call, Response<List<NominatimPlace>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.accept(response.body());
                } else {
                    onError.accept(new Exception("Respuesta no exitosa"));
                }
            }

            @Override
            public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                onError.accept(t);
            }
        });
    }
}
