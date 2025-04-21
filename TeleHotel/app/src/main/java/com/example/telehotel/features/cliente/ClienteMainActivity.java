package com.example.telehotel.features.cliente;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClienteMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_main_activity);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Esta es la forma MÁS SEGURA de obtener el NavController
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}
