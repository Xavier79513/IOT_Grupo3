package com.example.telehotel.cliente;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.telehotel.R;

import java.util.ArrayList;
import java.util.List;

public class ListaHotel extends AppCompatActivity {

    private RecyclerView recyclerHoteles;
    private List<Hotel> listaHoteles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lista_hotel); // Asegúrate de usar el layout correcto

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar RecyclerView
        recyclerHoteles = findViewById(R.id.recyclerHoteles);
        recyclerHoteles.setLayoutManager(new LinearLayoutManager(this));

        // Crear la lista de hoteles
        listaHoteles = new ArrayList<>();
        listaHoteles.add(new Hotel("Heden Golf", "⭐ 3.9", "Overlooking gardens...", "25% OFF", "$127"));
        listaHoteles.add(new Hotel("Cozy Inn", "⭐ 4.5", "Beach view, free WiFi...", "25% OFF", "$99"));
        listaHoteles.add(new Hotel("Mountain Lodge", "⭐ 4.2", "Scenic mountain views...", "15% OFF", "$159"));
        listaHoteles.add(new Hotel("City Center", "⭐ 4.0", "Downtown location...", "30% OFF", "$85"));
        listaHoteles.add(new Hotel("Lakeside Resort", "⭐ 4.7", "Peaceful lake retreat...", "10% OFF", "$199"));

        // Configurar el adaptador
        HotelAdapter adapter = new HotelAdapter(this, listaHoteles);
        recyclerHoteles.setAdapter(adapter);
    }
}