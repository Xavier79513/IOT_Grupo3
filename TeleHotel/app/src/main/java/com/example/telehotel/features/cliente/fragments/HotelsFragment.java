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
import com.example.telehotel.core.utils.ReservaValidationUtil;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.SearchHistory;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.core.storage.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelsFragment extends Fragment {
    private Reserva reservaActiva = null; // Para mostrar detalles

    // ============= VISTAS =============
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textCantidad;
    private TextView txtSeleccionarAmenidades;
    private TextView txtSeleccionarPuntuacion;
    private TextView textCiudadTitulo;
    private TextView textResumenBusqueda;
    private View viewEstadoVacio; // Vista para mostrar cuando no hay resultados

    // ============= DATOS =============
    private List<Hotel> listaHotelesOriginal = new ArrayList<>();
    private List<Hotel> listaHotelesFiltrada = new ArrayList<>();
    private HotelAdapter adapter;

    // ============= ESTADOS DE BÚSQUEDA =============
    private boolean busquedaCompletada = false;
    private boolean busquedaExitosa = false;
    private String motivoSinResultados = "";

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
    private boolean parametrosValidados = false;

    // ============= CONSTRUCTORES =============
    private ReservaValidationUtil reservaValidator;
    private boolean tieneReservaActiva = false;
    private boolean validacionCompletada = false;
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
            // PASO 1: Inicializar PrefsManager
            prefsManager = new PrefsManager(requireContext());

            // PASO 2: Inicializar validador de reservas
            reservaValidator = new ReservaValidationUtil(prefsManager);

            // PASO 3: Inicializar vistas
            inicializarVistas(view);

            // PASO 4: Configurar componentes básicos
            configurarRecyclerView();
            configurarFiltros();

            // PASO 5: Obtener parámetros
            obtenerParametrosConEstrategiaMejorada();

            // 🔥 PASO 6: SIEMPRE validar reserva ANTES de cualquier carga
            Log.d(TAG, "=== INICIANDO VALIDACIÓN DE RESERVA ===");
            validarReservaYDecidirAccion();

        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated", e);
            mostrarEstadoError("Error configurando la vista");
        }
    }
    // ============= 🔥 NUEVO MÉTODO: BLOQUEAR POR RESERVA ACTIVA =============

    /**
     * 🔥 BLOQUEAR completamente la búsqueda de hoteles
     */
    private void bloquearPorReservaActiva(Reserva reserva) {
        Log.w(TAG, "🛑 BLOQUEANDO BÚSQUEDA - Reserva activa detectada: " +
                (reserva.getId() != null ? reserva.getId() : "Sin ID"));

        // Ocultar loading
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        // Limpiar cualquier lista existente
        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // 🔥 MENSAJE CLARO Y CONTUNDENTE
        String mensaje = "🚫 BÚSQUEDA BLOQUEADA\n\n" +
                "⚠️ YA TIENES UNA RESERVA ACTIVA\n\n";

        // Agregar detalles de la reserva
        if (reserva.getHotelNombre() != null) {
            mensaje += "🏨 Hotel: " + reserva.getHotelNombre() + "\n";
        }
        if (reserva.getCodigoReserva() != null) {
            mensaje += "🎫 Código: " + reserva.getCodigoReserva() + "\n";
        }
        if (reserva.getEstado() != null) {
            mensaje += "📋 Estado: " + reserva.getEstado() + "\n";
        }

        mensaje += "\n❌ NO PUEDES BUSCAR NUEVOS HOTELES\n" +
                "💡 Completa tu estadía actual primero";

        textCantidad.setText(mensaje);

        // Actualizar título también
        if (textCiudadTitulo != null) {
            textCiudadTitulo.setText("🚫 Búsqueda Bloqueada");
        }

        // 🔥 MARCAR ESTADOS PARA PREVENIR CARGAS FUTURAS
        busquedaCompletada = true;
        busquedaExitosa = false;
        motivoSinResultados = "Reserva activa - Búsqueda bloqueada";

        Log.w(TAG, "Usuario completamente bloqueado por reserva activa");
    }

