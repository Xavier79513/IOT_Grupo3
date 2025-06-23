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
import com.example.telehotel.core.storage.PrefsManager; // ✅ AGREGAR IMPORT
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.features.admin.HabitacionesAdapter;
import com.example.telehotel.core.utils.LogUtils; // ✅ AGREGAR IMPORT
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

    // ✅ AGREGAR para logs
    private String adminId;
    private String adminName;

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

        // ✅ OBTENER DATOS DEL ADMIN PARA LOGS
        PrefsManager prefsManager = new PrefsManager(requireContext());
        adminId = prefsManager.getUserId();
        adminName = prefsManager.getUserName();

        initFirebase();
        initViews(view);
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
        obtenerHotelDelAdmin();

        // ✅ LOG: Acceso al módulo de habitaciones
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Accedió al módulo de gestión de habitaciones (Admin: " + adminName + ")"
        );
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

            // ✅ LOG: Error - usuario no autenticado
            LogUtils.logError("Usuario no autenticado al acceder a gestión de habitaciones", adminId);
            return;
        }

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hotelId = documentSnapshot.getString("hotelAsignado");
                        if (hotelId != null && !hotelId.isEmpty()) {
                            // ✅ LOG: Hotel asignado encontrado
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Hotel asignado identificado: " + hotelId
                            );
                            cargarHabitaciones();
                        } else {
                            Toast.makeText(getContext(), "Error: Administrador sin hotel asignado", Toast.LENGTH_LONG).show();

                            // ✅ LOG: Error - admin sin hotel asignado
                            LogUtils.logError("Administrador sin hotel asignado al acceder a gestión de habitaciones", adminId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HabitacionesFragment", "Error al obtener hotel del admin: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al obtener datos del admin
                    LogUtils.logError("Error al obtener hotel del admin en gestión de habitaciones: " + e.getMessage(), adminId);
                });
    }

    private void registrarHabitacion() {
        if (!validarCampos()) {
            return;
        }

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Hotel no identificado", Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - hotel no identificado
            LogUtils.logError("Intentó registrar habitación sin hotel identificado", adminId);
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

        // ✅ LOG: Inicio del proceso de registro
        LogUtils.registrarActividad(
                LogUtils.ActionType.CREATE,
                adminId,
                "Inició registro de habitación - Hotel: " + hotelId +
                        " - Número: " + numero + " - Tipo: " + tipo +
                        " - Capacidad: " + adultos + " adultos, " + ninos + " niños" +
                        " - Tamaño: " + tamaño + "m² - Precio: S/" + precio
        );

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

                        // ✅ LOG: Error - habitación duplicada
                        LogUtils.registrarActividad(
                                LogUtils.ActionType.ERROR,
                                adminId,
                                "Intentó registrar habitación con número duplicado: " + numero + " en hotel: " + hotelId
                        );
                        return;
                    }

                    // Guardar en Firestore
                    db.collection("habitaciones")
                            .add(habitacion)
                            .addOnSuccessListener(documentReference -> {
                                mostrarProgreso(false);
                                Toast.makeText(getContext(), "Habitación registrada exitosamente", Toast.LENGTH_SHORT).show();

                                // ✅ LOG: Habitación registrada exitosamente
                                LogUtils.registrarActividad(
                                        LogUtils.ActionType.CREATE,
                                        adminId,
                                        "Registró habitación exitosamente - ID: " + documentReference.getId() +
                                                " - Hotel: " + hotelId + " - Número: " + numero + " - Tipo: " + tipo +
                                                " - Capacidad: " + adultos + " adultos, " + ninos + " niños" +
                                                " - Tamaño: " + tamaño + "m² - Precio: S/" + precio
                                );

                                limpiarFormulario();
                                cargarHabitaciones(); // Recargar lista
                            })
                            .addOnFailureListener(e -> {
                                mostrarProgreso(false);
                                Log.e("HabitacionesFragment", "Error al registrar habitación: " + e.getMessage());
                                Toast.makeText(getContext(), "Error al registrar habitación: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                // ✅ LOG: Error al registrar habitación
                                LogUtils.logError("Error al registrar habitación " + numero + " en hotel " + hotelId + ": " + e.getMessage(), adminId);
                            });
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e("HabitacionesFragment", "Error al verificar habitación: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al verificar habitación", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al verificar habitación
                    LogUtils.logError("Error al verificar habitación duplicada " + numero + " en hotel " + hotelId + ": " + e.getMessage(), adminId);
                });
    }

    private boolean validarCampos() {
        List<String> errores = new ArrayList<>();

        // Validar número
        if (TextUtils.isEmpty(etNumero.getText())) {
            etNumero.setError("El número de habitación es requerido");
            etNumero.requestFocus();
            errores.add("número vacío");
        }

        // Validar tipo
        if (TextUtils.isEmpty(spinnerTipo.getText())) {
            spinnerTipo.setError("Seleccione el tipo de habitación");
            spinnerTipo.requestFocus();
            errores.add("tipo no seleccionado");
        }

        // Validar capacidad adultos
        if (TextUtils.isEmpty(etCapacidadAdultos.getText())) {
            etCapacidadAdultos.setError("La capacidad de adultos es requerida");
            etCapacidadAdultos.requestFocus();
            errores.add("capacidad adultos vacía");
        } else {
            try {
                int adultos = Integer.parseInt(etCapacidadAdultos.getText().toString());
                if (adultos <= 0 || adultos > 10) {
                    etCapacidadAdultos.setError("Capacidad de adultos debe ser entre 1 y 10");
                    etCapacidadAdultos.requestFocus();
                    errores.add("capacidad adultos fuera de rango");
                }
            } catch (NumberFormatException e) {
                etCapacidadAdultos.setError("Ingrese un número válido");
                etCapacidadAdultos.requestFocus();
                errores.add("capacidad adultos inválida");
            }
        }

        // Validar capacidad niños
        if (TextUtils.isEmpty(etCapacidadNinos.getText())) {
            etCapacidadNinos.setError("La capacidad de niños es requerida");
            etCapacidadNinos.requestFocus();
            errores.add("capacidad niños vacía");
        } else {
            try {
                int ninos = Integer.parseInt(etCapacidadNinos.getText().toString());
                if (ninos < 0 || ninos > 6) {
                    etCapacidadNinos.setError("Capacidad de niños debe ser entre 0 y 6");
                    etCapacidadNinos.requestFocus();
                    errores.add("capacidad niños fuera de rango");
                }
            } catch (NumberFormatException e) {
                etCapacidadNinos.setError("Ingrese un número válido");
                etCapacidadNinos.requestFocus();
                errores.add("capacidad niños inválida");
            }
        }

        // Validar tamaño
        if (TextUtils.isEmpty(etTamaño.getText())) {
            etTamaño.setError("El tamaño es requerido");
            etTamaño.requestFocus();
            errores.add("tamaño vacío");
        } else {
            try {
                double tamaño = Double.parseDouble(etTamaño.getText().toString());
                if (tamaño <= 0 || tamaño > 200) {
                    etTamaño.setError("El tamaño debe ser entre 1 y 200 m²");
                    etTamaño.requestFocus();
                    errores.add("tamaño fuera de rango");
                }
            } catch (NumberFormatException e) {
                etTamaño.setError("Ingrese un número válido");
                etTamaño.requestFocus();
                errores.add("tamaño inválido");
            }
        }

        // Validar precio
        if (TextUtils.isEmpty(etPrecio.getText())) {
            etPrecio.setError("El precio es requerido");
            etPrecio.requestFocus();
            errores.add("precio vacío");
        } else {
            try {
                double precio = Double.parseDouble(etPrecio.getText().toString());
                if (precio <= 0 || precio > 5000) {
                    etPrecio.setError("El precio debe ser entre 1 y 5000 soles");
                    etPrecio.requestFocus();
                    errores.add("precio fuera de rango");
                }
            } catch (NumberFormatException e) {
                etPrecio.setError("Ingrese un número válido");
                etPrecio.requestFocus();
                errores.add("precio inválido");
            }
        }

        // ✅ LOG: Errores de validación si existen
        if (!errores.isEmpty()) {
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Errores de validación al registrar habitación: " + String.join(", ", errores)
            );
        }

        return errores.isEmpty();
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

                    // ✅ LOG: Habitaciones cargadas exitosamente
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.SYSTEM,
                            adminId,
                            "Cargó lista de habitaciones - Hotel: " + hotelId + " - Total: " + habitacionesList.size() + " habitaciones"
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e("HabitacionesFragment", "Error al cargar habitaciones: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar habitaciones", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al cargar habitaciones
                    LogUtils.logError("Error al cargar habitaciones del hotel " + hotelId + ": " + e.getMessage(), adminId);
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

        // ✅ LOG: Formulario limpiado
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Limpió formulario de registro de habitaciones"
        );
    }

    private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrar.setEnabled(!mostrar);
        btnRegistrar.setText(mostrar ? "Registrando..." : "Registrar Habitación");
    }
}