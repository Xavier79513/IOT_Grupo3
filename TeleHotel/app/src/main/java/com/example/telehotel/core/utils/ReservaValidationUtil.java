package com.example.telehotel.core.utils;

import android.util.Log;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.ServicioAdicional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * 🔥 UTILIDAD REUTILIZABLE para validar reservas activas
 *
 * Esta clase encapsula toda la lógica para verificar si un usuario
 * tiene reservas activas y puede ser usada en cualquier Fragment o Activity
 */
public class ReservaValidationUtil {

    private static final String TAG = "ReservaValidationUtil";

    private FirebaseFirestore db;
    private PrefsManager prefsManager;

    public ReservaValidationUtil(PrefsManager prefsManager) {
        this.db = FirebaseFirestore.getInstance();
        this.prefsManager = prefsManager;
    }

    // ============= 🔥 MÉTODO PRINCIPAL PARA USAR EN CUALQUIER VISTA =============

    /**
     * 🔥 MÉTODO PRINCIPAL: Verifica si el usuario actual tiene reserva activa
     *
     * @param callback Callback que recibe el resultado
     */
    public void verificarReservaActiva(ReservaValidationCallback callback) {
        // 1. Verificar usuario válido
        if (!tieneUsuarioValido()) {
            callback.onResult(false, null, "Usuario no autenticado");
            return;
        }

        String userId = getCurrentUserId();
        Log.d(TAG, "Verificando reserva activa para usuario: " + userId);

        // 2. Consultar reservas activas en Firebase
        db.collection("reservas")
                .whereEqualTo("clienteId", userId)
                .whereIn("estado", Arrays.asList("activa", "confirmada", "CONFIRMADA", "en_proceso"))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Documentos encontrados: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Hay reserva(s), verificar si es válida
                        Reserva reserva = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(Reserva.class);

                        if (reserva != null) {
                            reserva.setId(queryDocumentSnapshots.getDocuments().get(0).getId());

                            Log.d(TAG, "Reserva encontrada - ID: " + reserva.getId() +
                                    ", Estado: " + reserva.getEstado());

                            // 3. Validar que la reserva esté realmente activa
                            if (esReservaRealmenteActiva(reserva)) {
                                callback.onResult(true, reserva, "Reserva activa encontrada");
                            } else {
                                callback.onResult(false, reserva, "Reserva no está en período válido");
                            }
                        } else {
                            callback.onResult(false, null, "Error procesando reserva");
                        }
                    } else {
                        // No hay reservas activas
                        callback.onResult(false, null, "No tienes reservas activas");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error consultando reservas", e);
                    callback.onResult(false, null, "Error de conexión: " + e.getMessage());
                });
    }

    // ============= MÉTODOS DE VALIDACIÓN =============

    /**
     * 🔥 VALIDACIÓN COMPLETA: Verifica si una reserva está realmente activa
     */
    private boolean esReservaRealmenteActiva(Reserva reserva) {
        if (reserva == null) {
            Log.d(TAG, "❌ Reserva es null");
            return false;
        }

        // 1. Verificar que NO haya hecho checkout
        if (yaHizoCheckout(reserva)) {
            Log.d(TAG, "❌ Ya hizo checkout");
            return false;
        }

        // 2. Verificar estado válido
        if (!tieneEstadoValido(reserva)) {
            Log.d(TAG, "❌ Estado no válido: " + reserva.getEstado());
            return false;
        }

        // 3. Verificar período de estadía
        if (!estaEnPeriodoValido(reserva)) {
            Log.d(TAG, "❌ Fuera del período válido");
            return false;
        }

        Log.d(TAG, "✅ Reserva es válida");
        return true;
    }

    /**
     * Verifica si ya se realizó el checkout
     */
    private boolean yaHizoCheckout(Reserva reserva) {
        if (reserva == null) return false;

        // 1. Verificar si tiene datos de checkout
        if (reserva.getCheckout() != null) {
            Log.d(TAG, "Checkout detectado por datos de checkout");
            return true;
        }

        // 2. Verificar estados que indican checkout realizado
        String estado = reserva.getEstado();
        if (estado != null) {
            estado = estado.toLowerCase().trim();
            boolean esCheckout = estado.equals("checkout_realizado") ||
                    estado.equals("finalizada") ||
                    estado.equals("completada") ||
                    estado.equals("cerrada") ||
                    estado.equals("terminada") ||
                    estado.equals("finished");

            if (esCheckout) {
                Log.d(TAG, "Checkout detectado por estado: " + estado);
            }

            return esCheckout;
        }

        return false;
    }

    /**
     * Verifica si el estado de la reserva es válido para estar activa
     */
    private boolean tieneEstadoValido(Reserva reserva) {
        if (reserva == null) return false;

        String estado = reserva.getEstado();
        if (estado == null || estado.trim().isEmpty()) {
            // Sin estado específico, considerar válida si no hay checkout
            return !yaHizoCheckout(reserva);
        }

        estado = estado.toLowerCase().trim();

        // Estados que indican reserva activa
        boolean esValido = estado.equals("activa") ||
                estado.equals("confirmada") ||
                estado.equals("en_proceso") ||
                estado.equals("vigente") ||
                estado.equals("active") ||
                estado.equals("confirmed");

        Log.d(TAG, "Estado '" + estado + "' es válido: " + esValido);
        return esValido;
    }

    /**
     * Verifica si la reserva está en período válido (no vencida)
     */
    private boolean estaEnPeriodoValido(Reserva reserva) {
        if (reserva == null) return false;

        long ahora = System.currentTimeMillis();

        // Estrategia 1: Usar fechaInicio y fechaFin (timestamps)
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            boolean enPeriodo = ahora >= reserva.getFechaInicio() && ahora <= reserva.getFechaFin();
            Log.d(TAG, String.format("Período por timestamps - Inicio: %d, Fin: %d, Ahora: %d, Válido: %s",
                    reserva.getFechaInicio(), reserva.getFechaFin(), ahora, enPeriodo));
            return enPeriodo;
        }

        // Estrategia 2: Usar fechaReservaTimestamp con período extendido
        if (reserva.getFechaReservaTimestamp() != null) {
            long unDiaEnMs = 24 * 60 * 60 * 1000L;
            long treintaDiasEnMs = 30 * unDiaEnMs;

            // Válida desde la fecha de reserva hasta 30 días después
            long limiteInferior = reserva.getFechaReservaTimestamp();
            long limiteSuperior = reserva.getFechaReservaTimestamp() + treintaDiasEnMs;

            boolean enPeriodo = ahora >= limiteInferior && ahora <= limiteSuperior;
            Log.d(TAG, String.format("Período por fechaReserva - Reserva: %d, Límite: %d, Ahora: %d, Válido: %s",
                    limiteInferior, limiteSuperior, ahora, enPeriodo));
            return enPeriodo;
        }

        // Estrategia 3: Si no hay fechas, considerar válida (dejar que otros criterios decidan)
        Log.d(TAG, "Sin fechas disponibles, considerando válida por fechas");
        return true;
    }

    // ============= MÉTODOS AUXILIARES =============

    /**
     * Verifica si hay información de usuario válida
     */
    private boolean tieneUsuarioValido() {
        if (prefsManager == null) {
            Log.e(TAG, "PrefsManager es null");
            return false;
        }

        boolean isLoggedIn = prefsManager.isUserLoggedIn();
        String userId = prefsManager.getUserId();

        boolean esValido = isLoggedIn &&
                userId != null &&
                !userId.trim().isEmpty();

        Log.d(TAG, String.format("Usuario válido - LoggedIn: %s, UserId: %s, Válido: %s",
                isLoggedIn, userId, esValido));

        return esValido;
    }

    /**
     * Obtiene el ID del usuario actual
     */
    private String getCurrentUserId() {
        if (prefsManager != null) {
            return prefsManager.getUserId();
        }

        // Fallback: Firebase Auth
        try {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo userId", e);
            return null;
        }
    }

    // ============= MÉTODOS ADICIONALES ÚTILES =============

    /**
     * Calcula el monto total real incluyendo servicios adicionales
     */
    public double calcularMontoTotalConServicios(Reserva reserva) {
        if (reserva == null) return 0.0;

        double montoBase = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        double montoServicios = 0.0;

        if (reserva.getServiciosAdicionales() != null) {
            for (ServicioAdicional servicio : reserva.getServiciosAdicionales()) {
                if (servicio.precio != null && servicio.cantidad != null) {
                    montoServicios += servicio.precio * servicio.cantidad;
                }
            }
        }

        return montoBase + montoServicios;
    }

    /**
     * Obtiene las fechas formateadas de la reserva
     */
    public String obtenerFechasFormateadas(Reserva reserva) {
        if (reserva == null) return "Fechas no disponibles";

        try {
            if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                LocalDate inicio = Instant.ofEpochMilli(reserva.getFechaInicio())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate fin = Instant.ofEpochMilli(reserva.getFechaFin())
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                return inicio.toString() + " - " + fin.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formateando fechas", e);
        }

        return "Fechas no disponibles";
    }

    /**
     * Obtiene información resumida de la reserva para mostrar en UI
     */
    public String obtenerResumenReserva(Reserva reserva) {
        if (reserva == null) return "Información no disponible";

        StringBuilder resumen = new StringBuilder();

        // Información básica
        if (reserva.getHotelNombre() != null) {
            resumen.append("🏨 ").append(reserva.getHotelNombre()).append("\n");
        }

        if (reserva.getHotelUbicacion() != null) {
            resumen.append("📍 ").append(reserva.getHotelUbicacion()).append("\n");
        }

        // Fechas
        String fechas = obtenerFechasFormateadas(reserva);
        if (!fechas.equals("Fechas no disponibles")) {
            resumen.append("📅 ").append(fechas).append("\n");
        }

        // Código de reserva
        if (reserva.getCodigoReserva() != null) {
            resumen.append("🎫 ").append(reserva.getCodigoReserva()).append("\n");
        }

        // Monto total
        double montoTotal = calcularMontoTotalConServicios(reserva);
        resumen.append("💰 S/").append(String.format("%.2f", montoTotal));

        return resumen.toString();
    }

    // ============= INTERFACE =============

    /**
     * Callback para recibir el resultado de la validación
     */
    public interface ReservaValidationCallback {
        /**
         * @param tieneReserva true si tiene reserva activa válida
         * @param reserva la reserva encontrada (puede ser null)
         * @param mensaje mensaje explicativo del resultado
         */
        void onResult(boolean tieneReserva, Reserva reserva, String mensaje);
    }
}