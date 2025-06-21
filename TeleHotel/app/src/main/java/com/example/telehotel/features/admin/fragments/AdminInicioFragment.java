package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;


import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/*public class AdminInicioFragment extends Fragment {

    private TextView tvBienvenida, tvHotelNombre, tvReservasHoy, tvOcupacion;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadAdminData();
    }

    private void initViews(View view) {
        tvBienvenida = view.findViewById(R.id.tvBienvenida);
        tvHotelNombre = view.findViewById(R.id.tvHotelNombre);
    }

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e("AdminInicio", "Usuario no autenticado");
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // 1. Obtener datos del administrador
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String nombreAdmin = userDoc.getString("nombres");
                        String hotelAsignado = userDoc.getString("hotelAsignado");

                        // Mostrar bienvenida con nombre del admin
                        if (nombreAdmin != null && !nombreAdmin.isEmpty()) {
                            tvBienvenida.setText("¡Bienvenido " + nombreAdmin + "!");
                        } else {
                            tvBienvenida.setText("¡Bienvenido Administrador!");
                        }

                        // 2. Obtener datos del hotel asignado
                        if (hotelAsignado != null && !hotelAsignado.isEmpty()) {
                            loadHotelData(hotelAsignado);
                        } else {
                            Log.w("AdminInicio", "Administrador sin hotel asignado");
                            tvHotelNombre.setText("Sin hotel asignado");
                        }

                        Log.d("AdminInicio", "Datos del admin cargados: " + nombreAdmin + ", Hotel: " + hotelAsignado);

                    } else {
                        Log.e("AdminInicio", "Documento del usuario no encontrado");
                        tvBienvenida.setText("¡Bienvenido Administrador!");
                        tvHotelNombre.setText("Hotel no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminInicio", "Error al cargar datos del usuario: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                    tvBienvenida.setText("¡Bienvenido Administrador!");
                    tvHotelNombre.setText("Error al cargar hotel");
                });
    }

    private void loadHotelData(String hotelId) {
        db.collection("hoteles").document(hotelId)
                .get()
                .addOnSuccessListener(hotelDoc -> {
                    if (hotelDoc.exists()) {
                        String nombreHotel = hotelDoc.getString("nombre");

                        if (nombreHotel != null && !nombreHotel.isEmpty()) {
                            tvHotelNombre.setText(nombreHotel);
                        } else {
                            tvHotelNombre.setText("Hotel sin nombre");
                        }

                        Log.d("AdminInicio", "Hotel cargado: " + nombreHotel);


                    } else {
                        Log.w("AdminInicio", "Documento del hotel no encontrado");
                        tvHotelNombre.setText("Hotel no encontrado");;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminInicio", "Error al cargar datos del hotel: " + e.getMessage());
                    tvHotelNombre.setText("Error al cargar hotel");
                });
    }


}*/
public class AdminInicioFragment extends Fragment {

    private static final String TAG = "AdminInicioFragment";

