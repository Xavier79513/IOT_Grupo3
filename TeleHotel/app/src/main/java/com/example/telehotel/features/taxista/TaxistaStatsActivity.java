package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.features.taxista.adapter.ServicioTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaxistaStatsActivity extends AppCompatActivity {
    private TextView textViajes, textViajesEsteMes;

    private RecyclerView recyclerHistorial;
    private ServicioTaxiAdapter adapter;
    private final List<ServicioTaxi> historialReciente = new ArrayList<>(); // Lista real

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cargarEstadisticas();

        setContentView(R.layout.taxista_actividad);
        textViajes = findViewById(R.id.textViajes);
        textViajesEsteMes = findViewById(R.id.textViajesEsteMes);

        // Navegación inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaStatsActivity.this, item.getItemId());
            return true;
        });

        setupViewSolicitudesButton();
        setupRecyclerHistorial();
        cargarHistorialReciente();   }

    private void setupViewSolicitudesButton() {
        Button viewSolicitudes = findViewById(R.id.btnVerMas);
        viewSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaStatsActivity.this, TaxistaHistorialActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerHistorial() {
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServicioTaxiAdapter(historialReciente);
        recyclerHistorial.setAdapter(adapter);
    }

    private void cargarHistorialReciente() {

        new SolicitudRepository().getUltimosViajesByTaxista(
                "uid_12",
                3, // Límite
                new SolicitudRepository.OnViajesLoadedListener() {
                    @Override
                    public void onViajesLoaded(List<ServicioTaxi> viajes) {
                        historialReciente.clear();
                        historialReciente.addAll(viajes);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(TaxistaStatsActivity.this, "Error al cargar historial: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void cargarEstadisticas() {
        new SolicitudRepository().getEstadisticasViajes("uid_12", new SolicitudRepository.OnEstadisticasLoadedListener() {
            @Override
            public void onEstadisticasLoaded(int viajesHoy, int viajesMes) {
                textViajes.setText(String.valueOf(viajesHoy));
                textViajesEsteMes.setText(String.valueOf(viajesMes));
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(TaxistaStatsActivity.this, "Error al obtener estadísticas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // No llamar a super.onBackPressed() para bloquear atrás
    }






}
