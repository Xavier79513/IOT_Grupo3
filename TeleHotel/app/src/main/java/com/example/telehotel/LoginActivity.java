package com.example.telehotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Vincular vistas con el layout
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        createAccountTextView = findViewById(R.id.createAccountTextView);

        // Botón de inicio de sesión
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validación básica de campos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validación simulada de credenciales
            if (email.equals("admin@example.com") && password.equals("123")) {
                // Ir a selección de rol
                Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish(); // Cierra el login para no volver atrás
            } else {
                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        // "¿Olvidaste tu contraseña?"
        forgotPasswordTextView.setOnClickListener(v -> {
            // Aquí puedes abrir una actividad para recuperar contraseña
            Toast.makeText(this, "Función aún no implementada", Toast.LENGTH_SHORT).show();
        });

        // "¿Nuevo usuario? Crear cuenta"
        createAccountTextView.setOnClickListener(v -> {
            // Aquí puedes abrir la actividad de registro
            Toast.makeText(this, "Función aún no implementada", Toast.LENGTH_SHORT).show();
        });
    }
}
