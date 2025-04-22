package com.example.lab3_20206331;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private TextView tvCorrectas, tvIncorrectas, tvNoRespondidas;
    private Button btnVolverAJugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        tvCorrectas = findViewById(R.id.tvCorrectas);
        tvIncorrectas = findViewById(R.id.tvIncorrectas);
        tvNoRespondidas = findViewById(R.id.tvNoRespondidas);
        btnVolverAJugar = findViewById(R.id.btnVolverAJugar);

        // Recibir los datos desde la actividad anterior
        int correctas = getIntent().getIntExtra("correctas", 0);
        int incorrectas = getIntent().getIntExtra("incorrectas", 0);
        int noRespondidas = getIntent().getIntExtra("noRespondidas", 0);

        // Mostrar resultados
        tvCorrectas.setText(String.valueOf(correctas));
        tvIncorrectas.setText(String.valueOf(incorrectas));
        tvNoRespondidas.setText(String.valueOf(noRespondidas));

        btnVolverAJugar.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, com.example.teletrivia.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
