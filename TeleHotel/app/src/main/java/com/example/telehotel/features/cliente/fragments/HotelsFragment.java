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
import com.example.telehotel.core.storage.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelsFragment extends Fragment {

    // ============= VISTAS =============
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textCantidad;
    private TextView txtSeleccionarAmenidades;
    private TextView txtSeleccionarPuntuacion;

    // ============= DATOS =============
    private List<Hotel> listaHotelesOriginal = new ArrayList<>(); // Lista completa sin filtrar
    private List<Hotel> listaHotelesFiltrada = new ArrayList<>(); // Lista filtrada para mostrar
    private HotelAdapter adapter;

    // ============= FILTROS =============
    private boolean[] seleccionados;
    private String[] opcionesAmenidades;
    private List<String> amenidadesSeleccionadas = new ArrayList<>();

    private boolean[] seleccionadasPuntuacion;
    private String[] opcionesPuntuacion;
    private List<String> puntuacionSeleccionada = new ArrayList<>();

    // ============= CONFIGURACIÓN =============
    private PrefsManager prefsManager;

    // ============= PARÁMETROS DE BÚSQUEDA =============
    private String searchLocation = "";
    private long startDate = 0;
    private long endDate = 0;
    private int adults = 2;
    private int children = 0;
    private int rooms = 1;

    // ============= CONSTRUCTORES =============

    public HotelsFragment() {}

    /**
     * Método factory para crear el fragment con parámetros de búsqueda
     */
    public static HotelsFragment newInstance(String searchLocation, long startDate, long endDate,
                                             int adults, int children, int rooms) {
        HotelsFragment fragment = new HotelsFragment();
        Bundle args = new Bundle();
        args.putString("search_location", searchLocation);
        args.putLong("start_date", startDate);
        args.putLong("end_date", endDate);
        args.putInt("adults", adults);
        args.putInt("children", children);
        args.putInt("rooms", rooms);
        fragment.setArguments(args);
        return fragment;
    }

    // ============= CICLO DE VIDA =============

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Iniciando configuración del fragment");

        try {
            // Inicializar PrefsManager
            prefsManager = new PrefsManager(requireContext());

            // Obtener parámetros de búsqueda
            obtenerParametrosBusqueda();

            // Inicializar vistas
            inicializarVistas(view);

            // Configurar RecyclerView
            configurarRecyclerView();

            // Configurar filtros
            configurarFiltros();

            // Cargar hoteles con filtros de búsqueda
            cargarHotelesConFiltros();

            Log.d(TAG, "onViewCreated - Configuración completada");

        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated", e);
            Toast.makeText(getContext(), "Error configurando la vista", Toast.LENGTH_SHORT).show();

            // En caso de error crítico, volver atrás
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Recargar datos si es necesario
        if (listaHotelesOriginal.isEmpty()) {
            cargarHotelesConFiltros();
        }
    }

    // ============= INICIALIZACIÓN =============

    private void obtenerParametrosBusqueda() {
        Log.d(TAG, "Obteniendo parámetros de búsqueda");

        // *** CAMBIO: Primero intentar obtener argumentos del Navigation Component ***
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("search_location")) {
            // Parámetros desde Navigation Component (caso principal)
            searchLocation = arguments.getString("search_location", "");
            startDate = arguments.getLong("start_date", 0);
            endDate = arguments.getLong("end_date", 0);
            adults = arguments.getInt("adults", 2);
            children = arguments.getInt("children", 0);
            rooms = arguments.getInt("rooms", 1);

            Log.d(TAG, "Parámetros obtenidos desde Navigation Component");
            Log.d(TAG, String.format("Navigation Args - Ubicación: '%s', Fechas: %d-%d, Huéspedes: %d+%d, Habitaciones: %d",
                    searchLocation, startDate, endDate, adults, children, rooms));

        } else if (arguments != null) {
            // Parámetros desde newInstance() (método factory)
            searchLocation = arguments.getString("search_location", "");
            startDate = arguments.getLong("start_date", 0);
            endDate = arguments.getLong("end_date", 0);
            adults = arguments.getInt("adults", 2);
            children = arguments.getInt("children", 0);
            rooms = arguments.getInt("rooms", 1);

            Log.d(TAG, "Parámetros obtenidos desde newInstance()");

        } else {
            // Fallback: intentar obtener desde PrefsManager
            Log.w(TAG, "No hay argumentos, intentando obtener desde PrefsManager");

            searchLocation = ""; // No hay ubicación guardada en prefs por defecto
            startDate = prefsManager.getStartDate();
            endDate = prefsManager.getEndDate();

            // Parsear string de personas
            String peopleString = prefsManager.getPeopleString();
            parseSearchParams(peopleString);
        }

        // Log detallado de parámetros finales
        Log.d(TAG, String.format("=== PARÁMETROS FINALES ===\n" +
                        "  Ubicación: '%s'\n" +
                        "  Start Date: %d (%s)\n" +
                        "  End Date: %d (%s)\n" +
                        "  Adultos: %d\n" +
                        "  Niños: %d\n" +
                        "  Habitaciones: %d\n" +
                        "=========================",
                searchLocation,
                startDate, startDate > 0 ? new java.util.Date(startDate).toString() : "INVÁLIDA",
                endDate, endDate > 0 ? new java.util.Date(endDate).toString() : "INVÁLIDA",
                adults, children, rooms));

        // Validación crítica
        if (!hayParametrosMinimos()) {
            Log.e(TAG, "ERROR CRÍTICO: Fragment iniciado sin parámetros mínimos requeridos");
        }
    }

    private void parseSearchParams(String peopleString) {
        adults = 2;
        children = 0;
        rooms = 1;

        if (peopleString != null && !peopleString.trim().isEmpty()) {
            try {
                String[] parts = peopleString.split(" · ");
                for (String part : parts) {
                    part = part.trim().toLowerCase();
                    if (part.contains("habitación")) {
                        String[] roomParts = part.split(" ");
                        if (roomParts.length > 0) {
                            rooms = Integer.parseInt(roomParts[0]);
                        }
                    } else if (part.contains("adulto")) {
                        String[] adultParts = part.split(" ");
                        if (adultParts.length > 0) {
                            adults = Integer.parseInt(adultParts[0]);
                        }
                    } else if (part.contains("niño")) {
                        String[] childParts = part.split(" ");
                        if (childParts.length > 0) {
                            children = Integer.parseInt(childParts[0]);
                        }
                    }
                }
                Log.d(TAG, "Parámetros parseados correctamente");
            } catch (Exception e) {
                Log.w(TAG, "Error parseando parámetros: " + peopleString + ". Usando valores por defecto.");
            }
        }
    }

    /**
     * Verifica si tenemos al menos los parámetros mínimos
     */
    private boolean hayParametrosMinimos() {
        return searchLocation != null &&
                !searchLocation.trim().isEmpty() &&
                startDate > 0 &&
                endDate > 0 &&
                adults > 0 &&
                rooms > 0;
    }

    /**
     * Método de debugging para cuando hay problemas con parámetros
     */
    private void debugParametros() {
        Log.d(TAG, "=== DEBUG PARÁMETROS ===");
        Log.d(TAG, "Bundle arguments: " + (getArguments() != null ? "SÍ" : "NO"));

        if (getArguments() != null) {
            Bundle args = getArguments();
            Log.d(TAG, "Contenido del Bundle:");
            for (String key : args.keySet()) {
                Object value = args.get(key);
                Log.d(TAG, "  " + key + " = " + value + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            }
        }

        Log.d(TAG, "PrefsManager disponible: " + (prefsManager != null ? "SÍ" : "NO"));
        if (prefsManager != null) {
            Log.d(TAG, "PrefsManager startDate: " + prefsManager.getStartDate());
            Log.d(TAG, "PrefsManager endDate: " + prefsManager.getEndDate());
            Log.d(TAG, "PrefsManager peopleString: '" + prefsManager.getPeopleString() + "'");
        }

        Log.d(TAG, "=== FIN DEBUG ===");
    }

    private void inicializarVistas(View view) {
        recyclerView = view.findViewById(R.id.recyclerHotelList);
        progressBar = view.findViewById(R.id.progressBarHotels);
        textCantidad = view.findViewById(R.id.textCantidadHoteles);
        txtSeleccionarAmenidades = view.findViewById(R.id.txtSeleccionarAmenidades);
        txtSeleccionarPuntuacion = view.findViewById(R.id.txtSeleccionarPuntuacion);

        Log.d(TAG, "Vistas inicializadas");
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelAdapter(listaHotelesFiltrada);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "RecyclerView configurado");
    }

    private void configurarFiltros() {
        configurarFiltroAmenidades();
        configurarFiltroPuntuacion();

        Log.d(TAG, "Filtros configurados");
    }

    // ============= CONFIGURACIÓN DE FILTROS =============

    private void configurarFiltroAmenidades() {
        try {
            opcionesAmenidades = getResources().getStringArray(R.array.amenities_options);
            seleccionados = new boolean[opcionesAmenidades.length];

            txtSeleccionarAmenidades.setOnClickListener(v -> mostrarDialogoAmenidades());

            // Establecer texto inicial
            actualizarTextoAmenidades();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando filtro de amenidades", e);
        }
    }

    private void configurarFiltroPuntuacion() {
        try {
            opcionesPuntuacion = getResources().getStringArray(R.array.filter_options);
            seleccionadasPuntuacion = new boolean[opcionesPuntuacion.length];

            txtSeleccionarPuntuacion.setOnClickListener(v -> mostrarDialogoPuntuacion());

            // Establecer texto inicial
            actualizarTextoPuntuacion();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando filtro de puntuación", e);
        }
    }

    private void mostrarDialogoAmenidades() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Selecciona amenidades");

            builder.setMultiChoiceItems(opcionesAmenidades, seleccionados, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!amenidadesSeleccionadas.contains(opcionesAmenidades[which])) {
                        amenidadesSeleccionadas.add(opcionesAmenidades[which]);
                    }
                } else {
                    amenidadesSeleccionadas.remove(opcionesAmenidades[which]);
                }
            });

            builder.setPositiveButton("Aplicar filtro", (dialog, which) -> {
                actualizarTextoAmenidades();
                aplicarFiltros();
                Toast.makeText(getContext(), "Filtro de amenidades aplicado", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancelar", null);

            builder.setNeutralButton("Limpiar", (dialog, which) -> {
                amenidadesSeleccionadas.clear();
                for (int i = 0; i < seleccionados.length; i++) {
                    seleccionados[i] = false;
                }
                actualizarTextoAmenidades();
                aplicarFiltros();
                Toast.makeText(getContext(), "Filtro de amenidades limpiado", Toast.LENGTH_SHORT).show();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de amenidades", e);
            Toast.makeText(getContext(), "Error mostrando filtros", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoPuntuacion() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Selecciona la puntuación");

            builder.setMultiChoiceItems(opcionesPuntuacion, seleccionadasPuntuacion, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!puntuacionSeleccionada.contains(opcionesPuntuacion[which])) {
                        puntuacionSeleccionada.add(opcionesPuntuacion[which]);
                    }
                } else {
                    puntuacionSeleccionada.remove(opcionesPuntuacion[which]);
                }
            });

            builder.setPositiveButton("Aplicar filtro", (dialog, which) -> {
                actualizarTextoPuntuacion();
                aplicarFiltros();
                Toast.makeText(getContext(), "Filtro de puntuación aplicado", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancelar", null);

            builder.setNeutralButton("Limpiar", (dialog, which) -> {
                puntuacionSeleccionada.clear();
                for (int i = 0; i < seleccionadasPuntuacion.length; i++) {
                    seleccionadasPuntuacion[i] = false;
                }
                actualizarTextoPuntuacion();
                aplicarFiltros();
                Toast.makeText(getContext(), "Filtro de puntuación limpiado", Toast.LENGTH_SHORT).show();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando diálogo de puntuación", e);
            Toast.makeText(getContext(), "Error mostrando filtros", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarTextoAmenidades() {
        if (amenidadesSeleccionadas.isEmpty()) {
            txtSeleccionarAmenidades.setText("Seleccionar amenidades");
        } else {
            String texto = TextUtils.join(", ", amenidadesSeleccionadas);
            if (texto.length() > 30) {
                texto = amenidadesSeleccionadas.size() + " amenidades seleccionadas";
            }
            txtSeleccionarAmenidades.setText(texto);
        }
    }

    private void actualizarTextoPuntuacion() {
        if (puntuacionSeleccionada.isEmpty()) {
            txtSeleccionarPuntuacion.setText("Seleccionar puntuación");
        } else {
            String texto = TextUtils.join(", ", puntuacionSeleccionada);
            if (texto.length() > 30) {
                texto = puntuacionSeleccionada.size() + " filtros de puntuación";
            }
            txtSeleccionarPuntuacion.setText(texto);
        }
    }

    // ============= CARGA DE DATOS =============

    private void cargarHotelesConFiltros() {
        Log.d(TAG, "Iniciando carga de hoteles con filtros obligatorios");

        // Validar que tenemos los parámetros mínimos requeridos
        if (!validarParametrosRequeridos()) {
            mostrarErrorParametros();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textCantidad.setText("Buscando hoteles...");

        // SIEMPRE buscar con parámetros específicos
        // Si llegamos aquí, debe haber una búsqueda válida
        buscarHotelesConParametros();
    }

    /**
     * Valida que tengamos todos los parámetros requeridos para la búsqueda
     */
    private boolean validarParametrosRequeridos() {
        // Validar ubicación
        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            Log.e(TAG, "Error: No hay ubicación de búsqueda");
            return false;
        }

        // Validar fechas
        if (startDate <= 0 || endDate <= 0) {
            Log.e(TAG, "Error: Fechas inválidas - startDate: " + startDate + ", endDate: " + endDate);
            return false;
        }

        // Validar que las fechas sean coherentes
        if (startDate >= endDate) {
            Log.e(TAG, "Error: Fecha de inicio debe ser anterior a fecha de fin");
            return false;
        }

        // Validar huéspedes
        if (adults <= 0) {
            Log.e(TAG, "Error: Debe haber al menos 1 adulto");
            return false;
        }

        if (rooms <= 0) {
            Log.e(TAG, "Error: Debe haber al menos 1 habitación");
            return false;
        }

        Log.d(TAG, "Parámetros validados correctamente");
        return true;
    }

    /**
     * Muestra error cuando faltan parámetros requeridos
     */
    private void mostrarErrorParametros() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        String errorMessage;
        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            errorMessage = "Error: No se especificó ubicación de búsqueda";
            textCantidad.setText("Ubicación requerida");
        } else if (startDate <= 0 || endDate <= 0) {
            errorMessage = "Error: Fechas de estadía no especificadas";
            textCantidad.setText("Fechas requeridas");
        } else {
            errorMessage = "Error: Parámetros de búsqueda incompletos";
            textCantidad.setText("Parámetros incompletos");
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, errorMessage);

        // Opcional: Navegar de vuelta a la pantalla de búsqueda
        // getParentFragmentManager().popBackStack();
    }

    private void buscarHotelesConParametros() {
        Log.d(TAG, String.format("Buscando hoteles - Ubicación: '%s', Fechas: %d-%d, Huéspedes: %d adultos + %d niños, Habitaciones: %d",
                searchLocation, startDate, endDate, adults, children, rooms));

        HotelRepository.searchHotelsWithCustomParamsEnhanced(
                requireContext(),
                searchLocation,
                adults,
                children,
                rooms,
                hoteles -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                listaHotelesOriginal.clear();
                                listaHotelesOriginal.addAll(hoteles);

                                Log.d(TAG, String.format("Búsqueda completada - Encontrados: %d hoteles para '%s'",
                                        hoteles.size(), searchLocation));

                                // Aplicar filtros adicionales (amenidades, puntuación)
                                aplicarFiltros();

                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                // Log adicional para debugging
                                if (hoteles.isEmpty()) {
                                    Log.w(TAG, "No se encontraron hoteles que cumplan los criterios de búsqueda");
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando hoteles encontrados", e);
                                mostrarError("Error procesando resultados");
                            }
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, String.format("Error en búsqueda para '%s': %s", searchLocation, error.getMessage()));

                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            listaHotelesOriginal.clear();
                            aplicarFiltros();

                            // Mensaje de error más específico
                            String errorMessage = String.format(
                                    "No se encontraron hoteles disponibles en '%s' para las fechas y criterios seleccionados",
                                    searchLocation);

                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void mostrarError(String mensaje) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        textCantidad.setText("Error cargando hoteles");
    }

    // ============= APLICACIÓN DE FILTROS =============

    private void aplicarFiltros() {
        Log.d(TAG, "Aplicando filtros - Hoteles originales: " + listaHotelesOriginal.size());

        listaHotelesFiltrada.clear();

        for (Hotel hotel : listaHotelesOriginal) {
            if (cumpleFiltros(hotel)) {
                listaHotelesFiltrada.add(hotel);
            }
        }

        adapter.notifyDataSetChanged();
        actualizarContadorHoteles();

        Log.d(TAG, "Filtros aplicados - Hoteles filtrados: " + listaHotelesFiltrada.size());
    }

    private boolean cumpleFiltros(Hotel hotel) {
        try {
            // Filtro de amenidades/servicios
            if (!amenidadesSeleccionadas.isEmpty()) {
                if (!cumpleFiltroAmenidades(hotel)) {
                    return false;
                }
            }

            // Filtro de puntuación
            if (!puntuacionSeleccionada.isEmpty()) {
                if (!cumpleFiltroPuntuacion(hotel)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error verificando filtros para hotel: " + hotel.getId(), e);
            return false;
        }
    }

    private boolean cumpleFiltroAmenidades(Hotel hotel) {
        if (hotel.getServicios() == null || hotel.getServicios().isEmpty()) {
            return false;
        }

        // El hotel debe tener al menos una de las amenidades seleccionadas
        for (String amenidadSolicitada : amenidadesSeleccionadas) {
            for (String servicioHotel : hotel.getServicios()) {
                if (servicioHotel != null &&
                        servicioHotel.toLowerCase().contains(amenidadSolicitada.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean cumpleFiltroPuntuacion(Hotel hotel) {
        // TODO: Implementar filtro de puntuación cuando tengas ese campo en Hotel
        // Por ejemplo, si tienes un campo rating, reputacion, etc.

        // Por ahora, devolver true para no filtrar por puntuación
        return true;

        /*
        // Ejemplo de implementación si tuvieras un campo rating:
        if (hotel.getRating() != null) {
            for (String filtro : puntuacionSeleccionada) {
                if (filtro.contains("5 estrellas") && hotel.getRating() >= 4.5) return true;
                if (filtro.contains("4 estrellas") && hotel.getRating() >= 3.5 && hotel.getRating() < 4.5) return true;
                // ... más condiciones
            }
        }
        return false;
        */
    }

    private void actualizarContadorHoteles() {
        String mensaje;

        try {
            // Obtener información de la búsqueda
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String fechas = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));

            if (listaHotelesFiltrada.isEmpty()) {
                mensaje = String.format("No se encontraron hoteles en %s (%s)", searchLocation, fechas);

                if (!amenidadesSeleccionadas.isEmpty() || !puntuacionSeleccionada.isEmpty()) {
                    mensaje += "\nIntenta quitar algunos filtros";
                }
            } else {
                mensaje = String.format("%d hotel%s en %s (%s)",
                        listaHotelesFiltrada.size(),
                        listaHotelesFiltrada.size() == 1 ? "" : "es",
                        searchLocation,
                        fechas);

                // Agregar información de filtros si están activos
                int filtrosActivos = 0;
                if (!amenidadesSeleccionadas.isEmpty()) filtrosActivos++;
                if (!puntuacionSeleccionada.isEmpty()) filtrosActivos++;

                if (filtrosActivos > 0) {
                    mensaje += String.format(" • %d filtro%s aplicado%s",
                            filtrosActivos,
                            filtrosActivos == 1 ? "" : "s",
                            filtrosActivos == 1 ? "" : "s");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando contador", e);
            mensaje = String.format("Hoteles en %s: %d", searchLocation, listaHotelesFiltrada.size());
        }

        textCantidad.setText(mensaje);
    }

    // ============= MÉTODOS PÚBLICOS =============

    /**
     * Actualiza los parámetros de búsqueda desde la actividad padre
     */
    public void actualizarParametrosBusqueda(String location, long start, long end,
                                             int adults, int children, int rooms) {
        Log.d(TAG, "Actualizando parámetros de búsqueda: " + location);

        this.searchLocation = location;
        this.startDate = start;
        this.endDate = end;
        this.adults = adults;
        this.children = children;
        this.rooms = rooms;

        // Limpiar filtros adicionales
        limpiarFiltrosSinRecargar();

        // Recargar hoteles con nuevos parámetros
        cargarHotelesConFiltros();
    }

    /**
     * Limpia todos los filtros
     */
    public void limpiarFiltros() {
        Log.d(TAG, "Limpiando todos los filtros");

        limpiarFiltrosSinRecargar();
        aplicarFiltros();

        Toast.makeText(getContext(), "Filtros limpiados", Toast.LENGTH_SHORT).show();
    }

    private void limpiarFiltrosSinRecargar() {
        amenidadesSeleccionadas.clear();
        puntuacionSeleccionada.clear();

        if (seleccionados != null) {
            for (int i = 0; i < seleccionados.length; i++) {
                seleccionados[i] = false;
            }
        }

        if (seleccionadasPuntuacion != null) {
            for (int i = 0; i < seleccionadasPuntuacion.length; i++) {
                seleccionadasPuntuacion[i] = false;
            }
        }

        actualizarTextoAmenidades();
        actualizarTextoPuntuacion();
    }

    /**
     * Obtiene el número de hoteles actualmente mostrados
     */
    public int getHotelesCount() {
        return listaHotelesFiltrada.size();
    }

    /**
     * Verifica si hay filtros aplicados
     */
    public boolean tieneFiltrosAplicados() {
        return !amenidadesSeleccionadas.isEmpty() || !puntuacionSeleccionada.isEmpty();
    }

    /**
     * Obtiene un resumen de los parámetros de búsqueda actuales
     */
    public String obtenerResumenBusqueda() {
        if (!validarParametrosRequeridos()) {
            return "Búsqueda inválida";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaInicio = sdf.format(new Date(startDate));
        String fechaFin = sdf.format(new Date(endDate));

        return String.format("%s • %s - %s • %d adulto%s, %d niño%s • %d habitación%s",
                searchLocation,
                fechaInicio, fechaFin,
                adults, adults == 1 ? "" : "s",
                children, children == 1 ? "" : "s",
                rooms, rooms == 1 ? "" : "es");
    }
}