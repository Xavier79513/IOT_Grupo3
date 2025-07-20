package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * Actividad para selección de lugar de recojo del taxi
 * Corregida para usar SolicitudTaxi con campos públicos
 */
public class TaxiPickupActivity extends AppCompatActivity {

    private static final String TAG = "TaxiPickupActivity";

    // Views
    private TextView tvHotelInfo;
    private TextView tvDestinationInfo;
    private Spinner spinnerAeropuertos;
    private Button btnConfirmar;
    private View loadingView;
    private MapView mapView;

    // Firebase y datos
    private FirebaseFirestore db;
    private PrefsManager prefsManager;
    private String reservaId;
    private String hotelId;
    private Reserva reservaActual;

    // Datos del mapa
    private IMapController mapController;
    private Marker hotelMarker;
    private Marker airportMarker;

    // Aeropuertos disponibles con coordenadas
    private AirportInfo[] aeropuertos = {
            new AirportInfo("Aeropuerto Internacional Jorge Chávez", -12.0219, -77.1143),
            new AirportInfo("Aeródromo Collique", -11.7669, -77.1089),
            new AirportInfo("Base Aérea Santa María", -12.0567, -76.9608),
            new AirportInfo("Aeródromo Las Palmas", -12.1594, -76.9689)
    };

