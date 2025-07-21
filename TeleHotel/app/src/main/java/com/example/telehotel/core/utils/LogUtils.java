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
        public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
        public static final String HOTEL_MANAGEMENT = "HOTEL_MANAGEMENT";
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
                .orderBy("fechaCreacionMillis", Query.Direction.DESCENDING)  // ✅ CAMBIO: Ordenar por fechaCreacionMillis
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
    // ✅ TAMBIÉN CAMBIAR el método registrarActividadCompleta:
    public static void registrarActividadCompleta(String action, String usuarioId,
                                                  String usuarioEmail, String usuarioNombre,
                                                  String usuarioRole, String descripcion) {
        try {
            // ✅ CAMBIO: Crear nueva instancia con timestamp único
            com.example.telehotel.data.model.LogSistema log =
                    new com.example.telehotel.data.model.LogSistema(
                            action, usuarioId, usuarioEmail, usuarioNombre, usuarioRole, descripcion);

            // ✅ AGREGAR: Log de debug para verificar timestamp
            Log.d(TAG, "Creando log con timestamp: " + log.getFechaCreacionMillis() +
                    " - " + log.getFormattedTime());

            db.collection(COLLECTION_NAME)
                    .add(log)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Log completo registrado exitosamente: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al registrar log completo", e);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error al crear log completo", e);
        }
    }

    // ✅ NUEVO: Obtener información del usuario actual y registrar
    public static void registrarActividadConUsuarioActual(String action, String descripcion) {
        com.google.firebase.auth.FirebaseUser currentUser =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Si no hay usuario, registrar como sistema
            registrarActividad(action, null, descripcion);
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        // Obtener información completa del usuario desde Firestore
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "Usuario";
                    String userRole = "desconocido";

                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("nombres");
                        userRole = documentSnapshot.getString("role");

                        if (userName == null || userName.isEmpty()) {
                            userName = documentSnapshot.getString("name"); // Fallback
                        }
                        if (userName == null || userName.isEmpty()) {
                            userName = userEmail; // Último fallback
                        }
                    }

                    registrarActividadCompleta(action, userId, userEmail, userName, userRole, descripcion);
                })
                .addOnFailureListener(e -> {
                    // Si falla, registrar con información básica
                    registrarActividadCompleta(action, userId, userEmail, userEmail, "desconocido", descripcion);
                });
    }

    // ✅ NUEVO: Log de login completo
    public static void logLoginCompleto(String userId, String userEmail, String userName, String userRole) {
        String descripcion = String.format("Acceso exitoso al sistema como %s - %s (%s)",
                getRoleDisplayName(userRole), userName, userEmail);

        registrarActividadCompleta(ActionType.LOGIN, userId, userEmail, userName, userRole, descripcion);
    }

    // ✅ NUEVO: Log de gestión de usuarios
    public static void logUserManagement(String action, String targetUserEmail, String targetUserName) {
        String descripcion = String.format("%s usuario: %s (%s)", action, targetUserName, targetUserEmail);
        registrarActividadConUsuarioActual("USER_MANAGEMENT", descripcion);
    }

    // ✅ NUEVO: Método helper para nombres de roles
    private static String getRoleDisplayName(String role) {
        if (role == null) return "Usuario";

        switch (role.toLowerCase()) {
            case "admin": return "Administrador";
            case "superadmin": return "Super Administrador";
            case "taxista": return "Taxista";
            case "cliente": return "Cliente";
            default: return role;
        }
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