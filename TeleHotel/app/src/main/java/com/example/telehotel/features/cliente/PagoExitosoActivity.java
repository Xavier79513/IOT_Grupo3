package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.fragments.ReservationHistoryFragment;

public class PagoExitosoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_pagoexitoso);

        Button btnVerReservas = findViewById(R.id.btnVerReservas);
        btnVerReservas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(PagoExitosoActivity.this, ClienteMainActivity.class);
                startActivity(intent);
                finish(); */// Opcional, si no quieres que regrese a esta pantalla con "Back"
                Intent intent = new Intent(PagoExitosoActivity.this, ClienteMainActivity.class);
                intent.putExtra("navegar_a", "historial");
                startActivity(intent);
                finish();
            }
        });
    }
}