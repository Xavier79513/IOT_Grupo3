package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // <-- Asegúrate de importar esto
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaxistaPerfil extends AppCompatActivity {

    private static final String TAG = "TaxistaPerfil"; // ← Etiqueta para logs

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtNombre, txtEmail, txtTelefono, txtBirth, txtDNI, txtDomicilio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_editar_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtNombre = findViewById(R.id.tvNombre);
        txtDNI = findViewById(R.id.tvDni);
        txtEmail = findViewById(R.id.tvEmail);
        txtBirth = findViewById(R.id.tvBirth);
        txtTelefono = findViewById(R.id.tvPhone);
        txtDomicilio = findViewById(R.id.tvDomicilio);

        setupBottomNavigation();
        setupButtons();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();

            Log.d(TAG, "UID del usuario: " + uid);
            Log.d(TAG, "Email del usuario: " + email);

            txtEmail.setText(email);

            db.collection("usuarios").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombres");
                            String apellidos = documentSnapshot.getString("apellidos");
                            String telefono = documentSnapshot.getString("telefono");
                            String dni = documentSnapshot.getString("numeroDocumento");
                            String birth = documentSnapshot.getString("fechaNacimiento");
                            String domicilio = documentSnapshot.getString("domicilio");

                            Log.d(TAG, "Datos del usuario:");
                            Log.d(TAG, "Nombre: " + nombre);
                            Log.d(TAG, "Apellidos: " + apellidos);
                            Log.d(TAG, "Teléfono: " + telefono);
                            Log.d(TAG, "DNI: " + dni);
                            Log.d(TAG, "Fecha de nacimiento: " + birth);

                            txtNombre.setText(nombre + " " + apellidos);
                            txtTelefono.setText(telefono);
                            txtDNI.setText(dni);
                            txtBirth.setText(birth);
                            txtDomicilio.setText(domicilio);
                        } else {
                            Log.w(TAG, "No se encontró el documento para UID: " + uid);
                            Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cargar datos: ", e);
                        Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.w(TAG, "Usuario no autenticado (null)");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaPerfil.this, item.getItemId());
            return true;
        });
    }

    private void setupButtons() {
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        Button btnLogout = findViewById(R.id.btnLogout);

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> navigateToHome());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(TaxistaPerfil.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TaxistaPerfil.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(TaxistaPerfil.this, TaxistaActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(TaxistaPerfil.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
