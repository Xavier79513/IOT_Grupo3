package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.utils.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.NominatimPlace;
import com.example.telehotel.data.repository.CityRepository;
import com.example.telehotel.features.auth.LoginActivity;
import com.example.telehotel.features.cliente.adapters.CitySuggestionAdapter;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.example.telehotel.features.cliente.adapters.LugaresAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ClientePaginaPrincipal extends AppCompatActivity {

    private EditText etCity;
    private RecyclerView rvCitySuggestions;
    private CitySuggestionAdapter citySuggestionAdapter;

    private TextView tvDate;
    private TextView tvPeople;
    private RecyclerView rvLugares;
    private RecyclerView rvHoteles;

    private HotelAdapter hotelAdapter;
    private List<Hotel> listaHoteles;

    private PrefsManager prefsManager;
    private ImageView ivLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_pagina_inicio);

        // Inicializar vistas
        ivLogout = findViewById(R.id.ivLogout);

        // Lógica para cerrar sesión
        ivLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Cerrar sesión de Firebase
            Toast.makeText(ClientePaginaPrincipal.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            // Ir al login y limpiar el back stack
            Intent intent = new Intent(ClientePaginaPrincipal.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finaliza esta actividad
        });

        prefsManager = new PrefsManager(this);

        // --- Inicialización de componentes ---
        etCity = findViewById(R.id.etCity);
        rvCitySuggestions = findViewById(R.id.rvCitySuggestions);
        tvDate = findViewById(R.id.tvDate);
        tvPeople = findViewById(R.id.tvPeople);

        rvLugares = findViewById(R.id.rvLugares);
        rvHoteles = findViewById(R.id.rvHoteles);

        // --- Setup RecyclerView de sugerencias de ciudades ---
        citySuggestionAdapter = new CitySuggestionAdapter(new ArrayList<>());
        rvCitySuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvCitySuggestions.setAdapter(citySuggestionAdapter);
        rvCitySuggestions.setVisibility(View.GONE);

        citySuggestionAdapter.setOnItemClickListener(place -> {
            etCity.setText(place.getDisplayName());
            rvCitySuggestions.setVisibility(View.GONE);
            // Aquí puedes guardar la ciudad o filtrar hoteles según place.getDisplayName()
        });

        // TextWatcher para el buscador de ciudad con debounce
        etCity.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long DELAY = 500; // ms

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            if (s.length() > 2) {
                                buscarCiudades(s.toString());
                            } else {
                                citySuggestionAdapter.updateList(new ArrayList<>());
                                rvCitySuggestions.setVisibility(View.GONE);
                            }
                        });
                    }
                }, DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // --- Cargar lista horizontal de lugares ---
        rvLugares.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<LugaresAdapter.LugarItem> lugares = Arrays.asList(
                new LugaresAdapter.LugarItem(R.drawable.ivory_coast, "Costa de Marfil"),
                new LugaresAdapter.LugarItem(R.drawable.newyork, "New York"),
                new LugaresAdapter.LugarItem(R.drawable.cancun, "Cancun"),
                new LugaresAdapter.LugarItem(R.drawable.barcelona, "Barcelona"),
                new LugaresAdapter.LugarItem(R.drawable.envigado, "Bucaramanga")
        );
        rvLugares.setAdapter(new LugaresAdapter(lugares));

        // --- Cargar lista horizontal de hoteles ---
        listaHoteles = new ArrayList<>();
        hotelAdapter = new HotelAdapter(listaHoteles);
        rvHoteles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHoteles.setAdapter(hotelAdapter);

        FirebaseUtil.getFirestore()
                .collection("hoteles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaHoteles.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) listaHoteles.add(hotel);
                    }
                    hotelAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar hoteles", Toast.LENGTH_SHORT).show()
                );

        // --- Selector de fechas ---
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Seleccionar las fechas")
                .build();

        tvDate.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            String formattedStart = formatter.format(new Date(startDate != null ? startDate : 0));
            String formattedEnd = formatter.format(new Date(endDate != null ? endDate : 0));

            tvDate.setText(formattedStart + " - " + formattedEnd);

            // Guarda fechas en PrefsManager
            prefsManager.saveDateRange(startDate != null ? startDate : 0, endDate != null ? endDate : 0);
        });

        // --- Diálogo para seleccionar personas/habitaciones ---
        tvPeople.setOnClickListener(v -> showPeopleDialog());

        // --- Botón Buscar ---
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(ClientePaginaPrincipal.this, ClienteMainActivity.class);
            startActivity(intent);
        });

        // Cargar datos guardados
        loadSavedData();
    }

    // Método para buscar ciudades usando el repositorio
    private void buscarCiudades(String query) {
        CityRepository.searchCities(query,
                lugares -> runOnUiThread(() -> {
                    if (lugares.isEmpty()) {
                        rvCitySuggestions.setVisibility(View.GONE);
                    } else {
                        citySuggestionAdapter.updateList(lugares);
                        rvCitySuggestions.setVisibility(View.VISIBLE);
                    }
                }),
                error -> runOnUiThread(() -> {
                    Toast.makeText(this, "Error al buscar ciudades: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("CitySearchError", "Error al buscar ciudades", error);
                    rvCitySuggestions.setVisibility(View.GONE);
                }));
    }

    // Diálogo para seleccionar personas y habitaciones
    private void showPeopleDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_people, null);

        TextView tvRoom = view.findViewById(R.id.tvRoomCount);
        TextView tvAdult = view.findViewById(R.id.tvAdultCount);
        TextView tvChild = view.findViewById(R.id.tvChildCount);

        View btnRoomPlus = view.findViewById(R.id.btnRoomPlus);
        View btnRoomMinus = view.findViewById(R.id.btnRoomMinus);
        View btnAdultPlus = view.findViewById(R.id.btnAdultPlus);
        View btnAdultMinus = view.findViewById(R.id.btnAdultMinus);
        View btnChildPlus = view.findViewById(R.id.btnChildPlus);
        View btnChildMinus = view.findViewById(R.id.btnChildMinus);

        View btnApply = view.findViewById(R.id.btnApply);

        final int[] roomCount = {0};
        final int[] adultCount = {0};
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
            tvPeople.setText(result);

            // Guarda string personas en PrefsManager
            prefsManager.savePeopleString(result);

            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // Cargar datos guardados en la UI
    private void loadSavedData() {
        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();
        String peopleString = prefsManager.getPeopleString();

        if (startDate != 0 && endDate != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            String formattedStart = formatter.format(new Date(startDate));
            String formattedEnd = formatter.format(new Date(endDate));
            tvDate.setText(formattedStart + " - " + formattedEnd);
        }

        if (peopleString != null) {
            tvPeople.setText(peopleString);
        }
    }
}
