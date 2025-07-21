package com.example.telehotel.features.taxista;

import static org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap;
import static org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement;
import static org.maplibre.android.style.layers.PropertyFactory.iconImage;
import static org.maplibre.android.style.layers.PropertyFactory.iconSize;

import com.example.telehotel.data.repository.EstadoViajeRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.data.repository.UserRepository;
import com.example.telehotel.features.taxista.adapter.SolicitudTaxiAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanOptions;

import org.maplibre.android.camera.CameraUpdateFactory;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaxistaActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "estado_app";
    private static final String KEY_VIAJE_EN_CURSO = "viajeEnCurso";
    private FusedLocationProviderClient fusedLocationClient;
    private TextView txtNombreViaje;
    private String uidTaxista;
    private GeoJsonSource carSource;  // fuente para mover el ícono
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private MapLibreMap mapLibreMapRef; // guardaremos el mapa para reutilizar
    private Location ultimaUbicacion;  // Guarda última ubicación para centrar mapa

    private FloatingActionButton btnCentrarMapa;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 2001;

    private MapView mapView;
    private MaterialButton btnMostrarQR;
    private RecyclerView recyclerSolicitudes;
    private LinearLayout layoutViajeActivo;
    private Button btnViewSolicitudes;
    private SolicitudRepository servicioTaxiRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        servicioTaxiRepository = new SolicitudRepository();
        MapLibre.getInstance(this);
        setContentView(R.layout.taxista_activity);

        // Vincular vistas
        mapView = findViewById(R.id.mapView);
        recyclerSolicitudes = findViewById(R.id.solicitudesRecyclerView);
        layoutViajeActivo = findViewById(R.id.viajeActivoLayout);
        txtNombreViaje = findViewById(R.id.txtNombreViaje);

        btnMostrarQR = findViewById(R.id.btnMostrarQR);
        btnViewSolicitudes = findViewById(R.id.viewSolicitudes);
        btnViewSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaActivity.this, TaxistaTodasSolicitudes.class);
            startActivity(intent);
        });

        // Inicializar MapView
        mapView.getMapAsync(mapLibreMap -> {
            this.mapLibreMapRef = mapLibreMap; // guardar referencia

            mapLibreMap.setStyle(new Style.Builder()
                    .fromUri("https://api.maptiler.com/maps/streets/style.json?key=f9F1eTaguwkNHqDsl8d5"), style -> {

                // Centrar la cámara en Lima
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(-12.073116161489578, -77.08184692648088))
                        .zoom(17.0)
                        .build();
                mapLibreMap.setCameraPosition(position);

                // Agregar ícono y fuente para el carro
                style.addImage("car-icon", BitmapFactory.decodeResource(getResources(), R.drawable.car_icon));
                carSource = new GeoJsonSource("car-source", Feature.fromGeometry(Point.fromLngLat(0, 0)));
                style.addSource(carSource);

                SymbolLayer carLayer = new SymbolLayer("car-layer", "car-source")
                        .withProperties(
                                iconImage("car-icon"),
                                iconSize(0.05f),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        );
                style.addLayer(carLayer);

                // Iniciar seguimiento GPS en tiempo real
                iniciarSeguimientoUbicacion();
            });
        });


        // Mostrar estado de vista según viaje activo o no

        // Escuchar viajes activos para este taxista
        EstadoViajeRepository estadoViajeRepository = new EstadoViajeRepository();
        String uidFirebase = FirebaseUtil.getAuth().getCurrentUser().getUid();
        Log.d("LOG_UID", "Campo uid obtenido de Firestore: " + uidFirebase);

        FirebaseUtil.getFirestore()
                .collection("usuarios")
                .document(uidFirebase)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String campoUid = document.getString("uid");  // Aquí está el campo "uid"
                        Log.d("LOG_UID", "Campo uid obtenido de Firestore: " + campoUid);

                        if (campoUid != null && !campoUid.isEmpty()) {
                            // Ahora que tienes campoUid, usalo aquí:
                            uidTaxista = campoUid; // Guarda el uid para usar luego en todo el activity

                            estadoViajeRepository.escucharViajePorUid(campoUid, (documentViaje, error) -> {
                                if (error != null) {
                                    Toast.makeText(TaxistaActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                boolean viajeActivo = false;
                                if (documentViaje != null && documentViaje.exists()) {
                                    Boolean estado = documentViaje.getBoolean("estaViajando");
                                    viajeActivo = estado != null && estado;
                                }
                                Log.d("ESTADO_VIAJE", "Viaje activo: " + viajeActivo);
                                Log.d("ESTADO_VIAJE", "UID actual taxista: " + campoUid);

                                mostrarVistaSegunEstado(viajeActivo);
                            });
                        } else {
                            Toast.makeText(this, "Campo uid no encontrado en documento usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Documento de usuario no existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });





        setupBottomNavigation();
        setupSolicitudes();

        btnMostrarQR.setOnClickListener(v -> abrirCamara());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // Manejo resultado captura de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                String qrContenido = result.getContents(); // Este es el taxista_uid escaneado
                Toast.makeText(this, "Código QR: " + qrContenido, Toast.LENGTH_LONG).show();

                finalizarViaje(qrContenido);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    // Configuración navegación inferior
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaActivity.this, item.getItemId());
            return true;
        });
    }



    // Guardar estado del viaje
    private void guardarEstadoViaje(boolean estado) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_VIAJE_EN_CURSO, estado).apply();
    }

    // Mostrar u ocultar layouts según estado de viaje
    private void mostrarVistaSegunEstado(boolean viajeEnCurso) {

        if (viajeEnCurso) {
            recyclerSolicitudes.setVisibility(View.GONE);
            layoutViajeActivo.setVisibility(View.VISIBLE);
            btnViewSolicitudes.setVisibility(View.GONE);
        } else {
            recyclerSolicitudes.setVisibility(View.VISIBLE);
            layoutViajeActivo.setVisibility(View.GONE);
            btnViewSolicitudes.setVisibility(View.VISIBLE);
        }
    }

    // Configurar RecyclerView de solicitudes con adapter y listener
    private void setupSolicitudes() {
        SolicitudTaxiAdapter adapter = new SolicitudTaxiAdapter(new ArrayList<>(), new SolicitudTaxiAdapter.OnSolicitudActionListener() {
            @Override
            public void onAceptar(ServicioTaxi solicitud) {
                String clienteId = solicitud.getClienteId();
                if (clienteId != null && !clienteId.isEmpty()) {
                    UserRepository.getUserByUid(clienteId,
                            usuario -> {
                                String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                                mostrarConfirmacion(nombreCompleto, solicitud, uidTaxista);  // Aquí falta el uidTaxista
                            },
                            error -> {
                                mostrarConfirmacion("(Nombre no disponible)", solicitud, uidTaxista);
                            }
                    );

                } else {
                    mostrarConfirmacion("(ID de cliente no disponible)", solicitud, uidTaxista);
                }
            }
            @Override
            public void onRechazar(ServicioTaxi solicitud) {
                String clienteId = solicitud.getClienteId();
                if (clienteId != null && !clienteId.isEmpty()) {
                    UserRepository.getUserByUid(clienteId,
                            usuario -> {
                                String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                                mostrarRechazo(nombreCompleto);
                            },
                            error -> {
                                mostrarRechazo("(Nombre no disponible)");
                            }
                    );
                } else {
                    mostrarRechazo("(ID de cliente no disponible)");
                }
            }
        });

        recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        recyclerSolicitudes.setAdapter(adapter);

        // Escucha en tiempo real con Firestore:
        servicioTaxiRepository.listenSolicitudesBuscando((value, error) -> {
            if (error != null) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }
            if (value != null) {
                List<ServicioTaxi> solicitudes = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    ServicioTaxi servicio = doc.toObject(ServicioTaxi.class);
                    assert servicio != null;
                    servicio.setId(doc.getId());

                    solicitudes.add(servicio);
                }
                runOnUiThread(() -> adapter.updateSolicitudes(solicitudes));
            }
        });
    }





    private void mostrarConfirmacion(String nombre, ServicioTaxi solicitud, String uidTaxista) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.taxista_bottom_sheet_confirmacion, null);
        dialog.setContentView(view);

        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        Button btnAceptar = view.findViewById(R.id.btnAceptarSolicitud);
        Button btnDenegar = view.findViewById(R.id.btnDenegarSolicitud);

        tvMensaje.setText("¿Aceptar la solicitud de " + nombre + "?");

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarViajeFirestore(solicitud, uidTaxista);
        });

        btnDenegar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    // Mostrar diálogo para rechazar solicitud
    private void mostrarRechazo(String nombre) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.taxista_bottom_sheet_confirmacion, null);
        dialog.setContentView(view);

        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        Button btnAceptar = view.findViewById(R.id.btnAceptarSolicitud);
        Button btnDenegar = view.findViewById(R.id.btnDenegarSolicitud);

        tvMensaje.setText("¿Rechazar la solicitud de " + nombre + "?");

        btnAceptar.setText("Sí, rechazar");
        btnDenegar.setText("Cancelar");

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
        });
        btnDenegar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Abrir cámara con permisos
    private void abrirCamara() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Coloca el código QR dentro del recuadro");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);  // vertical
        integrator.initiateScan();
    }



    private void lanzarCamara() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lanzarCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void iniciarSeguimientoUbicacion() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);  // cada 5s
        locationRequest.setFastestInterval(2000);  // mínimo 2s
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && carSource != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    carSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(lon, lat)));

                    // Opcional: mover cámara con la ubicación
                    if (mapLibreMapRef != null) {
                        mapLibreMapRef.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(lat, lon))
                                .zoom(17.0)
                                .build());
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3001);
        }
    }
    private void iniciarViajeFirestore(ServicioTaxi solicitud, String taxistaUid) {
        Log.d("DEBUG_VIAJE", "Solicitud: " + solicitud);
        Log.d("DEBUG_VIAJE", "ID Solicitud: " + (solicitud != null ? solicitud.getId() : "null"));
        Log.d("DEBUG_VIAJE", "UID Taxista: " + taxistaUid);

        if (solicitud == null || solicitud.getId() == null || taxistaUid == null) {
            Toast.makeText(this, "Datos inválidos para iniciar viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUtil.getFirestore().collection("solicitudTaxi")
                .document(solicitud.getId())
                .update(
                        "estado", "Aceptada",
                        "taxistaId", taxistaUid
                );

        FirebaseUtil.getFirestore().collection("estadoViaje")
                .document(taxistaUid)
                .set(new HashMap<String, Object>() {{
                    put("cliente_uid", solicitud.getClienteId());
                    put("taxista_uid", taxistaUid);
                    put("estaViajando", true);
                }})
                .addOnSuccessListener(unused -> {
                    guardarEstadoViaje(true);
                    mostrarVistaSegunEstado(true);
                    Toast.makeText(TaxistaActivity.this, "Viaje iniciado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaxistaActivity.this, "Error al iniciar viaje", Toast.LENGTH_SHORT).show();
                });
    }
    private void finalizarViaje(String clienteUidEscaneado) {
        FirebaseUtil.getFirestore().collection("estadoViaje")
                .document(clienteUidEscaneado)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String taxista_uid = documentSnapshot.getString("taxista_uid");

                        // Buscar solicitud activa con taxista_uid y cliente_uid
                        FirebaseUtil.getFirestore().collection("solicitudTaxi")
                                .whereEqualTo("taxistaId", taxista_uid)
                                .whereEqualTo("clienteId", clienteUidEscaneado)
                                .whereEqualTo("estado", "Aceptada")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot solicitudDoc = queryDocumentSnapshots.getDocuments().get(0);

                                        String solicitudId = solicitudDoc.getId();

                                        // Obtener fecha y hora actual
                                        String fechaFin = java.time.LocalDate.now().toString();
                                        String horaFin = java.time.LocalTime.now().withNano(0).toString();

                                        // Actualizar solicitudTaxi
                                        FirebaseUtil.getFirestore().collection("solicitudTaxi")
                                                .document(solicitudId)
                                                .update(
                                                        "estado", "Terminado",
                                                        "fechaFin", fechaFin,
                                                        "horaFin", horaFin
                                                )
                                                .addOnSuccessListener(aVoid -> {
                                                    // Eliminar estadoViaje
                                                    assert taxista_uid != null;
                                                    FirebaseUtil.getFirestore().collection("estadoViaje")
                                                            .document(taxista_uid)
                                                            .delete()
                                                            .addOnSuccessListener(unused -> {
                                                                Toast.makeText(this, "Viaje finalizado", Toast.LENGTH_SHORT).show();
                                                                mostrarVistaSegunEstado(false);  // Ocultar layout de viaje
                                                                guardarEstadoViaje(false);       // Actualizar en SharedPreferences
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Error al eliminar estadoViaje", Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error al actualizar solicitud", Toast.LENGTH_SHORT).show();
                                                });

                                    } else {
                                        Toast.makeText(this, "No se encontró solicitud activa", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this, "No existe estado de viaje para este taxista", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar estado de viaje", Toast.LENGTH_SHORT).show();
                });
    }







}
