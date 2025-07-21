/*package com.example.telehotel.features.superadmin.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.superadmin.adapters.ReporteHotelAdapter;
import com.example.telehotel.data.model.ReporteHotel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReporteReservasFragment extends Fragment {

    private static final String TAG = "ReporteReservasFragment";

    // Views
    private TextInputEditText etFechaInicio, etFechaFin;
    private MaterialButton btnGenerarReporte, btnExportarPDF;
    private RecyclerView recyclerViewReporte;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage, tvTotalReservas, tvTotalIngresos, tvRangoFechas;

    // Data
    private ReporteHotelAdapter adapter;
    private List<ReporteHotel> reportesList;
    private FirebaseFirestore db;
    private Map<String, Hotel> hotelesMap;

    // Fechas
    private Calendar fechaInicioCalendar;
    private Calendar fechaFinCalendar;
    private SimpleDateFormat dateFormat;

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

    private void exportarPDF() {
        if (reportesList.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implementar exportación a PDF
        Toast.makeText(getContext(), "Función de exportar PDF en desarrollo", Toast.LENGTH_SHORT).show();
    }

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

    @Override
    public void onResume() {
        super.onResume();
        // Regenerar reporte si es necesario
    }
}*/
/*package com.example.telehotel.features.superadmin.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.Query;
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
        // Crear el diálogo de detalles
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_hotel_detalles, null);

        // Configurar el diálogo
        AlertDialog dialog = builder.setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar el contenido del diálogo
        configurarDialogoDetalles(dialogView, reporte, dialog);

        // Mostrar el diálogo en pantalla completa
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    private void configurarDialogoDetalles(View dialogView, ReporteHotel reporte, AlertDialog dialog) {
        // Referencias a las vistas
        TextView tvNombreHotel = dialogView.findViewById(R.id.tvNombreHotelDetalle);
        TextView tvUbicacionHotel = dialogView.findViewById(R.id.tvUbicacionHotelDetalle);
        TextView tvPeriodo = dialogView.findViewById(R.id.tvPeriodoDetalle);
        TextView tvTotalReservas = dialogView.findViewById(R.id.tvTotalReservasDetalle);
        TextView tvTotalIngresos = dialogView.findViewById(R.id.tvTotalIngresosDetalle);
        TextView tvPromedio = dialogView.findViewById(R.id.tvPromedioDetalle);
        TextView tvTasaOcupacion = dialogView.findViewById(R.id.tvTasaOcupacion);
        TextView tvDescripcionHotel = dialogView.findViewById(R.id.tvDescripcionHotel);
        TextView tvServiciosHotel = dialogView.findViewById(R.id.tvServiciosHotel);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarDetalle);

        // TabLayout y contenido
        TabLayout tabLayout = dialogView.findViewById(R.id.tabLayoutDetalles);
        LinearLayout tabResumen = dialogView.findViewById(R.id.tabResumen);
        LinearLayout tabReservas = dialogView.findViewById(R.id.tabReservas);
        LinearLayout tabTendencias = dialogView.findViewById(R.id.tabTendencias);
        RecyclerView recyclerReservas = dialogView.findViewById(R.id.recyclerViewReservasDetalle);

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

        // Información del hotel (placeholder)
        tvDescripcionHotel.setText("Hotel ubicado en " + reporte.getUbicacionHotel() + " con excelentes servicios.");
        tvServiciosHotel.setText("Servicios: WiFi, Restaurante, Recepción 24h, Limpieza diaria");

        // Configurar tabs
        configurarTabsDetalles(tabLayout, tabResumen, tabReservas, tabTendencias);

        // Cargar reservas específicas del hotel
        cargarReservasHotel(reporte.getHotelId(), recyclerReservas);

        // Botón cerrar
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
    }

    private void configurarTabsDetalles(TabLayout tabLayout, LinearLayout tabResumen,
                                        LinearLayout tabReservas, LinearLayout tabTendencias) {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Ocultar todos los tabs
                tabResumen.setVisibility(View.GONE);
                tabReservas.setVisibility(View.GONE);
                tabTendencias.setVisibility(View.GONE);

                // Mostrar el tab seleccionado
                switch (tab.getPosition()) {
                    case 0:
                        tabResumen.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        tabReservas.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        tabTendencias.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cargarReservasHotel(String hotelId, RecyclerView recyclerView) {
        long fechaInicioTimestamp = fechaInicioCalendar.getTimeInMillis();
        long fechaFinTimestamp = fechaFinCalendar.getTimeInMillis();

        db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .whereGreaterThanOrEqualTo("fechaInicio", fechaInicioTimestamp)
                .whereLessThanOrEqualTo("fechaInicio", fechaFinTimestamp)
                .orderBy("fechaInicio", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reserva> reservasHotel = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reserva reserva = document.toObject(Reserva.class);
                        reserva.setId(document.getId());
                        reservasHotel.add(reserva);
                    }

                    // Configurar adapter para las reservas
                    ReservaDetalleAdapter reservaAdapter = new ReservaDetalleAdapter(getContext(), reservasHotel);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(reservaAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas del hotel", e);
                    Toast.makeText(getContext(), "Error cargando reservas detalladas", Toast.LENGTH_SHORT).show();
                });
    }

    // =============== PDF EXPORT METHODS ===============

    private void exportarPDF() {
        if (reportesList.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar permisos
        if (!verificarPermisos()) {
            solicitarPermisos();
            return;
        }

        // Mostrar loading
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Generando PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generar PDF en hilo separado
        new Thread(() -> {
            try {
                String rutaArchivo = generarPDFReporte();

                // Volver al hilo principal
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
                .setPositiveButton("Abrir", (dialog, which) -> abrirArchivoPDF(rutaArchivo))
                .setNegativeButton("Compartir", (dialog, which) -> compartirArchivoPDF(rutaArchivo))
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private void abrirArchivoPDF(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
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
            Uri uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".provider", archivo);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Reservas - TeleHotel");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Compartir PDF"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se pudo compartir el archivo", Toast.LENGTH_SHORT).show();
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

    // =============== LIFECYCLE METHODS ===============

    @Override
    public void onResume() {
        super.onResume();
        // Regenerar reporte si es necesario
    }
}*/
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
import com.google.firebase.firestore.Query;
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
        // Crear el diálogo de detalles
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_hotel_detalles, null);

        // Configurar el diálogo
        AlertDialog dialog = builder.setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar el contenido del diálogo
        configurarDialogoDetalles(dialogView, reporte, dialog);

        // Mostrar el diálogo en pantalla completa
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
    }


    private void configurarDialogoDetalles(View dialogView, ReporteHotel reporte, AlertDialog dialog) {
        // Referencias a las vistas
        TextView tvNombreHotel = dialogView.findViewById(R.id.tvNombreHotelDetalle);
        TextView tvUbicacionHotel = dialogView.findViewById(R.id.tvUbicacionHotelDetalle);
        TextView tvPeriodo = dialogView.findViewById(R.id.tvPeriodoDetalle);
        TextView tvTotalReservas = dialogView.findViewById(R.id.tvTotalReservasDetalle);
        TextView tvTotalIngresos = dialogView.findViewById(R.id.tvTotalIngresosDetalle);
        TextView tvPromedio = dialogView.findViewById(R.id.tvPromedioDetalle);
        TextView tvTasaOcupacion = dialogView.findViewById(R.id.tvTasaOcupacion);
        TextView tvDescripcionHotel = dialogView.findViewById(R.id.tvDescripcionHotel);
        TextView tvServiciosHotel = dialogView.findViewById(R.id.tvServiciosHotel);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrarDetalle);

        // TabLayout y contenido - CAMBIO: usar View en lugar de LinearLayout
        TabLayout tabLayout = dialogView.findViewById(R.id.tabLayoutDetalles);
        View tabResumen = dialogView.findViewById(R.id.tabResumen);           // ✅ View
        View tabReservas = dialogView.findViewById(R.id.tabReservas);         // ✅ View
        View tabTendencias = dialogView.findViewById(R.id.tabTendencias);     // ✅ View
        RecyclerView recyclerReservas = dialogView.findViewById(R.id.recyclerViewReservasDetalle);
        LinearLayout emptyStateReservas = dialogView.findViewById(R.id.emptyStateReservas);
        TextView tvMensajeEmpty = dialogView.findViewById(R.id.tvMensajeEmptyReservas);

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

        // Información del hotel (placeholder)
        tvDescripcionHotel.setText("Hotel ubicado en " + reporte.getUbicacionHotel() + " con excelentes servicios.");
        tvServiciosHotel.setText("Servicios: WiFi, Restaurante, Recepción 24h, Limpieza diaria");

        // Configurar tabs - CAMBIO: pasar View en lugar de LinearLayout
        configurarTabsDetalles(tabLayout, tabResumen, tabReservas, tabTendencias);

        // Cargar reservas específicas del hotel
        cargarReservasHotelConEmptyState(reporte.getHotelId(), recyclerReservas, emptyStateReservas, tvMensajeEmpty, rangoFechas);

        // Botón cerrar
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
    }

    private void cargarReservasHotelConEmptyState(String hotelId, RecyclerView recyclerView,
                                                  LinearLayout emptyState, TextView tvMensajeEmpty, String rangoFechas) {
        Log.d(TAG, "=== DEBUGGING RESERVAS HOTEL ===");
        Log.d(TAG, "Hotel ID: " + hotelId);

        db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total documentos encontrados: " + queryDocumentSnapshots.size());

                    List<Reserva> reservasFiltradas = new ArrayList<>();
                    long fechaInicioTimestamp = fechaInicioCalendar.getTimeInMillis();
                    long fechaFinTimestamp = fechaFinCalendar.getTimeInMillis();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Intentar mapear la reserva de forma más robusta
                            Map<String, Object> data = document.getData();

                            Reserva reserva = new Reserva();
                            reserva.setId(document.getId());

                            // Mapear campos básicos de forma segura
                            if (data.containsKey("clienteNombre")) {
                                reserva.setClienteNombre(String.valueOf(data.get("clienteNombre")));
                            }
                            if (data.containsKey("clienteEmail")) {
                                reserva.setClienteEmail(String.valueOf(data.get("clienteEmail")));
                            }
                            if (data.containsKey("clienteId")) {
                                reserva.setClienteId(String.valueOf(data.get("clienteId")));
                            }
                            if (data.containsKey("hotelId")) {
                                reserva.setHotelId(String.valueOf(data.get("hotelId")));
                            }
                            if (data.containsKey("habitacionNumero")) {
                                reserva.setHabitacionNumero(String.valueOf(data.get("habitacionNumero")));
                            }
                            if (data.containsKey("habitacionTipo")) {
                                reserva.setHabitacionTipo(String.valueOf(data.get("habitacionTipo")));
                            }
                            if (data.containsKey("estado")) {
                                reserva.setEstado(String.valueOf(data.get("estado")));
                            }
                            if (data.containsKey("montoTotal")) {
                                Object monto = data.get("montoTotal");
                                if (monto instanceof Number) {
                                    reserva.setMontoTotal(((Number) monto).doubleValue());
                                }
                            }
                            if (data.containsKey("fechaInicio")) {
                                Object fecha = data.get("fechaInicio");
                                if (fecha instanceof Number) {
                                    reserva.setFechaInicio(((Number) fecha).longValue());
                                }
                            }
                            if (data.containsKey("fechaFin")) {
                                Object fecha = data.get("fechaFin");
                                if (fecha instanceof Number) {
                                    reserva.setFechaFin(((Number) fecha).longValue());
                                }
                            }

                            Log.d(TAG, "Reserva mapeada ID: " + document.getId());
                            Log.d(TAG, "  - Cliente: " + reserva.getClienteNombre());
                            Log.d(TAG, "  - Estado: " + reserva.getEstado());
                            Log.d(TAG, "  - Monto: " + reserva.getMontoTotal());
                            Log.d(TAG, "  - Fecha inicio: " + reserva.getFechaInicio());

                            // Filtrar fechas
                            Long fechaReserva = reserva.getFechaInicio();
                            if (fechaReserva != null) {
                                if (fechaReserva >= fechaInicioTimestamp && fechaReserva <= fechaFinTimestamp) {
                                    reservasFiltradas.add(reserva);
                                    Log.d(TAG, "  - ✅ INCLUIDA en filtro");
                                } else {
                                    Log.d(TAG, "  - ❌ EXCLUIDA por filtro de fechas");
                                }
                            } else {
                                // Si no hay fechaInicio, incluir la reserva para debug
                                reservasFiltradas.add(reserva);
                                Log.d(TAG, "  - ⚠️ INCLUIDA - fechaInicio es null");
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error mapeando reserva " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Reservas después de filtro: " + reservasFiltradas.size());

                    // Mostrar resultados
                    if (reservasFiltradas.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                        tvMensajeEmpty.setText("No hay reservas en el período " + rangoFechas);
                        Log.w(TAG, "⚠️ Mostrando estado vacío");
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        // Ordenar por fecha
                        reservasFiltradas.sort((a, b) -> {
                            Long fechaA = a.getFechaInicio();
                            Long fechaB = b.getFechaInicio();

                            if (fechaA == null && fechaB == null) return 0;
                            if (fechaA == null) return 1;
                            if (fechaB == null) return -1;

                            return fechaB.compareTo(fechaA);
                        });

                        // Configurar adapter
                        ReservaDetalleAdapter reservaAdapter = new ReservaDetalleAdapter(getContext(), reservasFiltradas);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(reservaAdapter);

                        Log.d(TAG, "✅ Adapter configurado con " + reservasFiltradas.size() + " reservas");
                    }

                    Log.d(TAG, "=== FIN DEBUG RESERVAS HOTEL ===");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas del hotel", e);
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                    tvMensajeEmpty.setText("Error cargando reservas: " + e.getMessage());
                });
    }

    // TAMBIÉN ACTUALIZA EL MÉTODO configurarTabsDetalles:
    private void configurarTabsDetalles(TabLayout tabLayout, View tabResumen,
                                        View tabReservas, View tabTendencias) {
        Log.d(TAG, "=== CONFIGURANDO TABS ===");

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab seleccionado: " + tab.getPosition());

                // Ocultar todos los tabs
                tabResumen.setVisibility(View.GONE);
                tabReservas.setVisibility(View.GONE);
                tabTendencias.setVisibility(View.GONE);

                // Mostrar el tab seleccionado
                switch (tab.getPosition()) {
                    case 0:
                        tabResumen.setVisibility(View.VISIBLE);
                        Log.d(TAG, "✅ Tab Resumen visible");
                        break;
                    case 1:
                        tabReservas.setVisibility(View.VISIBLE);
                        Log.d(TAG, "✅ Tab Reservas visible");

                        // IMPORTANTE: Cuando se muestra el tab de reservas,
                        // forzar un refresh del RecyclerView
                        tabReservas.post(() -> {
                            RecyclerView recyclerView = tabReservas.findViewById(R.id.recyclerViewReservasDetalle);
                            if (recyclerView != null && recyclerView.getAdapter() != null) {
                                Log.d(TAG, "🔄 Refrescando RecyclerView en tab change");
                                recyclerView.getAdapter().notifyDataSetChanged();
                                recyclerView.requestLayout();
                            }
                        });
                        break;
                    case 2:
                        tabTendencias.setVisibility(View.VISIBLE);
                        Log.d(TAG, "✅ Tab Tendencias visible");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab deseleccionado: " + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab reseleccionado: " + tab.getPosition());
            }
        });

        Log.d(TAG, "✅ Tabs configurados correctamente");
    }

    private void cargarReservasHotel(String hotelId, RecyclerView recyclerView) {
        Log.d(TAG, "=== DEBUGGING RESERVAS HOTEL ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "Fecha inicio timestamp: " + fechaInicioCalendar.getTimeInMillis());
        Log.d(TAG, "Fecha fin timestamp: " + fechaFinCalendar.getTimeInMillis());

        // SOLUCIÓN: Solo filtrar por hotel, luego filtrar fechas en el cliente
        db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .get() // Sin filtros de fecha para evitar índice compuesto
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total documentos encontrados: " + queryDocumentSnapshots.size());

                    List<Reserva> todasLasReservas = new ArrayList<>();
                    List<Reserva> reservasFiltradas = new ArrayList<>();
                    long fechaInicioTimestamp = fechaInicioCalendar.getTimeInMillis();
                    long fechaFinTimestamp = fechaFinCalendar.getTimeInMillis();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reserva reserva = document.toObject(Reserva.class);
                        reserva.setId(document.getId());
                        todasLasReservas.add(reserva);

                        // Log de cada reserva para debug
                        Log.d(TAG, "Reserva ID: " + document.getId());
                        Log.d(TAG, "  - Cliente: " + reserva.getClienteNombre());
                        Log.d(TAG, "  - Hotel ID en reserva: " + reserva.getHotelId());
                        Log.d(TAG, "  - Fecha inicio: " + reserva.getFechaInicio());
                        Log.d(TAG, "  - Estado: " + reserva.getEstado());

                        // Filtrar fechas en el cliente
                        Long fechaReserva = reserva.getFechaInicio();
                        if (fechaReserva != null) {
                            Log.d(TAG, "  - Comparando fecha: " + fechaReserva + " con rango [" + fechaInicioTimestamp + " - " + fechaFinTimestamp + "]");
                            if (fechaReserva >= fechaInicioTimestamp && fechaReserva <= fechaFinTimestamp) {
                                reservasFiltradas.add(reserva);
                                Log.d(TAG, "  - ✅ INCLUIDA en filtro");
                            } else {
                                Log.d(TAG, "  - ❌ EXCLUIDA por filtro de fechas");
                            }
                        } else {
                            Log.d(TAG, "  - ❌ EXCLUIDA - fechaInicio es null");
                        }
                    }

                    Log.d(TAG, "Reservas después de filtro de fechas: " + reservasFiltradas.size());

                    // Si no hay reservas filtradas, mostrar todas para debug
                    List<Reserva> reservasParaMostrar;
                    if (reservasFiltradas.isEmpty() && !todasLasReservas.isEmpty()) {
                        Log.w(TAG, "⚠️ No hay reservas en el rango de fechas, mostrando todas las reservas del hotel para debug");
                        reservasParaMostrar = todasLasReservas;
                    } else {
                        reservasParaMostrar = reservasFiltradas;
                    }

                    // Ordenar manualmente en el cliente por fecha (más recientes primero)
                    reservasParaMostrar.sort((a, b) -> {
                        Long fechaA = a.getFechaInicio();
                        Long fechaB = b.getFechaInicio();

                        if (fechaA == null && fechaB == null) return 0;
                        if (fechaA == null) return 1;
                        if (fechaB == null) return -1;

                        return fechaB.compareTo(fechaA); // Orden descendente (más recientes primero)
                    });

                    // Configurar adapter para las reservas
                    ReservaDetalleAdapter reservaAdapter = new ReservaDetalleAdapter(getContext(), reservasParaMostrar);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(reservaAdapter);

                    Log.d(TAG, "✅ Adapter configurado con " + reservasParaMostrar.size() + " reservas");
                    Log.d(TAG, "=== FIN DEBUG RESERVAS HOTEL ===");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas del hotel", e);
                    Toast.makeText(getContext(), "Error cargando reservas detalladas", Toast.LENGTH_SHORT).show();
                });
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

    // =============== LIFECYCLE METHODS ===============

    @Override
    public void onResume() {
        super.onResume();
        // Regenerar reporte si es necesario
    }
}