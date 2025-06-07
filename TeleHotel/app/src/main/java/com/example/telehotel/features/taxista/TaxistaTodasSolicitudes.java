package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.model.Ubicacion;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.features.taxista.adapter.ServicioTaxiAdapter;
import com.example.telehotel.features.taxista.adapter.SolicitudTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxistaTodasSolicitudes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SolicitudTaxiAdapter solicitudAdapter;
    private SolicitudRepository solicitudRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_todas_solicitudes);

        recyclerView = findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        solicitudRepository = new SolicitudRepository();

        // Inicializar el adapter con lista vacía primero
        solicitudAdapter = new SolicitudTaxiAdapter(new ArrayList<>());
        recyclerView.setAdapter(solicitudAdapter);

        // Cargar datos desde Firestore con callback
        solicitudRepository.getSolicitudesBuscando(new SolicitudRepository.OnViajesLoadedListener() {
            @Override
            public void onViajesLoaded(List<ServicioTaxi> viajes) {
                // Actualizar adapter con los datos recibidos
                solicitudAdapter.updateSolicitudes(viajes);
            }

            @Override
            public void onError(String errorMessage) {
                // Mostrar error, por ejemplo con un Toast
                runOnUiThread(() -> {
                    Toast.makeText(TaxistaTodasSolicitudes.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaTodasSolicitudes.this, item.getItemId());
            return true;
        });
    }
}



