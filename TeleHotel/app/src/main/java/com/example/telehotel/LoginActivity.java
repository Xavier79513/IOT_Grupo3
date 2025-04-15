package com.example.telehotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Inicializar vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        createAccountTextView = findViewById(R.id.createAccountTextView);

        // Acción de inicio de sesión
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes validar las credenciales
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Validar el inicio de sesión (esto es solo un ejemplo)
                if (email.equals("admin@example.com") && password.equals("123")) {
                    // Si las credenciales son correctas, redirige a RoleSelectionActivity
                    Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
                    startActivity(intent);
                    finish();  // Finalizar esta actividad para evitar que el usuario regrese al login
                } else {
                    // Aquí podrías mostrar un mensaje de error si las credenciales no son correctas
                    // Por ejemplo, usar un Toast para informar al usuario
                }
            }
        });

        // Acción para "Olvidaste la contraseña?"
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para manejar "Olvidaste la contraseña"
                // Puede abrir una nueva actividad o mostrar un cuadro de diálogo
            }
        });

        // Acción para "Nuevo usuario? Crear una cuenta"
        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para manejar la creación de una nueva cuenta
                // Puede abrir una actividad para registrarse
            }
        });
    }
}
