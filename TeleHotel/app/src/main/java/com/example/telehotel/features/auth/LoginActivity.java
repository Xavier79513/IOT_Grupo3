package com.example.telehotel.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.data.repository.AuthRepository;
import com.example.telehotel.features.admin.AdminActivity;
import com.example.telehotel.features.cliente.ClientePaginaPrincipal;
import com.example.telehotel.features.superadmin.ui.SuperAdminActivity;
import com.example.telehotel.features.taxista.TaxistaActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

/*public class LoginActivity extends AppCompatActivity {

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
}*/
/*public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario ya ha iniciado sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            goToMain(user);
        } else {
            launchFirebaseUI();
        }
    }

    private void launchFirebaseUI() {
        // Lista de proveedores que quieres habilitar
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
                // Puedes añadir más proveedores aquí, como Google o Facebook
        );

        // Lanzar FirebaseUI Auth
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground) // Opcional
                .build();

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (resultCode == RESULT_OK && user != null) {
                Toast.makeText(this, "Bienvenido: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                goToMain(user);
            } else {
                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                if (response != null && response.getError() != null) {
                    response.getError().printStackTrace();
                }
                finish(); // Opcional: cerrar la app si cancelan login
            }
        }
    }

    private void goToMain(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid()) // UID del usuario autenticado
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String nombre = documentSnapshot.getString("nombres");

                        Toast.makeText(this, "Bienvenido: " + nombre, Toast.LENGTH_SHORT).show();

                        switch (role) {
                            case "taxista":
                                startActivity(new Intent(this, TaxistaActivity.class));
                                break;
                            case "admin":
                                startActivity(new Intent(this, AdminActivity.class));
                                break;
                            case "superadmin":
                                startActivity(new Intent(this, SuperAdminActivity.class));
                                break;
                            default: // cliente o cualquier otro
                                startActivity(new Intent(this, ClientePaginaPrincipal.class));
                                break;
                        }

                        finish();
                    } else {
                        Toast.makeText(this, "Usuario no registrado en la base de datos", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut(); // Cierra sesión porque no tiene datos
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}*/
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar si el usuario ya ha iniciado sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            goToMain(user);
            return;
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

        /*createAccountTextView.setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para ir a una pantalla de registro
            // o mostrar un mensaje indicando que contacte al administrador
            Toast.makeText(this, "Contacta al administrador para crear una cuenta", Toast.LENGTH_LONG).show();
        });*/
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
        // Mostrar loading
        loginButton.setEnabled(false);
        loginButton.setText("Iniciando sesión...");

        authViewModel.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                // El usuario existe en Firebase Auth, ahora verificar en Firestore
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    goToMain(firebaseUser);
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

    private void goToMain(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String nombre = documentSnapshot.getString("nombres");

                        Toast.makeText(this, "Bienvenido: " + nombre, Toast.LENGTH_SHORT).show();

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
    }
}