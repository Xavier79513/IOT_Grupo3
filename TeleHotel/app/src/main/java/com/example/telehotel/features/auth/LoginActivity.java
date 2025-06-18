package com.example.telehotel.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.data.repository.AuthRepository;
import com.example.telehotel.features.admin.AdminMainActivity;
import com.example.telehotel.features.cliente.ClientePaginaPrincipal;
import com.example.telehotel.features.superadmin.ui.SuperAdminActivity;
import com.example.telehotel.features.taxista.TaxistaActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar si el usuario ya ha iniciado sesión Y su email está verificado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            goToMain(user);
            return;
        } else if (user != null && !user.isEmailVerified()) {
            // Usuario autenticado pero email no verificado
            Toast.makeText(this, "Por favor verifica tu email antes de continuar", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut(); // Cerrar sesión
        }
        initViews();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        createAccountTextView = findViewById(R.id.createAccountTextView);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInput(email, password)) {
                login(email, password);
            }
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                forgotPassword(email);
            } else {
                Toast.makeText(this, "Ingresa tu correo electrónico primero", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountTextView.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            emailEditText.setError("Ingrese su correo electrónico");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingrese un correo electrónico válido");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Ingrese su contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        return isValid;
    }

    private void login(String email, String password) {
        loginButton.setEnabled(false);
        loginButton.setText("Iniciando sesión...");

        authViewModel.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String userEmail = firebaseUser.getEmail();

                    // Permitir acceso directo a emails de tu dominio
                    boolean esDominioInterno = userEmail != null && userEmail.endsWith("@telehotel.com");

                    if (esDominioInterno || firebaseUser.isEmailVerified()) {
                        goToMain(firebaseUser);
                    } else {
                        runOnUiThread(() -> {
                            loginButton.setEnabled(true);
                            loginButton.setText("Iniciar Sesión");
                            Toast.makeText(LoginActivity.this,
                                    "Por favor verifica tu email antes de iniciar sesión",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                        });
                    }
                }
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void forgotPassword(String email) {
        authViewModel.recoverPassword(email);
        Toast.makeText(this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show();
    }
    /*private void goToMain(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener role con valor por defecto
                        String role = documentSnapshot.getString("rol");
                        if (role == null || role.isEmpty()) {
                            role = "cliente"; // Valor por defecto
                        }

                        // Obtener nombre con valor por defecto
                        String nombre = documentSnapshot.getString("nombres");
                        if (nombre == null || nombre.isEmpty()) {
                            nombre = "Usuario"; // Valor por defecto
                        }

                        Toast.makeText(this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();

                        Intent intent = null;
                        switch (role) {
                            case "taxista":
                                intent = new Intent(this, TaxistaActivity.class);
                                break;
                            case "admin":
                                intent = new Intent(this, AdminActivity.class);
                                break;
                            case "superadmin":
                                intent = new Intent(this, SuperAdminActivity.class);
                                break;
                            default: // cliente o cualquier otro
                                intent = new Intent(this, ClientePaginaPrincipal.class);
                                break;
                        }

                        if (intent != null) {
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Usuario no registrado en la base de datos", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");
                });
    }*/
    private void goToMain(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ CORREGIDO: Usar "rol" en lugar de "role"
                        String role = documentSnapshot.getString("role");
                        if (role == null || role.isEmpty()) {
                            role = "cliente"; // Valor por defecto
                        }

                        // ✅ CORREGIDO: Usar "nombres" consistentemente
                        String nombre = documentSnapshot.getString("nombres");
                        if (nombre == null || nombre.isEmpty()) {
                            nombre = "Usuario"; // Valor por defecto
                        }

                        // ✅ AGREGADO: Verificar estado del usuario
                        String estado = documentSnapshot.getString("estado");
                        if (estado != null && !"activo".equals(estado)) {
                            Toast.makeText(this, "Tu cuenta está deshabilitada. Contacta al administrador.", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            return;
                        }

                        // ✅ AGREGADO: Log para debug
                        Log.d("LoginDebug", "Usuario: " + user.getEmail() + ", Rol: " + role + ", Estado: " + estado);

                        Toast.makeText(this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();

                        Intent intent = null;
                        switch (role) {
                            case "taxista":
                                intent = new Intent(this, TaxistaActivity.class);
                                break;
                            case "admin":
                                intent = new Intent(this, AdminMainActivity.class);
                                break;
                            case "superadmin":
                                intent = new Intent(this, SuperAdminActivity.class);
                                break;
                            default: // cliente o cualquier otro
                                intent = new Intent(this, ClientePaginaPrincipal.class);
                                break;
                        }

                        if (intent != null) {
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // ✅ MEJORADO: Mejor manejo cuando no existe el documento
                        Log.w("LoginDebug", "Documento del usuario no existe en Firestore");
                        Toast.makeText(this, "Usuario no registrado en la base de datos", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginDebug", "Error al consultar Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");
                });
    }
}