    // UI Components
    private TextView tvBienvenida, tvHotelNombre;
    private Button btnConfigurarUbicacion, btnGestionarImagenes;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variables para almacenar datos del admin y hotel
    private String hotelAsignadoId;
    private String nombreAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupClickListeners();
        loadAdminData();
    }

    private void initViews(View view) {
        try {
            tvBienvenida = view.findViewById(R.id.tvBienvenida);
            tvHotelNombre = view.findViewById(R.id.tvHotelNombre);
            btnConfigurarUbicacion = view.findViewById(R.id.btnConfigurarUbicacion);
            btnGestionarImagenes = view.findViewById(R.id.btnGestionarImagenes);

            Log.d(TAG, "Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage());
            Toast.makeText(getContext(), "Error inicializando la interfaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            // Listener para configurar ubicación
            btnConfigurarUbicacion.setOnClickListener(v -> {
                Log.d(TAG, "Botón Configurar Ubicación presionado");
                abrirConfiguracionUbicacion();
            });

            // Listener para gestionar imágenes
            btnGestionarImagenes.setOnClickListener(v -> {
                Log.d(TAG, "Botón Gestionar Imágenes presionado");
                abrirGestionImagenes();
            });

            Log.d(TAG, "Click listeners configurados");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando listeners: " + e.getMessage());
        }
    }

    private void abrirConfiguracionUbicacion() {
        try {
            if (getActivity() != null) {
                Log.d(TAG, "Navegando a AdminUbicacionFragment");

                // Verificar que el admin tenga un hotel asignado
                if (hotelAsignadoId == null || hotelAsignadoId.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No tienes un hotel asignado. Contacta al administrador del sistema.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Crear fragment con argumentos del hotel
                AdminUbicacionFragment ubicacionFragment = new AdminUbicacionFragment();
                Bundle args = new Bundle();
                args.putString("hotel_id", hotelAsignadoId);
                args.putString("admin_name", nombreAdmin);
                ubicacionFragment.setArguments(args);

                // Navegar al fragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, ubicacionFragment)
                        .addToBackStack("AdminUbicacion")
                        .commit();

                Toast.makeText(getContext(), "Abriendo configuración de ubicación...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navegando a configuración de ubicación: " + e.getMessage());
            Toast.makeText(getContext(), "Error abriendo configuración de ubicación", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGestionImagenes() {
        try {
            if (getActivity() != null) {
                Log.d(TAG, "Navegando a AdminImagenesFragment");

                // Verificar que el admin tenga un hotel asignado
                if (hotelAsignadoId == null || hotelAsignadoId.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No tienes un hotel asignado. Contacta al administrador del sistema.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Crear fragment para gestión de imágenes
                AdminImagenesFragment imagenesFragment = new AdminImagenesFragment();
                Bundle args = new Bundle();
                args.putString("hotel_id", hotelAsignadoId);
                args.putString("admin_name", nombreAdmin);
                imagenesFragment.setArguments(args);

                // Navegar al fragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, imagenesFragment)
                        .addToBackStack("AdminImagenes")
                        .commit();

                Toast.makeText(getContext(), "Abriendo gestión de imágenes...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navegando a gestión de imágenes: " + e.getMessage());
            Toast.makeText(getContext(), "Error abriendo gestión de imágenes", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado");
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Cargando datos para usuario: " + userId);

        // Obtener datos del administrador
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    try {
                        if (userDoc.exists()) {
                            nombreAdmin = userDoc.getString("nombres");
                            hotelAsignadoId = userDoc.getString("hotelAsignado");

                            // Si no existe hotelAsignado, intentar con hotelId
                            if (hotelAsignadoId == null || hotelAsignadoId.isEmpty()) {
                                hotelAsignadoId = userDoc.getString("hotelId");
                            }

                            Log.d(TAG, "Datos admin cargados - Nombre: " + nombreAdmin + ", Hotel: " + hotelAsignadoId);

                            // Mostrar bienvenida con nombre del admin
                            if (nombreAdmin != null && !nombreAdmin.isEmpty()) {
                                tvBienvenida.setText("¡Bienvenido " + nombreAdmin + "!");
                            } else {
                                tvBienvenida.setText("¡Bienvenido Administrador!");
                            }

                            // Cargar datos del hotel asignado
                            if (hotelAsignadoId != null && !hotelAsignadoId.isEmpty()) {
                                loadHotelData(hotelAsignadoId);
                            } else {
                                Log.w(TAG, "Administrador sin hotel asignado");
                                tvHotelNombre.setText("Sin hotel asignado");
                                // Deshabilitar botones si no hay hotel
                                btnConfigurarUbicacion.setEnabled(false);
                                btnGestionarImagenes.setEnabled(false);
                                btnConfigurarUbicacion.setText("Hotel no asignado");
                                btnGestionarImagenes.setText("Hotel no asignado");
                            }

                        } else {
                            Log.e(TAG, "Documento del usuario no encontrado");
                            tvBienvenida.setText("¡Bienvenido Administrador!");
                            tvHotelNombre.setText("Usuario no encontrado");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando datos del usuario: " + e.getMessage());
                        Toast.makeText(getContext(), "Error procesando datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del usuario: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                    tvBienvenida.setText("¡Bienvenido Administrador!");
                    tvHotelNombre.setText("Error al cargar hotel");
                });
    }

    private void loadHotelData(String hotelId) {
        Log.d(TAG, "Cargando datos del hotel: " + hotelId);

        db.collection("hoteles").document(hotelId)
                .get()
                .addOnSuccessListener(hotelDoc -> {
                    try {
                        if (hotelDoc.exists()) {
                            String nombreHotel = hotelDoc.getString("nombre");

                            if (nombreHotel != null && !nombreHotel.isEmpty()) {
                                tvHotelNombre.setText(nombreHotel);
                            } else {
                                tvHotelNombre.setText("Hotel sin nombre");
                            }

                            Log.d(TAG, "Hotel cargado exitosamente: " + nombreHotel);

                        } else {
                            Log.w(TAG, "Documento del hotel no encontrado: " + hotelId);
                            tvHotelNombre.setText("Hotel no encontrado");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando datos del hotel: " + e.getMessage());
                        tvHotelNombre.setText("Error procesando hotel");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del hotel: " + e.getMessage());
                    tvHotelNombre.setText("Error al cargar hotel");
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, recargando datos...");

        // Recargar datos cuando el fragment vuelve a estar visible
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            loadAdminData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destruido");
    }
}