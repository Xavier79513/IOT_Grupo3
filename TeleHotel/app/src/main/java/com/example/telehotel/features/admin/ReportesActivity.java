package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityReportesBinding;

public class ReportesActivity extends AppCompatActivity {

    private ActivityReportesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1) Aplica el tema Admin sin ActionBar nativo
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);
        binding = ActivityReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* ─── TOOLBAR ─── */
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
                // TODO: implementar logout
                return true;
            }
            return false;
        });

        /* ─── RADIOGROUP ─── */
        RadioGroup rg = binding.rgReports;
        RadioButton rbService = binding.rbByService;
        RadioButton rbUser    = binding.rbByUser;

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == rbService.getId()) {
                // TODO: mostrar reporte por servicio
            } else if (checkedId == rbUser.getId()) {
                // TODO: mostrar reporte por usuario
            }
        });

        /* ─── BOTTOM NAV ─── */
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.reportes_fragment);
    }

    /** Maneja clicks del BottomNavigationView */
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
            return true; // ya estamos aquí
        } else if (id == R.id.perfil_fragment) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }
        return false;
    }
}
