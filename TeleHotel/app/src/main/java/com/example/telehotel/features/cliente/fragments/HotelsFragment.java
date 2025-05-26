package com.example.telehotel.features.cliente.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.example.telehotel.data.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;

public class HotelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textCantidad;

    private List<Hotel> listaHoteles = new ArrayList<>();
    private HotelAdapter adapter;

    public HotelsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerHotelList);
        progressBar = view.findViewById(R.id.progressBarHotels);
        textCantidad = view.findViewById(R.id.textCantidadHoteles);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelAdapter(listaHoteles);
        recyclerView.setAdapter(adapter);

        cargarHoteles();
    }

    private void cargarHoteles() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        HotelRepository.getAllHotels(hoteles -> {
            listaHoteles.clear();
            listaHoteles.addAll(hoteles);
            adapter.notifyDataSetChanged();

            textCantidad.setText("Se han encontrado " + hoteles.size() + " hoteles");

            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al obtener hoteles", error);
        });
    }
}