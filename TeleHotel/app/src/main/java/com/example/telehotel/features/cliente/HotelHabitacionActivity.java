package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.features.cliente.adapters.HabitacionAdapter;
import com.example.telehotel.data.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;

public class HotelHabitacionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitacionAdapter adapter;
    private List<Habitacion> listaHabitaciones = new ArrayList<>();

    private String hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_habitacion);

        hotelId = getIntent().getStringExtra("hotelId");
        if (hotelId == null) {
            Toast.makeText(this, "Hotel inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewHabitaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
            // Navegar a la pantalla de resumen de reserva
            Intent intent = new Intent(this, ResumenReservaActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacion.getId()); // Asegúrate de que getId() existe en Habitacion
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        //cargarHotelYHabitaciones();
    }

    /*private void cargarHotelYHabitaciones() {
        HotelRepository.getHotelById(hotelId, hotel -> {
            if (hotel != null && hotel.getHabitaciones() != null) {
                listaHabitaciones.clear();
                listaHabitaciones.addAll(hotel.getHabitaciones());
                adapter.notifyDataSetChanged();
            }
        }, error -> {
            Toast.makeText(this, "Error al cargar habitaciones: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
    }*/
}
