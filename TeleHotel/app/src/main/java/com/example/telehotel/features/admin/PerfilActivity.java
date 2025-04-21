package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityPerfilBinding;
import com.google.android.material.snackbar.Snackbar;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema Admin sin ActionBar nativo
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* TOOLBAR */
        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setBackgroundColor(Color.BLACK);
        binding.topAppBar.setNavigationIcon(R.drawable.ic_menu);
        binding.topAppBar.setNavigationIconTint(Color.WHITE);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
        binding.topAppBar.inflateMenu(R.menu.menu_hotel_detail);
        Drawable ov = binding.topAppBar.getOverflowIcon();
        if (ov != null) ov.setTint(Color.WHITE);
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                // cerrar sesión desde overflow
                doLogout();
                return true;
            }
            return false;
        });

        /* BOTONES */
        binding.btnEditProfile.setOnClickListener(v -> {
            Snackbar.make(binding.root, "Editar perfil pulsado", Snackbar.LENGTH_SHORT).show();
        });
        binding.btnChangePassword.setOnClickListener(v -> {
            Snackbar.make(binding.root, "Cambiar contraseña", Snackbar.LENGTH_SHORT).show();
        });
        binding.btnLogout.setOnClickListener(v -> doLogout());

        /* BOTTOM NAV */
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.perfil_fragment);
    }

    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.inicio_fragment) {
            startActivity(new Intent(this, AdminActivity.class));
            return true;
        } else if (id == R.id.habitaciones_fragment) {
            startActivity(new Intent(this, HabitacionesActivity.class));
            return true;
        } else if (id == R.id.servicios_fragment) {
            startActivity(new Intent(this, ServiciosActivity.class));
            return true;
        } else if (id == R.id.reportes_fragment) {
            startActivity(new Intent(this, ReportesActivity.class));
            return true;
        } else if (id == R.id.perfil_fragment) {
            return true;
        }
        return false;
    }

    private void doLogout() {
        // TODO: lógica real de cerrar sesión
        Snackbar.make(binding.root, "Sesión cerrada", Snackbar.LENGTH_SHORT).show();
    }
}
