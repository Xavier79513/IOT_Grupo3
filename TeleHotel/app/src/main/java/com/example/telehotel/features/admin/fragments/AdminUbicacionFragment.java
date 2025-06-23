package com.example.telehotel.features.admin.fragments;

import android.graphics.Color;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager; // ✅ AGREGAR IMPORT
import com.example.telehotel.data.model.LatLng;
import com.example.telehotel.data.model.LugarHistorico;
import com.example.telehotel.features.admin.LugarHistoricoAdapter;
import com.example.telehotel.features.admin.OpenStreetMapService;
import com.example.telehotel.core.utils.LogUtils; // ✅ AGREGAR IMPORT
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUbicacionFragment extends Fragment {

    // UI Components
    private TextInputEditText etDireccion, etCiudad, etPais;
    private TextView tvCoordenadas, tvEstadoBusqueda;
    private Button btnBuscarCoordenadas, btnBuscarLugares, btnGuardarUbicacion;
    private RecyclerView rvLugaresHistoricos;
    private ProgressBar progressBar;
    private LinearLayout layoutUbicacionInfo;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Data
    private double latitud = 0, longitud = 0;
    private List<LugarHistorico> lugaresHistoricos;
    private LugarHistoricoAdapter adapter;

    // ✅ AGREGAR para logs
    private String adminId;
    private String adminName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_ubicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ OBTENER DATOS DEL ADMIN PARA LOGS
        PrefsManager prefsManager = new PrefsManager(requireContext());
        adminId = prefsManager.getUserId();
        adminName = prefsManager.getUserName();

        initFirebase();
        initViews(view);
        setupListeners();
        setupRecyclerView();
        loadExistingData();

        // ✅ LOG: Acceso al módulo de ubicación
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Accedió al módulo de gestión de ubicación (Admin: " + adminName + ")"
        );
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        etDireccion = view.findViewById(R.id.etDireccion);
        etCiudad = view.findViewById(R.id.etCiudad);
        etPais = view.findViewById(R.id.etPais);
        tvCoordenadas = view.findViewById(R.id.tvCoordenadas);
        tvEstadoBusqueda = view.findViewById(R.id.tvEstadoBusqueda);
        btnBuscarCoordenadas = view.findViewById(R.id.btnBuscarCoordenadas);
        btnBuscarLugares = view.findViewById(R.id.btnBuscarLugares);
        btnGuardarUbicacion = view.findViewById(R.id.btnGuardarUbicacion);
        rvLugaresHistoricos = view.findViewById(R.id.rvLugaresHistoricos);
        progressBar = view.findViewById(R.id.progressBar);
        layoutUbicacionInfo = view.findViewById(R.id.layoutUbicacionInfo);

        lugaresHistoricos = new ArrayList<>();
    }

    private void setupListeners() {
        btnBuscarCoordenadas.setOnClickListener(v -> buscarCoordenadas());
        btnBuscarLugares.setOnClickListener(v -> buscarLugaresHistoricos());
        btnGuardarUbicacion.setOnClickListener(v -> guardarConfiguracion());
    }

    private void setupRecyclerView() {
        adapter = new LugarHistoricoAdapter(lugaresHistoricos, this::toggleLugarSeleccionado);
        rvLugaresHistoricos.setAdapter(adapter);
        rvLugaresHistoricos.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void buscarCoordenadas() {
        String direccion = etDireccion.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String pais = etPais.getText().toString().trim();

        if (direccion.isEmpty()) {
            etDireccion.setError("Ingresa una dirección");

            // ✅ LOG: Error de validación
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó buscar coordenadas sin ingresar dirección"
            );
            return;
        }

        // Construir dirección completa
        StringBuilder direccionCompleta = new StringBuilder(direccion);
        if (!ciudad.isEmpty()) {
            direccionCompleta.append(", ").append(ciudad);
        }
        if (!pais.isEmpty()) {
            direccionCompleta.append(", ").append(pais);
        }

        // ✅ LOG: Inicio de búsqueda de coordenadas
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Inició búsqueda de coordenadas para: " + direccionCompleta.toString()
        );

        mostrarProgreso(true, "Buscando coordenadas...");
        btnBuscarCoordenadas.setEnabled(false);

        OpenStreetMapService.buscarCoordenadas(direccionCompleta.toString(),
                new OpenStreetMapService.OnLocationFoundListener() {
                    @Override
                    public void onLocationFound(OpenStreetMapService.LocationResult result) {
                        if (!isAdded()) return; // Verificar que el fragment siga activo

                        mostrarProgreso(false, "");
                        btnBuscarCoordenadas.setEnabled(true);

                        if (result.success) {
                            latitud = result.latitude;
                            longitud = result.longitude;

                            tvCoordenadas.setText(String.format("Lat: %.6f, Lng: %.6f",
                                    latitud, longitud));
                            layoutUbicacionInfo.setVisibility(View.VISIBLE);

                            tvEstadoBusqueda.setText("✓ Ubicación encontrada");
                            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                            tvEstadoBusqueda.setVisibility(View.VISIBLE);

                            btnBuscarLugares.setEnabled(true);
                            btnGuardarUbicacion.setEnabled(true);

                            // Auto-completar campos si están vacíos
                            autoCompletarCampos(result.address);

                            // Limpiar lugares históricos anteriores
                            lugaresHistoricos.clear();
                            adapter.notifyDataSetChanged();

                            // ✅ LOG: Coordenadas encontradas exitosamente
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Encontró coordenadas exitosamente: Lat " + String.format("%.6f", latitud) +
                                            ", Lng " + String.format("%.6f", longitud) + " para dirección: " + direccionCompleta.toString()
                            );

                        } else {
                            tvEstadoBusqueda.setText("✗ No se encontró la ubicación");
                            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                            tvEstadoBusqueda.setVisibility(View.VISIBLE);

                            btnBuscarLugares.setEnabled(false);
                            btnGuardarUbicacion.setEnabled(false);
                            layoutUbicacionInfo.setVisibility(View.GONE);

                            // ✅ LOG: No se encontró la ubicación
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.ERROR,
                                    adminId,
                                    "No se encontró ubicación para: " + direccionCompleta.toString()
                            );
                        }
                    }
                });
    }

    private void autoCompletarCampos(String direccionCompleta) {
        String[] partes = direccionCompleta.split(",");

        if (partes.length >= 2 && etCiudad.getText().toString().trim().isEmpty()) {
            // Intentar extraer la ciudad (penúltima parte)
            String posibleCiudad = partes[partes.length - 2].trim();
            etCiudad.setText(posibleCiudad);
        }

        if (partes.length >= 1 && etPais.getText().toString().trim().isEmpty()) {
            // Última parte como país
            String posiblePais = partes[partes.length - 1].trim();
            etPais.setText(posiblePais);
        }

        // ✅ LOG: Auto-completado de campos
        if (partes.length > 1) {
            LogUtils.registrarActividad(
                    LogUtils.ActionType.SYSTEM,
                    adminId,
                    "Auto-completó campos de ubicación desde la dirección encontrada"
            );
        }
    }

    private void buscarLugaresHistoricos() {
        if (latitud == 0 && longitud == 0) {
            Toast.makeText(getContext(), "Primero busca las coordenadas del hotel",
                    Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - no hay coordenadas
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó buscar lugares históricos sin coordenadas válidas"
            );
            return;
        }

        // ✅ LOG: Inicio de búsqueda de lugares históricos
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Inició búsqueda de lugares históricos en radio de 5km desde Lat: " +
                        String.format("%.6f", latitud) + ", Lng: " + String.format("%.6f", longitud)
        );

        mostrarProgreso(true, "Buscando lugares históricos cercanos...");
        btnBuscarLugares.setEnabled(false);

        OpenStreetMapService.buscarLugaresHistoricos(latitud, longitud, 5.0, // 5km de radio
                new OpenStreetMapService.OnHistoricalPlacesFoundListener() {
                    @Override
                    public void onHistoricalPlacesFound(List<LugarHistorico> lugares) {
                        if (!isAdded()) return; // Verificar que el fragment siga activo

                        mostrarProgreso(false, "");
                        btnBuscarLugares.setEnabled(true);

                        lugaresHistoricos.clear();
                        lugaresHistoricos.addAll(lugares);
                        adapter.notifyDataSetChanged();

                        if (lugares.isEmpty()) {
                            tvEstadoBusqueda.setText("ℹ️ No se encontraron lugares históricos en 5km a la redonda");
                            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

                            // ✅ LOG: No se encontraron lugares históricos
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "No se encontraron lugares históricos en radio de 5km"
                            );
                        } else {
                            tvEstadoBusqueda.setText("✓ Se encontraron " + lugares.size() +
                                    " lugares históricos cercanos");
                            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));

                            // ✅ LOG: Lugares históricos encontrados
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Encontró " + lugares.size() + " lugares históricos cercanos"
                            );
                        }
                        tvEstadoBusqueda.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void mostrarProgreso(boolean mostrar, String mensaje) {
        if (mostrar) {
            progressBar.setVisibility(View.VISIBLE);
            tvEstadoBusqueda.setText(mensaje);
            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark));
            tvEstadoBusqueda.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void toggleLugarSeleccionado(LugarHistorico lugar) {
        boolean wasSelected = lugar.isSeleccionado();
        lugar.setSeleccionado(!lugar.isSeleccionado());

        // ✅ LOG: Cambio de selección de lugar histórico
        LogUtils.registrarActividad(
                LogUtils.ActionType.UPDATE,
                adminId,
                (lugar.isSeleccionado() ? "Seleccionó" : "Deseleccionó") +
                        " lugar histórico: " + lugar.getNombre() + " (" + lugar.getTipoLugar() + ")"
        );

        // Usar Handler para evitar conflictos con el layout del RecyclerView
        if (rvLugaresHistoricos != null) {
            rvLugaresHistoricos.post(() -> {
                // Notificar solo el item específico que cambió
                int position = lugaresHistoricos.indexOf(lugar);
                if (position >= 0 && adapter != null) {
                    adapter.notifyItemChanged(position);
                }
            });
        }
    }

    private void guardarConfiguracion() {
        if (latitud == 0 && longitud == 0) {
            Toast.makeText(getContext(), "Primero busca las coordenadas del hotel",
                    Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - intentó guardar sin coordenadas
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó guardar configuración sin coordenadas válidas"
            );
            return;
        }

        String direccion = etDireccion.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String pais = etPais.getText().toString().trim();

        if (direccion.isEmpty() || ciudad.isEmpty() || pais.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - campos incompletos
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó guardar configuración con campos incompletos"
            );
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado",
                    Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - usuario no autenticado
            LogUtils.logError("Usuario no autenticado al intentar guardar ubicación", adminId);
            return;
        }

        // ✅ LOG: Inicio del proceso de guardado
        LogUtils.registrarActividad(
                LogUtils.ActionType.UPDATE,
                adminId,
                "Inició proceso de guardado de configuración de ubicación"
        );

        mostrarProgreso(true, "Guardando configuración...");

        // Obtener hotel asignado al administrador
        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String hotelId = userDoc.getString("hotelAsignado");
                    if (hotelId != null) {
                        guardarUbicacionHotel(hotelId, direccion, ciudad, pais);
                    } else {
                        mostrarProgreso(false, "");
                        Toast.makeText(getContext(), "Error: Sin hotel asignado",
                                Toast.LENGTH_SHORT).show();

                        // ✅ LOG: Error - sin hotel asignado
                        LogUtils.logError("Admin sin hotel asignado al intentar guardar ubicación", adminId);
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false, "");
                    Toast.makeText(getContext(), "Error al obtener datos del hotel",
                            Toast.LENGTH_SHORT).show();
                    Log.e("AdminUbicacion", "Error al obtener hotel asignado", e);

                    // ✅ LOG: Error al obtener datos del hotel
                    LogUtils.logError("Error al obtener hotel asignado: " + e.getMessage(), adminId);
                });
    }

    private void guardarUbicacionHotel(String hotelId, String direccion, String ciudad, String pais) {
        // Preparar datos de ubicación
        Map<String, Object> ubicacionData = new HashMap<>();
        ubicacionData.put("latitud", latitud);
        ubicacionData.put("longitud", longitud);
        ubicacionData.put("direccion", direccion);
        ubicacionData.put("ciudad", ciudad);
        ubicacionData.put("pais", pais);

        // Preparar lugares históricos seleccionados
        List<Map<String, Object>> lugaresSeleccionados = new ArrayList<>();
        for (LugarHistorico lugar : lugaresHistoricos) {
            if (lugar.isSeleccionado()) {
                Map<String, Object> lugarData = new HashMap<>();
                lugarData.put("nombre", lugar.getNombre());
                lugarData.put("descripcion", lugar.getDescripcion());
                lugarData.put("latitud", lugar.getUbicacion().latitude);
                lugarData.put("longitud", lugar.getUbicacion().longitude);
                lugarData.put("distancia", lugar.getDistancia());
                lugarData.put("tipoLugar", lugar.getTipoLugar());
                lugaresSeleccionados.add(lugarData);
            }
        }

        // Actualizar documento del hotel
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("ubicacion", ubicacionData);
        updateData.put("lugaresHistoricos", lugaresSeleccionados);

        db.collection("hoteles").document(hotelId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgreso(false, "");
                    Toast.makeText(getContext(), "✓ Configuración guardada exitosamente",
                            Toast.LENGTH_LONG).show();

                    tvEstadoBusqueda.setText("✓ Configuración guardada - " +
                            lugaresSeleccionados.size() + " lugares históricos seleccionados");
                    tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                    tvEstadoBusqueda.setVisibility(View.VISIBLE);

                    Log.d("AdminUbicacion", "Ubicación guardada para hotel: " + hotelId +
                            " con " + lugaresSeleccionados.size() + " lugares históricos");

                    // ✅ LOG: Configuración guardada exitosamente
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.UPDATE,
                            adminId,
                            "Guardó configuración de ubicación exitosamente - Hotel: " + hotelId +
                                    " - Dirección: " + direccion + ", " + ciudad + ", " + pais +
                                    " - Lugares históricos: " + lugaresSeleccionados.size() +
                                    " - Coordenadas: Lat " + String.format("%.6f", latitud) + ", Lng " + String.format("%.6f", longitud)
                    );
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false, "");
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("AdminUbicacion", "Error al guardar ubicación", e);

                    // ✅ LOG: Error al guardar configuración
                    LogUtils.logError("Error al guardar configuración de ubicación - Hotel: " + hotelId + " - Error: " + e.getMessage(), adminId);
                });
    }

    private void loadExistingData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String hotelId = userDoc.getString("hotelAsignado");
                    if (hotelId != null) {
                        cargarUbicacionExistente(hotelId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminUbicacion", "Error al cargar datos existentes", e);

                    // ✅ LOG: Error al cargar datos existentes
                    LogUtils.logError("Error al cargar datos existentes de ubicación: " + e.getMessage(), adminId);
                });
    }

    private void cargarUbicacionExistente(String hotelId) {
        db.collection("hoteles").document(hotelId)
                .get()
                .addOnSuccessListener(hotelDoc -> {
                    if (hotelDoc.exists()) {
                        Map<String, Object> ubicacion = (Map<String, Object>) hotelDoc.get("ubicacion");
                        if (ubicacion != null) {
                            latitud = (Double) ubicacion.get("latitud");
                            longitud = (Double) ubicacion.get("longitud");

                            etDireccion.setText((String) ubicacion.get("direccion"));
                            etCiudad.setText((String) ubicacion.get("ciudad"));
                            etPais.setText((String) ubicacion.get("pais"));

                            tvCoordenadas.setText(String.format("Lat: %.6f, Lng: %.6f",
                                    latitud, longitud));
                            layoutUbicacionInfo.setVisibility(View.VISIBLE);

                            btnBuscarLugares.setEnabled(true);
                            btnGuardarUbicacion.setEnabled(true);

                            // Cargar lugares históricos existentes
                            List<Map<String, Object>> lugares =
                                    (List<Map<String, Object>>) hotelDoc.get("lugaresHistoricos");
                            if (lugares != null) {
                                cargarLugaresExistentes(lugares);
                            }

                            tvEstadoBusqueda.setText("✓ Configuración cargada exitosamente");
                            tvEstadoBusqueda.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                            tvEstadoBusqueda.setVisibility(View.VISIBLE);

                            // ✅ LOG: Configuración cargada exitosamente
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Cargó configuración existente de ubicación - Hotel: " + hotelId +
                                            " - Lugares históricos: " + (lugares != null ? lugares.size() : 0)
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminUbicacion", "Error al cargar ubicación existente", e);

                    // ✅ LOG: Error al cargar ubicación existente
                    LogUtils.logError("Error al cargar ubicación existente - Hotel: " + hotelId + " - Error: " + e.getMessage(), adminId);
                });
    }

    private void cargarLugaresExistentes(List<Map<String, Object>> lugares) {
        lugaresHistoricos.clear();
        for (Map<String, Object> lugarData : lugares) {
            LugarHistorico lugar = new LugarHistorico(
                    (String) lugarData.get("nombre"),
                    (String) lugarData.get("descripcion"),
                    new LatLng((Double) lugarData.get("latitud"), (Double) lugarData.get("longitud")),
                    ((Number) lugarData.get("distancia")).floatValue(),
                    (String) lugarData.get("tipoLugar")
            );
            lugar.setSeleccionado(true);
            lugaresHistoricos.add(lugar);
        }

        adapter.notifyDataSetChanged();
    }
}