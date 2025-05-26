package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityServiciosBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

public class ServiciosActivity extends AppCompatActivity {
    private ActivityServiciosBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema Admin (sin ActionBar nativo)
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // Inflar layout con ViewBinding
        binding = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ─── CONFIGURAR DRAWER ───────────────────────────
        drawerLayout = binding.drawerLayout;      // @+id/drawer_layout
        NavigationView navView = binding.navView; // @+id/nav_view

        // Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        MaterialToolbar toolbar = binding.topAppBar;

        // Toggle para vincular toolbar ↔ drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Manejar clicks en el menú lateral
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: navegar a Estado de taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: navegar a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // ─── CONFIGURAR TOOLBAR ──────────────────────────
        toolbar.setBackgroundColor(Color.BLACK);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // ─── DATOS DE EJEMPLO ────────────────────────────
        List<Service> datos = Arrays.asList(
                new Service(R.drawable.ic_breakfast,    "Desayuno Buffet",
                        "Desayuno variado con jugos, pan, frutas, café, etc.",
                        "Precio: S/50.00"),
                new Service(R.drawable.ic_parking,      "Estacionamiento",
                        "Por noche.",
                        "Precio: S/30.00"),
                new Service(R.drawable.ic_lavanderia,   "Lavandería",
                        "Servicio de lavado y planchado.",
                        "Precio: S/25.00"),
                new Service(R.drawable.ic_spa,          "Spa",
                        "Masajes y tratamientos relajantes.",
                        "Precio: S/120.00"),
                new Service(R.drawable.ic_room_service, "Room Service",
                        "Servicio a la habitación 24/7.",
                        "Precio: S/70.00")
        );

        // ─── CONFIGURAR RECYCLER VIEW ───────────────────
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(new ServiceAdapter(datos));

        // ─── BOTTOM NAV ─────────────────────────────────
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.servicios_fragment);
    }

    /** Clic en “Cerrar sesión” (overflow) */
    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: cerrar sesión
            return true;
        }
        return false;
    }

    /** Navegación del BottomNavigationView */
    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.inicio_fragment) {
            intent = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            intent = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            // Ya estamos aquí
            return true;
        } else if (id == R.id.reportes_fragment) {
            intent = new Intent(this, ReportesActivity.class);
        } else if (id == R.id.perfil_fragment) {
            intent = new Intent(this, PerfilActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, lo cerramos primero
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
