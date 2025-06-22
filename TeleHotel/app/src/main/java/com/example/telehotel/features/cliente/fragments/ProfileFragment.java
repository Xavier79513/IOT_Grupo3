package com.example.telehotel.features.cliente.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;

/*public class ProfileFragment extends Fragment {

    private TextView tvNombre, tvDni;
    private EditText etNombre, etDni;
    private ImageButton btnEditName, btnEditDni, btnEditPhone,btnEditAddress,btnEditBirthdate;
    private MaterialButton btnSaveChanges;


    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvDni = view.findViewById(R.id.tvDni);
        etNombre = view.findViewById(R.id.etNombre);
        etDni = view.findViewById(R.id.etDni);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnEditPhone=view.findViewById(R.id.btnEditPhone);
        btnEditAddress=view.findViewById(R.id.btnEditAddress);
        btnEditBirthdate=view.findViewById(R.id.btnEditBirthdate);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);



        prefs = requireContext().getSharedPreferences("PerfilPrefs", Context.MODE_PRIVATE);

        // Cargar datos guardados o valores por defecto
        cargarDatos();

        // Configurar botones de edición
        btnEditName.setOnClickListener(v -> {
            tvNombre.setVisibility(View.GONE);
            etNombre.setVisibility(View.VISIBLE);
            etNombre.requestFocus();
            etNombre.setSelection(etNombre.getText().length());
        });


        // Guardar cambios al pulsar botón
        btnSaveChanges.setOnClickListener(v -> {
            String nombreNuevo = etNombre.getText().toString().trim();
            String dniNuevo = etDni.getText().toString().trim();

            if (nombreNuevo.isEmpty() || dniNuevo.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar en SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nombre", nombreNuevo);
            editor.putString("dni", dniNuevo);
            editor.apply();

            // Actualizar vistas
            tvNombre.setText(nombreNuevo);
            tvDni.setText(dniNuevo);

            // Mostrar TextViews y ocultar EditTexts
            tvNombre.setVisibility(View.VISIBLE);
            etNombre.setVisibility(View.GONE);

            tvDni.setVisibility(View.VISIBLE);
            etDni.setVisibility(View.GONE);

            Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatos() {
        String nombre = prefs.getString("nombre", "John Smith");
        String dni = prefs.getString("dni", "71030722");

        tvNombre.setText(nombre);
        etNombre.setText(nombre);

        tvDni.setText(dni);
        etDni.setText(dni);

        // Asegura que los EditText estén ocultos al iniciar
        etNombre.setVisibility(View.GONE);
        etDni.setVisibility(View.GONE);

        tvNombre.setVisibility(View.VISIBLE);
        tvDni.setVisibility(View.VISIBLE);
    }
}*/
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/*public class ProfileFragment extends Fragment {

    private TextView tvNombre, tvDni;
    private EditText etNombre, etDni, etTelefono, etDomicilio, etFechaNacimiento;
    private ImageButton btnEditName, btnEditPhone, btnEditAddress, btnEditBirthdate;
    private MaterialButton btnSaveChanges;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvDni = view.findViewById(R.id.tvDni);
        etNombre = view.findViewById(R.id.etNombre);
        etDni = view.findViewById(R.id.etDni);
        etTelefono = view.findViewById(R.id.etTelefono);
        etDomicilio = view.findViewById(R.id.etDomicilio);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnEditPhone = view.findViewById(R.id.btnEditPhone);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);
        btnEditBirthdate = view.findViewById(R.id.btnEditBirthdate);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        prefs = requireContext().getSharedPreferences("PerfilPrefs", Context.MODE_PRIVATE);

        cargarDatosFirebase();

        btnEditName.setOnClickListener(v -> {
            tvNombre.setVisibility(View.GONE);
            etNombre.setVisibility(View.VISIBLE);
            etNombre.requestFocus();
        });

        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        btnSaveChanges.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatosFirebase() {
        String userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);

        if (userId == null) {
            Toast.makeText(getContext(), "No se encontró el ID de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellido");
                        String telefono = documentSnapshot.getString("telefono");
                        String domicilio = documentSnapshot.getString("domicilio");
                        String fechaNacimiento = documentSnapshot.getString("fechaNacimiento");
                        String dni = documentSnapshot.getString("dni");

                        String nombreCompleto = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");

                        tvNombre.setText(nombreCompleto.trim());
                        etNombre.setText(nombreCompleto.trim());
                        tvDni.setText(dni);
                        etDni.setText(dni);
                        etTelefono.setText(telefono);
                        etDomicilio.setText(domicilio);
                        etFechaNacimiento.setText(fechaNacimiento);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarCambios() {
        String nombreCompleto = etNombre.getText().toString().trim();
        String dniNuevo = etDni.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();

        if (nombreCompleto.isEmpty() || dniNuevo.isEmpty()) {
            Toast.makeText(getContext(), "Complete los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] partes = nombreCompleto.split(" ", 2);
        String nombres = partes.length > 0 ? partes[0] : "";
        String apellidos = partes.length > 1 ? partes[1] : "";

        String userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);

        if (userId == null) {
            Toast.makeText(getContext(), "No se encontró el ID del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("nombres", nombres);
        datosActualizados.put("apellido", apellidos);
        datosActualizados.put("telefono", telefono);
        datosActualizados.put("domicilio", domicilio);
        datosActualizados.put("fechaNacimiento", fechaNacimiento);
        datosActualizados.put("dni", dniNuevo);

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    tvNombre.setText(nombreCompleto);
                    tvDni.setText(dniNuevo);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (DatePicker view, int year1, int month1, int dayOfMonth) -> {
            String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
            etFechaNacimiento.setText(fecha);
        }, year, month, day);

        datePickerDialog.show();
    }
}*/
public class ProfileFragment extends Fragment {

