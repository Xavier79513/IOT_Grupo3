package com.example.teletrivia;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab3_20206331.R;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategoria, spinnerDificultad;
    private EditText etCantidad;
    private Button btnComprobarConexion, btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        etCantidad = findViewById(R.id.etCantidad);
        btnComprobarConexion = findViewById(R.id.btnComprobarConexion);
        btnComenzar = findViewById(R.id.btnComenzar);

        // Llenar spinners
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this,
                R.array.categorias_array, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        ArrayAdapter<CharSequence> adapterDificultad = ArrayAdapter.createFromResource(this,
                R.array.dificultades_array, android.R.layout.simple_spinner_item);
        adapterDificultad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapterDificultad);

        // Listener de botón de conexión
        btnComprobarConexion.setOnClickListener(v -> {
            // Aquí puedes comprobar internet con un método real
            Toast.makeText(this, "Conexión OK", Toast.LENGTH_SHORT).show();
            btnComenzar.setEnabled(true);
        });

        // Listener de botón de comenzar
        btnComenzar.setOnClickListener(v -> {
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String dificultad = spinnerDificultad.getSelectedItem().toString();
            String cantidad = etCantidad.getText().toString();

            // Lógica para iniciar juego...
            Toast.makeText(this, "Comenzando juego con " + cantidad + " preguntas", Toast.LENGTH_SHORT).show();
        });
    }
}
