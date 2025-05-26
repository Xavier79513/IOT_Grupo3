package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.telehotel.R;
import com.example.telehotel.databinding.AdminActivityBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity {
    private AdminActivityBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica el tema sin ActionBar clásico
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // Inflar el layout con ViewBinding
        binding = AdminActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ─── CONFIGURACIÓN DEL DRAWER ─────────────────────────────
        drawerLayout = binding.drawerLayout;      // Debe corresponder a @+id/drawer_layout
        NavigationView navView = binding.navView; // Debe corresponder a @+id/nav_view

        // Usa la Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        MaterialToolbar toolbar = binding.topAppBar;

        // Configura el toggle para abrir/cerrar el Drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,   // “Abrir menú” en strings.xml
                R.string.drawer_close   // “Cerrar menú”
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Maneja clics en los ítems del Drawer
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: Navegar a Estado de taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: Navegar a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // ─── CONFIGURACIÓN DE LA TOP APP BAR ──────────────────────
        // Fondo negro y tint blanco para overflow
        toolbar.setBackgroundColor(Color.BLACK);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);
        // Infla el menú de logout
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // ─── BOTÓN “Abrir en Maps” ────────────────────────────────
        binding.btnOpenMap.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q=" +
                    Uri.encode(binding.tvAddress.getText().toString()));
            startActivity(new Intent(Intent.ACTION_VIEW, uri)
                    .setPackage("com.google.android.apps.maps"));
        });

        // ─── CONFIGURACIÓN DEL BOTTOM NAV ─────────────────────────
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
    }

    /**
     * Maneja el clic en el ítem “Cerrar sesión” de la Toolbar
     */
    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: Implementar lógica de logout
            return true;
        }
        return false;
    }

    /**
     * Maneja la navegación del BottomNavigationView
     */
    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.inicio_fragment) {
            intent = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            intent = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            intent = new Intent(this, ServiciosActivity.class);
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
        // Si el drawer está abierto, lo cierra en lugar de salir
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
