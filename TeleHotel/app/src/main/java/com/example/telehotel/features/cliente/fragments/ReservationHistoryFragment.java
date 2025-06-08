package com.example.telehotel.features.cliente.fragments;

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

/*public class ReservationHistoryFragment extends Fragment {
    private RecyclerView recyclerReservas;
    private HistorialReservaAdapter adapter;
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Reserva> copiaReservas = new ArrayList<>();
    private EditText buscarReserva;
    private TextView btnBorrarTodo;
    private ImageView btnBack;

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

        recyclerReservas = view.findViewById(R.id.recyclerReservas);
        //buscarReserva = view.findViewById(R.id.buscarReserva);
        //btnBorrarTodo = view.findViewById(R.id.btnBorrarTodo);
        btnBack = view.findViewById(R.id.btnBack);

        copiaReservas.addAll(listaReservas);

        adapter = new HistorialReservaAdapter(listaReservas, getContext(), reserva -> {
            Intent intent = new Intent(getActivity(), HistorialDetalleActivity.class);
            intent.putExtra("reserva", (CharSequence) reserva);
            startActivity(intent);
        });

        recyclerReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReservas.setAdapter(adapter);

        // Swipe para eliminar
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
            }
        }).attachToRecyclerView(recyclerReservas);

        // Buscar por nombre
        buscarReserva.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
            Map<String, Hotel> mapaHotelesPorId = new HashMap<>();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtro = s.toString().toLowerCase();

                List<Reserva> filtradas = copiaReservas.stream()
                        .filter(r -> {
                            Hotel hotel = mapaHotelesPorId.get(r.getHotelId());
                            return hotel != null && hotel.getNombre().toLowerCase().contains(filtro);
                        })
                        .collect(Collectors.toList());

                listaReservas.clear();
                listaReservas.addAll(filtradas);
                adapter.notifyDataSetChanged();
            }
        });

        // Botón borrar todo
        btnBorrarTodo.setOnClickListener(v -> {
            listaReservas.clear();
            adapter.notifyDataSetChanged();
        });

        // Botón regresar
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed(); // o usar NavigationComponent si estás usando Jetpack Navigation
        });
    }
}*/
public class ReservationHistoryFragment extends Fragment {

    // Views básicas
    private Toolbar toolbar;
    private RecyclerView recyclerReservas;
    private EditText etBuscarReserva;
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
        etBuscarReserva = view.findViewById(R.id.etBuscarReserva);
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

    // Método para cargar datos de ejemplo (descomenta si quieres probar con datos)
    /*
    private void loadSampleData() {
        // Agregar algunos datos de ejemplo
        listaReservas.add(new Reserva("1", "Hotel Paraíso", "12 Abr 2025", 125));
        listaReservas.add(new Reserva("2", "Luna Resort", "05 Mar 2025", 250));
        listaReservas.add(new Reserva("3", "Sunshine Inn", "15 Feb 2025", 180));
    }
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        toolbar = null;
        recyclerReservas = null;
        adapter = null;
    }
}