package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityHabitacionesBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class HabitacionesActivity extends AppCompatActivity {
    private ActivityHabitacionesBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1) Tema Admin (sin ActionBar nativo)
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // 2) Inflar layout con ViewBinding
        binding = ActivityHabitacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ─── CONFIGURACIÓN DEL DRAWER ────────────────────────────
        drawerLayout = binding.drawerLayout;      // <– @+id/drawer_layout
        NavigationView navView = binding.navView; // <– @+id/nav_view

        // Usa la Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        MaterialToolbar toolbar = binding.topAppBar;

        // Toggle para enlazar toolbar ↔ drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Maneja la selección de opciones en el NavigationView
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

        // ─── TOOLBAR NEGRA CON MENÚ “Cerrar sesión” ──────────────
        toolbar.setBackgroundColor(Color.BLACK);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // ─── BOTÓN “Agregar Habitación” ─────────────────────────
        binding.btnAgregarHabitacion.setOnClickListener(v -> {
            // TODO: lógica para añadir habitación
        });

        // ─── BOTTOM NAV ──────────────────────────────────────────
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        // Marca “Habitaciones” como seleccionada
        binding.bottomNavigationView.setSelectedItemId(R.id.habitaciones_fragment);
    }


    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: implementar logout
            return true;
        }
        return false;
    }


    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.inicio_fragment) {
            intent = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            // Ya estamos aquí
            return true;
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
        // Cierra el drawer si está abierto en lugar de salir
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
