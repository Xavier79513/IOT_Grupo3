package com.example.telehotel.features.superadmin.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;

/*public class StatisticsFragment extends Fragment {

    private EditText editTextNombre, editTextCorreo, editTextTelefono, editTextApellido,editTextDni,editTextPassword ;
    private Button btnRegistrarAdminHotel;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        editTextNombre = view.findViewById(R.id.editTextNombreAdmin);
        editTextApellido = view.findViewById(R.id.editTextApellidoAdmin);
        editTextDni = view.findViewById(R.id.editTextDniAdmin);
        editTextCorreo = view.findViewById(R.id.editTextCorreoAdmin);
        editTextTelefono = view.findViewById(R.id.editTextTelefonoAdmin);
        editTextPassword = view.findViewById(R.id.editPasswordAdmin);
        btnRegistrarAdminHotel = view.findViewById(R.id.btnRegistrarAdminHotel);

        btnRegistrarAdminHotel.setOnClickListener(v -> registrarAdministradorHotel());

        return view;
    }

    private void registrarAdministradorHotel() {
        String nombre = editTextNombre.getText().toString().trim();
        String correo = editTextCorreo.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo)
                || TextUtils.isEmpty(telefono) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(getContext(), "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí podrías luego guardar en Firebase, base de datos, etc.
        Toast.makeText(getContext(), "Administrador registrado exitosamente.", Toast.LENGTH_LONG).show();

        // Limpiar los campos
        editTextNombre.setText("");
        editTextCorreo.setText("");
        editTextTelefono.setText("");
    }
}*/

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private EditText editTextNombre, editTextCorreo, editTextTelefono, editTextApellido, editTextDni, editTextPassword;
    private Button btnRegistrarAdminHotel;

    private ProgressBar progressBarLoading, progressBar;
    private TextView txtPasswordStrength;
    private EditText etConfirmPassword;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        editTextNombre = view.findViewById(R.id.editTextNombreAdmin);
        editTextApellido = view.findViewById(R.id.editTextApellidoAdmin);
        editTextDni = view.findViewById(R.id.editTextDniAdmin);
        editTextCorreo = view.findViewById(R.id.editTextCorreoAdmin);
        editTextTelefono = view.findViewById(R.id.editTextTelefonoAdmin);
        editTextPassword = view.findViewById(R.id.editPasswordAdmin);
        btnRegistrarAdminHotel = view.findViewById(R.id.btnRegistrarAdminHotel);
        //progressBar = view.findViewById(R.id.progressBar); // Agregar al layout
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        progressBar = view.findViewById(R.id.progressBar);
        txtPasswordStrength = view.findViewById(R.id.txtPasswordStrength);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        btnRegistrarAdminHotel.setOnClickListener(v -> registrarAdministradorHotel());

        return view;
    }

    private void registrarAdministradorHotel() {
        // Obtener datos de los campos
        String nombre = editTextNombre.getText().toString().trim();
        String apellido = editTextApellido.getText().toString().trim();
        String dni = editTextDni.getText().toString().trim();
        String correo = editTextCorreo.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validar campos
        if (!validarCampos(nombre, apellido, dni, correo, telefono, password)) {
            return;
        }

        // Mostrar progreso
        mostrarProgreso(true);

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Usuario creado exitosamente, ahora guardar datos en Firestore
                        String userId = task.getResult().getUser().getUid();
                        guardarDatosEnFirestore(userId, nombre, apellido, dni, correo, telefono);
                    } else {
                        mostrarProgreso(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(getContext(), "Error al crear usuario: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validarCampos(String nombre, String apellido, String dni, String correo, String telefono, String password) {
        // Validar campos vacíos
        if (TextUtils.isEmpty(nombre)) {
            editTextNombre.setError("El nombre es requerido");
            editTextNombre.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(apellido)) {
            editTextApellido.setError("El apellido es requerido");
            editTextApellido.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dni)) {
            editTextDni.setError("El DNI es requerido");
            editTextDni.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correo)) {
            editTextCorreo.setError("El correo es requerido");
            editTextCorreo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(telefono)) {
            editTextTelefono.setError("El teléfono es requerido");
            editTextTelefono.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("La contraseña es requerida");
            editTextPassword.requestFocus();
            return false;
        }

        // Validar formato de correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            editTextCorreo.setError("Formato de correo inválido");
            editTextCorreo.requestFocus();
            return false;
        }

        // Validar longitud de contraseña
        if (password.length() < 6) {
            editTextPassword.setError("La contraseña debe tener al menos 6 caracteres");
            editTextPassword.requestFocus();
            return false;
        }

        // Validar DNI (8 dígitos)
        if (dni.length() != 8 || !dni.matches("\\d+")) {
            editTextDni.setError("El DNI debe tener 8 dígitos");
            editTextDni.requestFocus();
            return false;
        }

        // Validar teléfono (9 dígitos)
        if (telefono.length() != 9 || !telefono.matches("\\d+")) {
            editTextTelefono.setError("El teléfono debe tener 9 dígitos");
            editTextTelefono.requestFocus();
            return false;
        }

        return true;
    }

    private void guardarDatosEnFirestore(String userId, String nombre, String apellido, String dni, String correo, String telefono) {
        // Crear mapa con los datos del administrador
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("nombre", nombre);
        adminData.put("apellido", apellido);
        adminData.put("dni", dni);
        adminData.put("correo", correo);
        adminData.put("telefono", telefono);
        adminData.put("rol", "admin_hotel");
        adminData.put("estado", "activo");
        adminData.put("fechaRegistro", com.google.firebase.Timestamp.now());
        adminData.put("creadoPor", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "sistema");

        // Guardar en Firestore
        db.collection("usuarios").document(userId)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "Administrador de hotel registrado exitosamente", Toast.LENGTH_LONG).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Si falla el guardado en Firestore, eliminar el usuario de Auth
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete();
                    }
                });
    }

    /*private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrarAdminHotel.setEnabled(!mostrar);
        btnRegistrarAdminHotel.setText(mostrar ? "Registrando..." : "Registrar Administrador");
    }*/
    private void mostrarProgreso(boolean mostrar) {
        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrarAdminHotel.setEnabled(!mostrar);
        btnRegistrarAdminHotel.setText(mostrar ? "Registrando..." : "Registrar Administrador");
    }

    private void limpiarCampos() {
        editTextNombre.setText("");
        editTextApellido.setText("");
        editTextDni.setText("");
        editTextCorreo.setText("");
        editTextTelefono.setText("");
        editTextPassword.setText("");

        // Limpiar errores
        editTextNombre.setError(null);
        editTextApellido.setError(null);
        editTextDni.setError(null);
        editTextCorreo.setError(null);
        editTextTelefono.setError(null);
        editTextPassword.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        editTextNombre = null;
        editTextApellido = null;
        editTextDni = null;
        editTextCorreo = null;
        editTextTelefono = null;
        editTextPassword = null;
        btnRegistrarAdminHotel = null;
        progressBar = null;
    }
}
