package com.example.telehotel.core.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogUtils {

    private static final String TAG = "LogUtils";
    private static final String COLLECTION_NAME = "sistema_logs";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Tipos de acciones para categorizar los logs
    public static class ActionType {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String SYSTEM = "SYSTEM";
        public static final String ERROR = "ERROR";
    }

    /**
     * Registra una nueva actividad en el sistema
     * @param action Tipo de acción realizada
     * @param usuarioId ID del usuario que realizó la acción
     * @param descripcion Descripción detallada de la acción
     */
    public static void registrarActividad(String action, String usuarioId, String descripcion) {
        registrarActividad(action, usuarioId, descripcion, null);
    }

    /**
     * Registra una nueva actividad en el sistema con callback
     * @param action Tipo de acción realizada
     * @param usuarioId ID del usuario que realizó la acción
     * @param descripcion Descripción detallada de la acción
     * @param callback Callback para manejar el resultado
     */
    public static void registrarActividad(String action, String usuarioId, String descripcion, LogCallback callback) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            Map<String, Object> logData = new HashMap<>();
            logData.put("accion", action);
            logData.put("usuarioId", usuarioId != null ? usuarioId : "SISTEMA");
            logData.put("descripcion", descripcion);
            logData.put("timestamp", timestamp);
            logData.put("fecha_creacion", new Date());

            db.collection(COLLECTION_NAME)
                    .add(logData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Log registrado exitosamente: " + documentReference.getId());
                        if (callback != null) {
                            callback.onSuccess(documentReference.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al registrar log", e);
                        if (callback != null) {
                            callback.onError(e);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error al crear log", e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    /**
     * Obtiene los logs del sistema ordenados por fecha
     * @param limit Número máximo de logs a obtener
     * @param callback Callback para recibir los logs
     */
    public static void obtenerLogs(int limit, LogsCallback callback) {
        db.collection(COLLECTION_NAME)
                .orderBy("fecha_creacion", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (callback != null) {
                        callback.onSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener logs", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Elimina todos los logs del sistema
     * @param callback Callback para manejar el resultado
     */
    public static void limpiarLogs(LogCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (callback != null) {
                            callback.onSuccess("No hay logs para eliminar");
                        }
                        return;
                    }

                    // Eliminar en lotes para mejor performance
                    FirebaseFirestore.getInstance().runBatch(batch -> {
                        queryDocumentSnapshots.forEach(document ->
                                batch.delete(document.getReference()));
                    }).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Logs eliminados exitosamente");
                        if (callback != null) {
                            callback.onSuccess("Logs eliminados exitosamente");
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error al eliminar logs", e);
                        if (callback != null) {
                            callback.onError(e);
                        }
                    });
                });
    }

    // Métodos de conveniencia para diferentes tipos de acciones
    public static void logCrearUsuario(String adminId, String nombreUsuario) {
        registrarActividad(ActionType.CREATE, adminId,
                "Se creó el usuario: " + nombreUsuario);
    }

    public static void logEliminarUsuario(String adminId, String nombreUsuario) {
        registrarActividad(ActionType.DELETE, adminId,
                "Se eliminó el usuario: " + nombreUsuario);
    }

    public static void logLogin(String usuarioId, String email) {
        registrarActividad(ActionType.LOGIN, usuarioId,
                "Inicio de sesión: " + email);
    }

    public static void logLogout(String usuarioId, String email) {
        registrarActividad(ActionType.LOGOUT, usuarioId,
                "Cerró sesión: " + email);
    }

    public static void logSistema(String descripcion) {
        registrarActividad(ActionType.SYSTEM, null, descripcion);
    }

    public static void logError(String error, String usuarioId) {
        registrarActividad(ActionType.ERROR, usuarioId,
                "Error del sistema: " + error);
    }

    // Interfaces para callbacks
    public interface LogCallback {
        void onSuccess(String result);
        void onError(Exception e);
    }

    public interface LogsCallback {
        void onSuccess(com.google.firebase.firestore.QuerySnapshot logs);
        void onError(Exception e);
    }
}