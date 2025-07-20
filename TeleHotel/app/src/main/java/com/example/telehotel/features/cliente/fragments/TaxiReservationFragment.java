package com.example.telehotel.features.cliente.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.example.telehotel.data.model.ServicioAdicional;
import com.example.telehotel.features.cliente.TaxiPickupActivity;
import com.example.telehotel.features.cliente.TaxiTrackingActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * Fragment para gestión del servicio de taxi
 * SIN dependencias de Google Maps
 */
public class TaxiReservationFragment extends Fragment {

    private static final String TAG = "TaxiReservationFragment";
    private static final int REQUEST_CODE_TAXI_PICKUP = 1001;
    private static final int REQUEST_CODE_TAXI_TRACKING = 1002;

    // Views principales
    private View rootView;

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
        setupMapView();
        setupViews();
        mostrarVistaApropriada();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        prefsManager = new PrefsManager(requireContext());
        currentUserId = prefsManager.getUserId();

        Log.d(TAG, "Usuario actual desde PrefsManager: " + currentUserId);
        Log.d(TAG, "Usuario logueado: " + prefsManager.isUserLoggedIn());
    }

    private void setupMapView() {
        // Configurar mapa OSM si está disponible
        try {
            org.osmdroid.views.MapView osmMapView = rootView.findViewById(R.id.mapView);
            if (osmMapView != null) {
                setupOSMMap(osmMapView);
            }
        } catch (Exception e) {
            Log.w(TAG, "OSM MapView no encontrado o error al configurar: " + e.getMessage());
            // Ocultar el contenedor del mapa si no está disponible
            hideMapView();
        }
    }

    private void setupOSMMap(org.osmdroid.views.MapView osmMapView) {
        try {
            // Configurar mapa OSM básico
            osmMapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
            osmMapView.setMultiTouchControls(true);

            org.osmdroid.api.IMapController mapController = osmMapView.getController();
            mapController.setZoom(12.0);

            // Centrar en Lima
            org.osmdroid.util.GeoPoint lima = new org.osmdroid.util.GeoPoint(-12.0464, -77.0428);
            mapController.setCenter(lima);

            // Agregar marcador simple
            org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(osmMapView);
            marker.setPosition(lima);
            marker.setTitle("Lima, Perú");
            osmMapView.getOverlays().add(marker);

            Log.d(TAG, "Mapa OSM configurado exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando mapa OSM", e);
            hideMapView();
        }
    }

    private void hideMapView() {
        try {
            View mapContainer = rootView.findViewById(R.id.mapView);
            if (mapContainer != null) {
                mapContainer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error ocultando mapa", e);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TAXI_PICKUP && resultCode == Activity.RESULT_OK && data != null) {
            boolean taxiRequested = data.getBooleanExtra("taxi_requested", false);
            if (taxiRequested) {
                String airportDestination = data.getStringExtra("airport_destination");
                String qrCode = data.getStringExtra("qr_code");

                Toast.makeText(getContext(),
                        "¡Taxi solicitado exitosamente al " + airportDestination + "!",
                        Toast.LENGTH_LONG).show();

                // Refrescar vista para mostrar el estado actualizado
                mostrarVistaApropriada();
            }
        }
    }

    private void mostrarVistaApropriada() {
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

                reservaActual = reserva;
                guardarReservaEnPrefs(reserva);

                if (tieneTaxiSolicitado(reserva)) {
                    mostrarVistaConTaxi(reserva, reserva.getSolicitudTaxi());
                } else {
                    mostrarVistaSinTaxi(reserva);
                }
            }
        });
    }

    private void verificarTieneReservaActiva(ReservaValidationCallback callback) {
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

    private boolean tieneTaxiSolicitado(Reserva reserva) {
        if (reserva == null) return false;

        SolicitudTaxi solicitudTaxi = reserva.getSolicitudTaxi();
        if (solicitudTaxi == null) return false;

        return Boolean.TRUE.equals(solicitudTaxi.getSolicitado());
    }

    private boolean esReservaEnPeriodoValido(Reserva reserva) {
        if (reserva == null) return false;
        if (yaHizoCheckout(reserva)) return false;
        return estaEnPeriodoEstadiaConTimestamps(reserva);
    }

    private boolean estaEnPeriodoEstadiaConTimestamps(Reserva reserva) {
        long ahora = System.currentTimeMillis();

        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            return ahora <= reserva.getFechaFin();
        }

        if (reserva.getFechaReservaTimestamp() != null) {
            long unDiaEnMs = 24 * 60 * 60 * 1000L;
            long treintaDiasEnMs = 30 * unDiaEnMs;

            long limiteInferior = reserva.getFechaReservaTimestamp();
            long limiteSuperior = reserva.getFechaReservaTimestamp() + treintaDiasEnMs;

            return ahora >= limiteInferior && ahora <= limiteSuperior;
        }

        return false;
    }

    private boolean yaHizoCheckout(Reserva reserva) {
        if (reserva == null) return false;

        if (reserva.getCheckout() != null) return true;

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

    private String obtenerEstadoDetalladoTaxi(SolicitudTaxi solicitudTaxi) {
        if (solicitudTaxi == null) return "Sin taxi solicitado";

        if (!Boolean.TRUE.equals(solicitudTaxi.getSolicitado())) {
            return "Taxi no solicitado";
        }

        String estado = solicitudTaxi.getEstado();
        if (estado == null || estado.trim().isEmpty()) {
            return "Taxi solicitado - Procesando";
        }

        switch (estado.toLowerCase()) {
            case "solicitado":
                return "🔍 Buscando conductor disponible";
            case "asignado":
                return "✅ Conductor asignado";
            case "en_camino":
                return "🚗 Conductor en camino al hotel";
            case "llegado":
            case "en_destino":
                return "📍 Conductor ha llegado";
            case "recogido":
            case "en_viaje":
                return "✈️ En viaje al aeropuerto";
            case "completado":
            case "finalizado":
                return "✅ Viaje completado";
            case "cancelado":
                return "❌ Servicio cancelado";
            default:
                return "Estado: " + estado;
        }
    }

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
                            callback.onResult(true, "✓ Servicio disponible sin monto mínimo", null);
                            return;
                        }

                        if (montoReserva >= montoMinimo) {
                            callback.onResult(true,
                                    "✓ Cumples el monto mínimo (S/" + String.format("%.2f", montoMinimo) + ")",
                                    montoMinimo);
                        } else {
                            callback.onResult(false,
                                    "Monto mínimo requerido: S/" + String.format("%.2f", montoMinimo) +
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

    private String obtenerResumenReserva(Reserva reserva) {
        if (reserva == null) return "Información no disponible";

        StringBuilder resumen = new StringBuilder();

        if (reserva.getHotelNombre() != null) {
            resumen.append("Hotel: ").append(reserva.getHotelNombre()).append("\n");
        }

        String fechas = obtenerFechasFormateadas(reserva);
        if (!fechas.isEmpty()) {
            resumen.append("Período: ").append(fechas).append("\n");
        }

        double montoTotal = calcularMontoTotalConServicios(reserva);
        resumen.append("Monto total: S/").append(String.format("%.2f", montoTotal));

        if (reserva.getServiciosAdicionales() != null &&
                !reserva.getServiciosAdicionales().isEmpty()) {
            resumen.append("\nServicios adicionales: ")
                    .append(reserva.getServiciosAdicionales().size());
        }

        return resumen.toString();
    }

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

    private boolean tieneUsuarioValido() {
        return prefsManager.isUserLoggedIn() &&
                prefsManager.getUserId() != null &&
                !prefsManager.getUserId().trim().isEmpty();
    }

    private String obtenerInfoUsuario() {
        if (!tieneUsuarioValido()) {
            return "Usuario no identificado";
        }

        String nombre = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        if (nombre != null && !nombre.trim().isEmpty()) {
            return nombre;
        } else if (email != null && !email.trim().isEmpty()) {
            return email;
        } else {
            return "Usuario: " + prefsManager.getUserId().substring(0, 8) + "...";
        }
    }

    private void guardarReservaEnPrefs(Reserva reserva) {
        if (reserva == null) return;

        try {
            if (reserva.getHotelId() != null) {
                prefsManager.saveHotel(
                        reserva.getHotelId(),
                        reserva.getHotelNombre() != null ? reserva.getHotelNombre() : "",
                        reserva.getHotelUbicacion() != null ? reserva.getHotelUbicacion() : ""
                );
            }

            if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                prefsManager.saveDateRange(reserva.getFechaInicio(), reserva.getFechaFin());
            }

            Log.d(TAG, "Información de reserva guardada en PrefsManager");

        } catch (Exception e) {
            Log.e(TAG, "Error guardando reserva en PrefsManager", e);
        }
    }

    // UI Methods
    private void mostrarMensajeCargando() {
        ocultarTodasLasVistas();
        View vistaSinReserva = rootView.findViewById(R.id.vista_sin_reserva);
        if (vistaSinReserva != null) {
            vistaSinReserva.setVisibility(View.VISIBLE);
            TextView mensaje = vistaSinReserva.findViewById(R.id.tv_mensaje_sin_reserva);
            if (mensaje != null) {
                mensaje.setText(tieneUsuarioValido() ? "Verificando reservas..." : "Verificando sesión...");
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

            TextView infoReserva = vista.findViewById(R.id.tv_info_reserva);
            if (infoReserva != null) {
                String resumenReserva = obtenerResumenReserva(reserva);
                infoReserva.setText(resumenReserva);
            }

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
                titulo.setText("Taxi reservado" + (!fechas.isEmpty() ? " para " + fechas : ""));
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

            // Generar código QR
            if (solicitudTaxi.getCodigoQR() != null && !solicitudTaxi.getCodigoQR().trim().isEmpty()) {
                generarCodigoQR(solicitudTaxi.getCodigoQR());
            } else {
                String codigoTemp = "TAXI_" + reserva.getId() + "_" + System.currentTimeMillis();
                generarCodigoQR(codigoTemp);
            }

            // Configurar botón de seguimiento
            configurarBotonSeguimiento(vista, solicitudTaxi);
        }
        Log.d(TAG, "Mostrando vista con taxi - Estado: " + obtenerEstadoDetalladoTaxi(solicitudTaxi));
    }

    private void configurarBotonSeguimiento(View vista, SolicitudTaxi solicitudTaxi) {
        Button btnSeguimiento = vista.findViewById(R.id.btn_seguir_taxi);
        if (btnSeguimiento != null) {
            btnSeguimiento.setVisibility(View.VISIBLE);
            btnSeguimiento.setOnClickListener(v -> abrirSeguimientoTaxi());

            String estado = solicitudTaxi.getEstado();
            if ("asignado".equals(estado) || "en_camino".equals(estado) || "llegado".equals(estado)) {
                btnSeguimiento.setText("Ver ubicación del conductor");
                btnSeguimiento.setEnabled(true);
            } else if ("en_viaje".equals(estado)) {
                btnSeguimiento.setText("Seguir viaje en tiempo real");
                btnSeguimiento.setEnabled(true);
            } else {
                btnSeguimiento.setText("Seguimiento no disponible");
                btnSeguimiento.setEnabled(false);
            }
        }
    }

    private void mostrarInformacionTaxista(String taxistaId) {
        db.collection("usuarios")
                .document(taxistaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreTaxista = documentSnapshot.getString("nombres");
                        String telefonoTaxista = documentSnapshot.getString("telefono");
                        String placaAuto = documentSnapshot.getString("placaAuto");

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

    // Navigation and Actions
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
                verificarElegibilidadTaxiGratuito(reservaActual, new ElegibilidadCallback() {
                    @Override
                    public void onResult(boolean esElegible, String mensaje, Double montoMinimo) {
                        if (!esElegible) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Servicio no disponible")
                                    .setMessage(mensaje)
                                    .setPositiveButton("Entendido", null)
                                    .show();
                            return;
                        }

                        try {
                            Intent intent = new Intent(requireContext(), TaxiPickupActivity.class);
                            intent.putExtra("reservaId", reservaActual.getId());
                            intent.putExtra("hotelId", reservaActual.getHotelId());

                            if (reservaActual.getHotelNombre() != null) {
                                intent.putExtra("hotelNombre", reservaActual.getHotelNombre());
                            }
                            if (reservaActual.getMontoTotal() != null) {
                                intent.putExtra("montoReserva", reservaActual.getMontoTotal());
                            }

                            startActivityForResult(intent, REQUEST_CODE_TAXI_PICKUP);

                        } catch (android.content.ActivityNotFoundException e) {
                            Log.e(TAG, "TaxiPickupActivity no encontrada en el manifest", e);
                            Toast.makeText(getContext(),
                                    "Error: Actividad no registrada. Verifica el AndroidManifest.xml",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error general abriendo TaxiPickupActivity", e);
                            Toast.makeText(getContext(),
                                    "Error al abrir solicitud de taxi: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Error: No se encontró la reserva", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error general en abrirSolicitudTaxi", e);
            Toast.makeText(getContext(), "Error al procesar solicitud", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirSeguimientoTaxi() {
        if (reservaActual != null && reservaActual.getSolicitudTaxi() != null) {
            try {
                Intent intent = new Intent(requireContext(), TaxiTrackingActivity.class);
                intent.putExtra("reservaId", reservaActual.getId());

                buscarServicioTaxiId(servicioId -> {
                    if (servicioId != null) {
                        intent.putExtra("servicioTaxiId", servicioId);
                    }
                    startActivityForResult(intent, REQUEST_CODE_TAXI_TRACKING);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error abriendo TaxiTrackingActivity", e);
                Toast.makeText(getContext(), "Error al abrir seguimiento", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscarServicioTaxiId(Consumer<String> callback) {
        db.collection("servicios_taxi")
                .whereEqualTo("clienteId", prefsManager.getUserId())
                .whereIn("estado", Arrays.asList("solicitado", "asignado", "en_camino", "llegado", "en_viaje"))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String servicioId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        callback.accept(servicioId);
                    } else {
                        callback.accept(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error buscando servicio de taxi", e);
                    callback.accept(null);
                });
    }

    // Lifecycle methods para OSM
    @Override
    public void onResume() {
        super.onResume();
        try {
            org.osmdroid.views.MapView osmMapView = rootView.findViewById(R.id.mapView);
            if (osmMapView != null) {
                osmMapView.onResume();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error en onResume del mapa", e);
        }
        // Actualizar datos al volver al fragment
        mostrarVistaApropriada();
    }

    @Override
    public void onPause() {
        try {
            org.osmdroid.views.MapView osmMapView = rootView.findViewById(R.id.mapView);
            if (osmMapView != null) {
                osmMapView.onPause();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error en onPause del mapa", e);
        }
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        try {
            org.osmdroid.views.MapView osmMapView = rootView.findViewById(R.id.mapView);
            if (osmMapView != null) {
                osmMapView.onDetach();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error en onDestroyView del mapa", e);
        }
        super.onDestroyView();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // OSM no requiere manejo especial de estado como Google Maps
    }

    // Callback interfaces
    interface ReservaValidationCallback {
        void onResult(boolean tieneReserva, Reserva reserva, String mensaje);
    }

    interface ElegibilidadCallback {
        void onResult(boolean esElegible, String mensaje, Double montoMinimo);
    }

    interface Consumer<T> {
        void accept(T t);
    }
}