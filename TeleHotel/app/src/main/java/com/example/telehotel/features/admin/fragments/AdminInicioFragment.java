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

    private TextView tvBienvenida, tvHotelNombre, tvReservasHoy, tvOcupacion;
    private Button btnConfigurarUbicacion;

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
        btnConfigurarUbicacion = view.findViewById(R.id.btnConfigurarUbicacion);

        // Configurar listener para el botón
        btnConfigurarUbicacion.setOnClickListener(v -> abrirConfiguracionUbicacion());
    }

    private void abrirConfiguracionUbicacion() {
        // Navegar al fragment de ubicación
        if (getActivity() != null) {
            // CAMBIA 'fragment_container' por el ID correcto de tu contenedor
            // Posibles IDs comunes: R.id.container, R.id.main_container, R.id.frameLayout
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new AdminUbicacionFragment())// Cambia aquí el ID
                    .addToBackStack(null)
                    .commit();
        }
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
                        tvHotelNombre.setText("Hotel no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminInicio", "Error al cargar datos del hotel: " + e.getMessage());
                    tvHotelNombre.setText("Error al cargar hotel");
                });
    }
}