    // TextViews para mostrar datos (solo lectura para nombre, dni, email)
    private TextView tvNombre, tvDni, tvEmail, tvTelefono, tvDomicilio, tvFechaNacimiento;

    // EditTexts solo para los campos editables
    private EditText etTelefono, etDomicilio, etFechaNacimiento;

    // Botones de edición solo para campos editables
    private ImageButton btnEditPhone, btnEditAddress, btnEditBirthdate;
    private MaterialButton btnSaveChanges;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar TextViews (todos)
        tvNombre = view.findViewById(R.id.tvNombre);
        tvDni = view.findViewById(R.id.tvDni);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTelefono = view.findViewById(R.id.tvTelefono);
        tvDomicilio = view.findViewById(R.id.tvDomicilio);
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento);

        // Inicializar EditTexts (solo editables)
        etTelefono = view.findViewById(R.id.etTelefono);
        etDomicilio = view.findViewById(R.id.etDomicilio);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);

        // Inicializar botones (solo editables)
        btnEditPhone = view.findViewById(R.id.btnEditPhone);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);
        btnEditBirthdate = view.findViewById(R.id.btnEditBirthdate);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        prefs = requireContext().getSharedPreferences("PerfilPrefs", Context.MODE_PRIVATE);

        // Cargar datos desde Firebase
        cargarDatosFirebase();

        // Configurar listeners de edición
        configurarListeners();
    }

    private void configurarListeners() {
        // Editar teléfono
        btnEditPhone.setOnClickListener(v -> {
            toggleEditMode(tvTelefono, etTelefono);
        });

        // Validar longitud del teléfono mientras se escribe
        etTelefono.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 9) {
                    etTelefono.setText(s.toString().substring(0, 9));
                    etTelefono.setSelection(9);
                    Toast.makeText(getContext(), "Máximo 9 dígitos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Editar domicilio
        btnEditAddress.setOnClickListener(v -> {
            toggleEditMode(tvDomicilio, etDomicilio);
        });

        // Editar fecha de nacimiento
        btnEditBirthdate.setOnClickListener(v -> {
            toggleEditMode(tvFechaNacimiento, etFechaNacimiento);
        });

        // Click en campo de fecha para mostrar DatePicker
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        // Guardar cambios
        btnSaveChanges.setOnClickListener(v -> guardarCambios());
    }

    private void toggleEditMode(TextView textView, EditText editText) {
        if (textView.getVisibility() == View.VISIBLE) {
            // Cambiar a modo edición
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
        } else {
            // Cambiar a modo visualización
            textView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
        }
    }

    private void cargarDatosFirebase() {
        String userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);

        if (userId == null) {
            Toast.makeText(getContext(), "No se encontró el ID de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellido");
                        String email = documentSnapshot.getString("email");
                        String telefono = documentSnapshot.getString("telefono");
                        String domicilio = documentSnapshot.getString("domicilio");
                        String fechaNacimiento = documentSnapshot.getString("fechaNacimiento");
                        String dni = documentSnapshot.getString("dni");

                        String nombreCompleto = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");

                        // Actualizar TextViews (campos de solo lectura)
                        tvNombre.setText(nombreCompleto.trim());
                        tvDni.setText(dni != null ? dni : "No especificado");
                        tvEmail.setText(email != null ? email : "No especificado");

                        // Actualizar campos editables (TextView y EditText)
                        tvTelefono.setText(telefono != null ? telefono : "No especificado");
                        tvDomicilio.setText(domicilio != null ? domicilio : "No especificado");
                        tvFechaNacimiento.setText(fechaNacimiento != null ? fechaNacimiento : "No especificado");

                        etTelefono.setText(telefono);
                        etDomicilio.setText(domicilio);
                        etFechaNacimiento.setText(fechaNacimiento);

                        // Asegurarse de que los EditTexts estén ocultos
                        ocultarEditTexts();
                    } else {
                        Toast.makeText(getContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void ocultarEditTexts() {
        // Solo ocultar los EditTexts editables
        etTelefono.setVisibility(View.GONE);
        etDomicilio.setVisibility(View.GONE);
        etFechaNacimiento.setVisibility(View.GONE);

        // Mostrar los TextViews editables
        tvTelefono.setVisibility(View.VISIBLE);
        tvDomicilio.setVisibility(View.VISIBLE);
        tvFechaNacimiento.setVisibility(View.VISIBLE);
    }

    private void guardarCambios() {
        String telefono = etTelefono.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();

        // Validar teléfono: máximo 9 dígitos
        if (!telefono.isEmpty() && telefono.length() > 9) {
            Toast.makeText(getContext(), "El teléfono debe tener máximo 9 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);

        if (userId == null) {
            Toast.makeText(getContext(), "No se encontró el ID del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar datos para actualizar (solo campos editables)
        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("telefono", telefono);
        datosActualizados.put("domicilio", domicilio);
        datosActualizados.put("fechaNacimiento", fechaNacimiento);

        // Actualizar en Firebase
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();

                    // Actualizar TextViews con los nuevos datos
                    tvTelefono.setText(telefono.isEmpty() ? "No especificado" : telefono);
                    tvDomicilio.setText(domicilio.isEmpty() ? "No especificado" : domicilio);
                    tvFechaNacimiento.setText(fechaNacimiento.isEmpty() ? "No especificado" : fechaNacimiento);

                    // Ocultar todos los EditTexts
                    ocultarEditTexts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year1, int month1, int dayOfMonth) -> {
                    String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    etFechaNacimiento.setText(fecha);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}