package com.example.telehotel.core;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración simple de Cloudinary para TeleHotel
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";

    // TODO: Reemplaza con tus credenciales reales de Cloudinary
    private static final String CLOUD_NAME = "tu_cloud_name";
    private static final String UPLOAD_PRESET = "telehotel_unsigned"; // Preset unsigned

    private static boolean isInitialized = false;

    /**
     * Inicializa Cloudinary - llama esto en Application.onCreate()
     */
    public static void initialize(Context context) {
        if (isInitialized) return;

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("secure", "true");

            MediaManager.init(context, config);
            isInitialized = true;

            android.util.Log.d(TAG, "Cloudinary inicializado correctamente");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error inicializando Cloudinary", e);
        }
    }

    public static String getUploadPreset() {
        return UPLOAD_PRESET;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}