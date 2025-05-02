package com.example.telehotel.features.cliente;

import android.os.Bundle;
import androidx.core.util.Pair;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.example.telehotel.R;
import com.example.telehotel.features.cliente.adapters.HotelesAdapter;
import com.example.telehotel.features.cliente.adapters.LugaresAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PaginaPrincipal extends AppCompatActivity {

    TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_pagina_inicio);

        /*BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Esta es la forma MÁS SEGURA de obtener el NavController
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);*/

        // RECYCLER VIEW LUGARES
        RecyclerView rvLugares = findViewById(R.id.rvLugares);
        rvLugares.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<LugaresAdapter.LugarItem> lugares = Arrays.asList(
                new LugaresAdapter.LugarItem(R.drawable.ivory_coast, "Ivory Coast"),
                new LugaresAdapter.LugarItem(R.drawable.newyork, "New York"),
                new LugaresAdapter.LugarItem(R.drawable.cancun, "Cancun"),
                new LugaresAdapter.LugarItem(R.drawable.barcelona, "Barcelona"),
                new LugaresAdapter.LugarItem(R.drawable.envigado, "Envigado")
        );
        rvLugares.setAdapter(new LugaresAdapter(lugares));

        // RECYCLER VIEW HOTELES
        RecyclerView rvHoteles = findViewById(R.id.rvHoteles);
        rvHoteles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<HotelesAdapter.HotelItem> hoteles = Arrays.asList(
                new HotelesAdapter.HotelItem(R.drawable.hotel1, "Hotel BTH"),
                new HotelesAdapter.HotelItem(R.drawable.hotel2, "Hotel Westin"),
                new HotelesAdapter.HotelItem(R.drawable.hotel3, "Hotel DeCameron"),
                new HotelesAdapter.HotelItem(R.drawable.hotel4, "Hotel Royal Palace")

        );
        rvHoteles.setAdapter(new HotelesAdapter(hoteles));

        // RECYCLER VIEW OFERTAS
        /*RecyclerView rvOfertas = findViewById(R.id.rvOfertas);
        rvOfertas.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<OfertasAdapter.OfertaItem> ofertas = Arrays.asList(
                new OfertasAdapter.OfertaItem(R.drawable.offer1, "Oferta Caribe"),
                new OfertasAdapter.OfertaItem(R.drawable.offer2, "Descuento Europa")
        );
        rvOfertas.setAdapter(new OfertasAdapter(ofertas));*/


        tvDate = findViewById(R.id.tvDate);

        // Crear el selector de fechas
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Seleccionar las fechas");

        final MaterialDatePicker<Pair<Long, Long>> dateRangePicker = builder.build();

        // Mostrar el selector al hacer clic
        tvDate.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        // Manejar la selección de fechas
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            String formattedStart = formatter.format(new Date(startDate != null ? startDate : 0));
            String formattedEnd = formatter.format(new Date(endDate != null ? endDate : 0));

            tvDate.setText(formattedStart + " - " + formattedEnd);
        });


    }
}
