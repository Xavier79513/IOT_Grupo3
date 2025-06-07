package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.maplibre.android.maps.MapLibreMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;


public class TaxistaActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "estado_app";
    private static final String KEY_VIAJE_EN_CURSO = "viajeEnCurso";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 2001;

    private MapView mapView;
    private MaterialButton btnMostrarQR;
    private LinearLayout layoutSolicitudes;
    private LinearLayout layoutViajeActivo;
    private Button btnViewSolicitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.taxista_activity);


        // Vincular vistas
        mapView = findViewById(R.id.mapView);
        layoutSolicitudes = findViewById(R.id.solicitudesLayout);
        layoutViajeActivo = findViewById(R.id.viajeActivoLayout);
        btnMostrarQR = findViewById(R.id.btnMostrarQR);
        btnViewSolicitudes = findViewById(R.id.viewSolicitudes);

        // Inicializar MapView

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
                    mapLibreMap.setStyle(new Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"), new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            // Configurar la cámara con posición y zoom inicial
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(0.0, 0.0))
                                    .zoom(1.0)
                                    .build();
                            mapLibreMap.setCameraPosition(position);
                        }
                    });
                }
            });




        // Mostrar estado de vista según viaje activo o no
        boolean viajeEnCurso = obtenerEstadoViaje();
        mostrarVistaSegunEstado(viajeEnCurso);

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

    // Leer estado guardado del viaje
    private boolean obtenerEstadoViaje() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_VIAJE_EN_CURSO, false);
    }

    // Guardar estado del viaje
    private void guardarEstadoViaje(boolean estado) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_VIAJE_EN_CURSO, estado).apply();
    }

    // Mostrar u ocultar layouts según estado de viaje
    private void mostrarVistaSegunEstado(boolean viajeEnCurso) {
        if (viajeEnCurso) {
            layoutSolicitudes.setVisibility(View.GONE);
            layoutViajeActivo.setVisibility(View.VISIBLE);
            btnViewSolicitudes.setVisibility(View.GONE);
        } else {
            layoutSolicitudes.setVisibility(View.VISIBLE);
            layoutViajeActivo.setVisibility(View.GONE);
            btnViewSolicitudes.setVisibility(View.VISIBLE);
        }
    }

    // Configurar botones de solicitudes
    private void setupSolicitudes() {
        Button btnAceptar1 = findViewById(R.id.btnAceptar1);
        Button btnRechazar1 = findViewById(R.id.btnRechazar1);

        btnAceptar1.setOnClickListener(v -> mostrarConfirmacion("Luisa Pérez Acevedo"));
        btnRechazar1.setOnClickListener(v -> mostrarRechazo("Luisa Pérez Acevedo"));
    }

    // Mostrar diálogo para aceptar solicitud
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

        btnAceptar.setOnClickListener(v -> dialog.dismiss());
        btnDenegar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Abre la cámara solicitando permisos si es necesario
    private void abrirCamara() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzarCamara();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    // Lanzar intent de cámara
    private void lanzarCamara() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    // Resultado de la solicitud de permisos
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
