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
        List<ServicioTaxi> solicitudes = new ArrayList<>();
        solicitudes.add(new ServicioTaxi("1", "cliente01", "taxista01", "H01", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. El Polo 123, Surco", -12.1057, -76.9636), "QR001"));
        solicitudes.add(new ServicioTaxi("2", "cliente02", "taxista02", "H02", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Calle Lima 456, Miraflores", -12.1208, -77.0297), "QR002"));
        solicitudes.add(new ServicioTaxi("3", "cliente03", "taxista03", "H01", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Jr. Amazonas 789, Cercado de Lima", -12.0454, -77.0311), "QR003"));
        solicitudes.add(new ServicioTaxi("4", "cliente04", "taxista04", "H03", "Aeropuerto Internacional", "2025-05-04", "pendiente", new Ubicacion("Calle Los Olivos 320, San Isidro", -12.0957, -77.0426), "QR004"));
        solicitudes.add(new ServicioTaxi("5", "cliente05", "taxista05", "H02", "Aeropuerto Jorge Chávez", "2025-05-04", "pendiente", new Ubicacion("Calle Las Flores 67, Miraflores", -12.1212, -77.0337), "QR005"));
        solicitudes.add(new ServicioTaxi("6", "cliente06", "taxista06", "H01", "Aeropuerto Jorge Chávez", "2025-05-04", "pendiente", new Ubicacion("Calle Pardo 101, San Isidro", -12.0934, -77.0298), "QR006"));
        solicitudes.add(new ServicioTaxi("7", "cliente07", "taxista07", "H04", "Aeropuerto Internacional", "2025-05-05", "pendiente", new Ubicacion("Calle Los Cedros 100, Lince", -12.0695, -77.0422), "QR007"));
        solicitudes.add(new ServicioTaxi("8", "cliente08", "taxista08", "H03", "Aeropuerto Jorge Chávez", "2025-05-05", "pendiente", new Ubicacion("Calle San Borja 45, San Borja", -12.0974, -77.0279), "QR008"));
        solicitudes.add(new ServicioTaxi("9", "cliente09", "taxista09", "H02", "Aeropuerto Internacional", "2025-05-06", "pendiente", new Ubicacion("Avenida Javier Prado 212, San Isidro", -12.0896, -77.0400), "QR009"));
        solicitudes.add(new ServicioTaxi("10", "cliente10", "taxista10", "H01", "Aeropuerto Jorge Chávez", "2025-05-06", "pendiente", new Ubicacion("Avenida El Polo 455, Surco", -12.1100, -76.9820), "QR010"));
        solicitudes.add(new ServicioTaxi("11", "cliente11", "taxista11", "H04", "Aeropuerto Internacional", "2025-05-07", "pendiente", new Ubicacion("Avenida Los Olivos 222, Lima", -12.1010, -77.0580), "QR011"));
        solicitudes.add(new ServicioTaxi("12", "cliente12", "taxista12", "H02", "Aeropuerto Jorge Chávez", "2025-05-07", "pendiente", new Ubicacion("Avenida Pardo 125, Miraflores", -12.1123, -77.0311), "QR012"));

        return solicitudes;
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
