package com.example.telehotel.core;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración completa de Cloudinary para TeleHotel
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";

    // ✅ CONFIGURACIÓN COMPLETA DE CLOUDINARY
    private static final String CLOUD_NAME = "dwqmdnqrx";
    private static final String API_KEY = "347514942719473";       // 🔑 Tu API Key
    private static final String API_SECRET = "vHfSf56CTgMGXrcK1AeoKNXZgEk"; // 🔐 Tu API Secret
    private static final String UPLOAD_PRESET = "telehotel_unsigned"; // Preset unsigned

    private static boolean isInitialized = false;

    /**
     * Inicializa Cloudinary con configuración completa
     */
    public static void initialize(Context context) {
        if (isInitialized) return;

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);  // ✅ AGREGAR API SECRET
            config.put("secure", "true");

            MediaManager.init(context, config);
            isInitialized = true;

            android.util.Log.d(TAG, "✅ Cloudinary inicializado correctamente");
            android.util.Log.d(TAG, "Cloud Name: " + CLOUD_NAME);
            android.util.Log.d(TAG, "API Key: " + API_KEY.substring(0, 4) + "***"); // Solo mostrar primeros 4 dígitos

        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Error inicializando Cloudinary", e);
        }
    }

    public static String getUploadPreset() {
        return UPLOAD_PRESET;
    }

    public static String getCloudName() {
        return CLOUD_NAME;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Método para verificar configuración (útil para debugging)
     */
    public static boolean isConfigurationValid() {
        return !CLOUD_NAME.isEmpty() &&
                !API_KEY.equals("TU_API_KEY_AQUI") &&
                !API_SECRET.equals("TU_API_SECRET_AQUI");
    }
}