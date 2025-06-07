package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.features.taxista.adapter.ServicioTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxistaHistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServicioTaxiAdapter solicitudAdapter;
    private List<ServicioTaxi> listaSolicitudes;
    private SolicitudRepository solicitudRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_historial);

        recyclerView = findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaSolicitudes = new ArrayList<>();
        solicitudAdapter = new ServicioTaxiAdapter(listaSolicitudes);
        recyclerView.setAdapter(solicitudAdapter);

        solicitudRepository = new SolicitudRepository();

        String uidTaxista = "uid_12";  // <-- Reemplaza con tu lógica real (Auth o Intent extra)

        cargarSolicitudes(uidTaxista);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_stats);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHistorialActivity.this, item.getItemId());
            return true;
        });
    }

    private void cargarSolicitudes(String uidTaxista) {
        solicitudRepository.getAllByUidTaxista(uidTaxista, new SolicitudRepository.OnViajesLoadedListener() {
            @Override
            public void onViajesLoaded(List<ServicioTaxi> viajes) {
                Log.d("DEBUG_FIRESTORE", "Total de viajes recibidos: " + viajes.size());

                for (ServicioTaxi viaje : viajes) {
                    Log.d("DEBUG_FIRESTORE", "---- VIAJE ----");
                    Log.d("DEBUG_FIRESTORE", "ID: " + viaje.getId());
                    Log.d("DEBUG_FIRESTORE", "Cliente ID: " + viaje.getClienteId());
                    Log.d("DEBUG_FIRESTORE", "Taxista ID: " + viaje.getTaxistaId());
                    Log.d("DEBUG_FIRESTORE", "Hotel ID: " + viaje.getHotelId());
                    Log.d("DEBUG_FIRESTORE", "Destino: " + viaje.getAeropuertoId());
                    Log.d("DEBUG_FIRESTORE", "Fecha: " + viaje.getFechaInicio());
                    Log.d("DEBUG_FIRESTORE", "Fecha: " + viaje.getFechaFin());
                    Log.d("DEBUG_FIRESTORE", "Fecha: " + viaje.getHoraInicio());
                    Log.d("DEBUG_FIRESTORE", "Fecha: " + viaje.getHoraFin());
                    Log.d("DEBUG_FIRESTORE", "Estado: " + viaje.getEstado());
                    Log.d("DEBUG_FIRESTORE", "QR: " + viaje.getQrCodigo());

                    if (viaje.getUbicacionTaxista() != null) {
                        Log.d("DEBUG_FIRESTORE", "Ubicación: " + viaje.getUbicacionTaxista().getDireccion());
                    } else {
                        Log.d("DEBUG_FIRESTORE", "Ubicación: NULL ❗️");
                    }
                }

                listaSolicitudes.clear();
                listaSolicitudes.addAll(viajes);
                solicitudAdapter.notifyDataSetChanged();
            }


            @Override
            public void onError(String errorMessage) {
                Log.e("Firestore", "Error al obtener solicitudes: " + errorMessage);
            }
        });
    }

}
