package com.example.telehotel.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.features.admin.AdminActivity;
import com.example.telehotel.features.cliente.ClientePaginaPrincipal;
import com.example.telehotel.features.superadmin.ui.SuperAdminActivity;
import com.example.telehotel.features.taxista.TaxistaActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        createAccountTextView = findViewById(R.id.createAccountTextView);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = authViewModel.login(email, password);
            if (success) {
                Usuario usuario = authViewModel.getCurrentUser();
                Toast.makeText(this, "Bienvenido " + usuario.getName(), Toast.LENGTH_SHORT).show();

                // Redirigir según el rol del usuario
                switch (usuario.getRole()) {
                    case "Taxista":
                        startActivity(new Intent(this, TaxistaActivity.class));
                        break;
                    case "Admin":
                        startActivity(new Intent(this, AdminActivity.class));
                        break;
                    case "SuperAdmin":
                        startActivity(new Intent(this, SuperAdminActivity.class));
                        break;
                    case "Cliente":
                        startActivity(new Intent(this, ClientePaginaPrincipal.class));
                        break;
                    default:
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                        break;
                }

                finish();
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordTextView.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        createAccountTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
