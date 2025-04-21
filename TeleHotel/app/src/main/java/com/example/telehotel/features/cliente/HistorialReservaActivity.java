package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.cliente.adapters.HistorialReservaAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistorialReservaActivity extends AppCompatActivity {

    private RecyclerView recyclerReservas;
    private HistorialReservaAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Reserva> copiaReservas = new ArrayList<>();
    private EditText buscarReserva;
    private TextView btnBorrarTodo;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_historialreserva);

        recyclerReservas = findViewById(R.id.recyclerReservas);
        buscarReserva = findViewById(R.id.buscarReserva);
        btnBorrarTodo = findViewById(R.id.btnBorrarTodo);
        btnBack = findViewById(R.id.btnBack);

        // Datos de ejemplo
        listaReservas.add(new Reserva("Hotel Paraíso", "12 Abr 2025", 125, R.drawable.sample_hotel));
        listaReservas.add(new Reserva("Luna Resort", "05 Mar 2025", 125, R.drawable.sample_hotel));
        listaReservas.add(new Reserva("Sunshine Inn", "05 Mar 2025", 125, R.drawable.sample_hotel));
        copiaReservas.addAll(listaReservas);

        /*adapter = new HistorialReservaAdapter(listaReservas, this, reserva -> {
            // Ir a detalle
            Intent intent = new Intent(HistorialReservaActivity.this, DetalleReservaActivity.class);
            intent.putExtra("reserva", reserva);
            startActivity(intent);
        });

        recyclerReservas.setLayoutManager(new LinearLayoutManager(this));
        recyclerReservas.setAdapter(adapter);*/

        // Swipe para eliminar
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                listaReservas.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        }).attachToRecyclerView(recyclerReservas);

        // Buscar por nombre
        buscarReserva.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtro = s.toString().toLowerCase();
                List<Reserva> filtradas = copiaReservas.stream()
                        .filter(r -> r.getNombreHotel().toLowerCase().contains(filtro))
                        .collect(Collectors.toList());

                listaReservas.clear();
                listaReservas.addAll(filtradas);
                adapter.notifyDataSetChanged();
            }
        });

        // Botón borrar todo
        btnBorrarTodo.setOnClickListener(v -> {
            listaReservas.clear();
            adapter.notifyDataSetChanged();
        });

        // Botón regresar
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}