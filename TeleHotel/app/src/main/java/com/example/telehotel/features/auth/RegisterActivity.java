package com.example.telehotel.features.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

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
            tvPasswordStrength.setTextColor(getColor(R.color.error_color));
            pbPasswordStrength.setProgressTintList(getColorStateList(R.color.error_color));
        } else if (fuerza < 70) {
            tvPasswordStrength.setText("Media");
            tvPasswordStrength.setTextColor(getColor(R.color.warning_color));
            pbPasswordStrength.setProgressTintList(getColorStateList(R.color.warning_color));
        } else {
            tvPasswordStrength.setText("Fuerte");
            tvPasswordStrength.setTextColor(getColor(R.color.success_color));
            pbPasswordStrength.setProgressTintList(getColorStateList(R.color.success_color));
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
}
