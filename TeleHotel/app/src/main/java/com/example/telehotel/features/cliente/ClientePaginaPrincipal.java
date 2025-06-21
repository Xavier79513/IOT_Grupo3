package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.example.telehotel.features.cliente.fragments.HotelsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClientePaginaPrincipal extends AppCompatActivity {
    private AutoCompleteTextView etCity;
    private TextView tvDate, tvPeople;
    private MaterialButton btnSearch;
    private RecyclerView rvHoteles;

    private HotelRepository hotelRepository;
    private ArrayAdapter<String> ciudadesAdapter;
    private HotelAdapter hotelAdapter;
    private List<Hotel> listaHoteles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_pagina_inicio);

        inicializarVistas();
        configurarRepository();
        configurarBuscador();
        configurarRecyclerView();
        cargarCiudadesDisponibles();
    }

    private void inicializarVistas() {
        etCity = findViewById(R.id.etCity);
        tvDate = findViewById(R.id.tvDate);
        tvPeople = findViewById(R.id.tvPeople);
        btnSearch = findViewById(R.id.btnSearch);
        rvHoteles = findViewById(R.id.rvHoteles);
    }

    private void configurarRepository() {
        hotelRepository = new HotelRepository();
    }

    private void configurarBuscador() {
        // Configurar el AutoCompleteTextView
        ciudadesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        etCity.setAdapter(ciudadesAdapter);
        etCity.setThreshold(1); // Mostrar sugerencias desde el primer carácter

        // Configurar el botón de búsqueda
        btnSearch.setOnClickListener(v -> realizarBusqueda());

        // Opcional: Buscar al seleccionar una ciudad del dropdown
        etCity.setOnItemClickListener((parent, view, position, id) -> {
            String ciudadSeleccionada = (String) parent.getItemAtPosition(position);
            // Aquí podrías hacer una búsqueda automática si quieres
        });
    }

    private void configurarRecyclerView() {
        listaHoteles = new ArrayList<>();
        hotelAdapter = new HotelAdapter(listaHoteles, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        rvHoteles.setLayoutManager(layoutManager);
        rvHoteles.setAdapter(hotelAdapter);
    }

    private void cargarCiudadesDisponibles() {
        hotelRepository.obtenerCiudadesDisponibles(new HotelRepository.OnCiudadesObtenidas() {
            @Override
            public void onSuccess(List<String> ciudades) {
                ciudadesAdapter.clear();
                ciudadesAdapter.addAll(ciudades);
                ciudadesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ClientePaginaPrincipal.this,
                        "Error al cargar ciudades: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void realizarBusqueda() {
        String ciudadBusqueda = etCity.getText().toString().trim();

        if (ciudadBusqueda.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una ciudad o país",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar indicador de carga
        btnSearch.setEnabled(false);
        btnSearch.setText("Buscando...");

        hotelRepository.buscarHotelesPorUbicacion(ciudadBusqueda,
                new HotelRepository.OnHotelesObtenidos() {
                    @Override
                    public void onSuccess(List<Hotel> hoteles) {
                        // Restaurar botón
                        btnSearch.setEnabled(true);
                        btnSearch.setText("Buscar hoteles");

                        // Actualizar lista
                        listaHoteles.clear();
                        listaHoteles.addAll(hoteles);
                        hotelAdapter.notifyDataSetChanged();

                        if (hoteles.isEmpty()) {
                            Toast.makeText(ClientePaginaPrincipal.this,
                                    "No se encontraron hoteles en: " + ciudadBusqueda,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClientePaginaPrincipal.this,
                                    "Se encontraron " + hoteles.size() + " hoteles",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Restaurar botón
                        btnSearch.setEnabled(true);
                        btnSearch.setText("Buscar hoteles");

                        Toast.makeText(ClientePaginaPrincipal.this,
                                "Error en búsqueda: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}