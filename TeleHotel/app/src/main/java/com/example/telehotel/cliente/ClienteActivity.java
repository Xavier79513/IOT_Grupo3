package com.example.telehotel.cliente;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import android.view.MenuItem;

public class ClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        // Configuración del TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Cambiar el contenido basado en la selección de la pestaña
                if (tab.getPosition() == 0) {
                    // Acción para "Rooms"
                    // Aquí puedes cambiar el contenido de la vista según sea necesario
                } else if (tab.getPosition() == 1) {
                    // Acción para "Bookings"
                    // Cambiar el contenido según la pestaña seleccionada
                } else if (tab.getPosition() == 2) {
                    // Acción para "Profile"
                    // Cambiar el contenido según la pestaña seleccionada
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Configuración del BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Usamos if en lugar de switch para evitar el error "constant expression required"
                if (item.getItemId() == R.id.navigation_rooms) {
                    // Acción para navegar a "Rooms"
                    // Aquí puedes cambiar de actividad o mostrar contenido
                    return true;
                } else if (item.getItemId() == R.id.navigation_bookings) {
                    // Acción para navegar a "Bookings"
                    // Cambiar a la sección correspondiente
                    return true;
                } else if (item.getItemId() == R.id.navigation_profile) {
                    // Acción para navegar a "Profile"
                    // Cambiar a la sección de perfil
                    return true;
                }
                return false;
            }
        });
    }
}
