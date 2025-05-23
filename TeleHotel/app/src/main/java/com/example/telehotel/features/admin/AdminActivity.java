package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.databinding.AdminActivityBinding;

public class AdminActivity extends AppCompatActivity {
    private AdminActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica el tema Admin (sin ActionBar clásico)
        setTheme(R.style.Theme_TeleHotel_Admin);

        super.onCreate(savedInstanceState);
        binding = AdminActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* ─── TOP BAR SOBRE FONDO NEGRO ─── */
        // Usa la toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        // Pinta la toolbar de negro
        binding.topAppBar.setBackgroundColor(Color.BLACK);
        // Nav‑icon ☰ en blanco
        binding.topAppBar.setNavigationIcon(R.drawable.ic_menu);
        binding.topAppBar.setNavigationIconTint(Color.WHITE);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Infla el menú “Cerrar sesión” directamente sobre la toolbar
        binding.topAppBar.inflateMenu(R.menu.menu_hotel_detail);
        // Forzar tinte blanco en el icono de overflow (⋮)
        Drawable overflow = binding.topAppBar.getOverflowIcon();
        if (overflow != null) {
            overflow.setTint(Color.WHITE);
        }
        binding.topAppBar.setOnMenuItemClickListener(this::onLogoutItem);

        /* ─── BOTÓN “Abrir en Maps” ─── */
        binding.btnOpenMap.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q="
                    + Uri.encode(binding.tvAddress.getText().toString()));
            startActivity(new Intent(Intent.ACTION_VIEW, uri)
                    .setPackage("com.google.android.apps.maps"));
        });

        /* ─── BOTTOM NAV ─── */
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
    }

    /** Maneja el click del ítem “Cerrar sesión” */
    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: implementar logout
            return true;
        }
        return false;
    }


    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.inicio_fragment) {
            // Navegar a Inicio (AdminActivity)
            startActivity(new Intent(this, AdminActivity.class));
            return true;
        } else if (id == R.id.habitaciones_fragment) {
            // Navegar a Habitaciones
            startActivity(new Intent(this, HabitacionesActivity.class));
            return true;
        } else if (id == R.id.servicios_fragment) {
            // Navegar a Servicios
            startActivity(new Intent(this, ServiciosActivity.class));
            return true;
        } else if (id == R.id.reportes_fragment) {
            // Navegar a Reportes
            startActivity(new Intent(this, ReportesActivity.class));
            return true;
        } else if (id == R.id.perfil_fragment) {
            // Navegar a Perfil
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }
        return false;
    }

}