    private int selectedAirportIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.cliente_activity_taxi_pickup);

        initializeData();
        initializeViews();
        setupMap();
        loadReservaData();
    }

    private void initializeData() {
        db = FirebaseFirestore.getInstance();
        prefsManager = new PrefsManager(this);

        // Obtener datos del intent
        reservaId = getIntent().getStringExtra("reservaId");
        hotelId = getIntent().getStringExtra("hotelId");

        Log.d(TAG, "Iniciando actividad - ReservaID: " + reservaId + ", HotelID: " + hotelId);
    }

    private void initializeViews() {
        tvHotelInfo = findViewById(R.id.tv_hotel_info);
        tvDestinationInfo = findViewById(R.id.tv_destination_info);
        spinnerAeropuertos = findViewById(R.id.spinner_aeropuertos);
        btnConfirmar = findViewById(R.id.btn_confirmar);
        loadingView = findViewById(R.id.loading_view);
        mapView = findViewById(R.id.mapView);

        // Configurar spinner de aeropuertos
        String[] aeroportNames = new String[aeropuertos.length];
        for (int i = 0; i < aeropuertos.length; i++) {
            aeroportNames[i] = aeropuertos[i].name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, aeroportNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAeropuertos.setAdapter(adapter);

        // Configurar listeners
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
        btnConfirmar.setOnClickListener(v -> confirmarSolicitudTaxi());

        // Listener para cambio de aeropuerto
        spinnerAeropuertos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedAirportIndex = position;
                updateDestinationInfo();
                updateMapWithAirport();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupMap() {
        // Configurar mapa OSM
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(12.0);

        // Centrar en Lima por defecto
        GeoPoint limaCenter = new GeoPoint(-12.0464, -77.0428);
        mapController.setCenter(limaCenter);
    }

    private void loadReservaData() {
        showLoading(true);

        if (reservaId == null) {
            showError("No se encontró información de la reserva");
            return;
        }

        // Cargar datos de la reserva
        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        reservaActual = documentSnapshot.toObject(Reserva.class);
                        if (reservaActual != null) {
                            reservaActual.setId(documentSnapshot.getId());
                            updateUIWithReservaData();
                            loadHotelData();
                        } else {
                            showError("Error al cargar datos de la reserva");
                        }
                    } else {
                        showError("Reserva no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error de conexión: " + e.getMessage());
                    Log.e(TAG, "Error cargando reserva", e);
                });
    }

    private void loadHotelData() {
        String hotelIdToLoad = hotelId != null ? hotelId :
                (reservaActual != null ? reservaActual.getHotelId() : null);

        if (hotelIdToLoad == null) {
            showLoading(false);
            showError("No se encontró información del hotel");
            return;
        }

        db.collection("hoteles")
                .document(hotelIdToLoad)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        String hotelNombre = documentSnapshot.getString("nombre");

                        // Obtener ubicación del hotel
                        Map<String, Object> ubicacionMap = (Map<String, Object>) documentSnapshot.get("ubicacion");
                        if (ubicacionMap != null) {
                            Double latitud = (Double) ubicacionMap.get("latitud");
                            Double longitud = (Double) ubicacionMap.get("longitud");
                            String direccion = (String) ubicacionMap.get("direccion");

                            // Actualizar UI con información del hotel
                            updateHotelInfo(hotelNombre, direccion);

                            // Mostrar hotel en el mapa
                            if (latitud != null && longitud != null) {
                                showHotelOnMap(latitud, longitud, hotelNombre);
                            }
                        }

                        // Mostrar primer aeropuerto por defecto
                        updateDestinationInfo();
                        updateMapWithAirport();

                        // Habilitar botón de confirmación
                        btnConfirmar.setEnabled(true);
                    } else {
                        showError("Hotel no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Error cargando información del hotel: " + e.getMessage());
                    Log.e(TAG, "Error cargando hotel", e);
                });
    }

    private void updateUIWithReservaData() {
        if (reservaActual == null) return;

        // Mostrar información básica de la reserva en el título
        if (reservaActual.getHotelNombre() != null) {
            tvHotelInfo.setText("Cargando ubicación de " + reservaActual.getHotelNombre() + "...");
        }
    }

    private void updateHotelInfo(String hotelNombre, String direccion) {
        StringBuilder hotelInfo = new StringBuilder();
        hotelInfo.append("📍 Punto de recojo:\n");
        hotelInfo.append(hotelNombre != null ? hotelNombre : "Hotel");
        if (direccion != null && !direccion.trim().isEmpty()) {
            hotelInfo.append("\n").append(direccion);
        }

        tvHotelInfo.setText(hotelInfo.toString());
    }

    private void updateDestinationInfo() {
        if (selectedAirportIndex < aeropuertos.length) {
            AirportInfo airport = aeropuertos[selectedAirportIndex];

            // Calcular distancia aproximada (solo para display)
            String info = "✈️ Destino seleccionado:\n" + airport.name +
                    "\nDistancia aproximada: 15-25 km";
            tvDestinationInfo.setText(info);
        }
    }

    private void showHotelOnMap(double latitude, double longitude, String hotelName) {
        GeoPoint hotelPoint = new GeoPoint(latitude, longitude);

        // Remover marcador anterior si existe
        if (hotelMarker != null) {
            mapView.getOverlays().remove(hotelMarker);
        }

        // Crear marcador del hotel
        hotelMarker = new Marker(mapView);
        hotelMarker.setPosition(hotelPoint);
        hotelMarker.setTitle("Punto de recojo");
        hotelMarker.setSnippet(hotelName != null ? hotelName : "Tu hotel");
        hotelMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Agregar marcador al mapa
        mapView.getOverlays().add(hotelMarker);

        // Centrar mapa en el hotel
        mapController.setCenter(hotelPoint);
        mapController.setZoom(14.0);

        mapView.invalidate();
    }

    private void updateMapWithAirport() {
        if (selectedAirportIndex >= aeropuertos.length) return;

        AirportInfo airport = aeropuertos[selectedAirportIndex];
        GeoPoint airportPoint = new GeoPoint(airport.latitude, airport.longitude);

        // Remover marcador anterior del aeropuerto si existe
        if (airportMarker != null) {
            mapView.getOverlays().remove(airportMarker);
        }

        // Crear marcador del aeropuerto
        airportMarker = new Marker(mapView);
        airportMarker.setPosition(airportPoint);
        airportMarker.setTitle("Destino");
        airportMarker.setSnippet(airport.name);
        airportMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Agregar marcador al mapa
        mapView.getOverlays().add(airportMarker);

        // Si tenemos ambos marcadores, ajustar vista para mostrar ambos
        if (hotelMarker != null) {
            // Calcular bounds para mostrar ambos puntos
            double minLat = Math.min(hotelMarker.getPosition().getLatitude(), airport.latitude);
            double maxLat = Math.max(hotelMarker.getPosition().getLatitude(), airport.latitude);
            double minLon = Math.min(hotelMarker.getPosition().getLongitude(), airport.longitude);
            double maxLon = Math.max(hotelMarker.getPosition().getLongitude(), airport.longitude);

            // Centrar en punto medio
            GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);
            mapController.setCenter(center);
            mapController.setZoom(11.0);
        } else {
            mapController.setCenter(airportPoint);
            mapController.setZoom(13.0);
        }

        mapView.invalidate();
    }

    private void confirmarSolicitudTaxi() {
        if (selectedAirportIndex >= aeropuertos.length) {
            Toast.makeText(this, "Por favor selecciona un destino válido", Toast.LENGTH_SHORT).show();
            return;
        }

        AirportInfo selectedAirport = aeropuertos[selectedAirportIndex];
        showLoading(true);

        // ✅ CORREGIDO: Crear solicitud de taxi con campos públicos
        SolicitudTaxi solicitudTaxi = new SolicitudTaxi();
        solicitudTaxi.solicitado = true;
        solicitudTaxi.aeropuertoDestino = selectedAirport.name;
        solicitudTaxi.estado = "solicitado";

        // Generar código QR único
        String codigoQR = "TAXI_" + reservaId + "_" + System.currentTimeMillis();
        solicitudTaxi.codigoQR = codigoQR;

        // Actualizar reserva con la solicitud de taxi
        Map<String, Object> updates = new HashMap<>();
        updates.put("solicitudTaxi", solicitudTaxi);

        db.collection("reservas")
                .document(reservaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Crear registro en colección de servicios de taxi
                    createTaxiServiceRecord(solicitudTaxi, selectedAirport);

                    showLoading(false);

                    // Regresar con resultado exitoso
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("taxi_requested", true);
                    resultIntent.putExtra("airport_destination", selectedAirport.name);
                    resultIntent.putExtra("qr_code", codigoQR);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(this, "¡Taxi solicitado exitosamente!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Error al solicitar taxi: " + e.getMessage());
                    Log.e(TAG, "Error actualizando reserva", e);
                });
    }

    private void createTaxiServiceRecord(SolicitudTaxi solicitudTaxi, AirportInfo airport) {
        // Crear registro en la colección de servicios de taxi
        Map<String, Object> servicioTaxi = new HashMap<>();
        servicioTaxi.put("clienteId", prefsManager.getUserId());
        servicioTaxi.put("hotelId", hotelId);
        servicioTaxi.put("aeropuertoDestino", airport.name);
        servicioTaxi.put("aeropuertoLatitud", airport.latitude);
        servicioTaxi.put("aeropuertoLongitud", airport.longitude);
        servicioTaxi.put("fechaInicio", System.currentTimeMillis());
        servicioTaxi.put("estado", "solicitado");
        servicioTaxi.put("qrCodigo", solicitudTaxi.codigoQR);

        db.collection("servicios_taxi")
                .add(servicioTaxi)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Servicio de taxi creado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando servicio de taxi", e);
                });
    }

    private void showLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnConfirmar.setEnabled(!show);
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Lifecycle methods para OSMDroid
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    // Clase auxiliar para información de aeropuertos
    private static class AirportInfo {
        final String name;
        final double latitude;
        final double longitude;

        AirportInfo(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}