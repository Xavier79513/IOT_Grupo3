package com.example.telehotel.features.superadmin.Fragments;

import android.app.AlertDialog;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.features.superadmin.adapters.TaxistaSolicitudAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabilitarTaxistaFragment extends Fragment implements TaxistaSolicitudAdapter.OnTaxistaActionListener {

    private static final String TAG = "HabilitarTaxistaFragment";

    // Views
    private TabLayout tabLayout;
    private RecyclerView recyclerViewTaxistas;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage, tvPendingCount;
    private CardView badgeCard;

    // Data
    private TaxistaSolicitudAdapter adapter;
    private List<Usuario> taxistasList;
    private List<Usuario> taxistasPendientes;
    private List<Usuario> taxistasAprobados;
    private List<Usuario> taxistasRechazados;
    private FirebaseFirestore db;

    // Estados
    private String currentTab = "pendientes";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habilitar_taxista, container, false);

        initFirebase();
        initViews(view);
        setupRecyclerView();
        setupListeners();
        cargarTaxistas();

        return view;
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerViewTaxistas = view.findViewById(R.id.recyclerViewTaxistas);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        badgeCard = view.findViewById(R.id.badgeCard);

        // Inicializar listas
        taxistasList = new ArrayList<>();
        taxistasPendientes = new ArrayList<>();
        taxistasAprobados = new ArrayList<>();
        taxistasRechazados = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new TaxistaSolicitudAdapter(getContext(), taxistasList);
        adapter.setOnTaxistaActionListener(this);

        recyclerViewTaxistas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTaxistas.setAdapter(adapter);
    }

    private void setupListeners() {
        // TabLayout listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "pendientes";
                        mostrarTaxistasPorEstado("pendiente");
                        break;
                    case 1:
                        currentTab = "aprobados";
                        mostrarTaxistasPorEstado("activo");
                        break;
                    case 2:
                        currentTab = "rechazados";
                        mostrarTaxistasPorEstado("rechazado");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(this::cargarTaxistas);
    }

    private void cargarTaxistas() {
        mostrarLoading(true);

        db.collection("usuarios")
                .whereEqualTo("role", "taxista")
                .orderBy("fechaRegistro", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Limpiar listas
                    taxistasPendientes.clear();
                    taxistasAprobados.clear();
                    taxistasRechazados.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Usuario taxista = document.toObject(Usuario.class);
                        taxista.setUid(document.getId());

                        // Clasificar por estado
                        String estado = taxista.getEstado();
                        if (estado == null) estado = "pendiente";

                        switch (estado.toLowerCase()) {
                            case "pendiente":
                                taxistasPendientes.add(taxista);
                                break;
                            case "activo":
                            case "aprobado":
                                taxistasAprobados.add(taxista);
                                break;
                            case "rechazado":
                            case "inactivo":
                                taxistasRechazados.add(taxista);
                                break;
                        }
                    }

                    // Actualizar UI
                    actualizarContadorPendientes();
                    mostrarTaxistasPorEstado(getCurrentTabState());
                    mostrarLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando taxistas", e);
                    Toast.makeText(getContext(), "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
                    mostrarLoading(false);
                });
    }

    private String getCurrentTabState() {
        switch (currentTab) {
            case "pendientes":
                return "pendiente";
            case "aprobados":
                return "activo";
            case "rechazados":
                return "rechazado";
            default:
                return "pendiente";
        }
    }

    private void mostrarTaxistasPorEstado(String estado) {
        List<Usuario> listaFiltrada;
        String mensajeVacio;

        switch (estado.toLowerCase()) {
            case "pendiente":
                listaFiltrada = taxistasPendientes;
                mensajeVacio = "No hay solicitudes pendientes";
                break;
            case "activo":
            case "aprobado":
                listaFiltrada = taxistasAprobados;
                mensajeVacio = "No hay taxistas aprobados";
                break;
            case "rechazado":
            case "inactivo":
                listaFiltrada = taxistasRechazados;
                mensajeVacio = "No hay taxistas rechazados";
                break;
            default:
                listaFiltrada = new ArrayList<>();
                mensajeVacio = "No hay datos";
                break;
        }

        taxistasList.clear();
        taxistasList.addAll(listaFiltrada);
        adapter.updateList(taxistasList);

        // Mostrar/ocultar estado vacío
        if (listaFiltrada.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewTaxistas.setVisibility(View.GONE);
            tvEmptyMessage.setText(mensajeVacio);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewTaxistas.setVisibility(View.VISIBLE);
        }
    }

    private void actualizarContadorPendientes() {
        int cantidadPendientes = taxistasPendientes.size();
        if (cantidadPendientes > 0) {
            badgeCard.setVisibility(View.VISIBLE);
            tvPendingCount.setText(String.valueOf(cantidadPendientes));
        } else {
            badgeCard.setVisibility(View.GONE);
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            progressBar.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // =============== IMPLEMENTACIÓN DE OnTaxistaActionListener ===============

    @Override
    public void onVerDetalles(Usuario taxista) {
        mostrarDialogoDetalles(taxista);
    }

    @Override
    public void onAprobar(Usuario taxista) {
        mostrarDialogoConfirmacion(
                "Aprobar Taxista",
                "¿Estás seguro de que deseas aprobar a " + taxista.getNombres() + " " + taxista.getApellidos() + "?",
                () -> aprobarTaxista(taxista)
        );
    }

    @Override
    public void onRechazar(Usuario taxista) {
        mostrarDialogoConfirmacion(
                "Rechazar Taxista",
                "¿Estás seguro de que deseas rechazar a " + taxista.getNombres() + " " + taxista.getApellidos() + "?",
                () -> rechazarTaxista(taxista)
        );
    }

    // =============== MÉTODOS DE ACCIÓN ===============

    private void aprobarTaxista(Usuario taxista) {
        cambiarEstadoTaxista(taxista, "activo", "Taxista aprobado exitosamente");
    }

    private void rechazarTaxista(Usuario taxista) {
        cambiarEstadoTaxista(taxista, "rechazado", "Taxista rechazado");
    }

    private void cambiarEstadoTaxista(Usuario taxista, String nuevoEstado, String mensajeExito) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", nuevoEstado);

        db.collection("usuarios").document(taxista.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), mensajeExito, Toast.LENGTH_SHORT).show();
                    cargarTaxistas(); // Recargar la lista
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando estado del taxista", e);
                    Toast.makeText(getContext(), "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                });
    }

    // =============== MÉTODOS DE UI ===============

    private void mostrarDialogoDetalles(Usuario taxista) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Crear vista personalizada para el diálogo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taxista_detalles, null);

        // TODO: Configurar los elementos del diálogo con los datos del taxista
        TextView tvNombre = dialogView.findViewById(R.id.tvNombreDetalle);
        TextView tvDni = dialogView.findViewById(R.id.tvDniDetalle);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmailDetalle);
        TextView tvTelefono = dialogView.findViewById(R.id.tvTelefonoDetalle);
        TextView tvDireccion = dialogView.findViewById(R.id.tvDireccionDetalle);
        TextView tvPlaca = dialogView.findViewById(R.id.tvPlacaDetalle);
        TextView tvFechaNacimiento = dialogView.findViewById(R.id.tvFechaNacimientoDetalle);

        // Llenar datos
        tvNombre.setText(taxista.getNombres() + " " + taxista.getApellidos());
        tvDni.setText(taxista.getNumeroDocumento());
        tvEmail.setText(taxista.getEmail());
        tvTelefono.setText(taxista.getTelefono());
        tvDireccion.setText(taxista.getDireccion());
        tvPlaca.setText(taxista.getPlacaAuto());
        tvFechaNacimiento.setText(taxista.getFechaNacimiento());

        builder.setView(dialogView)
                .setTitle("Detalles del Taxista")
                .setPositiveButton("Cerrar", null);

        // Agregar botones de acción si está pendiente
        if ("pendiente".equals(taxista.getEstado())) {
            builder.setNegativeButton("Rechazar", (dialog, which) -> rechazarTaxista(taxista))
                    .setNeutralButton("Aprobar", (dialog, which) -> aprobarTaxista(taxista));
        }

        builder.show();
    }

    private void mostrarDialogoConfirmacion(String titulo, String mensaje, Runnable accion) {
        new AlertDialog.Builder(getContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Confirmar", (dialog, which) -> accion.run())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // =============== MÉTODOS DE CICLO DE VIDA ===============

    @Override
    public void onResume() {
        super.onResume();
        cargarTaxistas();
    }
}