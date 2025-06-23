package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.adapters.FullscreenPhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class FullscreenPhotoActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenPhotoActivity";

    private ViewPager2 viewPager;
    private TextView positionIndicator;
    private FullscreenPhotoAdapter adapter;
    private List<String> imageUrls;
    private int startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_fullscreen_photo);

        Log.d(TAG, "Iniciando FullscreenPhotoActivity");

        // Obtener datos del Intent
        obtenerDatosIntent();

        // Inicializar vistas
        inicializarVistas();

        // Configurar ViewPager
        configurarViewPager();

        // Configurar botón de cierre
        configurarBotonCierre();

        Log.d(TAG, "FullscreenPhotoActivity configurada correctamente");
    }

    private void obtenerDatosIntent() {
        // Obtener URLs de imágenes (método actualizado)
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
            Log.w(TAG, "No se recibieron URLs de imágenes");
        }

        // Obtener posición inicial
        startPosition = getIntent().getIntExtra("position", 0);

        // Validar posición
        if (startPosition < 0 || startPosition >= imageUrls.size()) {
            startPosition = 0;
            Log.w(TAG, "Posición inicial inválida, usando 0");
        }

        Log.d(TAG, String.format("Datos recibidos - Total imágenes: %d, Posición inicial: %d",
                imageUrls.size(), startPosition));
    }

    private void inicializarVistas() {
        viewPager = findViewById(R.id.viewPager);
        positionIndicator = findViewById(R.id.positionIndicator);

        // Verificar que las vistas existen
        if (viewPager == null) {
            Log.e(TAG, "ViewPager no encontrado en el layout");
        }
        if (positionIndicator == null) {
            Log.w(TAG, "Indicador de posición no encontrado en el layout");
        }
    }

    private void configurarViewPager() {
        if (viewPager == null || imageUrls.isEmpty()) {
            Log.e(TAG, "No se puede configurar ViewPager - ViewPager null o lista vacía");
            finish();
            return;
        }

        // Crear y configurar adapter
        adapter = new FullscreenPhotoAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        // Establecer posición inicial
        viewPager.setCurrentItem(startPosition, false);

        // Actualizar indicador de posición
        actualizarIndicadorPosicion(startPosition);

        // Configurar listener para cambios de página
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                actualizarIndicadorPosicion(position);
                Log.d(TAG, "Página cambiada a posición: " + position);
            }
        });

        Log.d(TAG, "ViewPager configurado con " + imageUrls.size() + " imágenes");
    }

    private void configurarBotonCierre() {
        ImageButton closeButton = findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                Log.d(TAG, "Botón cerrar presionado");
                finish();
            });
        } else {
            Log.w(TAG, "Botón de cierre no encontrado en el layout");
        }
    }

    /**
     * Actualiza el indicador de posición (ej: "3 / 12")
     */
    private void actualizarIndicadorPosicion(int position) {
        if (positionIndicator != null && !imageUrls.isEmpty()) {
            String texto = String.format("%d / %d", position + 1, imageUrls.size());
            positionIndicator.setText(texto);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Botón atrás presionado");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "FullscreenPhotoActivity destruida");
    }
}