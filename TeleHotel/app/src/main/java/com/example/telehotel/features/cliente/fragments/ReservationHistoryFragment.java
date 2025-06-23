/*package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.cliente.adapters.HistorialReservaAdapter;
import com.example.telehotel.features.cliente.HistorialDetalleActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationHistoryFragment extends Fragment {

    // Views básicas
    private Toolbar toolbar;
    private RecyclerView recyclerReservas;
    private TextView tvBorrarTodo;
    private LinearLayout emptyStateLayout;
    private Button btnExplorarHoteles;

    // Datos
    private HistorialReservaAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Reserva> copiaReservas = new ArrayList<>();

    public ReservationHistoryFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_historial_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views básicas
        initViews(view);

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerView
        setupRecyclerView();

        // Verificar estado vacío
        checkEmptyState();
    }

    private void initViews(View view) {
        // Solo las views que existen en tu XML
        toolbar = view.findViewById(R.id.toolbar);
        recyclerReservas = view.findViewById(R.id.recyclerReservas);
        tvBorrarTodo = view.findViewById(R.id.tvBorrarTodo);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        btnExplorarHoteles = view.findViewById(R.id.btnExplorarHoteles);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            // Configurar navegación hacia atrás
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });

            // Configurar botón limpiar (si existe)
            if (tvBorrarTodo != null) {
                tvBorrarTodo.setOnClickListener(v -> {
                    if (!listaReservas.isEmpty()) {
                        mostrarDialogoBorrarTodo();
                    }
                });
            }
        }
    }

    private void setupRecyclerView() {
        // Datos de ejemplo para probar (puedes comentar esto cuando tengas datos reales)
        // loadSampleData();

        copiaReservas.addAll(listaReservas);

        adapter = new HistorialReservaAdapter(listaReservas, getContext(), reserva -> {
            Intent intent = new Intent(getActivity(), HistorialDetalleActivity.class);
            intent.putExtra("reserva", (CharSequence) reserva);
            startActivity(intent);
        });

        recyclerReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReservas.setAdapter(adapter);

        // Swipe para eliminar (opcional, puedes comentar si no lo quieres por ahora)
        setupSwipeToDelete();

        // Configurar botón explorar hoteles
        if (btnExplorarHoteles != null) {
            btnExplorarHoteles.setOnClickListener(v -> {
                // Navegar a explorar hoteles
                Toast.makeText(getContext(), "Explorar hoteles", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                listaReservas.remove(pos);
                adapter.notifyItemRemoved(pos);
                checkEmptyState();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerReservas);
    }

    private void mostrarDialogoBorrarTodo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Limpiar historial")
                .setMessage("¿Estás seguro de que deseas eliminar todo el historial?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    listaReservas.clear();
                    copiaReservas.clear();
                    adapter.notifyDataSetChanged();
                    checkEmptyState();
                    Toast.makeText(getContext(), "Historial eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkEmptyState() {
        if (emptyStateLayout != null) {
            if (listaReservas.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerReservas.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerReservas.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        toolbar = null;
        recyclerReservas = null;
        adapter = null;
    }
}*/
package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.cliente.adapters.HistorialReservaAdapter;
import com.example.telehotel.features.cliente.HistorialDetalleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReservationHistoryFragment extends Fragment {

    private static final String TAG = "ReservationHistory";

    // Views
    private Toolbar toolbar;
    private RecyclerView recyclerReservas;
    private TextView tvBorrarTodo;
    private LinearLayout emptyStateLayout;
    private Button btnExplorarHoteles;
    private ProgressBar progressBar;

    // Datos
    private HistorialReservaAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();

    public ReservationHistoryFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_historial_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views
        initViews(view);

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar reservas desde Firebase
        cargarReservasDesdeFirebase();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerReservas = view.findViewById(R.id.recyclerReservas);
        //tvBorrarTodo = view.findViewById(R.id.tvBorrarTodo);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        btnExplorarHoteles = view.findViewById(R.id.btnExplorarHoteles);

        // Agregar ProgressBar si no lo tienes en el XML
        progressBar = new ProgressBar(getContext());
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });

            if (tvBorrarTodo != null) {
                tvBorrarTodo.setOnClickListener(v -> {
                    if (!listaReservas.isEmpty()) {
                        mostrarDialogoBorrarTodo();
                    }
                });
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new HistorialReservaAdapter(listaReservas, getContext(), reserva -> {
            // Abrir detalles de la reserva
            Intent intent = new Intent(getActivity(), HistorialDetalleActivity.class);
            intent.putExtra("reservaId", reserva.getId());
            intent.putExtra("codigoReserva", reserva.getCodigoReserva());
            startActivity(intent);
        });

        recyclerReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReservas.setAdapter(adapter);

        // Configurar swipe para eliminar
        //setupSwipeToDelete();

        // Configurar botón explorar hoteles
        if (btnExplorarHoteles != null) {
            btnExplorarHoteles.setOnClickListener(v -> {
                // Aquí puedes navegar a la pantalla de explorar hoteles
                // Por ejemplo, cambiar de fragment o abrir activity
                Toast.makeText(getContext(), "Explorar hoteles", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /*private void cargarReservasDesdeFirebase() {
        Log.d(TAG, "=== CARGANDO RESERVAS DESDE FIREBASE ===");

        // Verificar usuario autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarError("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Cargando reservas para usuario: " + userId);

        // Mostrar loading
        mostrarLoading(true);

        // Consultar reservas del usuario ordenadas por fecha de creación (más recientes primero)
        FirebaseFirestore.getInstance()
                .collection("reservas")
                .whereEqualTo("clienteId", userId)
                .orderBy("fechaReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Consulta exitosa. Documentos encontrados: " + queryDocumentSnapshots.size());

                    listaReservas.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Convertir documento a objeto Reserva
                            Reserva reserva = document.toObject(Reserva.class);

                            // Asegurar que tenga ID
                            if (reserva.getId() == null || reserva.getId().isEmpty()) {
                                reserva.setId(document.getId());
                            }

                            listaReservas.add(reserva);

                            Log.d(TAG, "Reserva agregada: " + reserva.getCodigoReserva() +
                                    " - Hotel: " + reserva.getHotelNombre());

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando documento: " + document.getId(), e);
                        }
                    }

                    // Actualizar UI en el hilo principal
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            adapter.notifyDataSetChanged();
                            checkEmptyState();

                            Log.d(TAG, "UI actualizada. Total reservas: " + listaReservas.size());
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas: " + e.getMessage(), e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            mostrarError("Error cargando el historial: " + e.getMessage());
                            checkEmptyState();
                        });
                    }
                });
    }*/
    private void cargarReservasDesdeFirebase() {
        Log.d(TAG, "=== CARGANDO RESERVAS DESDE FIREBASE ===");

        // Verificar usuario autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarError("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Cargando reservas para usuario: " + userId);

        // Mostrar loading
        mostrarLoading(true);

        // CAMBIO 1: QUITAR orderBy para evitar el problema del índice
        FirebaseFirestore.getInstance()
                .collection("reservas")
                .whereEqualTo("clienteId", userId)
                // NO usar .orderBy("fechaReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Consulta exitosa. Documentos encontrados: " + queryDocumentSnapshots.size());

                    listaReservas.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Convertir documento a objeto Reserva
                            Reserva reserva = document.toObject(Reserva.class);

                            // Asegurar que tenga ID
                            if (reserva.getId() == null || reserva.getId().isEmpty()) {
                                reserva.setId(document.getId());
                            }

                            listaReservas.add(reserva);

                            Log.d(TAG, "Reserva agregada: " + reserva.getCodigoReserva() +
                                    " - Hotel: " + reserva.getHotelNombre());

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando documento: " + document.getId(), e);
                        }
                    }

                    // CAMBIO 2: AGREGAR ORDENAMIENTO AQUÍ (después de cargar todas las reservas)
                    ordenarReservas();

                    // Actualizar UI en el hilo principal
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            adapter.notifyDataSetChanged();
                            checkEmptyState();

                            Log.d(TAG, "UI actualizada. Total reservas: " + listaReservas.size());
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reservas: " + e.getMessage(), e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            mostrarError("Error cargando el historial: " + e.getMessage());
                            checkEmptyState();
                        });
                    }
                });
    }

    // CAMBIO 3: AGREGAR ESTE MÉTODO NUEVO
    private void ordenarReservas() {
        Log.d(TAG, "=== ORDENANDO RESERVAS ===");

        listaReservas.sort((r1, r2) -> {
            try {
                // Opción 1: Usar fechaReservaTimestamp (para reservas nuevas)
                Long timestamp1 = r1.getFechaReservaTimestamp();
                Long timestamp2 = r2.getFechaReservaTimestamp();

                if (timestamp1 != null && timestamp2 != null) {
                    Log.d(TAG, "Ordenando por fechaReservaTimestamp");
                    return Long.compare(timestamp2, timestamp1); // Descendente (más recientes primero)
                }

                // Opción 2: Usar código de reserva (para todas las reservas)
                String codigo1 = r1.getCodigoReserva();
                String codigo2 = r2.getCodigoReserva();

                if (codigo1 != null && codigo2 != null) {
                    Log.d(TAG, "Ordenando por código de reserva");
                    return codigo2.compareTo(codigo1); // Descendente (códigos más altos = más recientes)
                }

                // Opción 3: Usar fechaInicio como último recurso
                Long fecha1 = r1.getFechaInicio();
                Long fecha2 = r2.getFechaInicio();

                if (fecha1 != null && fecha2 != null) {
                    Log.d(TAG, "Ordenando por fechaInicio");
                    return Long.compare(fecha2, fecha1); // Descendente
                }

                return 0; // Si no se puede comparar
            } catch (Exception e) {
                Log.e(TAG, "Error ordenando reservas", e);
                return 0;
            }
        });

        Log.d(TAG, "Reservas ordenadas. Total: " + listaReservas.size());
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            recyclerReservas.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
            // Mostrar progress bar
        } else {
            // Ocultar progress bar
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    /*private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Reserva reservaEliminada = listaReservas.get(pos);

                // Eliminar de la lista local
                listaReservas.remove(pos);
                adapter.notifyItemRemoved(pos);
                checkEmptyState();

                // Opcional: Eliminar también de Firebase
                // eliminarReservaDeFirebase(reservaEliminada.getId());

                Toast.makeText(getContext(), "Reserva eliminada del historial", Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerReservas);
    }*/

    private void mostrarDialogoBorrarTodo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Limpiar historial")
                .setMessage("¿Estás seguro de que deseas eliminar todo el historial?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    listaReservas.clear();
                    adapter.notifyDataSetChanged();
                    checkEmptyState();
                    Toast.makeText(getContext(), "Historial eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkEmptyState() {
        if (emptyStateLayout != null) {
            if (listaReservas.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerReservas.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerReservas.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar reservas cuando el fragment vuelve a ser visible
        cargarReservasDesdeFirebase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        toolbar = null;
        recyclerReservas = null;
        adapter = null;
    }
}