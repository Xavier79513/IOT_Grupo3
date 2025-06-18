package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.features.admin.HabitacionesAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HabitacionesFragment extends Fragment {

    // Vistas del formulario
    private TextInputEditText etNumero, etCapacidadAdultos, etCapacidadNinos, etTamaño, etPrecio;
    private AutoCompleteTextView spinnerTipo;
    private MaterialButton btnRegistrar, btnLimpiar;
    private ProgressBar progressBar;

    // RecyclerView para lista de habitaciones
    private RecyclerView recyclerHabitaciones;
    private HabitacionesAdapter habitacionesAdapter;
    private List<Habitacion> habitacionesList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String hotelId;

    // Tipos de habitación
    private final String[] tiposHabitacion = {"Económica", "Estándar", "Lux"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habitaciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        initViews(view);
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
        obtenerHotelDelAdmin();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        etNumero = view.findViewById(R.id.etNumero);
        etCapacidadAdultos = view.findViewById(R.id.etCapacidadAdultos);
        etCapacidadNinos = view.findViewById(R.id.etCapacidadNinos);
        etTamaño = view.findViewById(R.id.etTamaño);
        etPrecio = view.findViewById(R.id.etPrecio);
        spinnerTipo = view.findViewById(R.id.spinnerTipo);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerHabitaciones = view.findViewById(R.id.recyclerHabitaciones);
    }

    private void setupSpinners() {
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, tiposHabitacion);
        spinnerTipo.setAdapter(tipoAdapter);
    }

    private void setupRecyclerView() {
        habitacionesAdapter = new HabitacionesAdapter(habitacionesList);
        recyclerHabitaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHabitaciones.setAdapter(habitacionesAdapter);
    }

    private void setupClickListeners() {
        btnRegistrar.setOnClickListener(v -> registrarHabitacion());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
    }

    private void obtenerHotelDelAdmin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hotelId = documentSnapshot.getString("hotelAsignado");
                        if (hotelId != null && !hotelId.isEmpty()) {
                            cargarHabitaciones();
                        } else {
                            Toast.makeText(getContext(), "Error: Administrador sin hotel asignado", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HabitacionesFragment", "Error al obtener hotel del admin: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                });
    }

    private void registrarHabitacion() {
        if (!validarCampos()) {
            return;
        }

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Hotel no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarProgreso(true);

        // Obtener datos del formulario
        String numero = etNumero.getText().toString().trim();
        String tipo = spinnerTipo.getText().toString().trim().toLowerCase();
        int adultos = Integer.parseInt(etCapacidadAdultos.getText().toString().trim());
        int ninos = Integer.parseInt(etCapacidadNinos.getText().toString().trim());
        double tamaño = Double.parseDouble(etTamaño.getText().toString().trim());
        double precio = Double.parseDouble(etPrecio.getText().toString().trim());

        // Crear habitación
        Habitacion habitacion = new Habitacion(numero, tipo, adultos, ninos, tamaño, precio,
                hotelId, mAuth.getCurrentUser().getUid());

        // Verificar que no exista habitación con el mismo número
        db.collection("habitaciones")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("numero", numero)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        mostrarProgreso(false);
                        Toast.makeText(getContext(), "Ya existe una habitación con el número " + numero, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Guardar en Firestore
                    db.collection("habitaciones")
                            .add(habitacion)
                            .addOnSuccessListener(documentReference -> {
                                mostrarProgreso(false);
                                Toast.makeText(getContext(), "Habitación registrada exitosamente", Toast.LENGTH_SHORT).show();
                                limpiarFormulario();
                                cargarHabitaciones(); // Recargar lista
                            })
                            .addOnFailureListener(e -> {
                                mostrarProgreso(false);
                                Log.e("HabitacionesFragment", "Error al registrar habitación: " + e.getMessage());
                                Toast.makeText(getContext(), "Error al registrar habitación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e("HabitacionesFragment", "Error al verificar habitación: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al verificar habitación", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validarCampos() {
        // Validar número
        if (TextUtils.isEmpty(etNumero.getText())) {
            etNumero.setError("El número de habitación es requerido");
            etNumero.requestFocus();
            return false;
        }

        // Validar tipo
        if (TextUtils.isEmpty(spinnerTipo.getText())) {
            spinnerTipo.setError("Seleccione el tipo de habitación");
            spinnerTipo.requestFocus();
            return false;
        }

        // Validar capacidad adultos
        if (TextUtils.isEmpty(etCapacidadAdultos.getText())) {
            etCapacidadAdultos.setError("La capacidad de adultos es requerida");
            etCapacidadAdultos.requestFocus();
            return false;
        }

        try {
            int adultos = Integer.parseInt(etCapacidadAdultos.getText().toString());
            if (adultos <= 0 || adultos > 10) {
                etCapacidadAdultos.setError("Capacidad de adultos debe ser entre 1 y 10");
                etCapacidadAdultos.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etCapacidadAdultos.setError("Ingrese un número válido");
            etCapacidadAdultos.requestFocus();
            return false;
        }

        // Validar capacidad niños
        if (TextUtils.isEmpty(etCapacidadNinos.getText())) {
            etCapacidadNinos.setError("La capacidad de niños es requerida");
            etCapacidadNinos.requestFocus();
            return false;
        }

        try {
            int ninos = Integer.parseInt(etCapacidadNinos.getText().toString());
            if (ninos < 0 || ninos > 6) {
                etCapacidadNinos.setError("Capacidad de niños debe ser entre 0 y 6");
                etCapacidadNinos.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etCapacidadNinos.setError("Ingrese un número válido");
            etCapacidadNinos.requestFocus();
            return false;
        }

        // Validar tamaño
        if (TextUtils.isEmpty(etTamaño.getText())) {
            etTamaño.setError("El tamaño es requerido");
            etTamaño.requestFocus();
            return false;
        }

        try {
            double tamaño = Double.parseDouble(etTamaño.getText().toString());
            if (tamaño <= 0 || tamaño > 200) {
                etTamaño.setError("El tamaño debe ser entre 1 y 200 m²");
                etTamaño.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etTamaño.setError("Ingrese un número válido");
            etTamaño.requestFocus();
            return false;
        }

        // Validar precio
        if (TextUtils.isEmpty(etPrecio.getText())) {
            etPrecio.setError("El precio es requerido");
            etPrecio.requestFocus();
            return false;
        }

        try {
            double precio = Double.parseDouble(etPrecio.getText().toString());
            if (precio <= 0 || precio > 5000) {
                etPrecio.setError("El precio debe ser entre 1 y 5000 soles");
                etPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrecio.setError("Ingrese un número válido");
            etPrecio.requestFocus();
            return false;
        }

        return true;
    }

    private void cargarHabitaciones() {
        if (hotelId == null || hotelId.isEmpty()) {
            return;
        }

        db.collection("habitaciones")
                .whereEqualTo("hotelId", hotelId)
                .orderBy("numero", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    habitacionesList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Habitacion habitacion = doc.toObject(Habitacion.class);
                        if (habitacion != null) {
                            habitacion.setId(doc.getId());
                            habitacionesList.add(habitacion);
                        }
                    }

                    habitacionesAdapter.notifyDataSetChanged();
                    Log.d("HabitacionesFragment", "Habitaciones cargadas: " + habitacionesList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("HabitacionesFragment", "Error al cargar habitaciones: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar habitaciones", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarFormulario() {
        etNumero.setText("");
        etCapacidadAdultos.setText("");
        etCapacidadNinos.setText("");
        etTamaño.setText("");
        etPrecio.setText("");
        spinnerTipo.setText("");

        // Limpiar errores
        etNumero.setError(null);
        etCapacidadAdultos.setError(null);
        etCapacidadNinos.setError(null);
        etTamaño.setError(null);
        etPrecio.setError(null);
        spinnerTipo.setError(null);
    }

    private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrar.setEnabled(!mostrar);
        btnRegistrar.setText(mostrar ? "Registrando..." : "Registrar Habitación");
    }
}