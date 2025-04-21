package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxistaStatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_actividad);

        // Navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_stats);

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaStatsActivity.this, item.getItemId());
            return true;
        });

        setupViewSolicitudesButton();
        setupCardClick();
    }

    private void setupViewSolicitudesButton() {
        Button viewSolicitudes = findViewById(R.id.btnVerMas);
        viewSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaStatsActivity.this, TaxistaHistorialActivity.class);
            startActivity(intent);
        });
    }

    private void setupCardClick() {
        LinearLayout card1 = findViewById(R.id.card2);
        card1.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaStatsActivity.this, TaxistaDetalleViaje.class);
            startActivity(intent);
        });
    }}
