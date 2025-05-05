package com.example.telehotel.features.taxista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Ubicacion;
import com.example.telehotel.features.taxista.adapter.HotelAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaxistaHoteles extends AppCompatActivity {

    private RecyclerView recyclerHoteles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_activity_hoteles);

        // Configuración del Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_hotels);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHoteles.this, item.getItemId());
            return true;
        });

        // Configuración del RecyclerView
        recyclerHoteles = findViewById(R.id.recyclerViewHoteles);  // Asegúrate de que este ID coincida con el XML
        recyclerHoteles.setLayoutManager(new LinearLayoutManager(this));

        // Crear la lista de hoteles
        List<Hotel> listaHoteles = crearListaHoteles();

        // Configurar el Adapter para el RecyclerView
        HotelAdapter adapter = new HotelAdapter(listaHoteles, this);
        recyclerHoteles.setAdapter(adapter);
    }

    // Método para crear la lista de hoteles
    private List<Hotel> crearListaHoteles() {
        List<Hotel> hoteles = new ArrayList<>();

        hoteles.add(new Hotel("H01", "Hotel Los Delfines",
                new Ubicacion("Av. Los Eucaliptos 555, San Isidro", -12.0948, -77.0364),
                Arrays.asList("hotel1.jpg"), Arrays.asList("WiFi", "Desayuno", "Piscina"),
                Arrays.asList("Parque El Olivar", "Centro Financiero"), "admin01", "activo"));

        hoteles.add(new Hotel("H02", "Hotel El Polo",
                new Ubicacion("Av. El Polo 123, Surco", -12.1057, -76.9636),
                Arrays.asList("hotel2.jpg"), Arrays.asList("Gimnasio", "Restaurante"),
                Arrays.asList("Jockey Plaza", "Centro Empresarial"), "admin02", "activo"));

        hoteles.add(new Hotel("H03", "Hotel Miramar",
                new Ubicacion("Av. Pardo 980, Miraflores", -12.1192, -77.0320),
                Arrays.asList("hotel3.jpg"), Arrays.asList("WiFi", "Desayuno"),
                Arrays.asList("Parque Kennedy", "Larcomar"), "admin03", "activo"));

        hoteles.add(new Hotel("H04", "Hotel Pacifico",
                new Ubicacion("Av. Salaverry 2345, Jesús María", -12.0820, -77.0498),
                Arrays.asList("hotel4.jpg"), Arrays.asList("Spa", "Bar"),
                Arrays.asList("Campo de Marte", "Universidad del Pacífico"), "admin04", "activo"));

        hoteles.add(new Hotel("H05", "Hotel Costa del Sol",
                new Ubicacion("Malecón Cisneros 1500, Miraflores", -12.1257, -77.0350),
                Arrays.asList("hotel5.jpg"), Arrays.asList("Vista al mar", "Piscina"),
                Arrays.asList("Parque del Amor", "Farol de la Marina"), "admin05", "activo"));

        hoteles.add(new Hotel("H06", "Hotel San Blas",
                new Ubicacion("Calle Lima 456, Miraflores", -12.1208, -77.0297),
                Arrays.asList("hotel6.jpg"), Arrays.asList("WiFi", "Servicio de cuarto"),
                Arrays.asList("Larcomar", "Iglesia Virgen Milagrosa"), "admin06", "activo"));

        hoteles.add(new Hotel("H07", "Hotel Boulevard",
                new Ubicacion("Jr. Amazonas 789, Cercado de Lima", -12.0454, -77.0311),
                Arrays.asList("hotel7.jpg"), Arrays.asList("Estacionamiento", "Desayuno"),
                Arrays.asList("Plaza Mayor", "Museo de Arte"), "admin07", "activo"));

        hoteles.add(new Hotel("H08", "Hotel Aeropuerto",
                new Ubicacion("Av. La Marina 2400, San Miguel", -12.0789, -77.0891),
                Arrays.asList("hotel8.jpg"), Arrays.asList("Transporte al aeropuerto"),
                Arrays.asList("Plaza San Miguel", "UNMSM"), "admin08", "activo"));

        hoteles.add(new Hotel("H09", "Hotel Ejecutivo",
                new Ubicacion("Av. Javier Prado Este 5600, La Molina", -12.0866, -76.9745),
                Arrays.asList("hotel9.jpg"), Arrays.asList("Sala de reuniones", "Cafetería"),
                Arrays.asList("La Rambla", "UPC"), "admin09", "activo"));

        hoteles.add(new Hotel("H10", "Hotel Lima Center",
                new Ubicacion("Av. República de Panamá 3500, San Isidro", -12.1065, -77.0293),
                Arrays.asList("hotel10.jpg"), Arrays.asList("WiFi", "Desayuno"),
                Arrays.asList("Centro Empresarial", "Restaurantes"), "admin10", "activo"));

        return hoteles;
    }
}
