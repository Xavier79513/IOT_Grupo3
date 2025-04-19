package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class EditarPerfilActivity extends AppCompatActivity {
    EditText editNombre, editCorreo, editTelefono, editDireccion, editFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_editar_perfil);

        editNombre = findViewById(R.id.editNombre);
        editCorreo = findViewById(R.id.editCorreo);
        editTelefono = findViewById(R.id.editTelefono);
        editDireccion = findViewById(R.id.editDireccion);
        editFecha = findViewById(R.id.editFecha);

        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        btnGuardar.setOnClickListener(v -> {
            // Aquí puedes guardar los cambios (en SharedPreferences, DB o donde manejes tus datos)
            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
            finish(); // vuelve a la vista de perfil
        });

        btnCancelar.setOnClickListener(v -> {
            finish(); // simplemente vuelve a la vista sin guardar
        });
    }
}
