package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxistaPerfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_editar_perfil);

        setupBottomNavigation();
        setupButtons();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaPerfil.this, item.getItemId());
            return true;
        });
    }

    private void setupButtons() {
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        btnGuardar.setOnClickListener(v -> navigateToInicio());
        btnCancelar.setOnClickListener(v -> navigateToInicio());
    }

    private void navigateToInicio() {
        Intent intent = new Intent(TaxistaPerfil.this, TaxistaActivity.class);
        startActivity(intent);
        finish();
    }
}
