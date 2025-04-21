package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityHabitacionesBinding;

public class HabitacionesActivity extends AppCompatActivity {
    private ActivityHabitacionesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1) Aplica el tema Admin (sin ActionBar nativo)
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // 2) Infla el layout con ViewBinding
        binding = ActivityHabitacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* ─── TOOLBAR NEGRA CON MENÚ “Cerrar sesión” ─── */
        setSupportActionBar(binding.topAppBar);
        // Fondo negro
        binding.topAppBar.setBackgroundColor(Color.BLACK);
        // Icono hamburguesa en blanco
        binding.topAppBar.setNavigationIcon(R.drawable.ic_menu);
        binding.topAppBar.setNavigationIconTint(Color.WHITE);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
        // Infla tu menú de cerrar sesión
        binding.topAppBar.inflateMenu(R.menu.menu_hotel_detail);
        // Forzar tinte blanco en el overflow (⋮)
        Drawable overflow = binding.topAppBar.getOverflowIcon();
        if (overflow != null) {
            overflow.setTint(Color.WHITE);
        }
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                // TODO: lógica de logout
                return true;
            }
            return false;
        });

        /* ─── BOTÓN “Agregar Habitación” ─── */
        binding.btnAgregarHabitacion.setOnClickListener(v -> {
            // TODO: acción para añadir habitación
        });

        /* ─── BOTTOM NAV ─── */
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);

        // **Marcar la pestaña “Habitaciones” como seleccionada**
        binding.bottomNavigationView
                .setSelectedItemId(R.id.habitaciones_fragment);
    }

    /** Maneja clicks del BottomNavigationView */
    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.inicio_fragment) {
            startActivity(new Intent(this, AdminActivity.class));
            return true;
        } else if (id == R.id.habitaciones_fragment) {
            // Ya estamos aquí
            return true;
        } else if (id == R.id.servicios_fragment) {
            startActivity(new Intent(this, ServiciosActivity.class));
            return true;
        } else if (id == R.id.reportes_fragment) {
            startActivity(new Intent(this, ReportesActivity.class));
            return true;
        } else if (id == R.id.perfil_fragment) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }
        return false;
    }
}
