package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminInicioFragment extends Fragment {

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
        tvReservasHoy = view.findViewById(R.id.tvReservasHoy);
        tvOcupacion = view.findViewById(R.id.tvOcupacion);
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
                            loadDefaultDashboardData();
                        }

                        Log.d("AdminInicio", "Datos del admin cargados: " + nombreAdmin + ", Hotel: " + hotelAsignado);

                    } else {
                        Log.e("AdminInicio", "Documento del usuario no encontrado");
                        tvBienvenida.setText("¡Bienvenido Administrador!");
                        tvHotelNombre.setText("Hotel no encontrado");
                        loadDefaultDashboardData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminInicio", "Error al cargar datos del usuario: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                    tvBienvenida.setText("¡Bienvenido Administrador!");
                    tvHotelNombre.setText("Error al cargar hotel");
                    loadDefaultDashboardData();
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

                        // Cargar estadísticas del hotel
                        loadHotelStatistics(hotelId);

                    } else {
                        Log.w("AdminInicio", "Documento del hotel no encontrado");
                        tvHotelNombre.setText("Hotel no encontrado");
                        loadDefaultDashboardData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminInicio", "Error al cargar datos del hotel: " + e.getMessage());
                    tvHotelNombre.setText("Error al cargar hotel");
                    loadDefaultDashboardData();
                });
    }

    private void loadHotelStatistics(String hotelId) {
        // Por ahora, datos estáticos. Después se puede conectar con reservas reales
        tvReservasHoy.setText("5");
        tvOcupacion.setText("75%");

        // TODO: Implementar consultas reales a la colección de reservas
        // loadReservasHoyFromFirestore(hotelId);
        // loadOcupacionFromFirestore(hotelId);

        Log.d("AdminInicio", "Estadísticas cargadas para hotel: " + hotelId);
    }

    private void loadDefaultDashboardData() {
        // Datos por defecto cuando no se puede cargar información del hotel
        tvReservasHoy.setText("0");
        tvOcupacion.setText("0%");
    }

    // TODO: Métodos para implementar cuando tengas la colección de reservas
    /*
    private void loadReservasHoyFromFirestore(String hotelId) {
        // Contar reservas del día actual para este hotel
        db.collection("reservas")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("fecha", getCurrentDate())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    tvReservasHoy.setText(String.valueOf(count));
                });
    }

    private void loadOcupacionFromFirestore(String hotelId) {
        // Calcular porcentaje de ocupación basado en habitaciones ocupadas vs disponibles
        // Implementar lógica específica según tu estructura de datos
    }
    */
}