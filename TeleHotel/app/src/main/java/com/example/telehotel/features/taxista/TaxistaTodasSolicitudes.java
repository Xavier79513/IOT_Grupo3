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
        listaSolicitudes.add(new ServicioTaxi("1", "cliente01", "taxista01", "hotel01", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. El Polo 123, Surco", -12.1057, -76.9636), "QR001"));
        listaSolicitudes.add(new ServicioTaxi("2", "cliente02", "taxista02", "hotel02", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Calle Lima 456, Miraflores", -12.1208, -77.0297), "QR002"));
        listaSolicitudes.add(new ServicioTaxi("3", "cliente03", "taxista03", "hotel03", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Jr. Amazonas 789, Cercado de Lima", -12.0454, -77.0311), "QR003"));
        listaSolicitudes.add(new ServicioTaxi("4", "cliente04", "taxista04", "hotel04", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. Salaverry 2345, Jesús María", -12.0820, -77.0498), "QR004"));
        listaSolicitudes.add(new ServicioTaxi("5", "cliente05", "taxista05", "hotel05", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Calle Los Eucaliptos 555, San Isidro", -12.0948, -77.0364), "QR005"));
        listaSolicitudes.add(new ServicioTaxi("6", "cliente06", "taxista06", "hotel06", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. Pardo 980, Miraflores", -12.1192, -77.0320), "QR006"));
        listaSolicitudes.add(new ServicioTaxi("7", "cliente07", "taxista07", "hotel07", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Malecón Cisneros 1500, Miraflores", -12.1257, -77.0350), "QR007"));
        listaSolicitudes.add(new ServicioTaxi("8", "cliente08", "taxista08", "hotel08", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. La Marina 2400, San Miguel", -12.0789, -77.0891), "QR008"));
        listaSolicitudes.add(new ServicioTaxi("9", "cliente09", "taxista09", "hotel09", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. Javier Prado Este 5600, La Molina", -12.0866, -76.9745), "QR009"));
        listaSolicitudes.add(new ServicioTaxi("10", "cliente10", "taxista10", "hotel10", "Aeropuerto Jorge Chávez", "2025-05-03", "pendiente", new Ubicacion("Av. República de Panamá 3500, San Isidro", -12.1065, -77.0293), "QR010"));

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
