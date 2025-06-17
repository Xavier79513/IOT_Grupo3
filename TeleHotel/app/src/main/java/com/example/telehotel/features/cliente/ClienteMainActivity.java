package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClienteMainActivity extends AppCompatActivity {
    private static final String TAG = "ClienteMainActivity";

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

        // *** NUEVO: Procesar parámetros de búsqueda si existen ***
        procesarParametrosBusqueda(navController);
    }

    private void procesarParametrosBusqueda(NavController navController) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d(TAG, "No hay parámetros de búsqueda, navegación normal");
            return;
        }

        try {
            // Extraer parámetros del Intent
            String searchLocation = extras.getString("search_location", "");
            long startDate = extras.getLong("start_date", 0);
            long endDate = extras.getLong("end_date", 0);
            int adults = extras.getInt("adults", 2);
            int children = extras.getInt("children", 0);
            int rooms = extras.getInt("rooms", 1);
            int hotelsFound = extras.getInt("hotels_found", 0);
            boolean useFilters = extras.getBoolean("use_filters", true);

            Log.d(TAG, String.format("Parámetros recibidos: Ubicación='%s', Fechas=%d-%d, Huéspedes=%d+%d, Habitaciones=%d",
                    searchLocation, startDate, endDate, adults, children, rooms));

            // Validar parámetros críticos
            if (searchLocation.isEmpty() || startDate <= 0 || endDate <= 0) {
                Log.e(TAG, "Parámetros inválidos recibidos");
                return;
            }

            // *** CLAVE: Crear Bundle para Navigation Component ***
            Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString("search_location", searchLocation);
            fragmentArgs.putLong("start_date", startDate);
            fragmentArgs.putLong("end_date", endDate);
            fragmentArgs.putInt("adults", adults);
            fragmentArgs.putInt("children", children);
            fragmentArgs.putInt("rooms", rooms);
            fragmentArgs.putInt("hotels_found", hotelsFound);
            fragmentArgs.putBoolean("use_filters", useFilters);

            // *** NAVEGAR AL FRAGMENT DE HOTELES CON PARÁMETROS ***
            // Cambia R.id.hotelsFragment por el ID correcto de tu navigation graph
            navController.navigate(R.id.nav_rooms, fragmentArgs);

            Log.d(TAG, "Navegando a HotelsFragment con parámetros");

        } catch (Exception e) {
            Log.e(TAG, "Error procesando parámetros de búsqueda", e);
        }
    }
}