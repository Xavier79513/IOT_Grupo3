package com.example.telehotel.features.taxista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        // Verifica que el ID coincida con el de tu XML
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        Button btnLogout = findViewById(R.id.btnLogout);

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> navigateToHome());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> navigateToLogin());
        }
    }

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