// ============= 🔥 NUEVO MÉTODO: PERMITIR BÚSQUEDA =============

    /**
     * 🔥 PERMITIR búsqueda normal de hoteles
     */
    private void permitirBusquedaHoteles() {
        Log.d(TAG, "✅ PERMITIENDO BÚSQUEDA - No hay reserva activa");

        // Actualizar título normal
        actualizarTituloUbicacion();

        // Proceder según parámetros
        if (parametrosValidados) {
            Log.d(TAG, "Parámetros válidos - iniciando búsqueda de hoteles");
            cargarHotelesConFiltros();
        } else {
            Log.d(TAG, "Parámetros no válidos - mostrando estado sin parámetros");
            mostrarEstadoSinParametros();
        }
    }

    // ============= 🔥 MÉTODO UTILITARIO DE SEGURIDAD =============

    private boolean isSafeToUseContext() {
        return isAdded() && getContext() != null && !isRemoving() && !isDetached();
    }
    /**
     * 🔥 MÉTODO PARA FORZAR RE-VALIDACIÓN (útil para testing)
     */
    public void forzarRevalidacion() {
        Log.d(TAG, "=== FORZANDO RE-VALIDACIÓN ===");

        // Resetear todo
        validacionCompletada = false;
        tieneReservaActiva = false;
        reservaActiva = null;
        busquedaCompletada = false;
        busquedaExitosa = false;

        // Limpiar listas
        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        // Re-validar
        validarReservaYDecidirAccion();
    }
    /**
     * 🔥 MÉTODO PARA DEBUGGING: Ver estado actual
     */
    public void debugEstadoValidacion() {
        Log.d(TAG, "=== DEBUG ESTADO VALIDACIÓN ===");
        Log.d(TAG, "validacionCompletada: " + validacionCompletada);
        Log.d(TAG, "tieneReservaActiva: " + tieneReservaActiva);
        Log.d(TAG, "parametrosValidados: " + parametrosValidados);
        Log.d(TAG, "reservaActiva != null: " + (reservaActiva != null));
        if (reservaActiva != null) {
            Log.d(TAG, "reservaActiva.getId(): " + reservaActiva.getId());
            Log.d(TAG, "reservaActiva.getEstado(): " + reservaActiva.getEstado());
        }
        Log.d(TAG, "busquedaCompletada: " + busquedaCompletada);
        Log.d(TAG, "busquedaExitosa: " + busquedaExitosa);
        Log.d(TAG, "hoteles encontrados: " + listaHotelesOriginal.size());
        Log.d(TAG, "hoteles mostrados: " + listaHotelesFiltrada.size());
    }
    /**
     * 🔥 MÉTODO CENTRAL: Validar reserva y decidir qué hacer
     */
    private void validarReservaYDecidirAccion() {
        Log.d(TAG, "=== VALIDANDO RESERVA ACTIVA ===");

        // Resetear estados
        validacionCompletada = false;
        tieneReservaActiva = false;
        reservaActiva = null;

        // Mostrar estado de validación
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textCantidad.setText("🔍 Verificando reservas activas...");

        // Validar reserva
        reservaValidator.verificarReservaActiva((tieneReserva, reserva, mensaje) -> {
            if (!isSafeToUseContext()) {
                Log.w(TAG, "Fragment no válido - cancelando validación");
                return;
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isSafeToUseContext()) return;

                    // 🔥 MARCAR VALIDACIÓN COMO COMPLETADA
                    validacionCompletada = true;
                    tieneReservaActiva = tieneReserva;
                    reservaActiva = reserva;

                    Log.d(TAG, String.format("=== RESULTADO VALIDACIÓN ===\n" +
                                    "Tiene reserva: %s\n" +
                                    "Mensaje: %s\n" +
                                    "Reserva ID: %s",
                            tieneReserva, mensaje,
                            (reserva != null ? reserva.getId() : "null")));

                    // 🔥 DECISIÓN CRÍTICA
                    if (tieneReserva && reserva != null) {
                        // 🛑 BLOQUEAR: Usuario tiene reserva activa
                        bloquearPorReservaActiva(reserva);
                    } else {
                        // ✅ PERMITIR: Usuario puede buscar hoteles
                        permitirBusquedaHoteles();
                    }
                });
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "=== onResume ===");

        // 🔥 SI HAY RESERVA ACTIVA, NO HACER NADA MÁS
        if (tieneReservaActiva) {
            Log.w(TAG, "onResume: Usuario tiene reserva activa - no recargar");
            return;
        }

        if (prefsManager != null) {
            verificarCambiosEnParametros();
        }

        // Solo recargar si cumple TODAS las condiciones
        if (listaHotelesOriginal.isEmpty() &&
                parametrosValidados &&
                !busquedaCompletada &&
                validacionCompletada &&
                !tieneReservaActiva) {

            Log.d(TAG, "onResume: Recargando hoteles");
            cargarHotelesConFiltros();
        }
    }

    // ============= 🔥 NUEVOS MÉTODOS DE MANEJO DE ESTADOS =============

    /**
     * 🔥 NUEVO: Mostrar estado cuando no hay parámetros válidos
     */
    private void mostrarEstadoSinParametros() {
        Log.d(TAG, "Mostrando estado sin parámetros");

        busquedaCompletada = true;
        busquedaExitosa = false;
        motivoSinResultados = "Parámetros de búsqueda incompletos";

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        // Limpiar lista y actualizar adapter
        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Actualizar textos informativos
        actualizarTituloUbicacion();
        textCantidad.setText("⚠️ Completa los parámetros de búsqueda para ver hoteles disponibles");

        // 🔥 CLAVE: Permitir que la vista esté lista para navegación
        // La lógica de navegación en la actividad padre puede seguir funcionando

        Log.d(TAG, "Vista preparada para navegación sin resultados");
    }

    /**
     * 🔥 NUEVO: Mostrar estado de error pero permitir navegación
     */
    private void mostrarEstadoError(String mensaje) {
        Log.d(TAG, "Mostrando estado de error: " + mensaje);

        busquedaCompletada = true;
        busquedaExitosa = false;
        motivoSinResultados = mensaje;

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        textCantidad.setText("❌ " + mensaje);
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
    private void validarYCargarHoteles() {
        progressBar.setVisibility(View.VISIBLE);
        textCantidad.setText("🔍 Verificando reservas...");

        reservaValidator.verificarReservaActiva((tieneReserva, reserva, mensaje) -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    validacionCompletada = true;
                    tieneReservaActiva = tieneReserva;

                    if (tieneReserva) {
                        mostrarMensajeReservaActiva(reserva);
                    } else {
                        continuarCargaNormal();
                    }
                });
            }
        });
    }

    private void mostrarMensajeReservaActiva(Reserva reserva) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        String mensaje = "🚫 NO PUEDES HACER NUEVAS RESERVAS\n\n⚠️ Ya tienes una reserva activa";
        if (reserva != null && reserva.getHotelNombre() != null) {
            mensaje += ":\n\n🏨 " + reserva.getHotelNombre();
        }
        mensaje += "\n\n💡 Completa tu estadía actual antes de hacer nuevas reservas";

        textCantidad.setText(mensaje);
    }

    private void continuarCargaNormal() {
        if (parametrosValidados) {
            cargarHotelesConFiltros();
        } else {
            mostrarEstadoSinParametros();
        }
    }
    /**
     * 🔥 MEJORADO: Búsqueda que siempre completa, con o sin resultados
     */
    private void buscarHotelesConParametros() {
        Log.d(TAG, String.format("Buscando hoteles - Ubicación: '%s', Fechas: %d-%d, Huéspedes: %d adultos + %d niños, Habitaciones: %d",
                searchLocation, startDate, endDate, adults, children, rooms));

        HotelRepository.searchHotelsWithExactCapacityFromDB(
                requireContext(),
                searchLocation,
                adults,
                children,
                rooms,
                hoteles -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                // 🔥 MEJORADO: Siempre marcar como completada
                                busquedaCompletada = true;

                                listaHotelesOriginal.clear();
                                listaHotelesOriginal.addAll(hoteles);

                                if (hoteles.isEmpty()) {
                                    // 🔥 NUEVO: Sin resultados pero búsqueda exitosa
                                    busquedaExitosa = false;
                                    motivoSinResultados = String.format(
                                            "No hay hoteles disponibles en '%s' con habitaciones para %d adultos y %d niños",
                                            searchLocation, adults, children
                                    );

                                    Log.w(TAG, motivoSinResultados);
                                } else {
                                    // Búsqueda exitosa con resultados
                                    busquedaExitosa = true;
                                    motivoSinResultados = "";

                                    Log.d(TAG, String.format("Búsqueda exitosa - Encontrados: %d hoteles", hoteles.size()));
                                }

                                // Siempre aplicar filtros y actualizar vista
                                aplicarFiltros();
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando hoteles encontrados", e);
                                manejarErrorBusqueda("Error procesando resultados");
                            }
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, String.format("Error en búsqueda para '%s': %s", searchLocation, error.getMessage()));

                            // 🔥 MEJORADO: Error pero búsqueda completada
                            busquedaCompletada = true;
                            busquedaExitosa = false;
                            motivoSinResultados = String.format(
                                    "Error buscando hoteles en '%s': %s",
                                    searchLocation,
                                    error.getMessage()
                            );

                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            listaHotelesOriginal.clear();
                            aplicarFiltros();

                            // 🔥 NUEVO: Mostrar mensaje más amigable
                            String mensajeUsuario = String.format(
                                    "No se encontraron hoteles disponibles en '%s' para las fechas y huéspedes seleccionados",
                                    searchLocation
                            );

                            Toast.makeText(getContext(), mensajeUsuario, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    /**
     * 🔥 NUEVO: Manejar errores de búsqueda sin bloquear navegación
     */
    private void manejarErrorBusqueda(String mensaje) {
        busquedaCompletada = true;
        busquedaExitosa = false;
        motivoSinResultados = mensaje;

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        listaHotelesOriginal.clear();
        aplicarFiltros();

        mostrarError(mensaje);
    }

    // ============= MÉTODOS EXISTENTES MEJORADOS =============

    private void obtenerParametrosConEstrategiaMejorada() {
        Log.d(TAG, "=== INICIANDO OBTENCIÓN DE PARÁMETROS ===");

        // Estrategia 1: Arguments del Navigation Component (prioridad alta)
        if (intentarObtenerDesdeArguments()) {
            Log.d(TAG, "✅ Parámetros obtenidos desde Arguments");
            parametrosValidados = validarParametros();
            actualizarVistaConParametros();
            return;
        }

        // Estrategia 2: PrefsManager con búsqueda completa (prioridad media)
        if (intentarObtenerDesdePrefsManagerCompleto()) {
            Log.d(TAG, "✅ Parámetros obtenidos desde PrefsManager completo");
            parametrosValidados = validarParametros();
            actualizarVistaConParametros();
            return;
        }

        // Estrategia 3: PrefsManager con datos parciales (prioridad baja)
        if (intentarObtenerDesdePrefsManagerParcial()) {
            Log.d(TAG, "⚠️ Parámetros parciales obtenidos desde PrefsManager");
            parametrosValidados = validarParametros();
            actualizarVistaConParametros();
            return;
        }

        // Estrategia 4: Historial de búsquedas (último recurso)
        if (intentarObtenerDesdeHistorial()) {
            Log.d(TAG, "⚠️ Parámetros obtenidos desde historial");
            parametrosValidados = validarParametros();
            actualizarVistaConParametros();
            return;
        }

        Log.e(TAG, "❌ NO SE PUDIERON OBTENER PARÁMETROS VÁLIDOS");
        parametrosValidados = false;

        // 🔥 MEJORADO: No mostrar debug, solo preparar para estado sin parámetros
        // La vista seguirá funcionando sin parámetros válidos
    }

    /**
     * 🔥 MEJORADO: Contador que siempre muestra información útil
     */
    private void actualizarContadorHoteles() {
        String mensaje;

        try {
            if (!parametrosValidados) {
                mensaje = "⚠️ Completa los parámetros de búsqueda";
                textCantidad.setText(mensaje);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String fechas = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));

            if (!busquedaCompletada) {
                mensaje = "🔍 Buscando hoteles...";
            } else if (!busquedaExitosa) {
                // 🔥 MEJORADO: Mensaje más informativo cuando no hay resultados
                if (motivoSinResultados.contains("Error")) {
                    mensaje = "❌ Error en la búsqueda. Puedes continuar sin seleccionar hotel.";
                } else {
                    mensaje = String.format("🏨 No hay hoteles disponibles en %s (%s)\n💡 Puedes continuar para explorar otras opciones",
                            searchLocation, fechas);
                }
            } else if (listaHotelesFiltrada.isEmpty() && !listaHotelesOriginal.isEmpty()) {
                // Hay hoteles pero los filtros los ocultan
                mensaje = String.format("🔍 %d hotel%s encontrado%s en %s (%s)\n⚠️ Ninguno cumple los filtros actuales",
                        listaHotelesOriginal.size(),
                        listaHotelesOriginal.size() == 1 ? "" : "es",
                        listaHotelesOriginal.size() == 1 ? "" : "s",
                        searchLocation,
                        fechas);
            } else {
                // Resultados normales
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
            mensaje = busquedaCompletada ?
                    String.format("Hoteles en %s: %d", searchLocation, listaHotelesFiltrada.size()) :
                    "Preparando búsqueda...";
        }

        textCantidad.setText(mensaje);
    }

    /**
     * 🔥 MEJORADO: Cargar hoteles sin bloquear navegación
     */
    private void cargarHotelesConFiltros() {
        Log.d(TAG, "Iniciando carga de hoteles");

        // 🔥 NUEVA VALIDACIÓN: Verificar reserva activa PRIMERO
        if (!validacionCompletada) {
            Log.w(TAG, "Validación de reserva no completada, esperando...");
            return;
        }

        if (tieneReservaActiva) {
            Log.w(TAG, "No se pueden cargar hoteles - Usuario tiene reserva activa");
            return;
        }

        // Validación existente de parámetros
        if (!parametrosValidados) {
            mostrarEstadoSinParametros();
            return;
        }

        // 🔥 RESETEAR estados de búsqueda
        busquedaCompletada = false;
        busquedaExitosa = false;
        motivoSinResultados = "";

        // Mostrar UI de carga
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textCantidad.setText("Buscando hoteles...");

        // Iniciar búsqueda
        buscarHotelesConParametros();
    }

    // ============= MÉTODOS PÚBLICOS MEJORADOS =============

    /**
     * 🔥 NUEVO: Verificar si la vista está lista para navegación
     */
    public boolean isListaParaNavegacion() {
        // La vista está lista si:
        // 1. Tiene parámetros válidos Y búsqueda completada, O
        // 2. No tiene parámetros válidos pero está en estado inicial
        return (parametrosValidados && busquedaCompletada) || !parametrosValidados;
    }

    /**
     * 🔥 NUEVO: Obtener estado actual de la búsqueda
     */
    public String getEstadoBusqueda() {
        if (!parametrosValidados) {
            return "sin_parametros";
        }

        if (!busquedaCompletada) {
            return "buscando";
        }

        if (!busquedaExitosa) {
            return "sin_resultados";
        }

        return listaHotelesFiltrada.isEmpty() ? "filtrado_vacio" : "con_resultados";
    }

    /**
     * 🔥 NUEVO: Obtener mensaje explicativo del estado actual
     */
    public String getMensajeEstado() {
        switch (getEstadoBusqueda()) {
            case "sin_parametros":
                return "Completa los parámetros de búsqueda para encontrar hoteles";

            case "buscando":
                return "Buscando hoteles disponibles...";

            case "sin_resultados":
                return motivoSinResultados.isEmpty() ?
                        "No se encontraron hoteles para los criterios especificados" :
                        motivoSinResultados;

            case "filtrado_vacio":
                return String.format("Se encontraron %d hoteles pero ninguno cumple los filtros actuales",
                        listaHotelesOriginal.size());

            case "con_resultados":
                return String.format("Se encontraron %d hoteles disponibles", listaHotelesFiltrada.size());

            default:
                return "Estado desconocido";
        }
    }

    /**
     * 🔥 NUEVO: Verificar si hay hoteles disponibles
     */
    public boolean tieneHotelesDisponibles() {
        return busquedaExitosa && !listaHotelesFiltrada.isEmpty();
    }

    /**
     * 🔥 NUEVO: Obtener número de hoteles encontrados (antes de filtros)
     */
    public int getHotelesEncontrados() {
        return listaHotelesOriginal.size();
    }

    /**
     * 🔥 MEJORADO: Información completa del estado
     */
    public String obtenerInformacionCompleta() {
        return String.format(
                "Estado: %s\n" +
                        "Parámetros válidos: %s\n" +
                        "Búsqueda completada: %s\n" +
                        "Búsqueda exitosa: %s\n" +
                        "Hoteles encontrados: %d\n" +
                        "Hoteles mostrados: %d\n" +
                        "Filtros activos: %s\n" +
                        "Mensaje: %s",
                getEstadoBusqueda(),
                parametrosValidados ? "SÍ" : "NO",
                busquedaCompletada ? "SÍ" : "NO",
                busquedaExitosa ? "SÍ" : "NO",
                listaHotelesOriginal.size(),
                listaHotelesFiltrada.size(),
                tieneFiltrosAplicados() ? "SÍ" : "NO",
                getMensajeEstado()
        );
    }

    // ============= RESTO DE MÉTODOS EXISTENTES (sin cambios significativos) =============

    private boolean intentarObtenerDesdeArguments() {
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey("search_location")) {
            return false;
        }

        try {
            searchLocation = arguments.getString("search_location", "");
            startDate = arguments.getLong("start_date", 0);
            endDate = arguments.getLong("end_date", 0);
            adults = arguments.getInt("adults", 2);
            children = arguments.getInt("children", 0);
            rooms = arguments.getInt("rooms", 1);

            Log.d(TAG, String.format("Arguments - Ubicación: '%s', Fechas: %d-%d, Huéspedes: %d+%d, Habitaciones: %d",
                    searchLocation, startDate, endDate, adults, children, rooms));

            return !searchLocation.isEmpty() && startDate > 0 && endDate > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo desde Arguments", e);
            return false;
        }
    }

    private boolean intentarObtenerDesdePrefsManagerCompleto() {
        if (prefsManager == null) return false;

        try {
            String ubicacion = prefsManager.getBestAvailableLocation();
            long fechaInicio = prefsManager.getStartDate();
            long fechaFin = prefsManager.getEndDate();
            String peopleString = prefsManager.getPeopleString();

            if (ubicacion == null || ubicacion.isEmpty() || fechaInicio <= 0 || fechaFin <= 0) {
                return false;
            }

            searchLocation = ubicacion;
            startDate = fechaInicio;
            endDate = fechaFin;

            if (peopleString != null && !peopleString.isEmpty()) {
                parseSearchParams(peopleString);
            }

            Log.d(TAG, String.format("PrefsManager completo - Ubicación: '%s', Fechas: %d-%d, People: '%s'",
                    searchLocation, startDate, endDate, peopleString));

            if (prefsManager.hasRecentLocation()) {
                SearchHistory currentSearch = new SearchHistory(
                        searchLocation, startDate, endDate, adults, children, rooms
                );
                prefsManager.addSearchHistory(currentSearch);
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo desde PrefsManager completo", e);
            return false;
        }
    }

    private boolean intentarObtenerDesdePrefsManagerParcial() {
        if (prefsManager == null) return false;

        try {
            String ubicacion = prefsManager.getBestAvailableLocation();
            if (ubicacion == null || ubicacion.isEmpty()) {
                ubicacion = prefsManager.getSelectedLocation();
            }
            if (ubicacion == null || ubicacion.isEmpty()) {
                ubicacion = prefsManager.getLocation();
            }

            if (ubicacion == null || ubicacion.isEmpty()) {
                return false;
            }

            searchLocation = ubicacion;
            startDate = prefsManager.getStartDate();
            endDate = prefsManager.getEndDate();

            if (startDate <= 0 || endDate <= 0) {
                long ahora = System.currentTimeMillis();
                startDate = ahora;
                endDate = ahora + (24 * 60 * 60 * 1000);
                Log.w(TAG, "Usando fechas por defecto");
            }

            String peopleString = prefsManager.getPeopleString();
            if (peopleString != null && !peopleString.isEmpty()) {
                parseSearchParams(peopleString);
            }

            Log.d(TAG, String.format("PrefsManager parcial - Ubicación: '%s', Fechas por defecto: %d-%d",
                    searchLocation, startDate, endDate));

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo desde PrefsManager parcial", e);
            return false;
        }
    }

    private boolean intentarObtenerDesdeHistorial() {
        if (prefsManager == null) return false;

        try {
            List<SearchHistory> historial = prefsManager.getSearchHistory();
            if (historial.isEmpty()) return false;

            SearchHistory ultimaBusqueda = historial.get(0);

            searchLocation = ultimaBusqueda.getLocation();
            startDate = ultimaBusqueda.getStartDate();
            endDate = ultimaBusqueda.getEndDate();
            adults = ultimaBusqueda.getAdultsCount();
            children = ultimaBusqueda.getChildrenCount();
            rooms = ultimaBusqueda.getRoomsCount();

            Log.d(TAG, String.format("Historial - Ubicación: '%s', Fechas: %d-%d",
                    searchLocation, startDate, endDate));

            return !searchLocation.isEmpty() && startDate > 0 && endDate > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo desde historial", e);
            return false;
        }
    }

    private void verificarCambiosEnParametros() {
        if (prefsManager == null) return;

        try {
            String nuevaUbicacion = prefsManager.getBestAvailableLocation();
            long nuevaFechaInicio = prefsManager.getStartDate();
            long nuevaFechaFin = prefsManager.getEndDate();

            if (nuevaUbicacion != null && !nuevaUbicacion.equals(searchLocation)) {
                Log.d(TAG, "Cambio detectado en ubicación: " + searchLocation + " -> " + nuevaUbicacion);

                searchLocation = nuevaUbicacion;
                startDate = nuevaFechaInicio;
                endDate = nuevaFechaFin;

                String peopleString = prefsManager.getPeopleString();
                if (peopleString != null) {
                    parseSearchParams(peopleString);
                }

                parametrosValidados = validarParametros();
                if (parametrosValidados) {
                    actualizarVistaConParametros();
                    cargarHotelesConFiltros();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error verificando cambios en parámetros", e);
        }
    }

    private boolean validarParametros() {
        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            Log.e(TAG, "❌ Validación falló: No hay ubicación");
            return false;
        }

        if (startDate <= 0 || endDate <= 0) {
            Log.e(TAG, "❌ Validación falló: Fechas inválidas - start: " + startDate + ", end: " + endDate);
            return false;
        }

        if (startDate >= endDate) {
            Log.e(TAG, "❌ Validación falló: Fecha inicio >= fecha fin");
            return false;
        }

        if (adults <= 0) {
            Log.e(TAG, "❌ Validación falló: Adultos <= 0");
            return false;
        }

        if (rooms <= 0) {
            Log.e(TAG, "❌ Validación falló: Habitaciones <= 0");
            return false;
        }

        Log.d(TAG, "✅ Parámetros validados correctamente");
        return true;
    }

    private void actualizarVistaConParametros() {
        try {
            actualizarTituloUbicacion();
            actualizarResumenBusqueda();

            if (prefsManager != null && parametrosValidados) {
                prefsManager.saveSelectedLocation(searchLocation);
                prefsManager.saveDateRange(startDate, endDate);

                String peopleString = String.format("%d habitación%s · %d adulto%s · %d niño%s",
                        rooms, rooms == 1 ? "" : "es",
                        adults, adults == 1 ? "" : "s",
                        children, children == 1 ? "" : "s");
                prefsManager.savePeopleString(peopleString);

                Log.d(TAG, "Parámetros sincronizados con PrefsManager");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando vista con parámetros", e);
        }
    }

    private void inicializarVistas(View view) {
        recyclerView = view.findViewById(R.id.recyclerHotelList);
        progressBar = view.findViewById(R.id.progressBarHotels);
        textCantidad = view.findViewById(R.id.textCantidadHoteles);
        textCiudadTitulo = view.findViewById(R.id.textCiudadTitulo);

        // Vistas opcionales (pueden no existir en el layout)
        //textResumenBusqueda = view.findViewById(R.id.textResumenBusqueda);
        //txtSeleccionarAmenidades = view.findViewById(R.id.txtSeleccionarAmenidades);
        //txtSeleccionarPuntuacion = view.findViewById(R.id.txtSeleccionarPuntuacion);

        Log.d(TAG, "Vistas inicializadas - textCiudadTitulo: " + (textCiudadTitulo != null) +
                ", textResumenBusqueda: " + (textResumenBusqueda != null));
    }

    private void actualizarTituloUbicacion() {
        if (textCiudadTitulo != null) {
            if (searchLocation != null && !searchLocation.trim().isEmpty()) {
                textCiudadTitulo.setText(searchLocation);
                textCiudadTitulo.setVisibility(View.VISIBLE);
                Log.d(TAG, "Título actualizado: " + searchLocation);
            } else {
                textCiudadTitulo.setText("Ubicación no especificada");
                textCiudadTitulo.setVisibility(View.VISIBLE);
            }
        }
    }

    private void actualizarResumenBusqueda() {
        if (textResumenBusqueda != null && parametrosValidados) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                String fechaInicio = sdf.format(new Date(startDate));
                String fechaFin = sdf.format(new Date(endDate));

                String resumen = String.format("📅 %s - %s • 👥 %d adulto%s, %d niño%s • 🏨 %d habitación%s",
                        fechaInicio, fechaFin,
                        adults, adults == 1 ? "" : "s",
                        children, children == 1 ? "" : "s",
                        rooms, rooms == 1 ? "" : "es");

                textResumenBusqueda.setText(resumen);
                textResumenBusqueda.setVisibility(View.VISIBLE);

                Log.d(TAG, "Resumen actualizado: " + resumen);

            } catch (Exception e) {
                Log.e(TAG, "Error actualizando resumen", e);
                textResumenBusqueda.setText("Error generando resumen");
            }
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

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelAdapter(listaHotelesFiltrada, getContext());
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView configurado");
    }

    private void configurarFiltros() {
        configurarFiltroAmenidades();
        configurarFiltroPuntuacion();
        Log.d(TAG, "Filtros configurados");
    }

    private void configurarFiltroAmenidades() {
        try {
            opcionesAmenidades = getResources().getStringArray(R.array.amenities_options);
            seleccionados = new boolean[opcionesAmenidades.length];

            if (txtSeleccionarAmenidades != null) {
                txtSeleccionarAmenidades.setOnClickListener(v -> mostrarDialogoAmenidades());
            }

            actualizarTextoAmenidades();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando filtro de amenidades", e);
        }
    }

    private void configurarFiltroPuntuacion() {
        try {
            opcionesPuntuacion = getResources().getStringArray(R.array.filter_options);
            seleccionadasPuntuacion = new boolean[opcionesPuntuacion.length];

            if (txtSeleccionarPuntuacion != null) {
                txtSeleccionarPuntuacion.setOnClickListener(v -> mostrarDialogoPuntuacion());
            }

            actualizarTextoPuntuacion();

        } catch (Exception e) {
            Log.e(TAG, "Error configurando filtro de puntuación", e);
        }
    }

    private void mostrarError(String mensaje) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        textCantidad.setText("Error cargando hoteles");
    }

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
        // Por ahora, devolver true para no filtrar por puntuación
        return true;
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
        if (txtSeleccionarAmenidades == null) return;

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
        if (txtSeleccionarPuntuacion == null) return;

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

    // ============= MÉTODOS PÚBLICOS PARA LA ACTIVIDAD PADRE =============

    /**
     * 🔥 MEJORADO: Actualiza los parámetros de búsqueda desde la actividad padre
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

        // Resetear estados de búsqueda
        this.busquedaCompletada = false;
        this.busquedaExitosa = false;
        this.motivoSinResultados = "";

        this.parametrosValidados = validarParametros();

        if (parametrosValidados) {
            actualizarVistaConParametros();
            limpiarFiltrosSinRecargar();
            cargarHotelesConFiltros();
        } else {
            mostrarEstadoSinParametros();
        }
    }

    /**
     * 🔥 NUEVO: Actualizar parámetros desde PrefsManager
     */
    public void actualizarParametrosDesdePrefsManager() {
        Log.d(TAG, "Actualizando parámetros desde PrefsManager");

        if (prefsManager == null) {
            Log.w(TAG, "PrefsManager no disponible");
            return;
        }

        // Resetear estados
        this.busquedaCompletada = false;
        this.busquedaExitosa = false;
        this.motivoSinResultados = "";

        try {
            String ubicacion = prefsManager.getBestAvailableLocation();
            long fechaInicio = prefsManager.getStartDate();
            long fechaFin = prefsManager.getEndDate();
            String peopleString = prefsManager.getPeopleString();

            if (ubicacion != null && !ubicacion.isEmpty() && fechaInicio > 0 && fechaFin > 0) {
                this.searchLocation = ubicacion;
                this.startDate = fechaInicio;
                this.endDate = fechaFin;

                if (peopleString != null && !peopleString.isEmpty()) {
                    parseSearchParams(peopleString);
                }

                this.parametrosValidados = validarParametros();

                if (parametrosValidados) {
                    actualizarVistaConParametros();
                    cargarHotelesConFiltros();
                    Log.d(TAG, "Parámetros actualizados desde PrefsManager exitosamente");
                } else {
                    Log.w(TAG, "Parámetros desde PrefsManager no son válidos");
                    mostrarEstadoSinParametros();
                }
            } else {
                Log.w(TAG, "PrefsManager no tiene datos suficientes");
                mostrarEstadoSinParametros();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando desde PrefsManager", e);
            mostrarEstadoError("Error obteniendo parámetros de búsqueda");
        }
    }

    /**
     * 🔥 NUEVO: Forzar recarga completa
     */
    public void recargarDesdePrefsManager() {
        Log.d(TAG, "Forzando recarga desde PrefsManager");

        limpiarDatos();
        obtenerParametrosConEstrategiaMejorada();

        if (parametrosValidados) {
            cargarHotelesConFiltros();
        } else {
            mostrarEstadoSinParametros();
        }
    }

    /**
     * 🔥 NUEVO: Limpiar todos los datos
     */
    private void limpiarDatos() {
        listaHotelesOriginal.clear();
        listaHotelesFiltrada.clear();
        limpiarFiltrosSinRecargar();

        // Resetear estados
        busquedaCompletada = false;
        busquedaExitosa = false;
        motivoSinResultados = "";

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
     * 🔥 MEJORADO: Obtiene un resumen de los parámetros de búsqueda actuales
     */
    public String obtenerResumenBusqueda() {
        if (!parametrosValidados) {
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

    /**
     * 🔥 NUEVO: Verificar si los parámetros están validados
     */
    public boolean isParametrosValidados() {
        return parametrosValidados;
    }

    /**
     * 🔥 NUEVO: Obtener ubicación actual
     */
    public String getSearchLocation() {
        return searchLocation;
    }

    /**
     * 🔥 NUEVO: Obtener lista de hoteles seleccionados (para pasar a la siguiente vista)
     */
    public List<Hotel> getHotelesSeleccionados() {
        // Si hay filtros aplicados, devolver la lista filtrada
        // Si no hay filtros, devolver la lista original
        // Si no hay resultados, devolver lista vacía (la siguiente vista manejará esto)
        return new ArrayList<>(listaHotelesFiltrada);
    }

    /**
     * 🔥 NUEVO: Obtener parámetros de búsqueda actuales (para pasar a la siguiente vista)
     */
    public Bundle getParametrosBusqueda() {
        Bundle bundle = new Bundle();
        bundle.putString("search_location", searchLocation);
        bundle.putLong("start_date", startDate);
        bundle.putLong("end_date", endDate);
        bundle.putInt("adults", adults);
        bundle.putInt("children", children);
        bundle.putInt("rooms", rooms);
        bundle.putBoolean("parametros_validados", parametrosValidados);
        bundle.putBoolean("busqueda_completada", busquedaCompletada);
        bundle.putBoolean("busqueda_exitosa", busquedaExitosa);
        bundle.putString("motivo_sin_resultados", motivoSinResultados);
        return bundle;
    }

    /**
     * 🔥 NUEVO: Verificar si se puede navegar a la siguiente vista
     * (siempre devuelve true, pero proporciona información del estado)
     */
    public boolean puedeNavegarASiguienteVista() {
        return true; // Siempre permitir navegación
    }

    /**
     * 🔥 NUEVO: Obtener mensaje informativo para la navegación
     */
    public String getMensajeNavegacion() {
        if (!parametrosValidados) {
            return "Los parámetros de búsqueda no están completos, pero puedes continuar explorando opciones.";
        }

        if (!busquedaCompletada) {
            return "La búsqueda aún está en progreso. Puedes continuar sin esperar los resultados.";
        }

        if (!busquedaExitosa || listaHotelesFiltrada.isEmpty()) {
            return "No se encontraron hoteles para tus criterios, pero puedes continuar para explorar otras opciones.";
        }

        return String.format("Se encontraron %d hoteles disponibles. ¡Continúa para ver más detalles!",
                listaHotelesFiltrada.size());
    }
}