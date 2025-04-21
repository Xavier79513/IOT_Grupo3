package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.cliente.adapters.HistorialReservaAdapter;
import com.example.telehotel.features.cliente.HistorialDetalleActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationHistoryFragment extends Fragment {

    private RecyclerView recyclerReservas;
    private HistorialReservaAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Reserva> copiaReservas = new ArrayList<>();
    private EditText buscarReserva;
    private TextView btnBorrarTodo;
    private ImageView btnBack;

    public ReservationHistoryFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_historial_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerReservas = view.findViewById(R.id.recyclerReservas);
        buscarReserva = view.findViewById(R.id.buscarReserva);
        btnBorrarTodo = view.findViewById(R.id.btnBorrarTodo);
        btnBack = view.findViewById(R.id.btnBack);

        // Datos de ejemplo
        listaReservas.add(new Reserva("Hotel Paraíso", "12 Abr 2025", 125, R.drawable.sample_hotel));
        listaReservas.add(new Reserva("Luna Resort", "05 Mar 2025", 125, R.drawable.sample_hotel));
        listaReservas.add(new Reserva("Sunshine Inn", "05 Mar 2025", 125, R.drawable.sample_hotel));
        copiaReservas.addAll(listaReservas);

        adapter = new HistorialReservaAdapter(listaReservas, getContext(), reserva -> {
            Intent intent = new Intent(getActivity(), HistorialDetalleActivity.class);
            intent.putExtra("reserva", reserva);
            startActivity(intent);
        });

        recyclerReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReservas.setAdapter(adapter);

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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

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
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed(); // o usar NavigationComponent si estás usando Jetpack Navigation
        });
    }
}
