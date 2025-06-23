package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.data.repository.UserRepository;
import com.example.telehotel.features.taxista.adapter.SolicitudTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class TaxistaActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "estado_app";
    private static final String KEY_VIAJE_EN_CURSO = "viajeEnCurso";

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
        btnMostrarQR = findViewById(R.id.btnMostrarQR);
        btnViewSolicitudes = findViewById(R.id.viewSolicitudes);
        btnViewSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaActivity.this, TaxistaTodasSolicitudes.class);
            startActivity(intent);
        });


        // Inicializar MapView
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
                mapLibreMap.setStyle(new Style.Builder()
                                .fromUri("https://api.maptiler.com/maps/streets/style.json?key=f9F1eTaguwkNHqDsl8d5"),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                // Centrar la cámara en Lima con zoom 13
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(-12.073116161489578, -77.08184692648088))  // Lima
                                        .zoom(18.0)
                                        .build();
                                mapLibreMap.setCameraPosition(position);
                            }
                        });
            }
        });

        // Mostrar estado de vista según viaje activo o no
        String uidActual = FirebaseUtil.getAuth().getCurrentUser().getUid();

        UserRepository.escucharEstadoUsuario(uidActual, new UserRepository.UserEstadoListener() {
            @Override
            public void onEstadoChanged(String estado) {
                boolean viajeEnCurso = "en_curso".equalsIgnoreCase(estado);
                mostrarVistaSegunEstado(viajeEnCurso);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TaxistaActivity.this, error, Toast.LENGTH_SHORT).show();
            }
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                // Aquí puedes mostrar o procesar la imagen como desees
            }
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
        // Inicializa el repositorio si no lo has hecho


        // Crea el adapter con lista vacía y listener para acciones
        SolicitudTaxiAdapter adapter = new SolicitudTaxiAdapter(new ArrayList<>(), new SolicitudTaxiAdapter.OnSolicitudActionListener() {
            @Override
            public void onAceptar(ServicioTaxi solicitud) {
                String clienteId = solicitud.getClienteId();
                if (clienteId != null && !clienteId.isEmpty()) {
                    UserRepository.getUserByUid(clienteId,
                            usuario -> {
                                String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                                mostrarConfirmacion(nombreCompleto);
                            },
                            error -> {
                                mostrarConfirmacion("(Nombre no disponible)");
                            }
                    );
                } else {
                    mostrarConfirmacion("(ID de cliente no disponible)");
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

        // Llama al repositorio para obtener solicitudes con estado "Buscando"
        servicioTaxiRepository.getSolicitudesBuscando(new SolicitudRepository.OnViajesLoadedListener() {
            @Override
            public void onViajesLoaded(List<ServicioTaxi> solicitudes) {
                // Actualiza el adapter en el hilo principal
                runOnUiThread(() -> adapter.updateSolicitudes(solicitudes));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(TaxistaActivity.this, "Error al cargar solicitudes: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }



    private void mostrarConfirmacion(String nombre) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.taxista_bottom_sheet_confirmacion, null);
        dialog.setContentView(view);

        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        Button btnAceptar = view.findViewById(R.id.btnAceptarSolicitud);
        Button btnDenegar = view.findViewById(R.id.btnDenegarSolicitud);

        tvMensaje.setText("¿Aceptar la solicitud de " + nombre + "?");

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            guardarEstadoViaje(true);
            mostrarVistaSegunEstado(true);
            Toast.makeText(this, "Solicitud aceptada", Toast.LENGTH_SHORT).show();
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
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzarCamara();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
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
}
