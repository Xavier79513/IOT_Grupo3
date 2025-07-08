package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;

public class SolicitarTaxiActivity extends AppCompatActivity {

    private String reservaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_taxi);

        // Obtener reservaId del Intent
        reservaId = getIntent().getStringExtra("reservaId");

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Solicitar Taxi");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Por ahora, solo un botón de prueba
        /*Button btnSolicitar = findViewById(R.id.btnSolicitarTaxi);
        btnSolicitar.setOnClickListener(v -> {
            // TODO: Implementar lógica de solicitar taxi
            Toast.makeText(this, "Función de taxi en desarrollo", Toast.LENGTH_SHORT).show();
            finish();
        });*/
    }
}