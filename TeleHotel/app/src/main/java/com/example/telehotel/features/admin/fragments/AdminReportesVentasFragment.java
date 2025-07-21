package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.core.utils.LogUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.*;

public class AdminReportesVentasFragment extends Fragment {

    // UI Components
    private TextView tvTotalVentas, tvTotalReservas;
    private TextView tvEmptyServicios, tvEmptyClientes;
    private RecyclerView rvReporteServicios, rvReporteClientes;
    private ProgressBar progressBar;
    private Button btnHoy, btnSemana, btnMes, btnTodo, btnActualizarReportes;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Data
    private String adminId;
    private String adminName;
    private String hotelId;
    private int diasSeleccionados = -1; // -1 = todo, 0 = hoy, 7 = semana, 30 = mes

    // Adapters
    private ReporteServiciosAdapter serviciosAdapter;
    private ReporteClientesAdapter clientesAdapter;

    // Lists
    private List<ReporteServicio> reporteServicios = new ArrayList<>();
    private List<ReporteCliente> reporteClientes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reportes_ventas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos del admin
        PrefsManager prefsManager = new PrefsManager(requireContext());
        adminId = prefsManager.getUserId();
        adminName = prefsManager.getUserName();

        initFirebase();
        initViews(view);
        setupListeners();
        setupRecyclerViews();

