package com.example.telehotel.features.auth;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etDni, etCorreo, etPassword, etConfirmPassword;
    private ProgressBar pbSeguridad;
    private TextView tvNivelSeguridad;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etCorreo = findViewById(R.id.registerEmail);
        etPassword = findViewById(R.id.registerPassword);
        etConfirmPassword = findViewById(R.id.confirmPassword);
        pbSeguridad = findViewById(R.id.pbSeguridad);
        tvNivelSeguridad = findViewById(R.id.tvNivelSeguridad);
        btnRegistrar = findViewById(R.id.registerButton);

        // Monitorear seguridad de la contraseña
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                evaluarSeguridad(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Acción del botón Registrar
        btnRegistrar.setOnClickListener(v -> {
            if (validarCampos()) {
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                // Aquí podrías enviar los datos a un backend, guardar en local, etc.
            }
        });
    }

    private void evaluarSeguridad(String password) {
        int score = 0;

        if (password.length() >= 8) score += 30;
        if (password.matches(".*[A-Z].*")) score += 30;
        if (password.matches(".*[!@#$%^&*()_+\\-\\[\\]{};':\"\\\\|,.<>/?].*")) score += 40;

        pbSeguridad.setProgress(score);

        if (score < 40) {
            tvNivelSeguridad.setText("Seguridad: Débil");
            pbSeguridad.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (score < 80) {
            tvNivelSeguridad.setText("Seguridad: Media");
            pbSeguridad.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            tvNivelSeguridad.setText("Seguridad: Alta");
            pbSeguridad.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmarPassword = etConfirmPassword.getText().toString();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
