package com.example.telehotel.features.cliente;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.example.telehotel.data.model.Ubicacion;
import com.example.telehotel.data.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class TaxiTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TaxiTrackingActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    // Views
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvEstadoTaxi;
    private TextView tvInfoTaxista;
    private TextView tvAeropuertoDestino;
    private TextView tvTiempoEstimado;
    private ImageView ivQrCode;
    private TextView tvCodigoQr;
    private CardView cardTaxistaInfo;
    private View loadingView;

    // Firebase y datos
    private FirebaseFirestore db;
    private PrefsManager prefsManager;
    private String reservaId;
    private String servicioTaxiId;
    private ServicioTaxi servicioTaxiActual;
    private SolicitudTaxi solicitudTaxi;

    // Marcadores del mapa
    private Marker markerTaxista;
    private Marker markerDestino;
    private Marker markerHotel;

    // Listeners
    private ListenerRegistration servicioTaxiListener;
    private ListenerRegistration taxistaListener;
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_taxi_tracking);

        initializeData();
        initializeViews(savedInstanceState);
        setupMapView(savedInstanceState);
        loadTaxiServiceData();
        startLocationUpdates();
    }

    private void initializeData() {
        db = FirebaseFirestore.getInstance();
        prefsManager = new PrefsManager(this);

        // Obtener datos del intent
        reservaId = getIntent().getStringExtra("reservaId");
        servicioTaxiId = getIntent().getStringExtra("servicioTaxiId");

        updateHandler = new Handler();

        Log.d(TAG, "Iniciando seguimiento - ReservaID: " + reservaId);
    }

    private void initializeViews(Bundle savedInstanceState) {
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

        // FAB para regresar
        FloatingActionButton fabBack = findViewById(R.id.fab_back);
        fabBack.setOnClickListener(v -> onBackPressed());

        // FAB para centrar mapa
        FloatingActionButton fabCenter = findViewById(R.id.fab_center_map);
        fabCenter.setOnClickListener(v -> centerMapOnTaxi());
    }

    private void setupMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null ?
                savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Configurar mapa
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Ubicación inicial en Lima
        LatLng lima = new LatLng(-12.0464, -77.0428);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f));
    }

    private void loadTaxiServiceData() {
        showLoading(true);

        if (reservaId == null) {
            showError("No se encontró información del servicio");
            return;
        }

        // Primero cargar la reserva para obtener la solicitud de taxi
        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener solicitud de taxi de la reserva
                        solicitudTaxi = documentSnapshot.toObject(SolicitudTaxi.class);
                        if (solicitudTaxi != null) {
                            updateUIWithSolicitudTaxi();

                            // Buscar el servicio de taxi activo
                            buscarServicioTaxiActivo();
                        } else {
                            showError("No se encontró solicitud de taxi");
                        }
                    } else {
                        showError("Reserva no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error cargando datos: " + e.getMessage());
                    Log.e(TAG, "Error cargando reserva", e);
                });
    }

    private void buscarServicioTaxiActivo() {
        // Buscar servicio de taxi por clienteId y estado activo
        db.collection("servicios_taxi")
                .whereEqualTo("clienteId", prefsManager.getUserId())
                .whereIn("estado", java.util.Arrays.asList("solicitado", "asignado", "en_camino", "llegado", "en_viaje"))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        servicioTaxiActual = doc.toObject(ServicioTaxi.class);
                        if (servicioTaxiActual != null) {
                            servicioTaxiActual.setId(doc.getId());
                            servicioTaxiId = doc.getId();

                            // Configurar listener en tiempo real
                            setupRealtimeUpdates();
                            updateUIWithServicioTaxi();
                        }
                    } else {
                        // No hay servicio activo, mostrar solo la solicitud
                        showLoading(false);
                        Log.d(TAG, "No se encontró servicio de taxi activo");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error buscando servicio de taxi", e);
                });
    }

    private void setupRealtimeUpdates() {
        if (servicioTaxiId == null) return;

        // Listener para el servicio de taxi
        servicioTaxiListener = db.collection("servicios_taxi")
                .document(servicioTaxiId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error en listener del servicio", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        servicioTaxiActual = documentSnapshot.toObject(ServicioTaxi.class);
                        if (servicioTaxiActual != null) {
                            servicioTaxiActual.setId(documentSnapshot.getId());
                            updateUIWithServicioTaxi();

                            // Si hay taxista asignado, cargar su información
                            if (servicioTaxiActual.getTaxistaId() != null) {
                                loadTaxistaInfo(servicioTaxiActual.getTaxistaId());
                            }
                        }
                    }
                });
    }

    private void loadTaxistaInfo(String taxistaId) {
        // Configurar listener para información del taxista
        if (taxistaListener != null) {
            taxistaListener.remove();
        }

        taxistaListener = db.collection("usuarios")
                .document(taxistaId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error en listener del taxista", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Usuario taxista = documentSnapshot.toObject(Usuario.class);
                        if (taxista != null) {
                            updateTaxistaInfo(taxista);

                            // Actualizar ubicación en el mapa
                            if (taxista.getUbicacionActual() != null) {
                                updateTaxistaLocationOnMap(taxista.getUbicacionActual());
                            }
                        }
                    }
                });
    }

    private void updateUIWithSolicitudTaxi() {
        if (solicitudTaxi == null) return;

        // Mostrar destino
        if (solicitudTaxi.getAeropuertoDestino() != null) {
            tvAeropuertoDestino.setText("Destino: " + solicitudTaxi.getAeropuertoDestino());
        }

        // Generar código QR si existe
        if (solicitudTaxi.getCodigoQR() != null) {
            generateQRCode(solicitudTaxi.getCodigoQR());
        }
    }

    private void updateUIWithServicioTaxi() {
        if (servicioTaxiActual == null) return;

        showLoading(false);

        // Actualizar estado
        String estado = getEstadoAmigable(servicioTaxiActual.getEstado());
        tvEstadoTaxi.setText(estado);

        // Mostrar destino si no se mostró antes
        if (tvAeropuertoDestino.getText().toString().isEmpty() &&
                servicioTaxiActual.getAeropuertoId() != null) {
            tvAeropuertoDestino.setText("Destino: " + servicioTaxiActual.getAeropuertoId());
        }

        // Calcular tiempo estimado
        calculateEstimatedTime();
    }

    private void updateTaxistaInfo(Usuario taxista) {
        StringBuilder info = new StringBuilder();

        if (taxista.getNombres() != null) {
            info.append("Conductor: ").append(taxista.getNombres());
            if (taxista.getApellidos() != null) {
                info.append(" ").append(taxista.getApellidos());
            }
            info.append("\n");
        }

        if (taxista.getPlacaAuto() != null) {
            info.append("Placa: ").append(taxista.getPlacaAuto()).append("\n");
        }

        if (taxista.getTelefono() != null) {
            info.append("Teléfono: ").append(taxista.getTelefono()).append("\n");
        }

        info.append("Reputación: ").append(String.format("%.1f/5.0", taxista.getReputacion()));

        tvInfoTaxista.setText(info.toString());
        cardTaxistaInfo.setVisibility(View.VISIBLE);
    }

    private void updateTaxistaLocationOnMap(Ubicacion ubicacion) {
        if (googleMap == null || ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
            return;
        }

        LatLng taxistaLocation = new LatLng(ubicacion.getLatitud(), ubicacion.getLongitud());

        // Actualizar o crear marcador del taxista
        if (markerTaxista != null) {
            markerTaxista.setPosition(taxistaLocation);
        } else {
            markerTaxista = googleMap.addMarker(new MarkerOptions()
                    .position(taxistaLocation)
                    .title("Tu conductor")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        // Centrar mapa en el taxista
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taxistaLocation, 15f));
    }

    private void generateQRCode(String code) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 300, 300);
            ivQrCode.setImageBitmap(bitmap);
            tvCodigoQr.setText("Código: " + code);
        } catch (WriterException e) {
            Log.e(TAG, "Error generando código QR", e);
        }
    }

    private String getEstadoAmigable(String estado) {
        if (estado == null) return "Estado desconocido";

        switch (estado.toLowerCase()) {
            case "solicitado":
                return "🔍 Buscando conductor disponible...";
            case "asignado":
                return "✅ Conductor asignado";
            case "en_camino":
                return "🚗 El conductor se dirige hacia ti";
            case "llegado":
                return "📍 El conductor ha llegado";
            case "en_viaje":
                return "✈️ En camino al aeropuerto";
            case "completado":
                return "✅ Viaje completado";
            case "cancelado":
                return "❌ Servicio cancelado";
            default:
                return "Estado: " + estado;
        }
    }

    private void calculateEstimatedTime() {
        // Implementar cálculo de tiempo estimado basado en la ubicación actual
        // Por ahora, mostrar tiempo fijo
        tvTiempoEstimado.setText("Tiempo estimado: 15-25 min");
    }

    private void centerMapOnTaxi() {
        if (markerTaxista != null && googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    markerTaxista.getPosition(), 16f));
        } else {
            Toast.makeText(this, "Ubicación del conductor no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Actualizar cada 30 segundos
                if (servicioTaxiActual != null && servicioTaxiActual.getTaxistaId() != null) {
                    loadTaxistaInfo(servicioTaxiActual.getTaxistaId());
                }
                updateHandler.postDelayed(this, 30000); // 30 segundos
            }
        };
        updateHandler.post(updateRunnable);
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar listeners
        if (servicioTaxiListener != null) {
            servicioTaxiListener.remove();
        }
        if (taxistaListener != null) {
            taxistaListener.remove();
        }
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }

        // Destruir mapa
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    // Métodos del ciclo de vida del MapView
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        if (mapView != null) {
            mapView.onSaveInstanceState(mapViewBundle);
        }
    }
}