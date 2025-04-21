package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityServiciosBinding;
import java.util.Arrays;
import java.util.List;

public class ServiciosActivity extends AppCompatActivity {
    private ActivityServiciosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);
        binding = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
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
                // TODO: cerrar sesión
                return true;
            }
            return false;
        });

        // Datos de ejemplo
        List<Service> datos = Arrays.asList(
                new Service(R.drawable.ic_breakfast,   "Desayuno Buffet",
                        "Desayuno variado con jugos, pan, frutas, café, etc.",
                        "Precio: S/50.00"),
                new Service(R.drawable.ic_parking,     "Estacionamiento",
                        "Por noche.",
                        "Precio: S/30.00"),
                new Service(R.drawable.ic_lavanderia,     "Lavandería",
                        "Servicio de lavado y planchado.",
                        "Precio: S/25.00"),
                new Service(R.drawable.ic_spa,         "Spa",
                        "Masajes y tratamientos relajantes.",
                        "Precio: S/120.00"),
                new Service(R.drawable.ic_room_service,"Room Service",
                        "Servicio a la habitación 24/7.",
                        "Precio: S/70.00")
        );

        // RecyclerView
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(new ServiceAdapter(datos));

        // BottomNav
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView.setSelectedItemId(R.id.servicios_fragment);
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
