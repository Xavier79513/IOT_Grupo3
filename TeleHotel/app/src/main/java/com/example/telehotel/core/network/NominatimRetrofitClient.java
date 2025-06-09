package com.example.telehotel.core.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class NominatimRetrofitClient {
    private static final String BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String TAG = "NominatimClient";
    private static Retrofit retrofit = null;
    private static NominatimService service = null;

    public static NominatimService getService() {
        if (service == null) {
            service = getRetrofitInstance().create(NominatimService.class);
        }
        return service;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Configurar timeouts
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);

        // Agregar User-Agent personalizado (requerido por Nominatim)
        builder.addInterceptor(chain -> {
            return chain.proceed(
                    chain.request()
                            .newBuilder()
                            .addHeader("User-Agent", "TeleHotel-App/1.0 (Android)")
                            .addHeader("Accept", "application/json")
                            .build()
            );
        });

        // Logging interceptor (solo en debug)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    message -> Log.d(TAG, message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        // Interceptor para rate limiting (Nominatim requiere máximo 1 request por segundo)
        builder.addInterceptor(new RateLimitingInterceptor());

        return builder.build();
    }

    /**
     * Resetea la instancia del cliente (útil para testing)
     */
    public static void resetInstance() {
        retrofit = null;
        service = null;
    }
}