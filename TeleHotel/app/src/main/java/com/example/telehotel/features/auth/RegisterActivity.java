package com.example.telehotel.features.auth;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDni, etEmail, etPassword, etConfirmPassword;
    private ProgressBar pbPasswordStrength;
    private TextView tvPasswordStrength;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referencias
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);

        // Listener para evaluar fuerza de contraseña
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    evaluarFortaleza(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Habilitar botón solo si se aceptan los términos
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked);
        });

        btnRegister.setOnClickListener(v -> {
            if (validarCampos()) {
                // Acción de registro
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void evaluarFortaleza(String password) {
        int fuerza = 0;
        if (password.length() >= 8) fuerza += 30;
        if (password.matches(".*[A-Z].*")) fuerza += 30;
        if (password.matches(".*[0-9].*")) fuerza += 20;
        if (password.matches(".*[!@#\\$%\\^&\\*].*")) fuerza += 20;

        pbPasswordStrength.setProgress(fuerza);

        if (fuerza < 40) {
            tvPasswordStrength.setText("Débil");
            tvPasswordStrength.setTextColor(Color.parseColor("#F44336")); // rojo (colorError)
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
        } else if (fuerza < 70) {
            tvPasswordStrength.setText("Media");
            tvPasswordStrength.setTextColor(Color.parseColor("#FF9800")); // naranja (warningColor)
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
        } else {
            tvPasswordStrength.setText("Fuerte");
            tvPasswordStrength.setTextColor(Color.parseColor("#4CAF50")); // verde (successColor)
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }


    private boolean validarCampos() {
        if (etNombre.getText().toString().isEmpty() ||
                etApellido.getText().toString().isEmpty() ||
                etDni.getText().toString().isEmpty() ||
                etEmail.getText().toString().isEmpty() ||
                etPassword.getText().toString().isEmpty() ||
                etConfirmPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}*/
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDni, etFechaNacimiento,
            etEmail, etTelefono, etDomicilio, etPassword, etConfirmPassword;
    private ProgressBar pbPasswordStrength;
    private TextView tvPasswordStrength, tvLogin;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);

        // Referencias a las vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etDomicilio = findViewById(R.id.etDomicilio);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        // Listener para evaluar fuerza de contraseña
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    evaluarFortaleza(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Habilitar botón solo si se aceptan los términos
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked && validarCamposBasico());
        });

        // Listener del botón de registro
        btnRegister.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuarioFirebase();
            }
        });

        // Listener para ir al login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar DatePicker para fecha de nacimiento
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        etFechaNacimiento.setFocusable(false);
        etFechaNacimiento.setClickable(true);
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fecha = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFechaNacimiento.setText(fecha);
                }, year, month, day);

        // Establecer fecha máxima (hoy)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void registrarUsuarioFirebase() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();

        // Crear usuario con email y contraseña
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Actualizar perfil del usuario
                            actualizarPerfilUsuario(user);

                            // Enviar email de verificación
                            enviarEmailVerificacion(user);

                            // Guardar datos adicionales en Firestore
                            guardarDatosUsuarioFirestore(user.getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        manejarErrorRegistro(task.getException());
                    }
                });
    }

    private void actualizarPerfilUsuario(FirebaseUser user) {
        String nombreCompleto = etNombre.getText().toString().trim() + " " +
                etApellido.getText().toString().trim();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nombreCompleto)
                .build();

        user.updateProfile(profileUpdates);
    }

    private void enviarEmailVerificacion(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        mostrarDialogoVerificacion();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error al enviar email de verificación",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarDatosUsuarioFirestore(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombres", etNombre.getText().toString().trim());
        usuario.put("apellido", etApellido.getText().toString().trim());
        usuario.put("dni", etDni.getText().toString().trim());
        usuario.put("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
        usuario.put("email", etEmail.getText().toString().trim());
        usuario.put("telefono", etTelefono.getText().toString().trim());
        usuario.put("domicilio", etDomicilio.getText().toString().trim());
        usuario.put("fechaRegistro", new Date());
        //usuario.put("emailVerificado", false);
        usuario.put("role", "cliente");



        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Datos del usuario guardados exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error al guardar datos del usuario", e);
                });
    }

    private void mostrarDialogoVerificacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificación de Email")
                .setMessage("Se ha enviado un email de verificación a " + etEmail.getText().toString() +
                        "\n\nPor favor revisa tu bandeja de entrada y haz clic en el enlace de verificación.")
                .setIcon(R.drawable.ic_email)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    // Regresar al login
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", etEmail.getText().toString());
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void manejarErrorRegistro(Exception exception) {
        String mensaje = "Error al crear la cuenta";

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            mensaje = "La contraseña es muy débil";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            mensaje = "El email no es válido";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            mensaje = "Ya existe una cuenta con este email";
        } else if (exception != null) {
            mensaje = exception.getLocalizedMessage();
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void evaluarFortaleza(String password) {
        int fuerza = 0;
        if (password.length() >= 8) fuerza += 25;
        if (password.length() >= 12) fuerza += 10;
        if (password.matches(".*[A-Z].*")) fuerza += 20;
        if (password.matches(".*[a-z].*")) fuerza += 10;
        if (password.matches(".*[0-9].*")) fuerza += 15;
        if (password.matches(".*[!@#\\$%\\^&\\*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\?].*")) fuerza += 20;

        pbPasswordStrength.setProgress(fuerza);

        if (fuerza < 40) {
            tvPasswordStrength.setText("Débil");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)));
        } else if (fuerza < 70) {
            tvPasswordStrength.setText("Media");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark)));
        } else {
            tvPasswordStrength.setText("Fuerte");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)));
        }

        // Actualizar estado del botón
        btnRegister.setEnabled(cbTerms.isChecked() && validarCamposBasico());
    }

    private boolean validarCamposBasico() {
        return !etNombre.getText().toString().trim().isEmpty() &&
                !etApellido.getText().toString().trim().isEmpty() &&
                !etDni.getText().toString().trim().isEmpty() &&
                !etFechaNacimiento.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etTelefono.getText().toString().trim().isEmpty() &&
                !etDomicilio.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().trim().isEmpty() &&
                !etConfirmPassword.getText().toString().trim().isEmpty();
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar campos vacíos
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
                fechaNacimiento.isEmpty() || email.isEmpty() || telefono.isEmpty() ||
                domicilio.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar DNI (8 dígitos)
        if (dni.length() != 8 || !dni.matches("\\d{8}")) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 dígitos", Toast.LENGTH_SHORT).show();
            etDni.requestFocus();
            return false;
        }

        // Validar email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        // Validar teléfono (9 dígitos para Perú)
        if (telefono.length() != 9 || !telefono.matches("\\d{9}")) {
            Toast.makeText(this, "El teléfono debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
            etTelefono.requestFocus();
            return false;
        }

        // Validar contraseña mínima
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return false;
        }

        // Validar coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        // Validar términos y condiciones
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
