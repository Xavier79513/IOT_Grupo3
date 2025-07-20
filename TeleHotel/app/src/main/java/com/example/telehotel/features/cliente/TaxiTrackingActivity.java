package com.example.telehotel.features.cliente;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.example.telehotel.data.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * Actividad para seguimiento en tiempo real del taxi usando OSMDroid únicamente
 */
public class TaxiTrackingActivity extends AppCompatActivity {

    private static final String TAG = "TaxiTrackingActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Coordenadas importantes
    private static final GeoPoint LIMA_CENTER = new GeoPoint(-12.0464, -77.0428);
    private static final GeoPoint AEROPUERTO_JORGE_CHAVEZ = new GeoPoint(-12.0219, -77.1143);

    // Views del layout
    private MapView mapView;
    private IMapController mapController;
    private TextView tvEstadoTaxi;
    private TextView tvInfoTaxista;
    private TextView tvAeropuertoDestino;
    private TextView tvTiempoEstimado;
    private ImageView ivQrCode;
    private TextView tvCodigoQr;
    private CardView cardTaxistaInfo;
    private View loadingView;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;
    private DatabaseReference taxiLocationRef;
    private ValueEventListener locationListener;
    private PrefsManager prefsManager;

    // Datos del servicio
    private String reservaId;
    private String servicioTaxiId;
    private String taxistaId;
    private ServicioTaxi servicioTaxiActual;
    private SolicitudTaxi solicitudTaxi;
    private Reserva reservaActual;
    private Usuario taxistaInfo;

    // Marcadores del mapa
    private Marker markerTaxista;
    private Marker markerDestino;
    private Marker markerMiUbicacion;

    // Listeners
    private ListenerRegistration servicioTaxiListener;
    private ListenerRegistration reservaListener;
    private ListenerRegistration taxistaListener;
    private Handler updateHandler;

    // Ubicación del cliente
    private LocationManager locationManager;
    private LocationListener clientLocationListener;

