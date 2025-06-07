package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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

public class TaxistaHotelDetalle extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServicioTaxiAdapter solicitudAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_hotel_detalle);

        // Recuperar datos del Intent
        String hotelId = getIntent().getStringExtra("hotel_id");
        String hotelNombre = getIntent().getStringExtra("hotel_nombre");
        String hotelDireccion = getIntent().getStringExtra("hotel_direccion");

        // Verificar si los datos del Intent existen
        if (hotelId == null || hotelNombre == null || hotelDireccion == null) {
            Log.e("TaxistaHotelDetalle", "Faltan datos en el Intent");
            return;
        }

        // Mostrar los datos del hotel
        TextView titulo = findViewById(R.id.textHotelTitulo);
        TextView direccion = findViewById(R.id.textHotelDireccion);
        if (titulo != null) titulo.setText(hotelNombre);
        if (direccion != null) direccion.setText(hotelDireccion);

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lista de solicitudes de taxi
        List<ServicioTaxi> listaSolicitudes = crearListaSolicitudes();

        // Filtrar las solicitudes para que solo se muestren las del hotel seleccionado
        List<ServicioTaxi> solicitudesFiltradas = filtrarSolicitudesPorHotel(listaSolicitudes, hotelId);

        // Verificar si se encontraron solicitudes
        if (solicitudesFiltradas.isEmpty()) {
            Log.e("TaxistaHotelDetalle", "No se encontraron solicitudes para el hotel con ID: " + hotelId);
        } else {
            Log.d("TaxistaHotelDetalle", "Se encontraron " + solicitudesFiltradas.size() + " solicitudes.");
        }

        // Adapter
        solicitudAdapter = new ServicioTaxiAdapter(solicitudesFiltradas);
        recyclerView.setAdapter(solicitudAdapter);

        // Configuración del Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_hotels);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHotelDetalle.this, item.getItemId());
            return true;
        });
    }

    // Metodo para crear la lista de solicitudes de taxi (simulada)
    private List<ServicioTaxi> crearListaSolicitudes() {
        List<ServicioTaxi> solicitudes = new ArrayList<>();    return solicitudes;
    }


    // Filtrar las solicitudes por el ID del hotel
    private List<ServicioTaxi> filtrarSolicitudesPorHotel(List<ServicioTaxi> solicitudes, String hotelId) {
        List<ServicioTaxi> solicitudesFiltradas = new ArrayList<>();
        for (ServicioTaxi solicitud : solicitudes) {
            // Verificar si el hotelId coincide
            Log.d("TaxistaHotelDetalle", "Comprobando solicitud para hotel ID: " + solicitud.getHotelId());
            if (solicitud.getHotelId().equals(hotelId)) {
                solicitudesFiltradas.add(solicitud);
            }
        }
        return solicitudesFiltradas;
    }
}
