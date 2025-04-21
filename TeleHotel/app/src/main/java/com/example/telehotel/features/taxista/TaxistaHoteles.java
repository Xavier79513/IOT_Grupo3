package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxistaHoteles extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_activity_hoteles);

        // Configuración de navegación del BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_hotels);  // Aquí debes poner el ID del ítem que estaba seleccionado.

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHoteles.this, item.getItemId());
            return true;
        });


        LinearLayout itemHotel1 = findViewById(R.id.itemHotel1); // Aquí es donde encuentras tu ítem
        itemHotel1.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaHoteles.this, TaxistaHotelDetalle.class);
            startActivity(intent); // Navegar a HotelDetalleActivity
        });
    }
}