        // LOG: Acceso a reportes
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Accedió a reportes de ventas (Admin: " + adminName + ")"
        );

        // Configurar flecha de regreso
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Cargar datos iniciales
        obtenerHotelId();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        tvTotalVentas = view.findViewById(R.id.tvTotalVentas);
        tvTotalReservas = view.findViewById(R.id.tvTotalReservas);
        tvEmptyServicios = view.findViewById(R.id.tvEmptyServicios);
        tvEmptyClientes = view.findViewById(R.id.tvEmptyClientes);
        rvReporteServicios = view.findViewById(R.id.rvReporteServicios);
        rvReporteClientes = view.findViewById(R.id.rvReporteClientes);
        progressBar = view.findViewById(R.id.progressBar);

        btnHoy = view.findViewById(R.id.btnHoy);
        btnSemana = view.findViewById(R.id.btnSemana);
        btnMes = view.findViewById(R.id.btnMes);
        btnTodo = view.findViewById(R.id.btnTodo);
        btnActualizarReportes = view.findViewById(R.id.btnActualizarReportes);

        // Seleccionar "Todo" por defecto
        actualizarBotonesSeleccion(btnTodo);
    }

    private void setupListeners() {
        btnHoy.setOnClickListener(v -> {
            diasSeleccionados = 0;
            actualizarBotonesSeleccion(btnHoy);
        });

        btnSemana.setOnClickListener(v -> {
            diasSeleccionados = 7;
            actualizarBotonesSeleccion(btnSemana);
        });

        btnMes.setOnClickListener(v -> {
            diasSeleccionados = 30;
            actualizarBotonesSeleccion(btnMes);
        });

        btnTodo.setOnClickListener(v -> {
            diasSeleccionados = -1;
            actualizarBotonesSeleccion(btnTodo);
        });

        btnActualizarReportes.setOnClickListener(v -> {
            if (hotelId != null) {
                cargarReportes();
            }
        });
    }

    private void setupRecyclerViews() {
        // Adapter para servicios
        serviciosAdapter = new ReporteServiciosAdapter(reporteServicios);
        rvReporteServicios.setAdapter(serviciosAdapter);
        rvReporteServicios.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter para clientes
        clientesAdapter = new ReporteClientesAdapter(reporteClientes);
        rvReporteClientes.setAdapter(clientesAdapter);
        rvReporteClientes.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void actualizarBotonesSeleccion(Button botonSeleccionado) {
        // Resetear todos los botones
        Button[] botones = {btnHoy, btnSemana, btnMes, btnTodo};
        for (Button btn : botones) {
            btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorSurface));
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary));
        }

        // Marcar el seleccionado
        botonSeleccionado.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primaryColor));
        botonSeleccionado.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void obtenerHotelId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarProgreso(true);

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    hotelId = userDoc.getString("hotelAsignado");
                    if (hotelId != null) {
                        cargarReportes();
                    } else {
                        mostrarProgreso(false);
                        Toast.makeText(getContext(), "Error: Sin hotel asignado", Toast.LENGTH_SHORT).show();
                        LogUtils.logError("Admin sin hotel asignado al generar reportes", adminId);
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "Error al obtener datos del hotel", Toast.LENGTH_SHORT).show();
                    LogUtils.logError("Error al obtener hotel asignado para reportes: " + e.getMessage(), adminId);
                });
    }

    private void cargarReportes() {
        if (hotelId == null) return;

        mostrarProgreso(true);

        // LOG: Generación de reportes
        String periodoTexto = diasSeleccionados == -1 ? "todo el período" :
                diasSeleccionados == 0 ? "hoy" : diasSeleccionados + " días";
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Generó reportes de ventas para " + periodoTexto + " - Hotel: " + hotelId
        );

        // Calcular fecha límite
        long fechaLimite = 0;
        if (diasSeleccionados >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -diasSeleccionados);
            fechaLimite = cal.getTimeInMillis();
        }

        // Query base para reservas del hotel
        Query query = db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("estado", "completada");

        // Agregar filtro de fecha si es necesario
        if (diasSeleccionados >= 0) {
            query = query.whereGreaterThan("fechaReservaTimestamp", fechaLimite);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    procesarReservas(queryDocumentSnapshots);
                    mostrarProgreso(false);
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "Error al cargar reportes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    LogUtils.logError("Error al cargar reportes de ventas: " + e.getMessage(), adminId);
                });
    }

    private void procesarReservas(QuerySnapshot queryDocumentSnapshots) {
        // Limpiar datos anteriores
        reporteServicios.clear();
        reporteClientes.clear();

        // Maps para acumular datos
        Map<String, ReporteServicio> serviciosMap = new HashMap<>();
        Map<String, ReporteCliente> clientesMap = new HashMap<>();

        double totalVentas = 0;
        int totalReservas = queryDocumentSnapshots.size();

        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            try {
                // Procesar monto total
                Double montoTotal = doc.getDouble("montoTotal");
                if (montoTotal != null) {
                    totalVentas += montoTotal;
                }

                // Procesar servicios adicionales
                List<Map<String, Object>> serviciosAdicionales =
                        (List<Map<String, Object>>) doc.get("serviciosAdicionales");

                if (serviciosAdicionales != null) {
                    for (Map<String, Object> servicio : serviciosAdicionales) {
                        String servicioId = (String) servicio.get("servicioId");
                        Integer cantidad = ((Long) servicio.get("cantidad")).intValue();
                        Double precio = (Double) servicio.get("precio");

                        if (servicioId != null && cantidad != null && precio != null) {
                            double totalServicio = cantidad * precio;

                            ReporteServicio reporteServicio = serviciosMap.get(servicioId);
                            if (reporteServicio == null) {
                                reporteServicio = new ReporteServicio();
                                reporteServicio.servicioId = servicioId;
                                reporteServicio.nombreServicio = "Servicio " + servicioId; // Se actualizará después
                                reporteServicio.totalVentas = 0;
                                reporteServicio.cantidadVendida = 0;
                                serviciosMap.put(servicioId, reporteServicio);
                            }

                            reporteServicio.totalVentas += totalServicio;
                            reporteServicio.cantidadVendida += cantidad;
                        }
                    }
                }

                // Procesar cliente
                String clienteId = doc.getString("clienteId");
                String clienteNombre = doc.getString("clienteNombre");

                if (clienteId != null) {
                    ReporteCliente reporteCliente = clientesMap.get(clienteId);
                    if (reporteCliente == null) {
                        reporteCliente = new ReporteCliente();
                        reporteCliente.clienteId = clienteId;
                        reporteCliente.nombreCliente = clienteNombre != null ? clienteNombre : "Cliente " + clienteId;
                        reporteCliente.totalGastado = 0;
                        reporteCliente.numeroReservas = 0;
                        clientesMap.put(clienteId, reporteCliente);
                    }

                    if (montoTotal != null) {
                        reporteCliente.totalGastado += montoTotal;
                    }
                    reporteCliente.numeroReservas++;
                }

            } catch (Exception e) {
                Log.e("ReportesVentas", "Error procesando reserva: " + doc.getId(), e);
            }
        }

        // Convertir maps a listas y ordenar
        reporteServicios.addAll(serviciosMap.values());
        reporteClientes.addAll(clientesMap.values());

        // Ordenar servicios (menor a mayor)
        Collections.sort(reporteServicios, (s1, s2) -> Double.compare(s1.totalVentas, s2.totalVentas));

        // Ordenar clientes (mayor a menor)
        Collections.sort(reporteClientes, (c1, c2) -> Double.compare(c2.totalGastado, c1.totalGastado));

        // Obtener nombres de servicios
        if (!reporteServicios.isEmpty()) {
            obtenerNombresServicios();
        }

        // Actualizar UI
        actualizarResumenGeneral(totalVentas, totalReservas);
        actualizarRecyclerViews();

        // LOG: Reportes generados exitosamente
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Generó reportes exitosamente - Total ventas: S/ " + String.format("%.2f", totalVentas) +
                        " - Reservas: " + totalReservas + " - Servicios: " + reporteServicios.size() +
                        " - Clientes: " + reporteClientes.size()
        );
    }

    private void obtenerNombresServicios() {
        // Obtener IDs únicos de servicios
        Set<String> servicioIds = new HashSet<>();
        for (ReporteServicio reporte : reporteServicios) {
            servicioIds.add(reporte.servicioId);
        }

        // Consultar nombres de servicios
        for (String servicioId : servicioIds) {
            db.collection("servicios").document(servicioId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                // Actualizar nombre en la lista
                                for (ReporteServicio reporte : reporteServicios) {
                                    if (reporte.servicioId.equals(servicioId)) {
                                        reporte.nombreServicio = nombre;
                                    }
                                }
                                serviciosAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ReportesVentas", "Error obteniendo nombre de servicio: " + servicioId, e);
                    });
        }
    }

    private void actualizarResumenGeneral(double totalVentas, int totalReservas) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvTotalVentas.setText("S/ " + df.format(totalVentas));
        tvTotalReservas.setText(String.valueOf(totalReservas));
    }

    private void actualizarRecyclerViews() {
        // Servicios
        if (reporteServicios.isEmpty()) {
            tvEmptyServicios.setVisibility(View.VISIBLE);
            rvReporteServicios.setVisibility(View.GONE);
        } else {
            tvEmptyServicios.setVisibility(View.GONE);
            rvReporteServicios.setVisibility(View.VISIBLE);
            serviciosAdapter.notifyDataSetChanged();
        }

        // Clientes
        if (reporteClientes.isEmpty()) {
            tvEmptyClientes.setVisibility(View.VISIBLE);
            rvReporteClientes.setVisibility(View.GONE);
        } else {
            tvEmptyClientes.setVisibility(View.GONE);
            rvReporteClientes.setVisibility(View.VISIBLE);
            clientesAdapter.notifyDataSetChanged();
        }
    }

    private void mostrarProgreso(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    // ========== CLASES MODELO ==========

    public static class ReporteServicio {
        public String servicioId;
        public String nombreServicio;
        public double totalVentas;
        public int cantidadVendida;
    }

    public static class ReporteCliente {
        public String clienteId;
        public String nombreCliente;
        public double totalGastado;
        public int numeroReservas;
    }

    // ========== ADAPTER PARA SERVICIOS ==========

    public static class ReporteServiciosAdapter extends RecyclerView.Adapter<ReporteServiciosAdapter.ServicioViewHolder> {
        private List<ReporteServicio> servicios;
        private DecimalFormat df = new DecimalFormat("#,##0.00");

        public ReporteServiciosAdapter(List<ReporteServicio> servicios) {
            this.servicios = servicios;
        }

        @NonNull
        @Override
        public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reporte_servicio, parent, false);
            return new ServicioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
            ReporteServicio servicio = servicios.get(position);

            holder.tvNombreServicio.setText(servicio.nombreServicio);
            holder.tvTotalVentas.setText("S/ " + df.format(servicio.totalVentas));
            holder.tvCantidadVendida.setText(servicio.cantidadVendida + " unidades");

            // Mostrar posición (menor a mayor)
            holder.tvPosicion.setText(String.valueOf(position + 1));
        }

        @Override
        public int getItemCount() {
            return servicios.size();
        }

        public static class ServicioViewHolder extends RecyclerView.ViewHolder {
            TextView tvPosicion, tvNombreServicio, tvTotalVentas, tvCantidadVendida;

            public ServicioViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosicion = itemView.findViewById(R.id.tvPosicion);
                tvNombreServicio = itemView.findViewById(R.id.tvNombreServicio);
                tvTotalVentas = itemView.findViewById(R.id.tvTotalVentas);
                tvCantidadVendida = itemView.findViewById(R.id.tvCantidadVendida);
            }
        }
    }

    // ========== ADAPTER PARA CLIENTES ==========

    public static class ReporteClientesAdapter extends RecyclerView.Adapter<ReporteClientesAdapter.ClienteViewHolder> {
        private List<ReporteCliente> clientes;
        private DecimalFormat df = new DecimalFormat("#,##0.00");

        public ReporteClientesAdapter(List<ReporteCliente> clientes) {
            this.clientes = clientes;
        }

        @NonNull
        @Override
        public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reporte_cliente, parent, false);
            return new ClienteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
            ReporteCliente cliente = clientes.get(position);

            holder.tvNombreCliente.setText(cliente.nombreCliente);
            holder.tvTotalGastado.setText("S/ " + df.format(cliente.totalGastado));
            holder.tvNumeroReservas.setText(cliente.numeroReservas + " reservas");

            // Mostrar posición (mayor a menor)
            holder.tvPosicion.setText(String.valueOf(position + 1));

            // Calcular promedio por reserva
            double promedio = cliente.totalGastado / cliente.numeroReservas;
            holder.tvPromedioReserva.setText("Prom: S/ " + df.format(promedio));
        }

        @Override
        public int getItemCount() {
            return clientes.size();
        }

        public static class ClienteViewHolder extends RecyclerView.ViewHolder {
            TextView tvPosicion, tvNombreCliente, tvTotalGastado, tvNumeroReservas, tvPromedioReserva;

            public ClienteViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosicion = itemView.findViewById(R.id.tvPosicion);
                tvNombreCliente = itemView.findViewById(R.id.tvNombreCliente);
                tvTotalGastado = itemView.findViewById(R.id.tvTotalGastado);
                tvNumeroReservas = itemView.findViewById(R.id.tvNumeroReservas);
                tvPromedioReserva = itemView.findViewById(R.id.tvPromedioReserva);
            }
        }
    }
}