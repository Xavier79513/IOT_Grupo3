package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.example.telehotel.data.model.ServicioAdicional;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

public class TaxiReservationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "TaxiReservationFragment";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    // Views principales
    private View rootView;
    private MapView mapView;
    private GoogleMap googleMap;

    // Firebase y gestión de datos
    private FirebaseFirestore db;
    private PrefsManager prefsManager;
    private String currentUserId;

    // Data
    private Reserva reservaActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cliente_fragment_taxi, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFirebase();
        setupMapView(savedInstanceState);
        setupViews();
        mostrarVistaApropriada();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        prefsManager = new PrefsManager(requireContext());
        currentUserId = prefsManager.getUserId();

        // Log para debugging
        Log.d(TAG, "Usuario actual desde PrefsManager: " + currentUserId);
        Log.d(TAG, "Usuario logueado: " + prefsManager.isUserLoggedIn());
    }

    private void setupMapView(Bundle savedInstanceState) {
        mapView = rootView.findViewById(R.id.mapView);
        if (mapView != null) {
            Bundle mapViewBundle = savedInstanceState != null
                    ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
                    : null;
            mapView.onCreate(mapViewBundle);
            mapView.getMapAsync(this);
        }
    }

    private void setupViews() {
        // Configurar botón para hacer reserva
        View btnReservar = rootView.findViewById(R.id.btn_ir_reservar);
        if (btnReservar != null) {
            btnReservar.setOnClickListener(v -> navegarAHoteles());
        }

        // Configurar botón para agregar taxi
        View btnTaxi = rootView.findViewById(R.id.btn_agregar_taxi);
        if (btnTaxi != null) {
            btnTaxi.setOnClickListener(v -> abrirSolicitudTaxi());
        }
    }

    // ✅ MÉTODO PRINCIPAL ACTUALIZADO QUE USA LAS FUNCIONES REALES
    private void mostrarVistaApropriada() {
        // Verificar primero si hay usuario válido
        if (!tieneUsuarioValido()) {
            mostrarVistaSinReserva("Inicia sesión para ver tus reservas");
            return;
        }

        mostrarMensajeCargando();

        verificarTieneReservaActiva(new ReservaValidationCallback() {
            @Override
            public void onResult(boolean tieneReserva, Reserva reserva, String mensaje) {
                if (!tieneReserva) {
                    mostrarVistaSinReserva(mensaje);
                    return;
                }

                // Tiene reserva válida
                reservaActual = reserva;

                // Guardar información en PrefsManager para uso posterior
                guardarReservaEnPrefs(reserva);

                // Verificar estado del taxi
                if (tieneTaxiSolicitado(reserva)) {
                    mostrarVistaConTaxi(reserva, reserva.getSolicitudTaxi());
                } else {
                    mostrarVistaSinTaxi(reserva);
                }
            }
        });
    }

    // ✅ FUNCIONES REALES REEMPLAZANDO LA SIMULACIÓN

    /**
     * Verifica si el usuario tiene una reserva activa consultando Firebase
     */
    private void verificarTieneReservaActiva(ReservaValidationCallback callback) {
        // Verificar usuario usando PrefsManager
        if (currentUserId == null || !prefsManager.isUserLoggedIn()) {
            callback.onResult(false, null, "Usuario no autenticado");
            return;
        }

        Log.d(TAG, "Consultando reservas para usuario: " + currentUserId);

        db.collection("reservas")
                .whereEqualTo("clienteId", currentUserId)
                .whereIn("estado", Arrays.asList("activa", "confirmada", "CONFIRMADA", "en_proceso"))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Documentos encontrados: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        Reserva reserva = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(Reserva.class);

                        if (reserva != null) {
                            reserva.setId(queryDocumentSnapshots.getDocuments().get(0).getId());

                            Log.d(TAG, "Reserva encontrada ID: " + reserva.getId());
                            Log.d(TAG, "Estado reserva: " + reserva.getEstado());

                            // Validar que la reserva esté en período válido
                            if (esReservaEnPeriodoValido(reserva)) {
                                callback.onResult(true, reserva, "Reserva activa encontrada");
                            } else {
                                callback.onResult(false, reserva, "Reserva fuera del período válido");
                            }
                        } else {
                            callback.onResult(false, null, "Error al procesar reserva");
                        }
                    } else {
                        callback.onResult(false, null, "No hay reservas activas");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error consultando reservas", e);
                    callback.onResult(false, null, "Error de conexión: " + e.getMessage());
                });
    }

    /**
     * Verifica si la reserva tiene taxi solicitado basándose en los datos reales
     */
    private boolean tieneTaxiSolicitado(Reserva reserva) {
        if (reserva == null) return false;

        SolicitudTaxi solicitudTaxi = reserva.getSolicitudTaxi();
        if (solicitudTaxi == null) return false;

        // Verificar que está marcado como solicitado
        return Boolean.TRUE.equals(solicitudTaxi.getSolicitado());
    }

    /**
     * Valida si la reserva está en un período válido para usar servicios
     */
    private boolean esReservaEnPeriodoValido(Reserva reserva) {
        if (reserva == null) return false;

        // Verificar que no haya hecho checkout
        if (yaHizoCheckout(reserva)) return false;

        // Verificar período de estadía usando timestamps
        return estaEnPeriodoEstadiaConTimestamps(reserva);
    }

    /**
     * Verifica período de estadía usando fechaReservaTimestamp y otros campos timestamp
     */
    private boolean estaEnPeriodoEstadiaConTimestamps(Reserva reserva) {
        long ahora = System.currentTimeMillis();

        // Si tiene fechaInicio y fechaFin (timestamps)
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            //return ahora >= reserva.getFechaInicio() && ahora <= reserva.getFechaFin();
            return ahora <= reserva.getFechaFin();

        }

        // Si solo tiene fechaReservaTimestamp, asumir que es válida por un período
        if (reserva.getFechaReservaTimestamp() != null) {
            long unDiaEnMs = 24 * 60 * 60 * 1000L;
            long treintaDiasEnMs = 30 * unDiaEnMs;

            // Válida desde la fecha de reserva hasta 30 días después
            long limiteInferior = reserva.getFechaReservaTimestamp();
            long limiteSuperior = reserva.getFechaReservaTimestamp() + treintaDiasEnMs;

            return ahora >= limiteInferior && ahora <= limiteSuperior;
        }

        return false;
    }

    /**
     * Verifica si ya se realizó el checkout basándose en los datos reales
     */
    private boolean yaHizoCheckout(Reserva reserva) {
        if (reserva == null) return false;

        // Verificar si tiene datos de checkout
        if (reserva.getCheckout() != null) return true;

        // Verificar estados que indican checkout realizado
        String estado = reserva.getEstado();
        if (estado != null) {
            estado = estado.toLowerCase();
            return estado.equals("checkout_realizado") ||
                    estado.equals("finalizada") ||
                    estado.equals("completada") ||
                    estado.equals("cerrada");
        }

        return false;
    }

    /**
     * Obtiene información detallada del estado del taxi
     */
    private String obtenerEstadoDetalladoTaxi(SolicitudTaxi solicitudTaxi) {
        if (solicitudTaxi == null) return "Sin taxi solicitado";

        if (!Boolean.TRUE.equals(solicitudTaxi.getSolicitado())) {
            return "Taxi no solicitado";
        }

        String estado = solicitudTaxi.getEstado();
        if (estado == null || estado.trim().isEmpty()) {
            return "Taxi solicitado - Procesando";
        }

        // Mapear estados a mensajes amigables
        switch (estado.toLowerCase()) {
            case "solicitado":
                return "Taxi solicitado - Buscando conductor";
            case "asignado":
                return "Conductor asignado - " +
                        (solicitudTaxi.getTaxistaAsignadoId() != null ? "Preparándose" : "Confirmando");
            case "en_camino":
                return "Conductor en camino al hotel";
            case "llegado":
            case "en_destino":
                return "Conductor ha llegado - Listo para partir";
            case "recogido":
            case "en_viaje":
                return "En viaje al aeropuerto";
            case "completado":
            case "finalizado":
                return "Viaje completado";
            case "cancelado":
                return "Servicio cancelado";
            default:
                return "Estado: " + estado;
        }
    }

    /**
     * Verifica si la reserva cumple con el monto mínimo para taxi gratuito
     */
    private void verificarElegibilidadTaxiGratuito(Reserva reserva, ElegibilidadCallback callback) {
        if (reserva == null || reserva.getHotelId() == null) {
            callback.onResult(false, "Datos de reserva incompletos", null);
            return;
        }

        db.collection("hoteles")
                .document(reserva.getHotelId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double montoMinimo = documentSnapshot.getDouble("montoMinimoTaxi");
                        Double montoReserva = calcularMontoTotalConServicios(reserva);

                        if (montoMinimo == null) {
                            callback.onResult(true, "No hay monto mínimo requerido", null);
                            return;
                        }

                        if (montoReserva >= montoMinimo) {
                            callback.onResult(true,
                                    "✓ Cumples el monto mínimo (S/" + montoMinimo + ")",
                                    montoMinimo);
                        } else {
                            callback.onResult(false,
                                    "Monto mínimo requerido: S/" + montoMinimo +
                                            " (Tu reserva: S/" + String.format("%.2f", montoReserva) + ")",
                                    montoMinimo);
                        }
                    } else {
                        callback.onResult(false, "Hotel no encontrado", null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verificando elegibilidad", e);
                    callback.onResult(false, "Error verificando elegibilidad", null);
                });
    }

    /**
     * Calcula el monto total real incluyendo servicios adicionales
     */
    private double calcularMontoTotalConServicios(Reserva reserva) {
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
     * Obtiene información completa de la reserva para mostrar en la UI
     */
    private String obtenerResumenReserva(Reserva reserva) {
        if (reserva == null) return "Información no disponible";

        StringBuilder resumen = new StringBuilder();

        // Información básica
        if (reserva.getHotelNombre() != null) {
            resumen.append("Hotel: ").append(reserva.getHotelNombre()).append("\n");
        }

        // Fechas
        String fechas = obtenerFechasFormateadas(reserva);
        if (!fechas.isEmpty()) {
            resumen.append("Período: ").append(fechas).append("\n");
        }

        // Monto total
        double montoTotal = calcularMontoTotalConServicios(reserva);
        resumen.append("Monto total: S/").append(String.format("%.2f", montoTotal));

        // Servicios adicionales
        if (reserva.getServiciosAdicionales() != null &&
                !reserva.getServiciosAdicionales().isEmpty()) {
            resumen.append("\nServicios adicionales: ")
                    .append(reserva.getServiciosAdicionales().size());
        }

        return resumen.toString();
    }

    /**
     * Obtiene las fechas formateadas de la reserva
     */
    private String obtenerFechasFormateadas(Reserva reserva) {
        if (reserva == null) return "";

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
     * Método para verificar si hay información de usuario válida
     */
    private boolean tieneUsuarioValido() {
        return prefsManager.isUserLoggedIn() &&
                prefsManager.getUserId() != null &&
                !prefsManager.getUserId().trim().isEmpty();
    }

    /**
     * Método para obtener información de usuario para mostrar en la UI
     */
    private String obtenerInfoUsuario() {
        if (!tieneUsuarioValido()) {
            return "Usuario no identificado";
        }

        StringBuilder info = new StringBuilder();

        String nombre = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        if (nombre != null && !nombre.trim().isEmpty()) {
            info.append(nombre);
        } else if (email != null && !email.trim().isEmpty()) {
            info.append(email);
        } else {
            info.append("Usuario: ").append(prefsManager.getUserId().substring(0, 8)).append("...");
        }

        return info.toString();
    }

    /**
     * Método para guardar información de la reserva actual en PrefsManager
     * Útil para compartir datos entre actividades
     */
    private void guardarReservaEnPrefs(Reserva reserva) {
        if (reserva == null) return;

        try {
            // Guardar información básica
            if (reserva.getHotelId() != null) {
                prefsManager.saveHotel(
                        reserva.getHotelId(),
                        reserva.getHotelNombre() != null ? reserva.getHotelNombre() : "",
                        reserva.getHotelUbicacion() != null ? reserva.getHotelUbicacion() : ""
                );
            }

            // Guardar fechas si existen
            if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                prefsManager.saveDateRange(reserva.getFechaInicio(), reserva.getFechaFin());
            }

            // Guardar información de habitación si existe
            if (reserva.getHabitacionTipo() != null) {
                prefsManager.saveRoom(
                        reserva.getHabitacionTipo(),
                        reserva.getHabitacionDescripcion() != null ? reserva.getHabitacionDescripcion() : "",
                        reserva.getHabitacionNumero() != null ? reserva.getHabitacionNumero() : "",
                        reserva.getHabitacionPrecio() != null ? reserva.getHabitacionPrecio() : 0.0,
                        1 // Cantidad por defecto
                );
            }

            // Guardar totales si existen
            if (reserva.getMontoTotal() != null) {
                prefsManager.saveTotals(
                        reserva.getTotalDias() != null ? reserva.getTotalDias() : 1,
                        reserva.getImpuestos() != null ? reserva.getImpuestos() : 0.0,
                        reserva.getMontoTotal()
                );
            }

            Log.d(TAG, "Información de reserva guardada en PrefsManager");

        } catch (Exception e) {
            Log.e(TAG, "Error guardando reserva en PrefsManager", e);
        }
    }

    // Interfaces para callbacks
    interface ReservaValidationCallback {
        void onResult(boolean tieneReserva, Reserva reserva, String mensaje);
    }

    interface ElegibilidadCallback {
        void onResult(boolean esElegible, String mensaje, Double montoMinimo);
    }

    private void mostrarMensajeCargando() {
        ocultarTodasLasVistas();
        // Mostrar un mensaje de carga temporal
        View vistaSinReserva = rootView.findViewById(R.id.vista_sin_reserva);
        if (vistaSinReserva != null) {
            vistaSinReserva.setVisibility(View.VISIBLE);
            TextView mensaje = vistaSinReserva.findViewById(R.id.tv_mensaje_sin_reserva);
            if (mensaje != null) {
                if (tieneUsuarioValido()) {
                    mensaje.setText("Verificando reservas...");
                } else {
                    mensaje.setText("Verificando sesión de usuario...");
                }
            }
        }

        Log.d(TAG, "Usuario actual: " + obtenerInfoUsuario());
    }

    private void ocultarTodasLasVistas() {
        View vistaSinReserva = rootView.findViewById(R.id.vista_sin_reserva);
        View vistaSinTaxi = rootView.findViewById(R.id.vista_sin_taxi);
        View vistaConTaxi = rootView.findViewById(R.id.vista_con_taxi);

        if (vistaSinReserva != null) vistaSinReserva.setVisibility(View.GONE);
        if (vistaSinTaxi != null) vistaSinTaxi.setVisibility(View.GONE);
        if (vistaConTaxi != null) vistaConTaxi.setVisibility(View.GONE);
    }

    private void mostrarVistaSinReserva(String mensaje) {
        ocultarTodasLasVistas();
        View vista = rootView.findViewById(R.id.vista_sin_reserva);
        if (vista != null) {
            vista.setVisibility(View.VISIBLE);

            TextView mensajeView = vista.findViewById(R.id.tv_mensaje_sin_reserva);
            if (mensajeView != null) {
                mensajeView.setText(mensaje);
            }
        }
        Log.d(TAG, "Mostrando vista sin reserva: " + mensaje);
    }

    private void mostrarVistaSinTaxi(Reserva reserva) {
        ocultarTodasLasVistas();
        View vista = rootView.findViewById(R.id.vista_sin_taxi);
        if (vista != null) {
            vista.setVisibility(View.VISIBLE);

            // Información detallada de la reserva
            TextView infoReserva = vista.findViewById(R.id.tv_info_reserva);
            if (infoReserva != null) {
                String resumenReserva = obtenerResumenReserva(reserva);
                infoReserva.setText(resumenReserva);
            }

            // Verificar elegibilidad para taxi gratuito
            verificarMontoMinimoTaxi(reserva);
        }
        Log.d(TAG, "Mostrando vista sin taxi - Reserva ID: " + reserva.getId());
    }

    private void mostrarVistaConTaxi(Reserva reserva, SolicitudTaxi solicitudTaxi) {
        ocultarTodasLasVistas();
        View vista = rootView.findViewById(R.id.vista_con_taxi);
        if (vista != null) {
            vista.setVisibility(View.VISIBLE);

            // Título con información de fechas
            TextView titulo = vista.findViewById(R.id.tv_title);
            if (titulo != null) {
                String fechas = obtenerFechasFormateadas(reserva);
                titulo.setText("Taxi reservado para " + fechas);
            }

            // Estado detallado del taxi
            TextView estado = vista.findViewById(R.id.tv_estado_taxi);
            if (estado != null) {
                String estadoDetallado = obtenerEstadoDetalladoTaxi(solicitudTaxi);
                estado.setText(estadoDetallado);
            }

            // Destino del aeropuerto
            TextView destino = vista.findViewById(R.id.tv_aeropuerto_destino);
            if (destino != null) {
                if (solicitudTaxi.getAeropuertoDestino() != null &&
                        !solicitudTaxi.getAeropuertoDestino().trim().isEmpty()) {
                    destino.setText("Destino: " + solicitudTaxi.getAeropuertoDestino());
                } else {
                    destino.setText("Destino: Por confirmar");
                }
            }

            // Información del taxista si está asignado
            if (solicitudTaxi.getTaxistaAsignadoId() != null) {
                mostrarInformacionTaxista(solicitudTaxi.getTaxistaAsignadoId());
            }

            // Generar código QR si existe
            if (solicitudTaxi.getCodigoQR() != null && !solicitudTaxi.getCodigoQR().trim().isEmpty()) {
                generarCodigoQR(solicitudTaxi.getCodigoQR());
            } else {
                // Generar código temporal si no existe
                String codigoTemp = "TAXI_" + reserva.getId() + "_" + System.currentTimeMillis();
                generarCodigoQR(codigoTemp);
            }
        }
        Log.d(TAG, "Mostrando vista con taxi - Estado: " +
                obtenerEstadoDetalladoTaxi(solicitudTaxi));
    }

    private void mostrarInformacionTaxista(String taxistaId) {
        // Consultar información del taxista
        db.collection("usuarios")
                .document(taxistaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreTaxista = documentSnapshot.getString("nombres");
                        String telefonoTaxista = documentSnapshot.getString("telefono");
                        String placaAuto = documentSnapshot.getString("placaAuto");

                        // Actualizar UI con información del taxista
                        TextView infoTaxista = rootView.findViewById(R.id.tv_info_taxista);
                        if (infoTaxista != null) {
                            StringBuilder info = new StringBuilder();
                            if (nombreTaxista != null) {
                                info.append("Conductor: ").append(nombreTaxista).append("\n");
                            }
                            if (placaAuto != null) {
                                info.append("Placa: ").append(placaAuto).append("\n");
                            }
                            if (telefonoTaxista != null) {
                                info.append("Tel: ").append(telefonoTaxista);
                            }
                            infoTaxista.setText(info.toString());
                            infoTaxista.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error consultando taxista", e);
                });
    }

    private void verificarMontoMinimoTaxi(Reserva reserva) {
        verificarElegibilidadTaxiGratuito(reserva, new ElegibilidadCallback() {
            @Override
            public void onResult(boolean esElegible, String mensaje, Double montoMinimo) {
                TextView tvMontoMinimo = rootView.findViewById(R.id.tv_monto_minimo);
                View btnTaxi = rootView.findViewById(R.id.btn_agregar_taxi);

                if (tvMontoMinimo != null) {
                    tvMontoMinimo.setText(mensaje);

                    if (esElegible) {
                        tvMontoMinimo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        if (btnTaxi != null) btnTaxi.setEnabled(true);
                    } else {
                        tvMontoMinimo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        if (btnTaxi != null) btnTaxi.setEnabled(false);
                    }
                }

                Log.d(TAG, "Elegibilidad taxi: " + esElegible + " - " + mensaje);
            }
        });
    }

    private void generarCodigoQR(String texto) {
        try {
            ImageView qrImage = rootView.findViewById(R.id.qr_code);
            if (qrImage != null) {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(texto, BarcodeFormat.QR_CODE, 400, 400);
                qrImage.setImageBitmap(bitmap);

                TextView codigoTexto = rootView.findViewById(R.id.tv_codigo_qr);
                if (codigoTexto != null) {
                    codigoTexto.setText("Código: " + texto);
                }
            }
        } catch (WriterException e) {
            Log.e(TAG, "Error al generar código QR", e);
        }
    }

    private void navegarAHoteles() {
        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_nav_car_to_nav_rooms);
        } catch (Exception e) {
            Log.e(TAG, "Error al navegar", e);
            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.nav_rooms);
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirSolicitudTaxi() {
        try {
            if (reservaActual != null) {
                Intent intent = new Intent(requireContext(),
                        com.example.telehotel.features.cliente.AgregarTaxiActivity.class);
                intent.putExtra("reservaId", reservaActual.getId());
                intent.putExtra("hotelId", reservaActual.getHotelId());

                // Guardar información adicional en PrefsManager para la actividad
                prefsManager.saveHotel(
                        reservaActual.getHotelId(),
                        reservaActual.getHotelNombre() != null ? reservaActual.getHotelNombre() : "Hotel",
                        reservaActual.getHotelUbicacion() != null ? reservaActual.getHotelUbicacion() : ""
                );

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: No se encontró la reserva", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error abriendo solicitud taxi", e);
            Toast.makeText(getContext(), "Función próximamente disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        // ✅ ACTUALIZAR DATOS AL VOLVER AL FRAGMENT
        mostrarVistaApropriada();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Ubicación por defecto (Lima, Perú)
        LatLng lima = new LatLng(-12.0464, -77.0428);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f));
        googleMap.addMarker(new MarkerOptions().position(lima).title("Lima"));
    }

    // Métodos del ciclo de vida del MapView
    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
            }
            mapView.onSaveInstanceState(mapViewBundle);
        }
    }
}