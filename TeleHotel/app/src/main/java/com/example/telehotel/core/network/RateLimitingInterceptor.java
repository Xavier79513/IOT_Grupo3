package com.example.telehotel.core.network;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor para controlar la velocidad de requests a Nominatim
 * Nominatim requiere máximo 1 request por segundo
 */
public class RateLimitingInterceptor implements Interceptor {
    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 1000; // 1 segundo en millisegundos

    @Override
    public Response intercept(Chain chain) throws IOException {
        synchronized (RateLimitingInterceptor.class) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;

            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                long sleepTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while rate limiting", e);
                }
            }

            lastRequestTime = System.currentTimeMillis();
            return chain.proceed(chain.request());
        }
    }
}