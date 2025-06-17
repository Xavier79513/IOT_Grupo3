package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.core.storage.StorageHelper;
import com.example.telehotel.data.model.SearchHistory;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.LocationSearch;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.features.auth.LoginActivity;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.example.telehotel.features.cliente.adapters.LugaresAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientePaginaPrincipal extends AppCompatActivity {
    private static final String TAG = "ClientePaginaPrincipal";

    // Componentes de búsqueda
    private AutoCompleteTextView etCity;
    private ArrayAdapter<String> cityAdapter;
    private MaterialCardView dateSelector;
    private MaterialCardView guestSelector;
    private TextView tvDate;
    private TextView tvPeople;
    private MaterialButton btnSearch;

    // RecyclerViews principales
    private RecyclerView rvLugares;
    private RecyclerView rvHoteles;

    // Adaptadores
    private HotelAdapter hotelAdapter;
    private LugaresAdapter lugaresAdapter;
    private List<Hotel> listaHoteles;

    // Sistema de storage
    private PrefsManager prefsManager;
    private StorageHelper storageHelper;

    // Botones adicionales
    private MaterialButton btnVerTodoLugares;
    private MaterialButton btnVerTodoHoteles;
    private ImageView ivLogout;

    // Variables para el selector de personas
    private int roomCount = 1;
    private int adultCount = 2;
    private int childCount = 0;

    // Handler para búsqueda con debounce
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Lista de ubicaciones para el buscador
    private List<String> allHotelLocations = new ArrayList<>();
    private List<LocationSearch> detailedLocations = new ArrayList<>();
    private static final String NO_LOCATION_MESSAGE = "No se cuenta con la ubicación escrita";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - Iniciando actividad");

        setContentView(R.layout.cliente_pagina_inicio);

        // Inicializar sistema de storage
        initializeStorage();

        // Inicializar vistas
        initializeViews();

        // Configurar listeners
        setupListeners();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Cargar datos
        loadInitialData();

        Log.d(TAG, "onCreate - Completado");
    }

    private void initializeStorage() {
        try {
            prefsManager = new PrefsManager(this);
            storageHelper = new StorageHelper(this);
            Log.d(TAG, "Storage inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando storage", e);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews - Iniciando");

        try {
            // Componente de búsqueda
            etCity = findViewById(R.id.etCity);
            Log.d(TAG, "AutoCompleteTextView encontrado: " + (etCity != null));

            dateSelector = findViewById(R.id.dateSelector);
            guestSelector = findViewById(R.id.guestSelector);
            tvDate = findViewById(R.id.tvDate);
            tvPeople = findViewById(R.id.tvPeople);
            btnSearch = findViewById(R.id.btnSearch);

            // RecyclerViews principales
            rvLugares = findViewById(R.id.rvLugares);
            rvHoteles = findViewById(R.id.rvHoteles);

            // Botones adicionales
            btnVerTodoLugares = findViewById(R.id.btnVerTodoLugares);
            btnVerTodoHoteles = findViewById(R.id.btnVerTodoHoteles);
            ivLogout = findViewById(R.id.ivLogout);

            Log.d(TAG, "initializeViews - Completado");

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas", e);
        }
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - Iniciando");

        try {
            // Logout
            if (ivLogout != null) {
                ivLogout.setOnClickListener(v -> performLogout());
            }

            // Configurar AutoCompleteTextView
            setupAutoCompleteTextView();

            // Selector de fechas
            setupDateSelector();

            // Selector de personas/habitaciones
            setupGuestSelector();

            // Botón Buscar
            if (btnSearch != null) {
                btnSearch.setOnClickListener(v -> performSearch());
            }

            // Botones "Ver todo"
            if (btnVerTodoLugares != null) {
                btnVerTodoLugares.setOnClickListener(v -> {
                    Toast.makeText(this, "Ver todos los lugares", Toast.LENGTH_SHORT).show();
                });
            }

            if (btnVerTodoHoteles != null) {
                btnVerTodoHoteles.setOnClickListener(v -> {
                    Toast.makeText(this, "Ver todos los hoteles", Toast.LENGTH_SHORT).show();
                });
            }

            Log.d(TAG, "setupListeners - Completado");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando listeners", e);
        }
    }

    private void setupAutoCompleteTextView() {
        Log.d(TAG, "setupAutoCompleteTextView - Iniciando");

        if (etCity == null) {
            Log.e(TAG, "AutoCompleteTextView es null");
            return;
        }

        try {
            // Crear ArrayAdapter simple
            cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            etCity.setAdapter(cityAdapter);

            // Configurar comportamiento
            etCity.setThreshold(2);
            etCity.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            etCity.setDropDownHeight(400);

            // TextWatcher para filtrar ubicaciones
            etCity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Cancelar búsqueda anterior
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    // Programar nueva búsqueda
                    searchRunnable = () -> {
                        String query = s.toString().trim();
                        Log.d(TAG, "Filtrando ubicaciones para: " + query);

                        if (query.length() >= 2) {
                            filtrarUbicacionesSimple(query);
                        } else {
                            // Mostrar todas las ubicaciones
                            cityAdapter.clear();
                            cityAdapter.addAll(allHotelLocations);
                            cityAdapter.notifyDataSetChanged();
                        }
                    };

                    searchHandler.postDelayed(searchRunnable, 300);
                }

                private void filtrarUbicacionesSimple(String query) {
                    if (query == null || query.trim().isEmpty()) {
                        return;
                    }

                    try {
                        String queryLower = query.toLowerCase();
                        List<String> ubicacionesFiltradas = new ArrayList<>();

                        // Filtrar ubicaciones existentes
                        for (String ubicacion : allHotelLocations) {
                            if (ubicacion.toLowerCase().contains(queryLower)) {
                                ubicacionesFiltradas.add(ubicacion);
                            }
                        }

                        // Si no se encontraron ubicaciones, mostrar mensaje simple
                        if (ubicacionesFiltradas.isEmpty()) {
                            ubicacionesFiltradas.add(NO_LOCATION_MESSAGE);
                        }

                        // Actualizar adapter
                        cityAdapter.clear();
                        cityAdapter.addAll(ubicacionesFiltradas);
                        cityAdapter.notifyDataSetChanged();

                        // Mostrar dropdown
                        if (!ubicacionesFiltradas.isEmpty() && etCity.hasFocus()) {
                            etCity.showDropDown();

                            if (ubicacionesFiltradas.get(0).equals(NO_LOCATION_MESSAGE)) {
                                Log.d(TAG, "Mostrando mensaje de ubicación no encontrada para: " + query);
                            } else {
                                Log.d(TAG, "Mostrando " + ubicacionesFiltradas.size() + " ubicaciones filtradas");
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error filtrando ubicaciones", e);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            // Listener para selección
            etCity.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    String selectedLocation = cityAdapter.getItem(position);
                    if (selectedLocation != null) {
                        // *** CAMBIO: Verificar si es el mensaje de "no encontrado" ***
                        if (selectedLocation.equals(NO_LOCATION_MESSAGE)) {
                            // No hacer nada, es solo un mensaje informativo
                            return;
                        }

                        Log.d(TAG, "Ubicación seleccionada: " + selectedLocation);

                        // Buscar información detallada de la ubicación
                        LocationSearch locationDetails = findLocationDetails(selectedLocation);
                        if (locationDetails != null) {
                            String message = "✓ " + selectedLocation + " (" + locationDetails.getHotelCount() + " hoteles)";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "✓ " + selectedLocation, Toast.LENGTH_SHORT).show();
                        }

                        // Establecer el texto en el campo
                        etCity.setText(selectedLocation);
                        etCity.dismissDropDown();
                        etCity.clearFocus();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error en selección de ubicación", e);
                }
            });

            // Cargar ubicaciones de hoteles al inicializar
            loadHotelLocations();

            Log.d(TAG, "AutoCompleteTextView configurado correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando AutoCompleteTextView", e);
        }
    }

    /**
     * Carga todas las ubicaciones únicas de los hoteles
     */
    private void loadHotelLocations() {
        Log.d(TAG, "Cargando ubicaciones de hoteles...");

        // Cargar ubicaciones simples
        HotelRepository.getUniqueLocations(
                ubicaciones -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            try {
                                allHotelLocations.clear();
                                allHotelLocations.addAll(ubicaciones);

                                // Actualizar adapter
                                cityAdapter.clear();
                                cityAdapter.addAll(allHotelLocations);
                                cityAdapter.notifyDataSetChanged();

                                Log.d(TAG, "Ubicaciones cargadas: " + allHotelLocations.size());

                            } catch (Exception e) {
                                Log.e(TAG, "Error actualizando ubicaciones", e);
                            }
                        });
                    }
                },
                error -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error cargando ubicaciones: " + error.getMessage());
                            Toast.makeText(this, "Error cargando ubicaciones", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );

        // Cargar ubicaciones con detalles
        HotelRepository.getUniqueLocationsWithDetails(
                ubicacionesDetalladas -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            try {
                                detailedLocations.clear();
                                detailedLocations.addAll(ubicacionesDetalladas);
                                Log.d(TAG, "Ubicaciones detalladas cargadas: " + detailedLocations.size());
                            } catch (Exception e) {
                                Log.e(TAG, "Error actualizando ubicaciones detalladas", e);
                            }
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "Error cargando ubicaciones detalladas: " + error.getMessage());
                }
        );
    }

    /**
     * Filtra las ubicaciones basándose en el texto ingresado
     */
    private void filtrarUbicaciones(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        try {
            String queryLower = query.toLowerCase();
            List<String> ubicacionesFiltradas = new ArrayList<>();

            for (String ubicacion : allHotelLocations) {
                if (ubicacion.toLowerCase().contains(queryLower)) {
                    ubicacionesFiltradas.add(ubicacion);
                }
            }

            // Actualizar adapter
            cityAdapter.clear();
            cityAdapter.addAll(ubicacionesFiltradas);
            cityAdapter.notifyDataSetChanged();

            // Mostrar dropdown si hay resultados y el campo tiene foco
            if (!ubicacionesFiltradas.isEmpty() && etCity.hasFocus()) {
                etCity.showDropDown();
                Log.d(TAG, "Mostrando " + ubicacionesFiltradas.size() + " ubicaciones filtradas");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error filtrando ubicaciones", e);
        }
    }

    /**
     * Busca los detalles de una ubicación específica
     */
    private LocationSearch findLocationDetails(String locationName) {
        for (LocationSearch location : detailedLocations) {
            if (location.getDisplayName().equals(locationName)) {
                return location;
            }
        }
        return null;
    }

    private void setupDateSelector() {
        if (dateSelector == null) return;

        try {
            MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Seleccionar las fechas")
                    .build();

            dateSelector.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                Long startDate = selection.first;
                Long endDate = selection.second;

                if (startDate != null && endDate != null) {
                    if (storageHelper != null && storageHelper.isPastDate(startDate)) {
                        Toast.makeText(this, "No puedes seleccionar fechas pasadas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
                    String dateText = formatter.format(new Date(startDate)) + " - " + formatter.format(new Date(endDate));

                    if (tvDate != null) {
                        tvDate.setText(dateText);
                        tvDate.setTextColor(getResources().getColor(R.color.black));
                    }

                    if (prefsManager != null) {
                        prefsManager.saveDateRange(startDate, endDate);
                    }

                    if (storageHelper != null) {
                        int days = storageHelper.calculateDaysDifference(startDate, endDate);
                        Toast.makeText(this, "Estadía de " + days + " días seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error configurando selector de fechas", e);
        }
    }

    private void setupGuestSelector() {
        if (guestSelector != null) {
            guestSelector.setOnClickListener(v -> showPeopleDialog());
        }
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "setupRecyclerViews - Iniciando");

        try {
            // Setup lugares turísticos
            setupLugaresRecyclerView();

            // Setup hoteles
            setupHotelesRecyclerView();

            Log.d(TAG, "setupRecyclerViews - Completado");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando RecyclerViews", e);
        }
    }

    private void setupLugaresRecyclerView() {
        if (rvLugares == null) return;

        try {
            rvLugares.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            List<LugaresAdapter.LugarItem> lugares = Arrays.asList(
                    new LugaresAdapter.LugarItem(R.drawable.ivory_coast, "Costa de Marfil"),
                    new LugaresAdapter.LugarItem(R.drawable.newyork, "Nueva York"),
                    new LugaresAdapter.LugarItem(R.drawable.cancun, "Cancún"),
                    new LugaresAdapter.LugarItem(R.drawable.barcelona, "Barcelona"),
                    new LugaresAdapter.LugarItem(R.drawable.envigado, "Bucaramanga")
            );

            lugaresAdapter = new LugaresAdapter(lugares);
            lugaresAdapter.setOnItemClickListener(lugar -> {
                if (etCity != null) {
                    etCity.setText(lugar.getNombre());
                }
                Toast.makeText(this, "🌍 Destino seleccionado: " + lugar.getNombre(), Toast.LENGTH_SHORT).show();
            });
            rvLugares.setAdapter(lugaresAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Error configurando lugares", e);
        }
    }

    private void setupHotelesRecyclerView() {
        if (rvHoteles == null) return;

        try {
            listaHoteles = new ArrayList<>();
            hotelAdapter = new HotelAdapter(listaHoteles);
            rvHoteles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvHoteles.setAdapter(hotelAdapter);

            hotelAdapter.setOnItemClickListener(hotel -> {
                Toast.makeText(this, "Hotel " + hotel.getNombre() + " seleccionado", Toast.LENGTH_SHORT).show();
            });

            hotelAdapter.setOnFavoriteClickListener((hotel, isFavorite) -> {
                String message = isFavorite ? "Agregado a favoritos" : "Removido de favoritos";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });

            loadHotelesFromRepository();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando hoteles", e);
        }
    }

    private void loadInitialData() {
        try {
            loadSavedData();
        } catch (Exception e) {
            Log.e(TAG, "Error cargando datos iniciales", e);
        }
    }

    private void loadHotelesFromRepository() {
        try {
            HotelRepository.getAllHotels(
                    hoteles -> {
                        if (!isFinishing() && !isDestroyed()) {
                            runOnUiThread(() -> {
                                try {
                                    listaHoteles.clear();
                                    listaHoteles.addAll(hoteles);

                                    if (hotelAdapter != null) {
                                        hotelAdapter.notifyDataSetChanged();
                                    }

                                    Log.d(TAG, "Hoteles cargados: " + listaHoteles.size());
                                    Toast.makeText(this, "Cargados " + listaHoteles.size() + " hoteles", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error actualizando lista de hoteles", e);
                                }
                            });
                        }
                    },
                    error -> {
                        if (!isFinishing() && !isDestroyed()) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Error cargando hoteles: " + error.getMessage());
                                Toast.makeText(this, "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error iniciando carga de hoteles", e);
        }
    }

    private void showPeopleDialog() {
        try {
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

            // Establecer valores actuales
            tvRoom.setText(String.valueOf(roomCount));
            tvAdult.setText(String.valueOf(adultCount));
            tvChild.setText(String.valueOf(childCount));

            // Configurar listeners con límites
            btnRoomPlus.setOnClickListener(v -> {
                if (roomCount < 10) {
                    roomCount++;
                    tvRoom.setText(String.valueOf(roomCount));
                }
            });

            btnRoomMinus.setOnClickListener(v -> {
                if (roomCount > 1) {
                    roomCount--;
                    tvRoom.setText(String.valueOf(roomCount));
                }
            });

            btnAdultPlus.setOnClickListener(v -> {
                if (adultCount < 20) {
                    adultCount++;
                    tvAdult.setText(String.valueOf(adultCount));
                }
            });

            btnAdultMinus.setOnClickListener(v -> {
                if (adultCount > 1) {
                    adultCount--;
                    tvAdult.setText(String.valueOf(adultCount));
                }
            });

            btnChildPlus.setOnClickListener(v -> {
                if (childCount < 10) {
                    childCount++;
                    tvChild.setText(String.valueOf(childCount));
                }
            });

            btnChildMinus.setOnClickListener(v -> {
                if (childCount > 0) {
                    childCount--;
                    tvChild.setText(String.valueOf(childCount));
                }
            });

            btnApply.setOnClickListener(v -> {
                // Actualizar UI
                updatePeopleDisplay();

                // Guardar búsqueda completa si hay datos suficientes
                String location = etCity.getText().toString().trim();
                if (!location.isEmpty() && prefsManager != null &&
                        prefsManager.getStartDate() > 0 && prefsManager.getEndDate() > 0) {
                    storageHelper.saveCompleteSearch(location,
                            prefsManager.getStartDate(),
                            prefsManager.getEndDate(),
                            adultCount, childCount, roomCount);

                    Log.d(TAG, "Búsqueda guardada: " + location);
                }

                dialog.dismiss();
            });

            dialog.setContentView(view);
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de personas", e);
            Toast.makeText(this, "Error mostrando selector de huéspedes", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePeopleDisplay() {
        String result = roomCount + " habitación" + (roomCount > 1 ? "es" : "") + " · " +
                adultCount + " adulto" + (adultCount > 1 ? "s" : "") + " · " +
                childCount + " niño" + (childCount > 1 ? "s" : "");

        if (tvPeople != null) {
            tvPeople.setText(result);
            tvPeople.setTextColor(getResources().getColor(R.color.black));
        }

        if (prefsManager != null) {
            prefsManager.savePeopleString(result);
        }
    }

    private void performSearch() {
        String location = etCity != null ? etCity.getText().toString().trim() : "";

        if (location.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una ciudad o país", Toast.LENGTH_SHORT).show();
            if (etCity != null) {
                etCity.requestFocus();
            }
            return;
        }

        // *** CAMBIO: Verificar si es el mensaje de error ***
        if (location.equals(NO_LOCATION_MESSAGE)) {
            Toast.makeText(this, "Por favor selecciona una ubicación válida", Toast.LENGTH_SHORT).show();
            if (etCity != null) {
                etCity.setText("");
                etCity.requestFocus();
            }
            return;
        }

        if (prefsManager != null && (prefsManager.getStartDate() == 0 || prefsManager.getEndDate() == 0)) {
            Toast.makeText(this, "Por favor selecciona las fechas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Continuar con la búsqueda usando la función mejorada del repository
        searchHotelsByLocationEnhanced(location);
    }

    private void searchHotelsByLocationEnhanced(String location) {
        Log.d(TAG, "Buscando hoteles en: " + location);

        String searchMessage = String.format("Buscando hoteles en %s para %d adultos, %d niños, %d habitación(es)...",
                location, adultCount, childCount, roomCount);
        Toast.makeText(this, searchMessage, Toast.LENGTH_SHORT).show();

        // Usar la función mejorada del repository que maneja tanto búsquedas estándar como flexibles
        HotelRepository.searchHotelsWithCustomParamsEnhanced(
                this,
                location,
                adultCount,
                childCount,
                roomCount,
                hoteles -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            try {
                                // Guardar búsqueda
                                if (storageHelper != null && prefsManager != null) {
                                    storageHelper.saveCompleteSearch(location,
                                            prefsManager.getStartDate(),
                                            prefsManager.getEndDate(),
                                            adultCount, childCount, roomCount);
                                }

                                // Calcular días de estadía
                                int dias = storageHelper != null && prefsManager != null ?
                                        storageHelper.calculateDaysDifference(prefsManager.getStartDate(), prefsManager.getEndDate()) : 0;

                                if (hoteles.isEmpty()) {
                                    // No se encontraron hoteles
                                    String noResultsMessage = String.format(
                                            "No se encontraron hoteles disponibles en '%s' para %d adultos, %d niños y %d habitación(es)",
                                            location, adultCount, childCount, roomCount);
                                    Toast.makeText(this, noResultsMessage, Toast.LENGTH_LONG).show();
                                } else {
                                    // Hoteles encontrados
                                    String successMessage = String.format(
                                            "✅ %d hoteles encontrados en '%s' - %d días - %d habitación(es)",
                                            hoteles.size(), location, dias, roomCount);
                                    Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();

                                    // Navegar a resultados
                                    navigateToSearchResults(location,
                                            prefsManager != null ? prefsManager.getStartDate() : 0L,
                                            prefsManager != null ? prefsManager.getEndDate() : 0L,
                                            hoteles.size());
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando resultados", e);
                                Toast.makeText(this, "Error procesando resultados", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                },
                error -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error en búsqueda: " + error.getMessage());
                            String errorMessage = String.format(
                                    "No se pudieron buscar hoteles en '%s'. Intenta con otra ubicación.",
                                    location);
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    /**
     * Busca hoteles por ubicación y navega a los resultados
     */
    private void searchHotelsByLocation(String location) {
        Log.d(TAG, "Buscando hoteles en: " + location);

        // Mostrar mensaje de búsqueda con parámetros
        String searchMessage = String.format("Buscando hoteles en %s para %d adultos, %d niños, %d habitación(es)...",
                location, adultCount, childCount, roomCount);
        Toast.makeText(this, searchMessage, Toast.LENGTH_SHORT).show();

        // Buscar hoteles usando parámetros personalizados
        HotelRepository.searchHotelsWithCustomParams(
                this, // context
                location,
                adultCount,
                childCount,
                roomCount,
                hoteles -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            try {
                                // Guardar búsqueda en el storage
                                if (storageHelper != null && prefsManager != null) {
                                    storageHelper.saveCompleteSearch(location,
                                            prefsManager.getStartDate(),
                                            prefsManager.getEndDate(),
                                            adultCount, childCount, roomCount);
                                }

                                // Calcular días de estadía
                                int dias = storageHelper != null && prefsManager != null ?
                                        storageHelper.calculateDaysDifference(prefsManager.getStartDate(), prefsManager.getEndDate()) : 0;

                                // Mostrar resumen de búsqueda
                                String searchSummary = String.format(
                                        "%s - %d días - %d habitación(es) - %d adulto(s) - %d niño(s) - %d hoteles encontrados",
                                        location, dias, roomCount, adultCount, childCount, hoteles.size());

                                if (hoteles.isEmpty()) {
                                    // No se encontraron hoteles que cumplan los criterios
                                    showNoHotelsFoundDialog(location, adultCount, childCount, roomCount);
                                } else {
                                    // Hoteles encontrados
                                    Toast.makeText(this, searchSummary, Toast.LENGTH_LONG).show();

                                    // Navegar a resultados con información completa
                                    navigateToSearchResults(location,
                                            prefsManager != null ? prefsManager.getStartDate() : 0L,
                                            prefsManager != null ? prefsManager.getEndDate() : 0L,
                                            hoteles.size());
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando resultados de búsqueda", e);
                                Toast.makeText(this, "Error procesando resultados", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                },
                error -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error buscando hoteles: " + error.getMessage());

                            if (error.getMessage().contains("Fechas no configuradas")) {
                                Toast.makeText(this, "Por favor selecciona las fechas de estadía", Toast.LENGTH_LONG).show();
                            } else if (error.getMessage().contains("Formato de ubicación inválido")) {
                                Toast.makeText(this, "Formato de ubicación inválido. Use: 'Ciudad, País'", Toast.LENGTH_LONG).show();
                            } else {
                                String errorMessage = String.format(
                                        "No se encontraron hoteles disponibles en %s para %d adultos, %d niños y %d habitación(es)",
                                        location, adultCount, childCount, roomCount);
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
        );
    }

    /**
     * Muestra un diálogo cuando no se encuentran hoteles
     */
    private void showNoHotelsFoundDialog(String location, int adults, int children, int rooms) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_no_hotels_found, null);

            TextView tvMessage = view.findViewById(R.id.tvNoHotelsMessage);
            MaterialButton btnTryAgain = view.findViewById(R.id.btnTryAgain);
            MaterialButton btnShowAll = view.findViewById(R.id.btnShowAllHotels);

            // Mensaje personalizado
            String message = String.format(
                    "No encontramos hoteles en %s que cumplan exactamente con tus criterios:\n\n" +
                            "• %d adulto(s)\n• %d niño(s)\n• %d habitación(es)\n\n" +
                            "¿Te gustaría ver todos los hoteles disponibles en esta ubicación?",
                    location, adults, children, rooms);

            tvMessage.setText(message);

            // Botón para intentar de nuevo (modificar búsqueda)
            btnTryAgain.setOnClickListener(v -> {
                dialog.dismiss();
                // Reabrir el selector de huéspedes para modificar criterios
                showPeopleDialog();
            });

            // Botón para mostrar todos los hoteles sin filtros
            btnShowAll.setOnClickListener(v -> {
                dialog.dismiss();
                searchAllHotelsInLocation(location);
            });

            dialog.setContentView(view);
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de no encontrados", e);
            // Fallback: mostrar toast simple
            String message = String.format("No se encontraron hoteles en %s para tus criterios. Intenta con menos restricciones.", location);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Busca todos los hoteles en una ubicación sin filtros de capacidad
     */
    private void searchAllHotelsInLocation(String location) {
        Log.d(TAG, "Buscando todos los hoteles en: " + location);

        HotelRepository.searchHotelsByLocationString(location,
                hoteles -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            String message = String.format("Mostrando todos los %d hoteles en %s",
                                    hoteles.size(), location);
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            navigateToSearchResults(location,
                                    prefsManager.getStartDate(),
                                    prefsManager.getEndDate(),
                                    hoteles.size());
                        });
                    }
                },
                error -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    /**
     * Navega a la pantalla de resultados de búsqueda
     */
    private void navigateToSearchResults(String location, long startDate, long endDate, int hotelsFound) {
        Log.d(TAG, String.format("Navegando a resultados: %s, fechas: %d-%d, hoteles: %d",
                location, startDate, endDate, hotelsFound));

        Intent intent = new Intent(this, ClienteMainActivity.class);
        intent.putExtra("search_location", location);
        intent.putExtra("start_date", startDate);
        intent.putExtra("end_date", endDate);
        intent.putExtra("adults", adultCount);
        intent.putExtra("children", childCount);
        intent.putExtra("rooms", roomCount);
        intent.putExtra("hotels_found", hotelsFound);
        intent.putExtra("use_filters", true); // Indica que se usaron filtros

        // *** NUEVO: Log para debug ***
        Log.d(TAG, String.format("Intent extras: ubicacion='%s', adultos=%d, niños=%d, habitaciones=%d",
                location, adultCount, childCount, roomCount));

        startActivity(intent);
    }

    /**
     * Muestra estadísticas de disponibilidad para una ubicación
     */
    private void showAvailabilityStats(String location) {
        if (prefsManager == null) return;

        HotelRepository.getAvailabilityStats(location,
                prefsManager.getStartDate(),
                prefsManager.getEndDate(),
                stats -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            String message = String.format(
                                    "📊 Disponibilidad en %s:\n• %s\n• Capacidad máxima: %d adultos, %d niños",
                                    location, stats.getResumen(),
                                    stats.capacidadMaximaAdultos, stats.capacidadMaximaNinos
                            );
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "Error obteniendo estadísticas: " + error.getMessage());
                }
        );
    }

    private void performLogout() {
        try {
            if (prefsManager != null) {
                prefsManager.clearUserSession();
            }
            if (storageHelper != null) {
                storageHelper.clearCurrentSessionData();
            }

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error durante logout", e);
            Toast.makeText(this, "Error cerrando sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedData() {
        if (prefsManager == null) return;

        try {
            // Cargar fechas
            long startDate = prefsManager.getStartDate();
            long endDate = prefsManager.getEndDate();

            if (startDate != 0 && endDate != 0 && tvDate != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
                String dateText = formatter.format(new Date(startDate)) + " - " + formatter.format(new Date(endDate));
                tvDate.setText(dateText);
                tvDate.setTextColor(getResources().getColor(R.color.black));
            }

            // Cargar personas
            String peopleString = prefsManager.getPeopleString();
            if (peopleString != null && !peopleString.isEmpty() && tvPeople != null) {
                tvPeople.setText(peopleString);
                tvPeople.setTextColor(getResources().getColor(R.color.black));

                // Intentar parsear para actualizar las variables
                try {
                    parsePeopleString(peopleString);
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo parsear: " + peopleString);
                }
            }

            // Cargar última búsqueda si existe (opcional)
            if (storageHelper != null) {
                List<SearchHistory> history = storageHelper.getRecentSearches();
                if (!history.isEmpty() && etCity != null) {
                    SearchHistory lastSearch = history.get(0);
                    etCity.setText(lastSearch.getLocation());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error cargando datos guardados", e);
        }
    }

    private void parsePeopleString(String peopleString) {
        // Ejemplo: "2 habitaciones · 3 adultos · 1 niño"
        try {
            String[] parts = peopleString.split(" · ");
            if (parts.length >= 3) {
                roomCount = Integer.parseInt(parts[0].split(" ")[0]);
                adultCount = Integer.parseInt(parts[1].split(" ")[0]);
                childCount = Integer.parseInt(parts[2].split(" ")[0]);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing people string", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadSavedData();
            // Recargar ubicaciones en caso de que hayan cambiado
            loadHotelLocations();
        } catch (Exception e) {
            Log.e(TAG, "Error en onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            // Limpiar handler para evitar memory leaks
            if (searchHandler != null && searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            // Limpiar adapters
            if (cityAdapter != null) {
                cityAdapter.clear();
            }

            // Limpiar listas
            allHotelLocations.clear();
            detailedLocations.clear();

            Log.d(TAG, "onDestroy - Recursos limpiados");
        } catch (Exception e) {
            Log.e(TAG, "Error en onDestroy", e);
        }
    }
}