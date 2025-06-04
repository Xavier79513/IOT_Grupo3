package com.example.telehotel.features.cliente.fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
    private TextView txtSeleccionarAmenidades;
    private boolean[] seleccionados;
    private String[] opcionesAmenidades;
    private List<String> amenidadesSeleccionadas = new ArrayList<>();

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

        txtSeleccionarAmenidades = view.findViewById(R.id.txtSeleccionarAmenidades);

        // SERVICIOS
        opcionesAmenidades = getResources().getStringArray(R.array.amenities_options);
        seleccionados = new boolean[opcionesAmenidades.length];

        txtSeleccionarAmenidades.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Selecciona amenities");

            builder.setMultiChoiceItems(opcionesAmenidades, seleccionados, (dialog, which, isChecked) -> {
                if (isChecked) {
                    amenidadesSeleccionadas.add(opcionesAmenidades[which]);
                } else {
                    amenidadesSeleccionadas.remove(opcionesAmenidades[which]);
                }
            });

            builder.setPositiveButton("Aceptar", (dialog, which) -> {
                txtSeleccionarAmenidades.setText(TextUtils.join(", ", amenidadesSeleccionadas));
                // Aquí podrías agregar lógica para filtrar la lista
            });

            builder.setNegativeButton("Cancelar", null);

            builder.show();
        });

        // PUNTUACIÓN
        TextView txtSeleccionarPuntuacion = view.findViewById(R.id.txtSeleccionarPuntuacion);

        String[] opcionesPuntuacion = getResources().getStringArray(R.array.filter_options);
        boolean[] seleccionadas = new boolean[opcionesPuntuacion.length];
        List<String> puntuacionSeleccionada = new ArrayList<>();

        txtSeleccionarPuntuacion.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Selecciona la puntuación");

            builder.setMultiChoiceItems(opcionesPuntuacion, seleccionadas, (dialog, which, isChecked) -> {
                if (isChecked) {
                    puntuacionSeleccionada.add(opcionesPuntuacion[which]);
                } else {
                    puntuacionSeleccionada.remove(opcionesPuntuacion[which]);
                }
            });

            builder.setPositiveButton("Aceptar", (dialog, which) -> {
                txtSeleccionarPuntuacion.setText(TextUtils.join(", ", puntuacionSeleccionada));
                // Aquí también podrías aplicar algún filtro si lo deseas
            });

            builder.setNegativeButton("Cancelar", null);

            builder.show();
        });


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