    // Control de estado
    private boolean isMapReady = false;
    private long lastLocationUpdate = 0;
    private boolean isFirstLocationUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANTE: Configurar OSMDroid ANTES de setContentView
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.cliente_activity_taxi_tracking);

        initializeComponents();
        initializeViews();
        setupMapView();
        loadInitialData();
    }

    private void initializeComponents() {
        // Firebase
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();
        prefsManager = new PrefsManager(this);
        updateHandler = new Handler();

        // Datos del Intent
        reservaId = getIntent().getStringExtra("reservaId");
        servicioTaxiId = getIntent().getStringExtra("servicioTaxiId");
        taxistaId = getIntent().getStringExtra("taxistaId");

        Log.d(TAG, String.format("Iniciando tracking - Reserva: %s, Servicio: %s, Taxista: %s",
                reservaId, servicioTaxiId, taxistaId));
    }

    private void initializeViews() {
        // TextViews de información
        tvEstadoTaxi = findViewById(R.id.tv_estado_taxi);
        tvInfoTaxista = findViewById(R.id.tv_info_taxista);
        tvAeropuertoDestino = findViewById(R.id.tv_aeropuerto_destino);
        tvTiempoEstimado = findViewById(R.id.tv_tiempo_estimado);

        // QR Code
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvCodigoQr = findViewById(R.id.tv_codigo_qr);

        // Cards y layouts
        cardTaxistaInfo = findViewById(R.id.card_taxista_info);
        loadingView = findViewById(R.id.loading_view);

        // FABs
        FloatingActionButton fabBack = findViewById(R.id.fab_back);
        fabBack.setOnClickListener(v -> onBackPressed());

        FloatingActionButton fabCenter = findViewById(R.id.fab_center_map);
        fabCenter.setOnClickListener(v -> centerMapOnTaxi());

        // Mapa
        mapView = findViewById(R.id.mapView);

        // Inicialmente ocultar card del taxista
        cardTaxistaInfo.setVisibility(View.GONE);
    }

    private void setupMapView() {
        if (mapView == null) {
            Log.e(TAG, "MapView es null - verifica que el XML use org.osmdroid.views.MapView");
            showError("Error: No se encontró el mapa en la interfaz");
            return;
        }

        try {
            // Configurar OSMDroid
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.setBuiltInZoomControls(false); // Deshabilitado para usar FABs

            // Configurar zoom y límites
            mapView.setMaxZoomLevel(19.0);
            mapView.setMinZoomLevel(3.0);

            mapController = mapView.getController();
            mapController.setZoom(12.0);

            // Ubicación inicial en Lima
            mapController.setCenter(LIMA_CENTER);

            isMapReady = true;
            Log.d(TAG, "OSMDroid MapView configurado correctamente");

            // Solicitar permisos de ubicación
            checkLocationPermissions();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando mapa", e);
            showError("Error configurando el mapa: " + e.getMessage());
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            // Ya tenemos permisos, iniciar tracking de ubicación del cliente
            startClientLocationTracking();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startClientLocationTracking();
            } else {
                Toast.makeText(this, "Permisos de ubicación recomendados para mostrar tu posición",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startClientLocationTracking() {
        if (!isMapReady) return;

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            clientLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    updateMyLocationOnMap(location.getLatitude(), location.getLongitude());
                }
            };

            // Verificar permisos nuevamente (para lint)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        10000, // 10 segundos
                        50,    // 50 metros
                        clientLocationListener);

                // Obtener última ubicación conocida
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    updateMyLocationOnMap(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos al acceder a ubicación", e);
        }
    }

    private void updateMyLocationOnMap(double lat, double lng) {
        if (!isMapReady || mapView == null) return;

        GeoPoint myPosition = new GeoPoint(lat, lng);

        // Remover marcador anterior
        if (markerMiUbicacion != null) {
            mapView.getOverlays().remove(markerMiUbicacion);
        }

        // Crear marcador de mi ubicación
        markerMiUbicacion = new Marker(mapView);
        markerMiUbicacion.setPosition(myPosition);
        markerMiUbicacion.setTitle("📍 Tu ubicación");
        markerMiUbicacion.setSnippet("Ubicación actual del cliente");
        markerMiUbicacion.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapView.getOverlays().add(markerMiUbicacion);
        mapView.invalidate();

        Log.d(TAG, "Mi ubicación actualizada: " + lat + ", " + lng);
    }

    private void loadInitialData() {
        showLoading(true);

        if (reservaId == null) {
            showError("No se encontró información del servicio");
            return;
        }

        loadReservaData();
    }

    private void loadReservaData() {
        reservaListener = db.collection("reservas")
                .document(reservaId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando reserva", error);
                        showError("Error cargando datos de la reserva");
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        try {
                            reservaActual = doc.toObject(Reserva.class);
                            if (reservaActual != null) {
                                reservaActual.setId(doc.getId());
                                procesarReserva();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parseando reserva", e);
                            showError("Error procesando datos de la reserva");
                        }
                    } else {
                        showError("Reserva no encontrada");
                    }
                });
    }

    private void procesarReserva() {
        solicitudTaxi = reservaActual.getSolicitudTaxi();
        updateUIWithSolicitudTaxi();

        if (servicioTaxiId != null) {
            loadServicioTaxi();
        } else {
            buscarServicioTaxiActivo();
        }
    }

    private void updateUIWithSolicitudTaxi() {
        if (solicitudTaxi == null) return;

        if (solicitudTaxi.getAeropuertoDestino() != null &&
                !solicitudTaxi.getAeropuertoDestino().trim().isEmpty()) {
            String destino = getAeropuertoNombre(solicitudTaxi.getAeropuertoDestino());
            tvAeropuertoDestino.setText("Destino: " + destino);
            addDestinoMarker(solicitudTaxi.getAeropuertoDestino());
        }

        if (solicitudTaxi.getCodigoQR() != null &&
                !solicitudTaxi.getCodigoQR().trim().isEmpty()) {
            generateQRCode(solicitudTaxi.getCodigoQR());
        }
    }

    private void loadServicioTaxi() {
        servicioTaxiListener = db.collection("servicios_taxi")
                .document(servicioTaxiId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando servicio taxi", error);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        ServicioTaxi servicioAnterior = servicioTaxiActual;
                        servicioTaxiActual = doc.toObject(ServicioTaxi.class);

                        if (servicioTaxiActual != null) {
                            servicioTaxiActual.setId(doc.getId());

                            if (servicioAnterior != null &&
                                    !servicioAnterior.getEstado().equals(servicioTaxiActual.getEstado())) {
                                mostrarNotificacionCambioEstado(servicioTaxiActual.getEstado());
                            }

                            updateUIWithServicioTaxi();

                            String nuevoTaxistaId = servicioTaxiActual.getTaxistaId();
                            if (nuevoTaxistaId != null && !nuevoTaxistaId.equals(taxistaId)) {
                                taxistaId = nuevoTaxistaId;
                                setupTaxistaTracking();
                            } else if (nuevoTaxistaId != null && taxistaId == null) {
                                taxistaId = nuevoTaxistaId;
                                setupTaxistaTracking();
                            }
                        }
                    }
                });
    }

    private void buscarServicioTaxiActivo() {
        db.collection("servicios_taxi")
                .whereEqualTo("clienteId", prefsManager.getUserId())
                .whereIn("estado", java.util.Arrays.asList("solicitado", "asignado", "en_camino", "llegado", "en_viaje"))
                .orderBy("fechaInicio", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        servicioTaxiActual = doc.toObject(ServicioTaxi.class);
                        if (servicioTaxiActual != null) {
                            servicioTaxiActual.setId(doc.getId());
                            servicioTaxiId = doc.getId();
                            taxistaId = servicioTaxiActual.getTaxistaId();

                            loadServicioTaxi();
                            updateUIWithServicioTaxi();

                            if (taxistaId != null) {
                                setupTaxistaTracking();
                            }
                        }
                    } else {
                        showLoading(false);
                        tvEstadoTaxi.setText("🔍 Esperando asignación de conductor...");
                        Log.d(TAG, "No se encontró servicio de taxi activo");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error buscando servicio activo", e);
                    showError("Error buscando información del taxi");
                });
    }

    private void setupTaxistaTracking() {
        if (taxistaId == null) return;

        loadTaxistaInfo();
        startRealtimeLocationTracking();
    }

    private void loadTaxistaInfo() {
        taxistaListener = db.collection("usuarios")
                .document(taxistaId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error cargando taxista", error);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        taxistaInfo = doc.toObject(Usuario.class);
                        if (taxistaInfo != null) {
                            updateTaxistaInfoUI();
                        }
                    }
                });
    }

    private void startRealtimeLocationTracking() {
        if (taxistaId == null) return;

        if (locationListener != null && taxiLocationRef != null) {
            taxiLocationRef.removeEventListener(locationListener);
        }

        taxiLocationRef = realtimeDb.getReference("taxi_locations").child(taxistaId);

        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        processLocationUpdate(snapshot);
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando ubicación en tiempo real", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en listener de ubicación: " + error.getMessage());
            }
        };

        taxiLocationRef.addValueEventListener(locationListener);
        Log.d(TAG, "Iniciado tracking de ubicación para taxista: " + taxistaId);
    }

    private void processLocationUpdate(DataSnapshot snapshot) {
        Double latitud = snapshot.child("latitud").getValue(Double.class);
        Double longitud = snapshot.child("longitud").getValue(Double.class);
        Long timestamp = snapshot.child("timestamp").getValue(Long.class);
        Float velocidad = snapshot.child("velocidad").getValue(Float.class);
        Float precision = snapshot.child("precision").getValue(Float.class);

        if (latitud != null && longitud != null) {
            updateTaxiLocationOnMap(latitud, longitud, velocidad, precision);
            updateLocationInfo(latitud, longitud, velocidad, timestamp);

            lastLocationUpdate = timestamp != null ? timestamp : System.currentTimeMillis();
            showLoading(false);

            Log.d(TAG, String.format("Ubicación actualizada: %.6f, %.6f (v=%.1f km/h)",
                    latitud, longitud, velocidad != null ? velocidad : 0));
        }
    }

    private void updateTaxiLocationOnMap(double lat, double lng, Float velocidad, Float precision) {
        if (!isMapReady || mapView == null) return;

        GeoPoint taxiPosition = new GeoPoint(lat, lng);

        // Remover marcador anterior
        if (markerTaxista != null) {
            mapView.getOverlays().remove(markerTaxista);
        }

        // Crear nuevo marcador del taxista
        markerTaxista = new Marker(mapView);
        markerTaxista.setPosition(taxiPosition);
        markerTaxista.setTitle("🚗 Tu conductor");
        markerTaxista.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Información detallada en el snippet
        StringBuilder snippet = new StringBuilder("Ubicación en tiempo real");
        if (velocidad != null && velocidad > 0) {
            snippet.append(String.format("\nVelocidad: %.1f km/h", velocidad));
        }
        if (precision != null) {
            snippet.append(String.format("\nPrecisión: %.0fm", precision));
        }
        markerTaxista.setSnippet(snippet.toString());

        mapView.getOverlays().add(markerTaxista);

        // Centrar mapa en la primera ubicación del taxista
        if (isFirstLocationUpdate) {
            mapController.setCenter(taxiPosition);
            mapController.setZoom(15.0);
            isFirstLocationUpdate = false;
        }

        mapView.invalidate();
    }

    private void addDestinoMarker(String aeropuertoId) {
        if (!isMapReady || mapView == null) return;

        GeoPoint destinoPos = getAeropuertoCoordinates(aeropuertoId);
        if (destinoPos != null) {
            if (markerDestino != null) {
                mapView.getOverlays().remove(markerDestino);
            }

            markerDestino = new Marker(mapView);
            markerDestino.setPosition(destinoPos);
            markerDestino.setTitle("✈️ " + getAeropuertoNombre(aeropuertoId));
            markerDestino.setSnippet("Destino del viaje");
            markerDestino.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            mapView.getOverlays().add(markerDestino);
            mapView.invalidate();
        }
    }

    private void updateLocationInfo(double taxiLat, double taxiLng, Float velocidad, Long timestamp) {
        // Calcular distancia si hay destino
        if (markerDestino != null) {
            GeoPoint destino = markerDestino.getPosition();
            double distance = calcularDistancia(taxiLat, taxiLng,
                    destino.getLatitude(), destino.getLongitude());

            float velocidadCalculo = (velocidad != null && velocidad > 5) ? velocidad : 30f;
            double tiempoMinutos = (distance / velocidadCalculo) * 60;

            String tiempoTexto = String.format("Tiempo estimado: %.0f min (%.1f km)",
                    Math.max(tiempoMinutos, 2), distance);
            tvTiempoEstimado.setText(tiempoTexto);
        } else {
            tvTiempoEstimado.setText("Calculando tiempo estimado...");
        }
    }

    private void updateUIWithServicioTaxi() {
        if (servicioTaxiActual == null) return;

        showLoading(false);

        String estadoAmigable = getEstadoAmigable(servicioTaxiActual.getEstado());
        tvEstadoTaxi.setText(estadoAmigable);

        if (tvAeropuertoDestino.getText().toString().isEmpty() &&
                servicioTaxiActual.getAeropuertoId() != null) {
            String destino = getAeropuertoNombre(servicioTaxiActual.getAeropuertoId());
            tvAeropuertoDestino.setText("Destino: " + destino);
            addDestinoMarker(servicioTaxiActual.getAeropuertoId());
        }
    }

    private void updateTaxistaInfoUI() {
        if (taxistaInfo == null) return;

        StringBuilder info = new StringBuilder();

        if (taxistaInfo.getNombres() != null) {
            info.append("Conductor: ").append(taxistaInfo.getNombres());
            if (taxistaInfo.getApellidos() != null) {
                info.append(" ").append(taxistaInfo.getApellidos());
            }
            info.append("\n");
        }

        if (taxistaInfo.getPlacaAuto() != null) {
            info.append("Placa: ").append(taxistaInfo.getPlacaAuto()).append("\n");
        }

        if (taxistaInfo.getTelefono() != null) {
            info.append("Teléfono: ").append(taxistaInfo.getTelefono()).append("\n");
        }

        info.append("Reputación: ").append(String.format("%.1f/5.0", taxistaInfo.getReputacion()));

        if (taxistaInfo.getViajesRealizadosHoy() > 0) {
            info.append("\nViajes hoy: ").append(taxistaInfo.getViajesRealizadosHoy());
        }

        tvInfoTaxista.setText(info.toString());
        cardTaxistaInfo.setVisibility(View.VISIBLE);
    }

    // Métodos auxiliares

    private void generateQRCode(String code) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            ivQrCode.setImageBitmap(bitmap);
            tvCodigoQr.setText("Código: " + code);
        } catch (WriterException e) {
            Log.e(TAG, "Error generando código QR", e);
            tvCodigoQr.setText("Error generando código QR");
        }
    }

    private String getEstadoAmigable(String estado) {
        if (estado == null) return "Estado desconocido";

        switch (estado.toLowerCase()) {
            case "solicitado":
            case "pendiente":
                return "🔍 Buscando conductor disponible...";
            case "asignado":
                return "✅ Conductor asignado - Preparándose";
            case "en_camino":
                return "🚗 El conductor se dirige hacia ti";
            case "llegado":
            case "esperando":
                return "📍 El conductor ha llegado al hotel";
            case "en_viaje":
                return "✈️ En camino al aeropuerto";
            case "completado":
            case "finalizado":
                return "✅ Viaje completado";
            case "cancelado":
                return "❌ Servicio cancelado";
            default:
                return "📋 " + estado.substring(0, 1).toUpperCase() + estado.substring(1);
        }
    }

    private String getAeropuertoNombre(String aeropuertoId) {
        if (aeropuertoId == null) return "Aeropuerto";

        switch (aeropuertoId.toUpperCase()) {
            case "LIM":
            case "JORGE_CHAVEZ":
                return "Aeropuerto Internacional Jorge Chávez";
            case "CUZ":
            case "CUSCO":
                return "Aeropuerto Internacional Alejandro Velasco Astete";
            case "AQP":
            case "AREQUIPA":
                return "Aeropuerto Internacional Rodríguez Ballón";
            default:
                return "Aeropuerto " + aeropuertoId;
        }
    }

    private GeoPoint getAeropuertoCoordinates(String aeropuertoId) {
        if (aeropuertoId == null) return AEROPUERTO_JORGE_CHAVEZ;

        switch (aeropuertoId.toUpperCase()) {
            case "LIM":
            case "JORGE_CHAVEZ":
                return AEROPUERTO_JORGE_CHAVEZ;
            case "CUZ":
            case "CUSCO":
                return new GeoPoint(-13.5372, -71.9387);
            case "AQP":
            case "AREQUIPA":
                return new GeoPoint(-16.3411, -71.5831);
            default:
                return AEROPUERTO_JORGE_CHAVEZ;
        }
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void mostrarNotificacionCambioEstado(String nuevoEstado) {
        String mensaje = "";
        switch (nuevoEstado.toLowerCase()) {
            case "asignado":
                mensaje = "¡Conductor asignado! Se está preparando para recogerte";
                break;
            case "en_camino":
                mensaje = "El conductor está en camino hacia tu ubicación";
                break;
            case "llegado":
                mensaje = "¡El conductor ha llegado! Búscalo en la entrada del hotel";
                break;
            case "en_viaje":
                mensaje = "¡En camino al aeropuerto! Buen viaje";
                break;
            case "completado":
                mensaje = "Viaje completado. ¡Gracias por usar nuestro servicio!";
                break;
            case "cancelado":
                mensaje = "El servicio ha sido cancelado";
                break;
        }

        if (!mensaje.isEmpty()) {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void centerMapOnTaxi() {
        if (!isMapReady) {
            Toast.makeText(this, "Mapa no está listo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (markerTaxista != null) {
            mapController.setCenter(markerTaxista.getPosition());
            mapController.setZoom(16.0);
            mapView.invalidate();
            Toast.makeText(this, "Centrado en el conductor", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ubicación del conductor no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos de UI

    private void showLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    // Métodos de limpieza

    private void cleanupListeners() {
        if (reservaListener != null) {
            reservaListener.remove();
            reservaListener = null;
        }
        if (servicioTaxiListener != null) {
            servicioTaxiListener.remove();
            servicioTaxiListener = null;
        }
        if (taxistaListener != null) {
            taxistaListener.remove();
            taxistaListener = null;
        }
        if (locationListener != null && taxiLocationRef != null) {
            taxiLocationRef.removeEventListener(locationListener);
            locationListener = null;
        }
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }

        // Limpiar location tracking del cliente
        if (locationManager != null && clientLocationListener != null) {
            locationManager.removeUpdates(clientLocationListener);
        }
    }

    // Ciclo de vida de la actividad

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupListeners();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        // Reconectar listeners si es necesario
        if (taxistaId != null && locationListener == null) {
            startRealtimeLocationTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        // Confirmar salida si hay un servicio activo
        if (servicioTaxiActual != null &&
                (servicioTaxiActual.getEstado().equals("en_camino") ||
                        servicioTaxiActual.getEstado().equals("en_viaje"))) {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("¿Estás seguro de que quieres salir del seguimiento? Podrás volver desde tu reserva.")
                    .setPositiveButton("Sí, salir", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Quedarse", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}