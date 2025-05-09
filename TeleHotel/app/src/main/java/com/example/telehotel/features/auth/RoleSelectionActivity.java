package com.example.telehotel.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.features.admin.AdminActivity;
import com.example.telehotel.features.cliente.ClientePaginaPrincipal;
import com.example.telehotel.features.superadmin.ui.SuperAdminActivity;
import com.example.telehotel.features.taxista.TaxistaActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Botón para Admin
        Button adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la vista de Admin
                navigateToAdminView();
            }
        });

        // Botón para Superadmin
        Button superAdminButton = findViewById(R.id.superAdminButton);
        superAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la vista de Superadmin
                navigateToSuperAdminView();
            }
        });

        // Botón para Taxista
        Button taxistaButton = findViewById(R.id.taxistaButton);
        taxistaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la vista de Taxista
                navigateToTaxistaView();
            }
        });

        // Botón para Cliente
        Button clienteButton = findViewById(R.id.clienteButton);
        clienteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a la vista de Cliente
                navigateToClienteView();
            }
        });
    }

    // Métodos para navegar a las diferentes vistas de los roles

    private void navigateToAdminView() {
        // Lógica para navegar a la vista de Admin
        Intent intent = new Intent(RoleSelectionActivity.this, AdminActivity.class);
        startActivity(intent);
    }

    private void navigateToSuperAdminView() {
        // Lógica para navegar a la vista de Superadmin
        Intent intent = new Intent(RoleSelectionActivity.this, SuperAdminActivity.class);
        startActivity(intent);
    }

    private void navigateToTaxistaView() {
        // Lógica para navegar a la vista de Taxista
        Intent intent = new Intent(RoleSelectionActivity.this, TaxistaActivity.class);
        startActivity(intent);
    }

    private void navigateToClienteView() {
        // Lógica para navegar a la nueva actividad principal del cliente
        //Intent intent = new Intent(RoleSelectionActivity.this, ClienteMainActivity.class);
        Intent intent = new Intent(RoleSelectionActivity.this, ClientePaginaPrincipal.class);
        startActivity(intent);
    }
}
