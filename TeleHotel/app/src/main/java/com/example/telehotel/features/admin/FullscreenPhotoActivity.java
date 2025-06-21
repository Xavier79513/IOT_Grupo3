package com.example.telehotel.features.admin;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.telehotel.R;
import com.example.telehotel.features.admin.adapters.FullscreenPhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class FullscreenPhotoActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvCounter;
    private List<String> imageUrls;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hacer pantalla completa
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fullscreen_photo);

        // Obtener datos del intent
        imageUrls = getIntent().getStringArrayListExtra("image_urls");
        currentPosition = getIntent().getIntExtra("position", 0);

        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
            finish();
            return;
        }

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tvCounter = findViewById(R.id.tvCounter);

        // Botón cerrar
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Click en la imagen para mostrar/ocultar controles
        viewPager.setOnClickListener(v -> toggleControls());
    }

    private void setupViewPager() {
        FullscreenPhotoAdapter adapter = new FullscreenPhotoAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        // Listener para actualizar contador
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateCounter(position);
            }
        });

        updateCounter(currentPosition);
    }

    private void updateCounter(int position) {
        String counter = (position + 1) + " de " + imageUrls.size();
        tvCounter.setText(counter);
    }

    private void toggleControls() {
        View controls = findViewById(R.id.controlsLayout);
        if (controls.getVisibility() == View.VISIBLE) {
            controls.setVisibility(View.GONE);
        } else {
            controls.setVisibility(View.VISIBLE);
        }
    }
}