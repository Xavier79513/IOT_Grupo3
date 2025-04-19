package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class PerfilActivity extends AppCompatActivity {
    // Aquí podrías cargar datos reales
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_perfil);

        Button btnEditar = findViewById(R.id.btnEditarPerfil);
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, EditarPerfilActivity.class);
            startActivity(intent);
        });
    }
}
