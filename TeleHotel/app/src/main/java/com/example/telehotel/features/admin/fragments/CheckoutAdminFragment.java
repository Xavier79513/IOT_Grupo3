/*package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.telehotel.R;
import com.example.telehotel.data.model.NotificacionCheckout;
import com.example.telehotel.features.admin.ProcesarCheckoutActivity;
import com.example.telehotel.features.admin.adapters.SolicitudesCheckoutAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckoutAdminFragment extends Fragment implements SolicitudesCheckoutAdapter.OnSolicitudClickListener {

    private static final String TAG = "CheckoutAdminFragment";

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerSolicitudes;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;
    private TextView tvContadorPendientes;

    // Datos
    private SolicitudesCheckoutAdapter adapter;
    private List<NotificacionCheckout> listaSolicitudes = new ArrayList<>();
    private FirebaseFirestore db;
    private String adminHotelId; // ID del hotel que administra este usuario

    public CheckoutAdminFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar views
        initViews(view);

        // Configurar toolbar
        setupToolbar();

        // Obtener hotel del administrador
        obtenerHotelDelAdministrador();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar SwipeRefresh
        setupSwipeRefresh();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerSolicitudes = view.findViewById(R.id.recyclerSolicitudes);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        progressBar = view.findViewById(R.id.progressBar);
        tvContadorPendientes = view.findViewById(R.id.tvContadorPendientes);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new SolicitudesCheckoutAdapter(listaSolicitudes, getContext(), this);
        recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSolicitudes.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
            swipeRefresh.setOnRefreshListener(this::cargarSolicitudesCheckout);
        }
    }

    private void obtenerHotelDelAdministrador() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarError("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Obteniendo hotel para administrador: " + userId);

        // Buscar el usuario en la colección de usuarios para obtener su hotelId
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        adminHotelId = documentSnapshot.getString("hotelAsignado");
                        Log.d(TAG, "Hotel del administrador: " + adminHotelId);

                        if (adminHotelId != null && !adminHotelId.isEmpty()) {
                            // Cargar solicitudes de checkout para este hotel
                            cargarSolicitudesCheckout();
                        } else {
                            Log.e(TAG, "Administrador no tiene hotel asignado");
                            mostrarError("Error: No tienes un hotel asignado");
                        }
                    } else {
                        Log.e(TAG, "Usuario no encontrado en la base de datos");
                        mostrarError("Error: Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo datos del usuario", e);
                    mostrarError("Error obteniendo datos del usuario: " + e.getMessage());
                });
    }

    private void cargarSolicitudesCheckout() {
        if (adminHotelId == null || adminHotelId.isEmpty()) {
            Log.w(TAG, "No se puede cargar solicitudes: hotel ID no disponible");
            return;
        }

        Log.d(TAG, "Cargando solicitudes de checkout para hotel: " + adminHotelId);
        mostrarLoading(true);

        // Cargar notificaciones de checkout para este hotel
        db.collection("notificaciones_admin")
                .whereEqualTo("hotelId", adminHotelId)
                .whereEqualTo("tipo", "checkout_solicitado")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Solicitudes encontradas: " + queryDocumentSnapshots.size());

                    listaSolicitudes.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            NotificacionCheckout notificacion = document.toObject(NotificacionCheckout.class);
                            notificacion.setId(document.getId());
                            listaSolicitudes.add(notificacion);

                            Log.d(TAG, "Solicitud agregada: " + notificacion.getClienteNombre() +
                                    " - Habitación: " + notificacion.getHabitacionNumero() +
                                    " - Procesada: " + notificacion.isProcesada());

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando notificación: " + document.getId(), e);
                        }
                    }

                    // Actualizar UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            adapter.notifyDataSetChanged();
                            checkEmptyState();
                            actualizarContadorPendientes();

                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando solicitudes de checkout", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            mostrarError("Error cargando solicitudes: " + e.getMessage());
                            checkEmptyState();

                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
    }

    @Override
    public void onSolicitudClick(NotificacionCheckout notificacion) {
        Log.d(TAG, "Abriendo detalle de solicitud: " + notificacion.getReservaId());

        // Abrir actividad para procesar el checkout
        Intent intent = new Intent(getActivity(), ProcesarCheckoutActivity.class);
        intent.putExtra("notificacionId", notificacion.getId());
        intent.putExtra("reservaId", notificacion.getReservaId());
        intent.putExtra("hotelId", notificacion.getHotelId());
        intent.putExtra("clienteNombre", notificacion.getClienteNombre());
        intent.putExtra("habitacionNumero", notificacion.getHabitacionNumero());

        startActivityForResult(intent, 100); // RequestCode para saber cuándo regresa
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) { // Regresa de ProcesarCheckoutActivity
            // Recargar la lista para reflejar cambios
            cargarSolicitudesCheckout();
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        if (recyclerSolicitudes != null) {
            recyclerSolicitudes.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    private void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void checkEmptyState() {
        if (emptyStateLayout != null && tvEmptyMessage != null) {
            boolean isEmpty = listaSolicitudes.isEmpty();

            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerSolicitudes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (isEmpty) {
                tvEmptyMessage.setText("No hay solicitudes de checkout pendientes");
            }
        }
    }

    private void actualizarContadorPendientes() {
        if (tvContadorPendientes != null) {
            // Contar solicitudes no procesadas
            long pendientes = listaSolicitudes.stream()
                    .filter(s -> !s.isProcesada())
                    .count();

            if (pendientes > 0) {
                tvContadorPendientes.setVisibility(View.VISIBLE);
                tvContadorPendientes.setText(String.valueOf(pendientes));
            } else {
                tvContadorPendientes.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar solicitudes cuando el fragment vuelve a ser visible
        if (adminHotelId != null) {
            cargarSolicitudesCheckout();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        toolbar = null;
        recyclerSolicitudes = null;
        adapter = null;
        db = null;
    }
}*/
package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.telehotel.R;
import com.example.telehotel.data.model.NotificacionCheckout;
import com.example.telehotel.features.admin.ProcesarCheckoutActivity;
import com.example.telehotel.features.admin.adapters.SolicitudesCheckoutAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckoutAdminFragment extends Fragment implements SolicitudesCheckoutAdapter.OnSolicitudClickListener {

    private static final String TAG = "CheckoutAdminFragment";

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerSolicitudes;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;
    private TextView tvContadorPendientes;

    // Datos
    private SolicitudesCheckoutAdapter adapter;
    private List<NotificacionCheckout> listaSolicitudes = new ArrayList<>();
    private FirebaseFirestore db;
    private String adminHotelId; // ID del hotel que administra este usuario

    public CheckoutAdminFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar views
        initViews(view);

        // Configurar toolbar
        setupToolbar();

        // Obtener hotel del administrador
        obtenerHotelDelAdministrador();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar SwipeRefresh
        setupSwipeRefresh();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerSolicitudes = view.findViewById(R.id.recyclerSolicitudes);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        progressBar = view.findViewById(R.id.progressBar);
        tvContadorPendientes = view.findViewById(R.id.tvContadorPendientes);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new SolicitudesCheckoutAdapter(listaSolicitudes, getContext(), this);
        recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSolicitudes.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
            swipeRefresh.setOnRefreshListener(this::cargarSolicitudesCheckout);
        }
    }

    private void buscarHotelPorAdministrador(String userId) {
        Log.d(TAG, "Buscando hotel por administradorId: " + userId);

        db.collection("hoteles")
                .whereEqualTo("administradorId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String hotelId = querySnapshot.getDocuments().get(0).getId();
                        Log.d(TAG, "Hotel encontrado: " + hotelId);

                        // Actualizar el documento del usuario con el hotelId
                        db.collection("usuarios")
                                .document(userId)
                                .update("hotelId", hotelId)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Usuario actualizado con hotelId");
                                    adminHotelId = hotelId;
                                    cargarSolicitudesCheckout();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error actualizando usuario", e);
                                    mostrarError("Error actualizando datos del usuario");
                                });
                    } else {
                        Log.e(TAG, "No se encontró hotel para este administrador");
                        mostrarError("Error: No se encontró un hotel asignado a este administrador");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error buscando hotel", e);
                    mostrarError("Error buscando hotel: " + e.getMessage());
                });
    }

    private void obtenerHotelDelAdministrador() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarError("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Obteniendo hotel para administrador: " + userId);

        // Buscar el usuario en la colección de usuarios para obtener su hotelId
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ DEBUG: Mostrar todos los datos del usuario
                        Log.d(TAG, "Documento del usuario encontrado: " + documentSnapshot.getData());

                        adminHotelId = documentSnapshot.getString("hotelAsignado");
                        Log.d(TAG, "Hotel del administrador: " + adminHotelId);

                        // ✅ DEBUG: Verificar otros campos posibles
                        String role = documentSnapshot.getString("role");
                        String email = documentSnapshot.getString("email");
                        Log.d(TAG, "Role: " + role + ", Email: " + email);

                        if (adminHotelId != null && !adminHotelId.isEmpty()) {
                            // Cargar solicitudes de checkout para este hotel
                            cargarSolicitudesCheckout();
                        } else {
                            Log.e(TAG, "Administrador no tiene hotel asignado");
                            Log.e(TAG, "Campos disponibles: " + documentSnapshot.getData().keySet());

                            // ✅ TEMPORAL: Intentar buscar hotel por administradorId
                            buscarHotelPorAdministrador(userId);
                        }
                    } else {
                        Log.e(TAG, "Usuario no encontrado en la base de datos");
                        mostrarError("Error: Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo datos del usuario", e);
                    mostrarError("Error obteniendo datos del usuario: " + e.getMessage());
                });
    }

    private void cargarSolicitudesCheckout() {
        if (adminHotelId == null || adminHotelId.isEmpty()) {
            Log.w(TAG, "No se puede cargar solicitudes: hotel ID no disponible");
            return;
        }

        Log.d(TAG, "Cargando solicitudes de checkout para hotel: " + adminHotelId);
        mostrarLoading(true);

        // Cargar notificaciones de checkout para este hotel
        db.collection("notificaciones_admin")
                .whereEqualTo("hotelId", adminHotelId)
                .whereEqualTo("tipo", "checkout_solicitado")
                // ✅ QUITAR TEMPORALMENTE orderBy para evitar error de índice
                // .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Solicitudes encontradas: " + queryDocumentSnapshots.size());

                    listaSolicitudes.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            NotificacionCheckout notificacion = document.toObject(NotificacionCheckout.class);
                            notificacion.setId(document.getId());
                            listaSolicitudes.add(notificacion);

                            Log.d(TAG, "Solicitud agregada: " + notificacion.getClienteNombre() +
                                    " - Habitación: " + notificacion.getHabitacionNumero() +
                                    " - Procesada: " + notificacion.isProcesada());

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando notificación: " + document.getId(), e);
                        }
                    }

                    // ✅ ORDENAR MANUALMENTE después de cargar
                    listaSolicitudes.sort((s1, s2) -> Long.compare(s2.getTimestamp(), s1.getTimestamp()));

                    // Actualizar UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            adapter.notifyDataSetChanged();
                            checkEmptyState();
                            actualizarContadorPendientes();

                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando solicitudes de checkout", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mostrarLoading(false);
                            mostrarError("Error cargando solicitudes: " + e.getMessage());
                            checkEmptyState();

                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
    }

    @Override
    public void onSolicitudClick(NotificacionCheckout notificacion) {
        Log.d(TAG, "Abriendo detalle de solicitud: " + notificacion.getReservaId());

        // Abrir actividad para procesar el checkout
        Intent intent = new Intent(getActivity(), ProcesarCheckoutActivity.class);
        intent.putExtra("notificacionId", notificacion.getId());
        intent.putExtra("reservaId", notificacion.getReservaId());
        intent.putExtra("hotelId", notificacion.getHotelId());
        intent.putExtra("clienteNombre", notificacion.getClienteNombre());
        intent.putExtra("habitacionNumero", notificacion.getHabitacionNumero());

        startActivityForResult(intent, 100); // RequestCode para saber cuándo regresa
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) { // Regresa de ProcesarCheckoutActivity
            // Recargar la lista para reflejar cambios
            cargarSolicitudesCheckout();
        }
    }

    private void mostrarLoading(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        if (recyclerSolicitudes != null) {
            recyclerSolicitudes.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    private void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void checkEmptyState() {
        if (emptyStateLayout != null && tvEmptyMessage != null) {
            boolean isEmpty = listaSolicitudes.isEmpty();

            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerSolicitudes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (isEmpty) {
                tvEmptyMessage.setText("No hay solicitudes de checkout pendientes");
            }
        }
    }

    private void actualizarContadorPendientes() {
        if (tvContadorPendientes != null) {
            // Contar solicitudes no procesadas
            long pendientes = listaSolicitudes.stream()
                    .filter(s -> !s.isProcesada())
                    .count();

            if (pendientes > 0) {
                tvContadorPendientes.setVisibility(View.VISIBLE);
                tvContadorPendientes.setText(String.valueOf(pendientes));
            } else {
                tvContadorPendientes.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar solicitudes cuando el fragment vuelve a ser visible
        if (adminHotelId != null) {
            cargarSolicitudesCheckout();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        toolbar = null;
        recyclerSolicitudes = null;
        adapter = null;
        db = null;
    }
}