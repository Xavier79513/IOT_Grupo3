package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class TaxistaPerfil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_editar_perfil); // Asegúrate de que este es el archivo correcto

        setupBottomNavigation();
        setupButtons();
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
                // Cerrar sesión de Firebase
                FirebaseAuth.getInstance().signOut();

                // Mostrar un mensaje (opcional)
                Toast.makeText(TaxistaPerfil.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

                // Ir al login y limpiar back stack para que no se pueda volver atrás
                Intent intent = new Intent(TaxistaPerfil.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }}

    private void navigateToHome() {
        Intent intent = new Intent(TaxistaPerfil.this, TaxistaActivity.class); // Cambia HomeActivity por la clase de inicio
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(TaxistaPerfil.this, LoginActivity.class); // Cambia LoginActivity por la clase de login
        startActivity(intent);
        finish();
    }
}
