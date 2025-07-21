package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.graphics.Color;
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
import com.example.telehotel.core.utils.ReservaValidationUtil;
import com.example.telehotel.data.model.Reserva;
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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    // 🔥 NUEVO: Sistema de validación de reserva
    private ReservaValidationUtil reservaValidator;
    private boolean tieneReservaActiva = false;
    private Reserva reservaActiva = null;

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

            // Botones adicionales
            //btnVerTodoLugares = findViewById(R.id.btnVerTodoLugares);
            //btnVerTodoHoteles = findViewById(R.id.btnVerTodoHoteles);
            ivLogout = findViewById(R.id.ivLogout);
            // Si no existe ivLogout, busca el botón en el encabezado:
            if (ivLogout == null) {
                // Buscar en el encabezado incluido
                ivLogout = findViewById(R.id.btnLogout);  // o como se llame en tu encabezado
            }

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
                ivLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
            } else {
                Log.w(TAG, "Botón de logout no encontrado");
            }

            // Configurar AutoCompleteTextView
            setupAutoCompleteTextView();

            // Selector de fechas
            setupDateSelector();

            // Selector de personas/habitaciones
            setupGuestSelector();

            // Botón Buscar - 🔥 CAMBIO PRINCIPAL: Solo validar y navegar
            if (btnSearch != null) {
                btnSearch.setOnClickListener(v -> performSearchAndNavigate());
            }

            Log.d(TAG, "setupListeners - Completado");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando listeners", e);
        }
    }

    // ============= 🔥 MÉTODO PRINCIPAL SIMPLIFICADO =============

    /**
     * 🔥 NUEVO: Método simplificado que SIEMPRE navega a ClienteMainActivity
     */
    private void performSearchAndNavigate() {
        Log.d(TAG, "performSearchAndNavigate - Iniciando navegación directa");

        try {
            // 1. Obtener ubicación (puede estar vacía)
            String location = etCity != null ? etCity.getText().toString().trim() : "";

            // 2. Limpiar ubicación si es el mensaje de "no encontrada"
            if (location.equals(NO_LOCATION_MESSAGE)) {
                location = "";
            }

            // 3. Obtener fechas (pueden ser 0)
            long startDate = prefsManager != null ? prefsManager.getStartDate() : 0;
            long endDate = prefsManager != null ? prefsManager.getEndDate() : 0;

            // 4. 🔥 SIEMPRE guardar búsqueda antes de navegar (incluso si está incompleta)
            saveSearchDataBeforeNavigation(location, startDate, endDate);

            // 5. 🔥 SIEMPRE navegar - HotelsFragment manejará los estados
            navigateToClienteMainActivity(location, startDate, endDate);

        } catch (Exception e) {
            Log.e(TAG, "Error en performSearchAndNavigate", e);

            // 🔥 INCLUSO CON ERROR, navegar para mostrar mensaje apropiado
            navigateToClienteMainActivity("", 0, 0);
        }
    }

    /**
     * 🔥 NUEVO: Guardar datos de búsqueda antes de navegar (sin validaciones)
     */
    private void saveSearchDataBeforeNavigation(String location, long startDate, long endDate) {
        try {
            if (prefsManager == null) return;

            Log.d(TAG, String.format("Guardando búsqueda: ubicación='%s', fechas=%d-%d, huéspedes=%d+%d+%d",
                    location, startDate, endDate, adultCount, childCount, roomCount));

            // Guardar ubicación (incluso si está vacía)
            if (!location.isEmpty()) {
                prefsManager.saveSelectedLocation(location);
                prefsManager.saveLastSearchLocation(location);
                prefsManager.saveLocation(location); // compatibilidad
            }

            // Guardar huéspedes
            updatePeopleDisplay(); // Esto ya guarda en prefsManager

            // Crear historial de búsqueda (incluso si está incompleto)
            SearchHistory search = new SearchHistory(
                    location.isEmpty() ? "Ubicación no especificada" : location,
                    startDate > 0 ? startDate : System.currentTimeMillis(),
                    endDate > 0 ? endDate : System.currentTimeMillis() + (24 * 60 * 60 * 1000),
                    adultCount, childCount, roomCount
            );

            prefsManager.addSearchHistory(search);

            Log.d(TAG, "Búsqueda guardada exitosamente");
            Log.d(TAG, "Resumen: " + prefsManager.getCurrentSearchSummary());

        } catch (Exception e) {
            Log.e(TAG, "Error guardando búsqueda", e);
            // No hacer nada - seguir con la navegación
        }
    }

    /**
     * 🔥 NUEVO: Navegación directa SIEMPRE exitosa
     */
    private void navigateToClienteMainActivity(String location, long startDate, long endDate) {
        Log.d(TAG, String.format("Navegando a ClienteMainActivity con: ubicación='%s', fechas=%d-%d",
                location, startDate, endDate));

        try {
            Intent intent = new Intent(this, ClienteMainActivity.class);

            // Pasar TODOS los parámetros (incluso si algunos están vacíos o son 0)
            intent.putExtra("search_location", location);
            intent.putExtra("start_date", startDate);
            intent.putExtra("end_date", endDate);
            intent.putExtra("adults", adultCount);
            intent.putExtra("children", childCount);
            intent.putExtra("rooms", roomCount);

            // 🔥 NUEVO: Indicadores de estado para el HotelsFragment
            intent.putExtra("force_navigation", true); // Siempre mostrar vista
            intent.putExtra("search_from_main", true); // Proviene de búsqueda principal

            // Información adicional para debug
            intent.putExtra("has_location", !location.isEmpty());
            intent.putExtra("has_dates", startDate > 0 && endDate > 0);
            intent.putExtra("search_timestamp", System.currentTimeMillis());

            startActivity(intent);

            // 🔥 OPCIONAL: Mostrar mensaje informativo
            showNavigationMessage(location, startDate, endDate);

            Log.d(TAG, "Navegación exitosa a ClienteMainActivity");

        } catch (Exception e) {
            Log.e(TAG, "Error navegando a ClienteMainActivity", e);
            Toast.makeText(this, "Error navegando a resultados", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🔥 NUEVO: Mostrar mensaje informativo sobre la navegación
     */
    private void showNavigationMessage(String location, long startDate, long endDate) {
        try {
            String message;

            boolean hasLocation = !location.isEmpty();
            boolean hasDates = startDate > 0 && endDate > 0;

            if (hasLocation && hasDates) {
                message = String.format("🔍 Buscando hoteles en %s...", location);
            } else if (hasLocation && !hasDates) {
                message = String.format("📍 Explorando %s (completa las fechas para mejores resultados)", location);
            } else if (!hasLocation && hasDates) {
                message = "📅 Fechas seleccionadas (especifica una ubicación para mejores resultados)";
            } else {
                message = "🔍 Explorando opciones disponibles";
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.w(TAG, "Error mostrando mensaje de navegación", e);
        }
    }

    // ============= MÉTODOS EXISTENTES (sin cambios significativos) =============

    private void showLogoutConfirmationDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);

            builder.setTitle("Cerrar sesión");
            builder.setMessage("¿Estás seguro que deseas cerrar sesión?");
            builder.setIcon(R.drawable.ic_logout);

            builder.setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                performLogout();
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> {
                dialog.dismiss();
            });

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();

            // ✅ Cambiar colores a negro
            try {
                TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
                TextView messageView = dialog.findViewById(android.R.id.message);

                if (titleView != null) titleView.setTextColor(Color.BLACK);
                if (messageView != null) messageView.setTextColor(Color.BLACK);
            } catch (Exception colorException) {
                Log.w(TAG, "No se pudo cambiar color del diálogo", colorException);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de confirmación", e);
            performLogout();
        }
    }

    private void setupAutoCompleteTextView() {
        Log.d(TAG, "setupAutoCompleteTextView - Iniciando");

        if (etCity == null) {
            Log.e(TAG, "AutoCompleteTextView es null");
            return;
        }

        try {
            cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            etCity.setAdapter(cityAdapter);

            etCity.setThreshold(2);
            etCity.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            etCity.setDropDownHeight(400);

            etCity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    searchRunnable = () -> {
                        String query = s.toString().trim();
                        Log.d(TAG, "Filtrando ubicaciones para: " + query);

                        if (query.length() >= 2) {
                            filtrarUbicacionesSimple(query);
                        } else {
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

                        for (String ubicacion : allHotelLocations) {
                            if (ubicacion.toLowerCase().contains(queryLower)) {
                                ubicacionesFiltradas.add(ubicacion);
                            }
                        }

                        if (ubicacionesFiltradas.isEmpty()) {
                            ubicacionesFiltradas.add(NO_LOCATION_MESSAGE);
                        }

                        cityAdapter.clear();
                        cityAdapter.addAll(ubicacionesFiltradas);
                        cityAdapter.notifyDataSetChanged();

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

            etCity.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    String selectedLocation = cityAdapter.getItem(position);
                    if (selectedLocation != null && !selectedLocation.equals(NO_LOCATION_MESSAGE)) {
                        Log.d(TAG, "Ubicación seleccionada: " + selectedLocation);

                        saveSelectedLocationImmediate(selectedLocation);

                        LocationSearch locationDetails = findLocationDetails(selectedLocation);
                        if (locationDetails != null) {
                            String message = "✓ " + selectedLocation + " (" + locationDetails.getHotelCount() + " hoteles)";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "✓ " + selectedLocation, Toast.LENGTH_SHORT).show();
                        }

                        etCity.setText(selectedLocation);
                        etCity.dismissDropDown();
                        etCity.clearFocus();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error en selección de ubicación", e);
                }
            });

            etCity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String typedLocation = etCity.getText().toString().trim();
                    if (!typedLocation.isEmpty() && !typedLocation.equals(NO_LOCATION_MESSAGE)) {
                        saveSelectedLocationImmediate(typedLocation);
                    }
                }
            });

            // 🔥 MODIFICADO: Cargar ubicaciones con validación
            cargarUbicacionesConValidacion();

            Log.d(TAG, "AutoCompleteTextView configurado correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando AutoCompleteTextView", e);
        }
    }

    /**
     * 🔥 NUEVO: Método que verifica reserva antes de cargar ubicaciones
     */
    private void cargarUbicacionesConValidacion() {
        if (reservaValidator == null) {
            // Sin validador, cargar ubicaciones normalmente
            loadHotelLocations();
            return;
        }

        reservaValidator.verificarReservaActiva((tieneReserva, reserva, mensaje) -> {
            runOnUiThread(() -> {
                if (tieneReserva) {
                    // 🚫 Usuario tiene reserva activa - no cargar ubicaciones
                    Log.d(TAG, "No cargando ubicaciones - usuario tiene reserva activa");
                    // Opcional: Deshabilitar el campo de texto
                    if (etCity != null) {
                        etCity.setEnabled(false);
                        etCity.setHint("Búsqueda no disponible (tienes una reserva activa)");
                    }
                } else {
                    // ✅ Usuario sin reserva activa - cargar ubicaciones normalmente
                    loadHotelLocations();
                }
            });
        });
    }

    private void saveSelectedLocationImmediate(String location) {
        try {
            Log.d(TAG, "Guardando ubicación seleccionada: " + location);

            if (prefsManager != null) {
                prefsManager.saveSelectedLocation(location);
                prefsManager.saveLocation(location);
                Log.d(TAG, "Ubicación guardada en PrefsManager: " + location);
            }

            getSharedPreferences("TeleHotelPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("selected_location", location)
                    .putLong("location_timestamp", System.currentTimeMillis())
                    .apply();

            Log.d(TAG, "Ubicación guardada correctamente: " + location);

        } catch (Exception e) {
            Log.e(TAG, "Error guardando ubicación seleccionada", e);
        }
    }

    private void loadHotelLocations() {
        Log.d(TAG, "Cargando ubicaciones de hoteles...");

        HotelRepository.getUniqueLocations(
                ubicaciones -> {
                    if (!isFinishing() && !isDestroyed()) {
                        runOnUiThread(() -> {
                            try {
                                allHotelLocations.clear();
                                allHotelLocations.addAll(ubicaciones);

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

    private LocationSearch findLocationDetails(String locationName) {
        for (LocationSearch location : detailedLocations) {
            if (location.getDisplayName().equals(locationName)) {
                return location;
            }
        }
        return null;
    }
    /**
     * 🔥 NUEVO: Verificar estado de reserva en onResume
     */
    private void verificarReservaYCargarDatos() {
        Log.d(TAG, "Verificando estado de reserva");

        if (reservaValidator == null) {
            Log.w(TAG, "ReservaValidator no disponible, cargando datos normalmente");
            loadInitialData();
            return;
        }

        reservaValidator.verificarReservaActiva((tieneReserva, reserva, mensaje) -> {
            runOnUiThread(() -> {
                if (tieneReserva) {
                    // 🚫 Usuario tiene reserva activa
                    Log.d(TAG, "Usuario tiene reserva activa - limitando funcionalidades");
                    mostrarMensajeNoMasReservas(reserva);
                    // Cargar solo datos básicos (sin ubicaciones)
                    loadInitialData();
                } else {
                    // ✅ Usuario sin reserva activa - todo normal
                    Log.d(TAG, "Usuario sin reserva activa - cargando todo");
                    loadInitialData();
                }
            });
        });
    }
    private void mostrarMensajeNoMasReservas(Reserva reserva) {
        // Toast informativo
        Toast.makeText(this, "No puedes realizar nuevas reservas porque tienes una activa",
                Toast.LENGTH_LONG).show();

        // Bloquear solo el botón de búsqueda
        btnSearch.setEnabled(false);
        btnSearch.setText("Tienes una reserva activa");
    }
    private void setupDateSelector() {
        if (dateSelector == null) return;

        try {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setStart(System.currentTimeMillis());

            MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Seleccionar las fechas")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            dateSelector.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                Long startDateUTC = selection.first;
                Long endDateUTC = selection.second;

                if (startDateUTC != null && endDateUTC != null) {
                    long startDateLocal = convertirUTCaLocal(startDateUTC);
                    long endDateLocal = convertirUTCaLocal(endDateUTC);

                    Log.d(TAG, "Fecha inicio UTC: " + new Date(startDateUTC));
                    Log.d(TAG, "Fecha inicio Local: " + new Date(startDateLocal));
                    Log.d(TAG, "Fecha fin UTC: " + new Date(endDateUTC));
                    Log.d(TAG, "Fecha fin Local: " + new Date(endDateLocal));

                    if (storageHelper != null && storageHelper.isPastDate(startDateLocal)) {
                        Toast.makeText(this, "No puedes seleccionar fechas pasadas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());

                    Calendar startCal = Calendar.getInstance();
                    startCal.setTimeInMillis(startDateLocal);
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTimeInMillis(endDateLocal);

                    String dateText = formatter.format(startCal.getTime()) + " - " + formatter.format(endCal.getTime());

                    if (tvDate != null) {
                        tvDate.setText(dateText);
                        tvDate.setTextColor(getResources().getColor(R.color.black));
                    }

                    if (prefsManager != null) {
                        prefsManager.saveDateRange(startDateLocal, endDateLocal);
                    }

                    if (storageHelper != null) {
                        int days = calcularDiasEstadia(startDateLocal, endDateLocal);
                        String dayMessage = String.format("Estadía de %d día%s seleccionada",
                                days, days > 1 ? "s" : "");
                        Toast.makeText(this, dayMessage, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Días calculados: " + days +
                                " (inicio: " + startCal.get(Calendar.DAY_OF_MONTH) +
                                ", fin: " + endCal.get(Calendar.DAY_OF_MONTH) + ")");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error configurando selector de fechas", e);
        }
    }

    private long convertirUTCaLocal(long utcTimestamp) {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(utcTimestamp);

        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
        localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
        localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));
        localCalendar.set(Calendar.HOUR_OF_DAY, 12);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        return localCalendar.getTimeInMillis();
    }

    private int calcularDiasEstadia(long startDate, long endDate) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDate);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endDate);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long diffInMillis = end.getTimeInMillis() - start.getTimeInMillis();
        int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));

        return Math.max(1, days);
    }

    private void setupGuestSelector() {
        if (guestSelector != null) {
            guestSelector.setOnClickListener(v -> showPeopleDialog());
        }
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "setupRecyclerViews - Iniciando");

        try {
            setupLugaresRecyclerView();
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
                    saveSelectedLocationImmediate(lugar.getNombre());
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

    // ============= MÉTODOS REMOVIDOS/SIMPLIFICADOS =============

    /**
     * 🔥 REMOVIDO: Ya no se necesitan los métodos complejos de búsqueda
     * - performSearch() -> reemplazado por performSearchAndNavigate()
     * - searchHotelsByLocationEnhanced() -> no necesario
     * - searchHotelsByLocation() -> no necesario
     * - showNoHotelsFoundDialog() -> no necesario
     * - searchAllHotelsInLocation() -> no necesario
     * - showAvailabilityStats() -> no necesario
     *
     * El HotelsFragment ahora maneja toda esta lógica
     */

    private void performLogout() {
        try {
            Log.d(TAG, "Iniciando proceso de logout");

            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

            if (prefsManager != null) {
                prefsManager.clearUserSession();
                Log.d(TAG, "PrefsManager limpiado");
            }

            if (storageHelper != null) {
                storageHelper.clearCurrentSessionData();
                Log.d(TAG, "StorageHelper limpiado");
            }

            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "Firebase Auth signOut completado");

            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Log.d(TAG, "Logout completado exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error durante logout", e);
            Toast.makeText(this, "Error cerrando sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();

            try {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Error crítico navegando al login", e2);
            }
        }
    }

    private void loadSavedData() {
        if (prefsManager == null) return;

        try {
            // Cargar fechas
            long startDate = prefsManager.getStartDate();
            long endDate = prefsManager.getEndDate();

            if (startDate != 0 && endDate != 0 && tvDate != null) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTimeInMillis(startDate);
                Calendar endCal = Calendar.getInstance();
                endCal.setTimeInMillis(endDate);

                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
                String dateText = formatter.format(startCal.getTime()) + " - " + formatter.format(endCal.getTime());

                tvDate.setText(dateText);
                tvDate.setTextColor(getResources().getColor(R.color.black));

                Log.d(TAG, "Fechas cargadas - Inicio: " + startCal.get(Calendar.DAY_OF_MONTH) +
                        ", Fin: " + endCal.get(Calendar.DAY_OF_MONTH));
            }

            // Cargar personas
            String peopleString = prefsManager.getPeopleString();
            if (peopleString != null && !peopleString.isEmpty() && tvPeople != null) {
                tvPeople.setText(peopleString);
                tvPeople.setTextColor(getResources().getColor(R.color.black));

                try {
                    parsePeopleString(peopleString);
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo parsear: " + peopleString);
                }
            }

            // Cargar última ubicación
            loadLastSelectedLocationFromPrefs();

        } catch (Exception e) {
            Log.e(TAG, "Error cargando datos guardados", e);
        }
    }

    /**
     * 🔥 MEJORADO: Cargar la última ubicación usando PrefsManager
     */
    private void loadLastSelectedLocationFromPrefs() {
        try {
            String lastLocation = null;

            if (prefsManager != null) {
                lastLocation = prefsManager.getBestAvailableLocation();

                if (lastLocation == null || lastLocation.isEmpty()) {
                    lastLocation = prefsManager.getLocation();
                }
            }

            if (lastLocation == null || lastLocation.isEmpty()) {
                lastLocation = getSharedPreferences("TeleHotelPrefs", MODE_PRIVATE)
                        .getString("selected_location", null);
            }

            if (lastLocation == null || lastLocation.isEmpty()) {
                if (storageHelper != null) {
                    List<SearchHistory> history = storageHelper.getRecentSearches();
                    if (!history.isEmpty()) {
                        lastLocation = history.get(0).getLocation();
                    }
                }
            }

            if (lastLocation != null && !lastLocation.isEmpty() && etCity != null) {
                etCity.setText(lastLocation);
                etCity.setTextColor(getResources().getColor(R.color.black));
                Log.d(TAG, "Última ubicación cargada: " + lastLocation);

                if (prefsManager != null) {
                    Log.d(TAG, prefsManager.getLocationDebugInfo());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error cargando última ubicación", e);
        }
    }

    private void updateDateDisplay(long startDate, long endDate) {
        if (tvDate == null) return;

        try {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(endDate);

            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            String dateText = formatter.format(startCal.getTime()) + " - " + formatter.format(endCal.getTime());

            tvDate.setText(dateText);
            tvDate.setTextColor(getResources().getColor(R.color.black));

            Log.d(TAG, "Display actualizado: " + dateText);
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando display de fechas", e);
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
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Log.w(TAG, "Usuario no autenticado, redirigiendo al login");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            loadSavedData();
            loadHotelLocations();
        } catch (Exception e) {
            Log.e(TAG, "Error en onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (searchHandler != null && searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            if (cityAdapter != null) {
                cityAdapter.clear();
            }

            allHotelLocations.clear();
            detailedLocations.clear();

            Log.d(TAG, "onDestroy - Recursos limpiados");
        } catch (Exception e) {
            Log.e(TAG, "Error en onDestroy", e);
        }
    }
}