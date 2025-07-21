package com.example.telehotel.features.superadmin.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.superadmin.adapters.ReporteHotelAdapter;
import com.example.telehotel.features.superadmin.adapters.ReservaDetalleAdapter;
import com.example.telehotel.data.model.ReporteHotel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Imports para PDF
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReporteReservasFragment extends Fragment {

    private static final String TAG = "ReporteReservasFragment";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // =============== VIEWS ===============
    private TextInputEditText etFechaInicio, etFechaFin;
    private MaterialButton btnGenerarReporte, btnExportarPDF;
    private RecyclerView recyclerViewReporte;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage, tvTotalReservas, tvTotalIngresos, tvRangoFechas;

    // =============== DATA ===============
    private ReporteHotelAdapter adapter;
    private List<ReporteHotel> reportesList;
    private FirebaseFirestore db;
    private Map<String, Hotel> hotelesMap;

    // =============== FECHAS ===============
    private Calendar fechaInicioCalendar;
    private Calendar fechaFinCalendar;
    private SimpleDateFormat dateFormat;

    // =============== LIFECYCLE METHODS ===============

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reporte_reservas, container, false);

        initFirebase();
        initViews(view);
        setupRecyclerView();
        setupListeners();
        setupFechasPorDefecto();

        return view;
    }

    // =============== INITIALIZATION METHODS ===============

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        hotelesMap = new HashMap<>();
    }

    private void initViews(View view) {
        etFechaInicio = view.findViewById(R.id.etFechaInicio);
        etFechaFin = view.findViewById(R.id.etFechaFin);
        btnGenerarReporte = view.findViewById(R.id.btnGenerarReporte);
        btnExportarPDF = view.findViewById(R.id.btnExportarPDF);
        recyclerViewReporte = view.findViewById(R.id.recyclerViewReporte);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        tvTotalReservas = view.findViewById(R.id.tvTotalReservas);
        tvTotalIngresos = view.findViewById(R.id.tvTotalIngresos);
        tvRangoFechas = view.findViewById(R.id.tvRangoFechas);

        // Inicializar datos
        reportesList = new ArrayList<>();
        fechaInicioCalendar = Calendar.getInstance();
        fechaFinCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configurar campos de fecha como no editables
        etFechaInicio.setFocusable(false);
        etFechaInicio.setClickable(true);
        etFechaFin.setFocusable(false);
        etFechaFin.setClickable(true);
    }

    private void setupRecyclerView() {
        adapter = new ReporteHotelAdapter(getContext(), reportesList);
        adapter.setOnReporteClickListener(reporte -> mostrarDetallesHotel(reporte));
        recyclerViewReporte.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReporte.setAdapter(adapter);
    }

    private void setupListeners() {
        // Fecha inicio
        etFechaInicio.setOnClickListener(v -> mostrarDatePickerInicio());

        // Fecha fin
        etFechaFin.setOnClickListener(v -> mostrarDatePickerFin());

        // Generar reporte
        btnGenerarReporte.setOnClickListener(v -> generarReporte());

        // Exportar PDF
        btnExportarPDF.setOnClickListener(v -> exportarPDF());

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::generarReporte);
    }

    private void setupFechasPorDefecto() {
        // Fecha fin: hoy
        fechaFinCalendar = Calendar.getInstance();

        // Fecha inicio: hace 30 días
        fechaInicioCalendar = Calendar.getInstance();
        fechaInicioCalendar.add(Calendar.DAY_OF_MONTH, -30);

        // Actualizar campos
        etFechaInicio.setText(dateFormat.format(fechaInicioCalendar.getTime()));
        etFechaFin.setText(dateFormat.format(fechaFinCalendar.getTime()));

        // Generar reporte inicial
        generarReporte();
    }

    // =============== DATE PICKER METHODS ===============

    private void mostrarDatePickerInicio() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                R.style.CustomDatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    fechaInicioCalendar.set(year, month, dayOfMonth);
                    etFechaInicio.setText(dateFormat.format(fechaInicioCalendar.getTime()));
                },
                fechaInicioCalendar.get(Calendar.YEAR),
                fechaInicioCalendar.get(Calendar.MONTH),
                fechaInicioCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Fecha máxima: hoy
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarDatePickerFin() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                R.style.CustomDatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    fechaFinCalendar.set(year, month, dayOfMonth);
                    etFechaFin.setText(dateFormat.format(fechaFinCalendar.getTime()));
                },
                fechaFinCalendar.get(Calendar.YEAR),
                fechaFinCalendar.get(Calendar.MONTH),
                fechaFinCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // Fecha máxima: hoy
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Fecha mínima: fecha de inicio
        if (fechaInicioCalendar != null) {
            datePickerDialog.getDatePicker().setMinDate(fechaInicioCalendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    // =============== REPORT GENERATION METHODS ===============

    private void generarReporte() {
        if (!validarFechas()) return;

        mostrarLoading(true);

        // Primero cargar hoteles
        cargarHoteles(() -> {
            // Luego cargar reservas
            cargarReservas();
        });
    }

    private boolean validarFechas() {
        if (fechaInicioCalendar.after(fechaFinCalendar)) {
            Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha fin", Toast.LENGTH_SHORT).show();
            return false;
        }

        long diffInMillis = fechaFinCalendar.getTimeInMillis() - fechaInicioCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays > 365) {
            Toast.makeText(getContext(), "El rango no puede ser mayor a 1 año", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void cargarHoteles(Runnable onComplete) {
        db.collection("hoteles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hotelesMap.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Hotel hotel = document.toObject(Hotel.class);
                        hotel.setId(document.getId());
                        hotelesMap.put(hotel.getId(), hotel);
                    }

                    Log.d(TAG, "Hoteles cargados: " + hotelesMap.size());
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando hoteles", e);
                    Toast.makeText(getContext(), "Error cargando hoteles", Toast.LENGTH_SHORT).show();
                    mostrarLoading(false);
                });
    }

    private void cargarReservas() {
        long fechaInicioTimestamp = fechaInicioCalendar.getTimeInMillis();
        long fechaFinTimestamp = fechaFinCalendar.getTimeInMillis();

        db.collection("reservas")
                .whereGreaterThanOrEqualTo("fechaInicio", fechaInicioTimestamp)
                .whereLessThanOrEqualTo("fechaInicio", fechaFinTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    procesarReservas(queryDocumentSnapshots);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas", e);
                    Toast.makeText(getContext(), "Error cargando reservas", Toast.LENGTH_SHORT).show();
                    mostrarLoading(false);
                });
    }

    private void procesarReservas(Iterable<QueryDocumentSnapshot> documentos) {
        Map<String, ReporteHotel> reportesMap = new HashMap<>();
        int totalReservas = 0;
        double totalIngresos = 0.0;

        for (QueryDocumentSnapshot document : documentos) {
            Reserva reserva = document.toObject(Reserva.class);
            reserva.setId(document.getId());

            String hotelId = reserva.getHotelId();
            if (hotelId == null) continue;

            // Obtener o crear reporte del hotel
            ReporteHotel reporte = reportesMap.get(hotelId);
            if (reporte == null) {
                Hotel hotel = hotelesMap.get(hotelId);
                String nombreHotel = hotel != null ? hotel.getNombre() : "Hotel Desconocido";
                String ubicacionHotel = hotel != null && hotel.getUbicacion() != null ?
                        hotel.getUbicacion().getCiudad() : "Ubicación no disponible";

                reporte = new ReporteHotel();
                reporte.setHotelId(hotelId);
                reporte.setNombreHotel(nombreHotel);
                reporte.setUbicacionHotel(ubicacionHotel);
                reporte.setTotalReservas(0);
                reporte.setTotalIngresos(0.0);
                reporte.setReservasActivas(0);
                reporte.setReservasCanceladas(0);

                reportesMap.put(hotelId, reporte);
            }

            // Actualizar estadísticas
            reporte.setTotalReservas(reporte.getTotalReservas() + 1);

            if (reserva.getMontoTotal() != null) {
                reporte.setTotalIngresos(reporte.getTotalIngresos() + reserva.getMontoTotal());
                totalIngresos += reserva.getMontoTotal();
            }

            // Contar por estado
            String estado = reserva.getEstado();
            if ("activa".equals(estado) || "completada".equals(estado)) {
                reporte.setReservasActivas(reporte.getReservasActivas() + 1);
            } else if ("cancelada".equals(estado)) {
                reporte.setReservasCanceladas(reporte.getReservasCanceladas() + 1);
            }

            totalReservas++;
        }

        // Convertir a lista y ordenar por ingresos
        reportesList.clear();
        reportesList.addAll(reportesMap.values());
        reportesList.sort((a, b) -> Double.compare(b.getTotalIngresos(), a.getTotalIngresos()));

        // Actualizar UI
        actualizarResumenGeneral(totalReservas, totalIngresos);
        adapter.updateList(reportesList);
        mostrarLoading(false);

        // Mostrar estado vacío si no hay datos
        if (reportesList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewReporte.setVisibility(View.GONE);
            tvEmptyMessage.setText("No hay reservas en el rango de fechas seleccionado");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewReporte.setVisibility(View.VISIBLE);
        }
    }

    private void actualizarResumenGeneral(int totalReservas, double totalIngresos) {
        tvTotalReservas.setText(String.valueOf(totalReservas));
        tvTotalIngresos.setText(String.format(Locale.getDefault(), "S/ %.2f", totalIngresos));

        String rangoFechas = dateFormat.format(fechaInicioCalendar.getTime()) + " - " +
                dateFormat.format(fechaFinCalendar.getTime());
        tvRangoFechas.setText(rangoFechas);
    }

    // =============== HOTEL DETAILS METHODS ===============

    private void mostrarDetallesHotel(ReporteHotel reporte) {
        Log.d(TAG, "=== MOSTRANDO DETALLES HOTEL ===");
        Log.d(TAG, "Hotel: " + reporte.getNombreHotel());
        Log.d(TAG, "Hotel ID: " + reporte.getHotelId());

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_hotel_detalles, null);

        // Verificar que el layout se infló correctamente
        if (dialogView == null) {
            Log.e(TAG, "❌ ERROR CRÍTICO: dialogView es null");
            Toast.makeText(getContext(), "Error cargando diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "✅ Layout del diálogo inflado correctamente");

        AlertDialog dialog = builder.setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar el diálogo
        configurarDialogoDetalles(dialogView, reporte, dialog);

        // Configurar el diálogo para pantalla completa
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Mostrar el diálogo
        dialog.show();

        Log.d(TAG, "✅ Diálogo mostrado");
    }

    private void configurarDialogoDetalles(View dialogView, ReporteHotel reporte, AlertDialog dialog) {
        Log.d(TAG, "=== CONFIGURANDO DIÁLOGO CON DEBUG ===");

        try {
            // Referencias a las vistas - ACTUALIZADO PARA FRAMELAYOUT
            TextView tvNombreHotel = dialogView.findViewById(R.id.tvNombreHotelDetalle);
            TextView tvUbicacionHotel = dialogView.findViewById(R.id.tvUbicacionHotelDetalle);
            TextView tvPeriodo = dialogView.findViewById(R.id.tvPeriodoDetalle);
            TextView tvTotalReservas = dialogView.findViewById(R.id.tvTotalReservasDetalle);
            TextView tvTotalIngresos = dialogView.findViewById(R.id.tvTotalIngresosDetalle);
            TextView tvPromedio = dialogView.findViewById(R.id.tvPromedioDetalle);
            TextView tvTasaOcupacion = dialogView.findViewById(R.id.tvTasaOcupacion);
            TextView tvDescripcionHotel = dialogView.findViewById(R.id.tvDescripcionHotel);
            //TextView tvServiciosHotel = dialogView.findViewById(R.id.tvServiciosHotel);
            MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarDetalle);

            // TabLayout y contenido - CAMBIO: FrameLayout en lugar de ConstraintLayout
            TabLayout tabLayout = dialogView.findViewById(R.id.tabLayoutDetalles);
            View tabResumen = dialogView.findViewById(R.id.tabResumen);
            FrameLayout tabReservas = dialogView.findViewById(R.id.tabReservas);      // ✅ FrameLayout
            View tabTendencias = dialogView.findViewById(R.id.tabTendencias);
            RecyclerView recyclerReservas = dialogView.findViewById(R.id.recyclerViewReservasDetalle);
            LinearLayout emptyStateReservas = dialogView.findViewById(R.id.emptyStateReservas);
            TextView tvMensajeEmpty = dialogView.findViewById(R.id.tvMensajeEmptyReservas);

            // LOG DE VERIFICACIÓN DE COMPONENTES
            Log.d(TAG, "Verificación de componentes del diálogo:");
            Log.d(TAG, "  - tabLayout: " + (tabLayout != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - tabResumen: " + (tabResumen != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - tabReservas: " + (tabReservas != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - tabTendencias: " + (tabTendencias != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - recyclerReservas: " + (recyclerReservas != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - emptyStateReservas: " + (emptyStateReservas != null ? "✅ ID encontrado" : "❌ ID no encontrado"));
            Log.d(TAG, "  - tvMensajeEmpty: " + (tvMensajeEmpty != null ? "✅ ID encontrado" : "❌ ID no encontrado"));

            // Si algún componente crítico es null, mostrar error detallado
            if (tabLayout == null || tabReservas == null || recyclerReservas == null) {
                Log.e(TAG, "❌ ERROR CRÍTICO: Componentes esenciales son null");
                Log.e(TAG, "Verificar que dialog_hotel_detalles.xml contiene todos los IDs requeridos");

                Toast.makeText(getContext(), "Error: Layout incompleto", Toast.LENGTH_LONG).show();
                return;
            }

            // Llenar datos básicos
            tvNombreHotel.setText(reporte.getNombreHotel());
            tvUbicacionHotel.setText("📍 " + reporte.getUbicacionHotel());

            String rangoFechas = dateFormat.format(fechaInicioCalendar.getTime()) + " - " +
                    dateFormat.format(fechaFinCalendar.getTime());
            tvPeriodo.setText("📅 " + rangoFechas);

            // Estadísticas
            tvTotalReservas.setText(String.valueOf(reporte.getTotalReservas()));
            tvTotalIngresos.setText(reporte.getTotalIngresosFormateado());
            tvPromedio.setText(reporte.getPromedioFormateado());
            tvTasaOcupacion.setText(String.format(Locale.getDefault(), "%.0f%%", reporte.getPorcentajeReservasActivas()));

            // Información del hotel
            tvDescripcionHotel.setText("Hotel ubicado en " + reporte.getUbicacionHotel() + " con excelentes servicios.");
            //tvServiciosHotel.setText("Servicios: WiFi, Restaurante, Recepción 24h, Limpieza diaria");

            // Configurar tabs - ACTUALIZADO PARA FRAMELAYOUT
            configurarTabsConFrameLayout(tabLayout, tabResumen, tabReservas, tabTendencias);

            fixEmergenciaTabReservas(dialog);
            // Cargar reservas CON DELAY MAYOR PARA ASEGURAR LAYOUT
            dialog.setOnShowListener(dialogInterface -> {
                Log.d(TAG, "🎭 Diálogo mostrado, esperando layout...");

                // Delay más largo para asegurar que el layout esté completamente listo
                dialogView.postDelayed(() -> {
                    Log.d(TAG, "⏰ Iniciando carga de reservas...");
                    cargarReservasConLayoutForzado(reporte.getHotelId(), recyclerReservas,
                            emptyStateReservas, tvMensajeEmpty, rangoFechas);
                }, 800); // Delay aumentado a 800ms
            });

            // Configurar botón cerrar
            btnCerrar.setOnClickListener(v -> {
                Log.d(TAG, "🚪 Cerrando diálogo");
                dialog.dismiss();
            });

            Log.d(TAG, "✅ Diálogo configurado completamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR CONFIGURANDO DIÁLOGO", e);
            Toast.makeText(getContext(), "Error configurando diálogo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void configurarTabsConFrameLayout(TabLayout tabLayout, View tabResumen,
                                              FrameLayout tabReservas, View tabTendencias) {
        Log.d(TAG, "=== CONFIGURANDO TABS CON FRAMELAYOUT - CORREGIDO ===");

        // Verificar que todos los componentes existen
        if (tabLayout == null || tabReservas == null) {
            Log.e(TAG, "❌ ERROR: Componentes esenciales son null");
            return;
        }

        // Estado inicial - IMPORTANTE: Asegurarse de que las vistas existen
        Log.d(TAG, "Estado inicial de tabs:");
        Log.d(TAG, "  - tabResumen: " + (tabResumen != null ? "✅" : "❌"));
        Log.d(TAG, "  - tabReservas: " + (tabReservas != null ? "✅" : "❌"));
        Log.d(TAG, "  - tabTendencias: " + (tabTendencias != null ? "✅" : "❌"));

        tabResumen.setVisibility(View.VISIBLE);
        tabReservas.setVisibility(View.GONE);
        tabTendencias.setVisibility(View.GONE);

        Log.d(TAG, "✅ Estado inicial configurado");

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d(TAG, "🎯 TAB SELECCIONADO: " + position);

                // PASO 1: Ocultar todos (con logs para debug)
                Log.d(TAG, "Ocultando todos los tabs...");
                tabResumen.setVisibility(View.GONE);
                tabReservas.setVisibility(View.GONE);
                tabTendencias.setVisibility(View.GONE);

                Log.d(TAG, "Estado después de ocultar:");
                Log.d(TAG, "  - tabResumen visibility: " + getVisibilityString(tabResumen.getVisibility()));
                Log.d(TAG, "  - tabReservas visibility: " + getVisibilityString(tabReservas.getVisibility()));
                Log.d(TAG, "  - tabTendencias visibility: " + getVisibilityString(tabTendencias.getVisibility()));

                // PASO 2: Mostrar el seleccionado CON DELAY MÁS CORTO
                tabLayout.postDelayed(() -> {
                    Log.d(TAG, "Mostrando tab " + position + "...");

                    switch (position) {
                        case 0:
                            tabResumen.setVisibility(View.VISIBLE);
                            Log.d(TAG, "✅ Tab Resumen mostrado");
                            Log.d(TAG, "  - Visibility final: " + getVisibilityString(tabResumen.getVisibility()));
                            break;

                        case 1:
                            Log.d(TAG, "🔄 Activando tab Reservas...");

                            // MOSTRAR EL TAB RESERVAS INMEDIATAMENTE
                            tabReservas.setVisibility(View.VISIBLE);

                            Log.d(TAG, "✅ Tab Reservas visible");
                            Log.d(TAG, "  - Visibility final: " + getVisibilityString(tabReservas.getVisibility()));
                            Log.d(TAG, "  - Dimensions: " + tabReservas.getWidth() + "x" + tabReservas.getHeight());

                            // VERIFICAR RECYCLERVIEW DESPUÉS DE HACER VISIBLE EL TAB
                            RecyclerView recyclerView = tabReservas.findViewById(R.id.recyclerViewReservasDetalle);
                            if (recyclerView != null) {
                                // Delay MÍNIMOa para permitir que el layout se actualice
                                recyclerView.postDelayed(() -> {
                                    Log.d(TAG, "🔍 Verificando RecyclerView después de hacer visible el tab...");
                                    debugRecyclerViewFinal(recyclerView, "DESPUÉS DE HACER VISIBLE TAB");

                                    // Si el parent ahora es visible pero el RV no tiene dimensiones, forzar
                                    if (tabReservas.getVisibility() == View.VISIBLE &&
                                            (recyclerView.getWidth() == 0 || recyclerView.getHeight() == 0)) {
                                        Log.w(TAG, "⚠️ Tab visible pero RecyclerView sin dimensiones, forzando...");
                                        forzarLayoutRecyclerViewConDimensionesParent(recyclerView, tabReservas);
                                    }
                                }, 100); // Delay muy corto
                            }
                            break;

                        case 2:
                            tabTendencias.setVisibility(View.VISIBLE);
                            Log.d(TAG, "✅ Tab Tendencias mostrado");
                            Log.d(TAG, "  - Visibility final: " + getVisibilityString(tabTendencias.getVisibility()));
                            break;
                    }
                }, 50); // Delay muy corto para evitar parpadeos
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab deseleccionado: " + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab reseleccionado: " + tab.getPosition());
                // Si el usuario toca el mismo tab, asegurar que esté visible
                if (tab.getPosition() == 1) {
                    Log.d(TAG, "🔄 Re-selección del tab Reservas, verificando visibilidad...");
                    tabReservas.setVisibility(View.VISIBLE);
                }
            }
        });

        Log.d(TAG, "✅ Tabs con FrameLayout configurados con debug mejorado");
    }
    // =============== MÉTODO MEJORADO PARA FORZAR LAYOUT CON DIMENSIONES DEL PARENT ===============

    private void forzarLayoutRecyclerViewConDimensionesParent(RecyclerView recyclerView, FrameLayout parent) {
        Log.d(TAG, "=== FORZANDO LAYOUT CON DIMENSIONES DEL PARENT ===");

        try {
            // Esperar a que el parent tenga dimensiones
            parent.post(() -> {
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                Log.d(TAG, "Dimensiones actuales del parent: " + parentWidth + "x" + parentHeight);

                if (parentWidth > 0 && parentHeight > 0) {
                    Log.d(TAG, "✅ Parent tiene dimensiones, aplicando al RecyclerView...");

                    // Aplicar las dimensiones del parent al RecyclerView
                    ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                    if (params instanceof FrameLayout.LayoutParams) {
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        recyclerView.setLayoutParams(params);
                    }

                    // Forzar medición con las dimensiones del parent
                    int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
                    int heightSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.EXACTLY);

                    recyclerView.measure(widthSpec, heightSpec);
                    recyclerView.layout(0, 0, parentWidth, parentHeight);

                    // Invalidar
                    recyclerView.requestLayout();
                    recyclerView.invalidate();

                    if (recyclerView.getAdapter() != null) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }

                    Log.d(TAG, "✅ Layout forzado completado");

                    // Debug final
                    recyclerView.postDelayed(() -> {
                        debugRecyclerViewFinal(recyclerView, "DESPUÉS DE FORZAR LAYOUT CON PARENT");
                    }, 200);

                } else {
                    Log.w(TAG, "⚠️ Parent aún no tiene dimensiones, reintentando...");

                    // Reintentar después de un delay más largo
                    parent.postDelayed(() -> {
                        forzarLayoutRecyclerViewConDimensionesParent(recyclerView, parent);
                    }, 200);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error forzando layout con dimensiones del parent", e);
        }
    }

    // =============== CARGA DE RESERVAS CON LAYOUT FORZADO ===============

    private void cargarReservasConLayoutForzado(String hotelId, RecyclerView recyclerView,
                                                LinearLayout emptyState, TextView tvMensajeEmpty,
                                                String rangoFechas) {
        Log.d(TAG, "=== CARGANDO RESERVAS CON LAYOUT FORZADO ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "RecyclerView: " + (recyclerView != null ? "✅" : "❌"));
        Log.d(TAG, "Empty state: " + (emptyState != null ? "✅" : "❌"));

        // Estado inicial
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Query exitosa. Documentos: " + queryDocumentSnapshots.size());

                    List<Reserva> reservas = procesarDocumentosReservas(queryDocumentSnapshots);

                    recyclerView.post(() -> {
                        if (reservas.isEmpty()) {
                            mostrarEstadoVacio(recyclerView, emptyState, tvMensajeEmpty, rangoFechas);
                        } else {
                            mostrarReservasConLayoutForzado(recyclerView, emptyState, reservas);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error en query", e);
                    recyclerView.post(() -> {
                        mostrarError(recyclerView, emptyState, tvMensajeEmpty, e.getMessage());
                    });
                });
    }

    // =============== MOSTRAR RESERVAS CON LAYOUT FORZADO ===============

    private void mostrarReservasConLayoutForzado(RecyclerView recyclerView, LinearLayout emptyState, List<Reserva> reservas) {
        Log.d(TAG, "=== MOSTRANDO RESERVAS CON LAYOUT FORZADO ===");
        Log.d(TAG, "Reservas a mostrar: " + reservas.size());

        try {
            // Ocultar empty state
            emptyState.setVisibility(View.GONE);

            // Ordenar reservas
            reservas.sort((a, b) -> {
                Long fechaA = a.getFechaInicio();
                Long fechaB = b.getFechaInicio();
                if (fechaA == null && fechaB == null) return 0;
                if (fechaA == null) return 1;
                if (fechaB == null) return -1;
                return fechaB.compareTo(fechaA);
            });

            // Configurar adapter y layout manager
            ReservaDetalleAdapter adapter = new ReservaDetalleAdapter(getContext(), reservas);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            // FORZAR MEDICIÓN Y LAYOUT
            recyclerView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            Log.d(TAG, "✅ RecyclerView medido");
            debugRecyclerViewFinal(recyclerView, "DESPUÉS DE MEASURE");

            // Mostrar RecyclerView
            recyclerView.setVisibility(View.VISIBLE);

            // Verificación final después de un delay
            recyclerView.postDelayed(() -> {
                debugRecyclerViewFinal(recyclerView, "VERIFICACIÓN FINAL");

                // Si aún no funciona, último intento
                if (recyclerView.getChildCount() == 0 && adapter.getItemCount() > 0) {
                    Log.w(TAG, "⚠️ ÚLTIMO INTENTO - Forzando refresh completo");
                    forzarLayoutRecyclerView(recyclerView);
                }
            }, 500);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando reservas", e);
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    // =============== MÉTODO PARA FORZAR LAYOUT DEL RECYCLERVIEW ===============

    private void forzarLayoutRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "=== FORZANDO LAYOUT RECYCLERVIEW ===");

        try {
            // Obtener dimensiones del parent
            ViewGroup parent = (ViewGroup) recyclerView.getParent();
            if (parent != null) {
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                Log.d(TAG, "Dimensiones del parent: " + parentWidth + "x" + parentHeight);

                if (parentWidth > 0 && parentHeight > 0) {
                    // Forzar medición con las dimensiones del parent
                    int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
                    int heightSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.EXACTLY);

                    recyclerView.measure(widthSpec, heightSpec);
                    recyclerView.layout(0, 0, parentWidth, parentHeight);

                    Log.d(TAG, "✅ Layout forzado con dimensiones exactas");
                }
            }

            // Forzar invalidación completa
            recyclerView.requestLayout();
            recyclerView.invalidate();

            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            Log.d(TAG, "✅ Invalidación completa ejecutada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error forzando layout", e);
        }
    }

    // =============== DEBUG FINAL DEL RECYCLERVIEW ===============

    private void debugRecyclerViewFinal(RecyclerView recyclerView, String momento) {
        Log.d(TAG, "=== DEBUG RECYCLERVIEW FINAL - " + momento + " ===");

        if (recyclerView == null) {
            Log.e(TAG, "❌ RecyclerView es null");
            return;
        }

        // Información detallada
        Log.d(TAG, "RecyclerView final info:");
        Log.d(TAG, "  📏 Dimensiones:");
        Log.d(TAG, "    - Width: " + recyclerView.getWidth() + " (measured: " + recyclerView.getMeasuredWidth() + ")");
        Log.d(TAG, "    - Height: " + recyclerView.getHeight() + " (measured: " + recyclerView.getMeasuredHeight() + ")");
        Log.d(TAG, "  👁️ Visibilidad: " + getVisibilityString(recyclerView.getVisibility()));
        Log.d(TAG, "  👶 Children: " + recyclerView.getChildCount());

        // Parent info
        if (recyclerView.getParent() != null) {
            ViewGroup parent = (ViewGroup) recyclerView.getParent();
            Log.d(TAG, "  👨‍👩‍👧‍👦 Parent (" + parent.getClass().getSimpleName() + "):");
            Log.d(TAG, "    - Visibility: " + getVisibilityString(parent.getVisibility()));
            Log.d(TAG, "    - Dimensions: " + parent.getWidth() + "x" + parent.getHeight());
            Log.d(TAG, "    - Children: " + parent.getChildCount());

            // Grand parent info (para debug profundo)
            if (parent.getParent() != null && parent.getParent() instanceof ViewGroup) {
                ViewGroup grandParent = (ViewGroup) parent.getParent();
                Log.d(TAG, "    - GrandParent (" + grandParent.getClass().getSimpleName() + "):");
                Log.d(TAG, "      - Visibility: " + getVisibilityString(grandParent.getVisibility()));
                Log.d(TAG, "      - Dimensions: " + grandParent.getWidth() + "x" + grandParent.getHeight());
            }
        }

        // Adapter info
        if (recyclerView.getAdapter() != null) {
            Log.d(TAG, "  📋 Adapter:");
            Log.d(TAG, "    - Class: " + recyclerView.getAdapter().getClass().getSimpleName());
            Log.d(TAG, "    - Item count: " + recyclerView.getAdapter().getItemCount());
        } else {
            Log.e(TAG, "  ❌ Adapter es null");
        }

        // Layout manager info
        if (recyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            Log.d(TAG, "  📐 Layout Manager:");
            Log.d(TAG, "    - Class: " + lm.getClass().getSimpleName());
            Log.d(TAG, "    - Item count: " + lm.getItemCount());

            if (lm instanceof LinearLayoutManager) {
                LinearLayoutManager llm = (LinearLayoutManager) lm;
                Log.d(TAG, "    - First visible position: " + llm.findFirstVisibleItemPosition());
                Log.d(TAG, "    - Last visible position: " + llm.findLastVisibleItemPosition());
            }
        } else {
            Log.e(TAG, "  ❌ Layout Manager es null");
        }

        Log.d(TAG, "=== FIN DEBUG RECYCLERVIEW FINAL ===");
    }

    // =============== MÉTODO AUXILIAR PARA PROCESAR DOCUMENTOS ===============

    private List<Reserva> procesarDocumentosReservas(Iterable<QueryDocumentSnapshot> documentos) {
        List<Reserva> reservas = new ArrayList<>();
        long fechaInicioTimestamp = fechaInicioCalendar.getTimeInMillis();
        long fechaFinTimestamp = fechaFinCalendar.getTimeInMillis();

        for (QueryDocumentSnapshot document : documentos) {
            try {
                Reserva reserva = mapearReservaSegura(document);

                // Filtrar por fechas
                Long fechaReserva = reserva.getFechaInicio();
                if (fechaReserva != null && fechaReserva >= fechaInicioTimestamp && fechaReserva <= fechaFinTimestamp) {
                    reservas.add(reserva);
                    Log.d(TAG, "Reserva incluida: " + reserva.getId() + " - " + reserva.getClienteNombre());
                } else {
                    Log.d(TAG, "Reserva excluida por fecha: " + reserva.getId());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error procesando reserva " + document.getId(), e);
            }
        }

        Log.d(TAG, "Total reservas procesadas: " + reservas.size());
        return reservas;
    }

    // =============== MAPEO SEGURO DE RESERVAS ===============

    private Reserva mapearReservaSegura(QueryDocumentSnapshot document) {
        Map<String, Object> data = document.getData();

        Reserva reserva = new Reserva();
        reserva.setId(document.getId());

        // Mapear campos de string
        reserva.setHotelId(obtenerString(data, "hotelId"));
        reserva.setClienteId(obtenerString(data, "clienteId"));
        reserva.setClienteNombre(obtenerString(data, "clienteNombre"));
        reserva.setClienteEmail(obtenerString(data, "clienteEmail"));
        reserva.setHabitacionNumero(obtenerString(data, "habitacionNumero"));
        reserva.setHabitacionTipo(obtenerString(data, "habitacionTipo"));
        reserva.setEstado(obtenerString(data, "estado"));

        // Valores por defecto
        if (reserva.getClienteNombre() == null && reserva.getClienteEmail() != null) {
            reserva.setClienteNombre(reserva.getClienteEmail());
        } else if (reserva.getClienteNombre() == null) {
            reserva.setClienteNombre("Cliente " + (reserva.getClienteId() != null ? reserva.getClienteId().substring(0, Math.min(8, reserva.getClienteId().length())) : "Desconocido"));
        }

        if (reserva.getEstado() == null) {
            reserva.setEstado("activa");
        }

        if (reserva.getHabitacionNumero() == null) {
            reserva.setHabitacionNumero("Sin especificar");
        }

        if (reserva.getHabitacionTipo() == null) {
            reserva.setHabitacionTipo("Estándar");
        }

        // Mapear números
        Object montoObj = data.get("montoTotal");
        if (montoObj instanceof Number) {
            reserva.setMontoTotal(((Number) montoObj).doubleValue());
        } else {
            reserva.setMontoTotal(0.0);
        }

        Object fechaInicioObj = data.get("fechaInicio");
        if (fechaInicioObj instanceof Number) {
            reserva.setFechaInicio(((Number) fechaInicioObj).longValue());
        }

        Object fechaFinObj = data.get("fechaFin");
        if (fechaFinObj instanceof Number) {
            reserva.setFechaFin(((Number) fechaFinObj).longValue());
        }

        return reserva;
    }

    // =============== MÉTODOS AUXILIARES ===============

    private String obtenerString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.VISIBLE: return "VISIBLE (0)";
            case View.INVISIBLE: return "INVISIBLE (4)";
            case View.GONE: return "GONE (8)";
            default: return "UNKNOWN(" + visibility + ")";
        }
    }

    private void mostrarEstadoVacio(RecyclerView recyclerView, LinearLayout emptyState,
                                    TextView tvMensajeEmpty, String rangoFechas) {
        Log.d(TAG, "=== MOSTRANDO ESTADO VACÍO ===");

        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);

        if (tvMensajeEmpty != null) {
            tvMensajeEmpty.setText("No hay reservas en el período " + rangoFechas);
        }

        Log.d(TAG, "✅ Estado vacío configurado");
    }

    private void mostrarError(RecyclerView recyclerView, LinearLayout emptyState,
                              TextView tvMensajeEmpty, String errorMsg) {
        Log.d(TAG, "=== MOSTRANDO ERROR ===");

        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);

        if (tvMensajeEmpty != null) {
            tvMensajeEmpty.setText("Error: " + errorMsg);
        }

        Log.d(TAG, "✅ Error mostrado: " + errorMsg);
    }

    // =============== PDF EXPORT METHODS ===============

    private void exportarPDF() {
        if (reportesList.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Para Android 10+ usar MediaStore (no necesita permisos)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            exportarPDFConMediaStore();
        } else {
            // Para versiones anteriores verificar permisos
            if (!verificarPermisos()) {
                solicitarPermisos();
                return;
            }
            exportarPDFLegacy();
        }
    }

    private void exportarPDFConMediaStore() {
        // Mostrar loading
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Generando PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generar PDF en hilo separado
        new Thread(() -> {
            try {
                String rutaArchivo = generarPDFConMediaStore();

                // Volver al hilo principal
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (rutaArchivo != null) {
                        Toast.makeText(getContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
                        mostrarDialogoExportacionExitosa(rutaArchivo);
                    } else {
                        Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error generando PDF", e);
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void exportarPDFLegacy() {
        // Código original para versiones anteriores
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Generando PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                String rutaArchivo = generarPDFReporte();

                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (rutaArchivo != null) {
                        mostrarDialogoExportacionExitosa(rutaArchivo);
                    } else {
                        Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error generando PDF", e);
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private String generarPDFConMediaStore() {
        try {
            // Nombre del archivo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "Reporte_Reservas_" + timestamp + ".pdf";

            // Usar MediaStore para guardar en Downloads
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TeleHotel");

            ContentResolver resolver = getContext().getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                Log.e(TAG, "No se pudo crear el URI para el archivo");
                return null;
            }

            // Escribir el PDF al URI
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream == null) {
                    Log.e(TAG, "No se pudo abrir el OutputStream");
                    return null;
                }

                // Crear el PDF
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument, PageSize.A4);

                // Configurar fuentes
                PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                // HEADER DEL DOCUMENTO
                agregarHeaderPDF(document, fontBold, fontRegular);

                // RESUMEN EJECUTIVO
                agregarResumenEjecutivoPDF(document, fontBold, fontRegular);

                // TABLA DE HOTELES
                agregarTablaHotelesPDF(document, fontBold, fontRegular);

                // FOOTER
                agregarFooterPDF(document, fontRegular);

                document.close();

                return "Downloads/TeleHotel/" + nombreArchivo;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creando PDF con MediaStore", e);
            return null;
        }
    }

    private boolean verificarPermisos() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void solicitarPermisos() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportarPDF();
            } else {
                Toast.makeText(getContext(), "Permiso requerido para guardar PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generarPDFReporte() {
        try {
            // Crear directorio
            File directorioDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File directorioTeleHotel = new File(directorioDescargas, "TeleHotel");
            if (!directorioTeleHotel.exists()) {
                directorioTeleHotel.mkdirs();
            }

            // Nombre del archivo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "Reporte_Reservas_" + timestamp + ".pdf";
            File archivoPDF = new File(directorioTeleHotel, nombreArchivo);

            // Crear el PDF
            PdfWriter writer = new PdfWriter(archivoPDF.getAbsolutePath());
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            // Configurar fuentes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // HEADER DEL DOCUMENTO
            agregarHeaderPDF(document, fontBold, fontRegular);

            // RESUMEN EJECUTIVO
            agregarResumenEjecutivoPDF(document, fontBold, fontRegular);

            // TABLA DE HOTELES
            agregarTablaHotelesPDF(document, fontBold, fontRegular);

            // FOOTER
            agregarFooterPDF(document, fontRegular);

            document.close();

            return archivoPDF.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error creando PDF", e);
            return null;
        }
    }

    private void agregarHeaderPDF(Document document, PdfFont fontBold, PdfFont fontRegular) throws Exception {
        // Título principal
        Paragraph titulo = new Paragraph("REPORTE DE RESERVAS POR HOTEL")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(titulo);

        // Subtítulo con fechas
        String rangoFechas = dateFormat.format(fechaInicioCalendar.getTime()) + " - " +
                dateFormat.format(fechaFinCalendar.getTime());
        Paragraph subtitulo = new Paragraph("Período: " + rangoFechas)
                .setFont(fontRegular)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitulo);

        // Fecha de generación
        String fechaGeneracion = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        Paragraph fechaGen = new Paragraph("Generado el: " + fechaGeneracion)
                .setFont(fontRegular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(fechaGen);
    }

    private void agregarResumenEjecutivoPDF(Document document, PdfFont fontBold, PdfFont fontRegular) throws Exception {
        // Título de sección
        Paragraph tituloResumen = new Paragraph("RESUMEN EJECUTIVO")
                .setFont(fontBold)
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(tituloResumen);

        // Calcular totales
        int totalReservas = reportesList.stream().mapToInt(ReporteHotel::getTotalReservas).sum();
        double totalIngresos = reportesList.stream().mapToDouble(ReporteHotel::getTotalIngresos).sum();
        double promedioGeneral = totalReservas > 0 ? totalIngresos / totalReservas : 0.0;

        // Crear tabla de resumen
        Table tablaResumen = new Table(new float[]{1, 1, 1});
        tablaResumen.setWidth(UnitValue.createPercentValue(100));

        // Headers
        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Total Reservas").setFont(fontBold)));
        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Total Ingresos").setFont(fontBold)));
        tablaResumen.addHeaderCell(new Cell().add(new Paragraph("Promedio/Reserva").setFont(fontBold)));

        // Datos
        tablaResumen.addCell(new Cell().add(new Paragraph(String.valueOf(totalReservas)).setFont(fontRegular)));
        tablaResumen.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", totalIngresos)).setFont(fontRegular)));
        tablaResumen.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", promedioGeneral)).setFont(fontRegular)));

        document.add(tablaResumen);
        document.add(new Paragraph("\n"));
    }

    private void agregarTablaHotelesPDF(Document document, PdfFont fontBold, PdfFont fontRegular) throws Exception {
        // Título de sección
        Paragraph tituloTabla = new Paragraph("DETALLE POR HOTEL")
                .setFont(fontBold)
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(tituloTabla);

        // Crear tabla
        Table tabla = new Table(new float[]{1, 2, 1, 1, 1, 1});
        tabla.setWidth(UnitValue.createPercentValue(100));

        // Headers
        tabla.addHeaderCell(new Cell().add(new Paragraph("#").setFont(fontBold)));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Hotel").setFont(fontBold)));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Reservas").setFont(fontBold)));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Ingresos").setFont(fontBold)));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Promedio").setFont(fontBold)));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Estado").setFont(fontBold)));

        // Datos
        for (int i = 0; i < reportesList.size(); i++) {
            ReporteHotel reporte = reportesList.get(i);

            tabla.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setFont(fontRegular)));
            tabla.addCell(new Cell().add(new Paragraph(reporte.getNombreHotel()).setFont(fontRegular)));
            tabla.addCell(new Cell().add(new Paragraph(String.valueOf(reporte.getTotalReservas())).setFont(fontRegular)));
            tabla.addCell(new Cell().add(new Paragraph(reporte.getTotalIngresosFormateado()).setFont(fontRegular)));
            tabla.addCell(new Cell().add(new Paragraph(reporte.getPromedioFormateado()).setFont(fontRegular)));
            tabla.addCell(new Cell().add(new Paragraph(reporte.getEstadoRendimiento()).setFont(fontRegular)));
        }

        document.add(tabla);
    }

    private void agregarFooterPDF(Document document, PdfFont fontRegular) throws Exception {
        document.add(new Paragraph("\n\n"));

        Paragraph footer = new Paragraph("Reporte generado por TeleHotel - Sistema de Gestión Hotelera")
                .setFont(fontRegular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);
    }

    private void mostrarDialogoExportacionExitosa(String rutaArchivo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("✅ PDF Generado")
                .setMessage("El reporte se ha exportado exitosamente.\n\nUbicación: " + rutaArchivo)
                .setPositiveButton("Abrir Carpeta", (dialog, which) -> abrirCarpetaDescargas())
                .setNegativeButton("Compartir", (dialog, which) -> compartirArchivoPDFGenerico())
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private void abrirCarpetaDescargas() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2FTeleHotel"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback: abrir la app de archivos
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, "Abrir con"));
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Busca el archivo en Downloads/TeleHotel", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void compartirArchivoPDFGenerico() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Reservas - TeleHotel");
            intent.putExtra(Intent.EXTRA_TEXT, "Reporte generado por TeleHotel\n\nBusca el archivo PDF en la carpeta Downloads/TeleHotel de tu dispositivo.");

            startActivity(Intent.createChooser(intent, "Compartir"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show();
        }
    }

    // Mantener los métodos originales para compatibilidad con versiones anteriores
    private void abrirArchivoPDF(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                Toast.makeText(getContext(), "Busca el archivo en Downloads/TeleHotel", Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".provider", archivo);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Abrir PDF"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartirArchivoPDF(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                compartirArchivoPDFGenerico();
                return;
            }

            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".provider", archivo);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Reservas - TeleHotel");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Compartir PDF"));
        } catch (Exception e) {
            compartirArchivoPDFGenerico();
        }
    }

    // =============== UI HELPER METHODS ===============

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            progressBar.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            btnGenerarReporte.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            btnGenerarReporte.setEnabled(true);
        }
    }

    // =============== MÉTODO DE EMERGENCIA ===============

    // EMERGENCIA: Forzar dimensiones fijas si el layout falla
    @SuppressWarnings("unused")
    private void emergencyFixRecyclerView(RecyclerView recyclerView) {
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = 800; // Altura fija temporal de 800dp
            recyclerView.setLayoutParams(params);
            recyclerView.requestLayout();

            Log.d(TAG, "🚨 EMERGENCIA: Dimensiones fijas aplicadas");
        }
    }
    // =============== MÉTODO DE EMERGENCIA - AGREGA ESTO TEMPORALMENTE ===============

    // AGREGA ESTE MÉTODO A TU ReporteReservasFragment
    private void fixEmergenciaTabReservas(AlertDialog dialog) {
        Log.d(TAG, "🚨 === FIX DE EMERGENCIA PARA TAB RESERVAS ===");

        // Buscar el tab de reservas en el diálogo
        View dialogView = dialog.findViewById(android.R.id.content);
        if (dialogView != null) {
            FrameLayout tabReservas = dialogView.findViewById(R.id.tabReservas);
            TabLayout tabLayout = dialogView.findViewById(R.id.tabLayoutDetalles);

            Log.d(TAG, "Componentes encontrados:");
            Log.d(TAG, "  - tabReservas: " + (tabReservas != null ? "✅" : "❌"));
            Log.d(TAG, "  - tabLayout: " + (tabLayout != null ? "✅" : "❌"));

            if (tabReservas != null && tabLayout != null) {
                // CONFIGURAR LISTENER SIMPLE Y DIRECTO
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        Log.d(TAG, "🎯 EMERGENCIA - Tab seleccionado: " + position);

                        if (position == 1) { // Tab Reservas
                            Log.d(TAG, "🚨 FORZANDO VISIBILIDAD TAB RESERVAS");

                            // HACER VISIBLE EL TAB INMEDIATAMENTE
                            tabReservas.setVisibility(View.VISIBLE);

                            Log.d(TAG, "Estado después de forzar:");
                            Log.d(TAG, "  - Visibility: " + getVisibilityString(tabReservas.getVisibility()));
                            Log.d(TAG, "  - Dimensions: " + tabReservas.getWidth() + "x" + tabReservas.getHeight());

                            // BUSCAR Y FORZAR RECYCLERVIEW
                            RecyclerView recyclerView = tabReservas.findViewById(R.id.recyclerViewReservasDetalle);
                            if (recyclerView != null) {
                                Log.d(TAG, "RecyclerView encontrado, forzando layout...");

                                // Post para asegurar que el parent ya esté visible
                                tabReservas.post(() -> {
                                    // Verificar que el parent ahora tenga dimensiones
                                    int parentWidth = tabReservas.getWidth();
                                    int parentHeight = tabReservas.getHeight();

                                    Log.d(TAG, "Dimensiones del parent después de hacerlo visible: " + parentWidth + "x" + parentHeight);

                                    if (parentWidth > 0 && parentHeight > 0) {
                                        // Aplicar dimensiones al RecyclerView
                                        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                        recyclerView.setLayoutParams(params);

                                        recyclerView.requestLayout();
                                        recyclerView.setVisibility(View.VISIBLE);

                                        Log.d(TAG, "✅ RecyclerView configurado");
                                    } else {
                                        Log.w(TAG, "⚠️ Parent aún sin dimensiones, usando dimensiones fijas");

                                        // ÚLTIMO RECURSO: dimensiones fijas
                                        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                        params.height = 800; // 800dp fijos
                                        recyclerView.setLayoutParams(params);

                                        recyclerView.requestLayout();
                                        recyclerView.setVisibility(View.VISIBLE);

                                        Log.d(TAG, "🚨 DIMENSIONES FIJAS APLICADAS");
                                    }

                                    // Debug final
                                    recyclerView.postDelayed(() -> {
                                        debugRecyclerViewFinal(recyclerView, "EMERGENCIA - DESPUÉS DE FIX");
                                    }, 300);
                                });
                            } else {
                                Log.e(TAG, "❌ RecyclerView no encontrado en tabReservas");
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

                Log.d(TAG, "✅ Listener de emergencia configurado");

            } else {
                Log.e(TAG, "❌ No se encontraron los componentes necesarios");
            }
        } else {
            Log.e(TAG, "❌ No se pudo acceder al contenido del diálogo");
        }
    }

    // =============== LIFECYCLE METHODS ===============

    @Override
    public void onResume() {
        super.onResume();
        // Regenerar reporte si es necesario
    }
}