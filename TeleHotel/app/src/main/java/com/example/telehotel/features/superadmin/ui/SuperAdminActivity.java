package com.example.telehotel.features.superadmin.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.example.telehotel.features.superadmin.Fragments.HabilitarTaxistaFragment;
import com.example.telehotel.features.superadmin.Fragments.LogsFragment;
import com.example.telehotel.features.superadmin.Fragments.UserListFragment;
import com.example.telehotel.features.superadmin.Fragments.StatisticsFragment;
import com.example.telehotel.features.superadmin.Fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_activity);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar fragmento por defecto (Usuarios)
        loadFragment(new UserListFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new UserListFragment();
            } else if (id == R.id.nav_stats) {
                fragment = new StatisticsFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new SettingsFragment();
            } else if (id == R.id.nav_taxista) {
                fragment = new HabilitarTaxistaFragment();
            } else if (id == R.id.nav_logs) {
                fragment = new LogsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}