package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.util.Pair;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.example.telehotel.R;
import com.example.telehotel.features.cliente.adapters.HotelesAdapter;
import com.example.telehotel.features.cliente.adapters.LugaresAdapter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PaginaPrincipal extends AppCompatActivity {

    TextView tvDate;
    TextView tvPeople;
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
                new LugaresAdapter.LugarItem(R.drawable.ivory_coast, "Costa de Marfil"),
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
                new HotelesAdapter.HotelItem(R.drawable.hotel4, "Hotel Royal Palace"),
                new HotelesAdapter.HotelItem(R.drawable.hotel5, "Hotel Raval House")

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
        // PERSONAS / HABITACIONES
        TextView tvPeople = findViewById(R.id.tvPeople);
        tvPeople.setOnClickListener(v -> showPeopleDialog());


        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(PaginaPrincipal.this, ClienteMainActivity.class);
            startActivity(intent);
        });



    }
    private void showPeopleDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_people, null);

        TextView tvRoom = view.findViewById(R.id.tvRoomCount);
        TextView tvAdult = view.findViewById(R.id.tvAdultCount);
        TextView tvChild = view.findViewById(R.id.tvChildCount);

        Button btnRoomPlus = view.findViewById(R.id.btnRoomPlus);
        Button btnRoomMinus = view.findViewById(R.id.btnRoomMinus);
        Button btnAdultPlus = view.findViewById(R.id.btnAdultPlus);
        Button btnAdultMinus = view.findViewById(R.id.btnAdultMinus);
        Button btnChildPlus = view.findViewById(R.id.btnChildPlus);
        Button btnChildMinus = view.findViewById(R.id.btnChildMinus);

        Button btnApply = view.findViewById(R.id.btnApply);

        final int[] roomCount = {1};
        final int[] adultCount = {2};
        final int[] childCount = {0};

        btnRoomPlus.setOnClickListener(v -> {
            roomCount[0]++;
            tvRoom.setText(String.valueOf(roomCount[0]));
        });
        btnRoomMinus.setOnClickListener(v -> {
            if (roomCount[0] > 1) roomCount[0]--;
            tvRoom.setText(String.valueOf(roomCount[0]));
        });

        btnAdultPlus.setOnClickListener(v -> {
            adultCount[0]++;
            tvAdult.setText(String.valueOf(adultCount[0]));
        });
        btnAdultMinus.setOnClickListener(v -> {
            if (adultCount[0] > 1) adultCount[0]--;
            tvAdult.setText(String.valueOf(adultCount[0]));
        });

        btnChildPlus.setOnClickListener(v -> {
            childCount[0]++;
            tvChild.setText(String.valueOf(childCount[0]));
        });
        btnChildMinus.setOnClickListener(v -> {
            if (childCount[0] > 0) childCount[0]--;
            tvChild.setText(String.valueOf(childCount[0]));
        });

        btnApply.setOnClickListener(v -> {
            String result = roomCount[0] + " habitaciones · " +
                    adultCount[0] + " adultos · " +
                    childCount[0] + " niños";
            ((TextView)findViewById(R.id.tvPeople)).setText(result);
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }


}
