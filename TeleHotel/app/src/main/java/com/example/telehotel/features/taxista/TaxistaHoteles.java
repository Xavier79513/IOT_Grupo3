package com.example.telehotel.features.taxista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.LugarCercano;
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

        hoteles.add(new Hotel(
                "H01", "Hotel Los Delfines",
                new Ubicacion("Av. Los Eucaliptos 555, San Isidro", -12.0948, -77.0364),
                Arrays.asList("hotel1.jpg"),
                Arrays.asList("WiFi", "Desayuno", "Piscina"),
                Arrays.asList(
                        new LugarCercano("Parque El Olivar", "Parque histórico en San Isidro", 0.5),
                        new LugarCercano("Centro Financiero", "Zona de negocios", 1.2)
                ),
                "admin01", "ACTIVO",
                "Hotel elegante con vista al club de golf.",
                25.0, true, "14:00", "11:00", "+51 123 456 789"
        ));

        hoteles.add(new Hotel(
                "H02", "Hotel El Polo",
                new Ubicacion("Av. El Polo 123, Surco", -12.1057, -76.9636),
                Arrays.asList("hotel2.jpg"),
                Arrays.asList("Gimnasio", "Restaurante"),
                Arrays.asList(
                        new LugarCercano("Jockey Plaza", "Centro comercial", 0.8),
                        new LugarCercano("Centro Empresarial", "Oficinas y coworking", 1.0)
                ),
                "admin02", "ACTIVO",
                "Ideal para viajeros de negocios en Surco.",
                20.0, true, "14:00", "11:00", "+51 123 456 789"
        ));

        hoteles.add(new Hotel(
                "H03", "Hotel Miramar",
                new Ubicacion("Av. Pardo 980, Miraflores", -12.1192, -77.0320),
                Arrays.asList("hotel3.jpg"),
                Arrays.asList("WiFi", "Desayuno"),
                Arrays.asList(
                        new LugarCercano("Parque Kennedy", "Centro turístico de Miraflores", 0.3),
                        new LugarCercano("Larcomar", "Centro comercial frente al mar", 1.1)
                ),
                "admin03", "ACTIVO",
                "Cerca de atractivos turísticos y vida nocturna.",
                22.5, true, "14:00", "11:00", "+51 123 456 789"
        ));

        hoteles.add(new Hotel(
                "H04", "Hotel Pacífico",
                new Ubicacion("Av. Salaverry 2345, Jesús María", -12.0820, -77.0498),
                Arrays.asList("hotel4.jpg"),
                Arrays.asList("Spa", "Bar"),
                Arrays.asList(
                        new LugarCercano("Campo de Marte", "Gran parque urbano", 0.7),
                        new LugarCercano("Universidad del Pacífico", "Institución educativa", 1.4)
                ),
                "admin04", "ACTIVO",
                "Relajación y negocios en un solo lugar.",
                18.0, true, "14:00", "11:00", "+51 123 456 789"
        ));

        hoteles.add(new Hotel(
                "H05", "Hotel Costa del Sol",
                new Ubicacion("Malecón Cisneros 1500, Miraflores", -12.1257, -77.0350),
                Arrays.asList("hotel5.jpg"),
                Arrays.asList("Vista al mar", "Piscina"),
                Arrays.asList(
                        new LugarCercano("Parque del Amor", "Parque con vista al mar", 0.2),
                        new LugarCercano("Farol de la Marina", "Mirador costero", 0.5)
                ),
                "admin05", "ACTIVO",
                "Vista panorámica al océano Pacífico.",
                30.0, true, "14:00", "11:00", "+51 123 456 789"
        ));


        return hoteles;
    }
}
