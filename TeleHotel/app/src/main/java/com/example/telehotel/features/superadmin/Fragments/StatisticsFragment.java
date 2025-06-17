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
import android.util.Patterns;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.firebase.auth.FirebaseUser;


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
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        progressBar = view.findViewById(R.id.progressBar);
        txtPasswordStrength = view.findViewById(R.id.txtPasswordStrength);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        // Configurar listeners
        btnRegistrarAdminHotel.setOnClickListener(v -> registrarAdministradorHotel());
        setupPasswordStrengthIndicator();

        return view;
    }

    private void setupPasswordStrengthIndicator() {
        if (editTextPassword != null && progressBar != null && txtPasswordStrength != null) {
            editTextPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePasswordStrength(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        progressBar.setProgress(strength);
        txtPasswordStrength.setVisibility(View.VISIBLE);

        if (strength < 30) {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
            txtPasswordStrength.setText("Débil");
            txtPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (strength < 70) {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
            txtPasswordStrength.setText("Media");
            txtPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            txtPasswordStrength.setText("Fuerte");
            txtPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 6) strength += 20;
        if (password.length() >= 8) strength += 10;
        if (password.matches(".*[a-z].*")) strength += 10;
        if (password.matches(".*[A-Z].*")) strength += 15;
        if (password.matches(".*\\d.*")) strength += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength += 20;
        if (password.length() >= 12) strength += 10;
        return Math.min(100, strength);
    }

    private void registrarAdministradorHotel() {
        // Obtener datos de los campos
        String nombre = editTextNombre.getText().toString().trim();
        String apellido = editTextApellido.getText().toString().trim();
        String dni = editTextDni.getText().toString().trim();
        String correo = editTextCorreo.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar campos
        if (!validarCampos(nombre, apellido, dni, correo, telefono, password, confirmPassword)) {
            return;
        }

        // Mostrar progreso
        mostrarProgreso(true);

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Enviar email de verificación
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Email enviado exitosamente, ahora guardar datos en Firestore
                                            String userId = user.getUid();
                                            guardarDatosEnFirestore(userId, nombre, apellido, dni, correo, telefono);
                                        } else {
                                            mostrarProgreso(false);
                                            String errorEmail = emailTask.getException() != null ?
                                                    emailTask.getException().getMessage() : "Error al enviar email";
                                            Toast.makeText(getContext(),
                                                    "Usuario creado pero error al enviar email: " + errorEmail,
                                                    Toast.LENGTH_LONG).show();

                                            // Aún así guardar los datos
                                            String userId = user.getUid();
                                            guardarDatosEnFirestore(userId, nombre, apellido, dni, correo, telefono);
                                        }
                                    });
                        }
                    } else {
                        mostrarProgreso(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(getContext(), "Error al crear usuario: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validarCampos(String nombre, String apellido, String dni, String correo, String telefono, String password, String confirmPassword) {
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

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirme la contraseña");
            etConfirmPassword.requestFocus();
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

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
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
        adminData.put("nombres", nombre);
        adminData.put("apellido", apellido);
        adminData.put("dni", dni);
        adminData.put("email", correo);
        adminData.put("telefono", telefono);
        adminData.put("role", "admin");
        adminData.put("estado", "activo");
        adminData.put("emailVerificado", false); // Inicialmente false
        adminData.put("fechaRegistro", com.google.firebase.Timestamp.now());
        adminData.put("creadoPor", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "sistema");

        // Guardar en Firestore
        db.collection("usuarios").document(userId)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(),
                            "Administrador registrado exitosamente. Se ha enviado un email de verificación a " + correo,
                            Toast.LENGTH_LONG).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Si falla el guardado en Firestore, eliminar el usuario de Auth
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.delete();
                    }
                });
    }

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
        if (etConfirmPassword != null) {
            etConfirmPassword.setText("");
        }

        // Limpiar errores
        editTextNombre.setError(null);
        editTextApellido.setError(null);
        editTextDni.setError(null);
        editTextCorreo.setError(null);
        editTextTelefono.setError(null);
        editTextPassword.setError(null);
        if (etConfirmPassword != null) {
            etConfirmPassword.setError(null);
        }

        // Ocultar indicador de fortaleza
        if (progressBar != null && txtPasswordStrength != null) {
            progressBar.setProgress(0);
            txtPasswordStrength.setVisibility(View.GONE);
        }
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
        etConfirmPassword = null;
        btnRegistrarAdminHotel = null;
        progressBar = null;
        progressBarLoading = null;
        txtPasswordStrength = null;
    }
}
