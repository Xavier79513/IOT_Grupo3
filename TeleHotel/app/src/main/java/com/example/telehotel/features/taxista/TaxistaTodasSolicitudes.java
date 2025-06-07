package com.example.telehotel.features.taxista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.model.Ubicacion;
import com.example.telehotel.features.taxista.adapter.ServicioTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxistaTodasSolicitudes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServicioTaxiAdapter solicitudAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_todas_solicitudes);

        recyclerView = findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ServicioTaxi> listaSolicitudes = new ArrayList<>();
        solicitudAdapter = new ServicioTaxiAdapter(listaSolicitudes);
        recyclerView.setAdapter(solicitudAdapter);

        // Navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaTodasSolicitudes.this, item.getItemId());
            return true;
        });
    }
}
