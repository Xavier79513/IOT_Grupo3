package com.example.telehotel.features.taxista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxistaTodasSolicitudes extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_todas_solicitudes);

        //Navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);  // Aquí debes poner el ID del ítem que estaba seleccionado.
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaTodasSolicitudes.this, item.getItemId());
            return true;
        }); }
}
