package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityPerfilBinding;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1) Tema Admin sin ActionBar nativo
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // 2) Inflate y binding
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3) Rellenar datos de ejemplo
        binding.tvName.setText("Diego Gonzales");
        binding.tvEmail.setText("diego@telehotel.com");
        binding.etFullName.setText("Diego Gonzales");
        binding.etPhone.setText("987654321");
        binding.etDocId.setText("71899878");
        binding.etBirthDate.setText("24/04/2002");

        // 4) Configurar Drawer + Toolbar
        drawerLayout = binding.drawerLayout;
        NavigationView navView = binding.navView;

        MaterialToolbar toolbar = binding.topAppBar;
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: ir a Estado de taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: ir a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 5) Overflow “Cerrar sesión”
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        Drawable ov = toolbar.getOverflowIcon();
        if (ov != null) ov.setTint(Color.WHITE);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // 6) Botones de perfil
        binding.btnEditProfile.setOnClickListener(v ->
                Snackbar.make(binding.root, "Editar perfil pulsado", Snackbar.LENGTH_SHORT).show()
        );
        binding.btnChangePassword.setOnClickListener(v ->
                Snackbar.make(binding.root, "Cambiar contraseña", Snackbar.LENGTH_SHORT).show()
        );
        binding.btnLogout.setOnClickListener(v -> doLogout());

        // 7) Bottom Navigation
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.perfil_fragment);
    }

    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            doLogout();
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
            intent = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            intent = new Intent(this, ServiciosActivity.class);
        } else if (id == R.id.reportes_fragment) {
            intent = new Intent(this, ReportesActivity.class);
        } else if (id == R.id.perfil_fragment) {
            return true;  // ya estamos aquí
        } else {
            return false;
        }
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Delega el ☰ al drawerToggle
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

    private void doLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión
                    FirebaseAuth.getInstance().signOut();

                    // Ir al LoginActivity
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
