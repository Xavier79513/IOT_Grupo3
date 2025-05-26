package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityReportesBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class ReportesActivity extends AppCompatActivity {

    private ActivityReportesBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);
        binding = ActivityReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) DRAWER
        drawerLayout = binding.drawerLayout;
        NavigationView navView = binding.navView;

        // 2) TOOLBAR COMO ACTION BAR
        MaterialToolbar toolbar = binding.topAppBar;
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);

        // 3) CONFIGURAR TOGGLE
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // 4) MENU LATERAL
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: ir a Estado taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: ir a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 5) OVERFLOW → LOGOUT
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        Drawable ov = toolbar.getOverflowIcon();
        if (ov != null) ov.setTint(Color.WHITE);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // 6) RADIOGROUP
        RadioGroup rg = binding.rgReports;
        RadioButton rbService = binding.rbByService;
        RadioButton rbUser    = binding.rbByUser;
        rg.setOnCheckedChangeListener((g, checkedId) -> {
            if (checkedId == rbService.getId()) {
                // TODO: reporte por servicio
            } else {
                // TODO: reporte por usuario
            }
        });

        // 7) BOTTOM NAV
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.reportes_fragment);
    }

    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: logout
            return true;
        }
        return false;
    }

    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent i = null;

        if (id == R.id.inicio_fragment) {
            i = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            i = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            i = new Intent(this, ServiciosActivity.class);
        } else if (id == R.id.reportes_fragment) {
            // Ya estamos aquí, no hacemos nada
            return true;
        } else if (id == R.id.perfil_fragment) {
            i = new Intent(this, PerfilActivity.class);
        } else {
            return false;
        }

        startActivity(i);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Esto es clave: deja que el toggle maneje el ☰
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
