package com.example.lab3_20206331;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class QuestionActivity extends AppCompatActivity {

    private TextView tvContador, tvPregunta, tvProgresoPregunta;
    private RadioGroup rgOpciones;
    private Button btnSiguiente;
    private int preguntaActual = 1;
    private int totalPreguntas = 3; // Esto deberías pasarlo desde la actividad anterior
    private CountDownTimer countDownTimer;

    private String[] preguntas = {
            "What is the largest living species of penguin?",
            "Which planet is closest to the sun?",
            "Who wrote Hamlet?"
    };

    private String[][] opciones = {
            {"Emperor", "Gentoo", "King", "Adele"},
            {"Mercury", "Venus", "Earth", "Mars"},
            {"Shakespeare", "Dickens", "Hemingway", "Rowling"}
    };

    private int[] respuestasCorrectas = {0, 0, 0}; // índices de las respuestas correctas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        tvContador = findViewById(R.id.tvContador);
        tvPregunta = findViewById(R.id.tvPregunta);
        tvProgresoPregunta = findViewById(R.id.tvProgresoPregunta);
        rgOpciones = findViewById(R.id.rgOpciones);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        cargarPregunta();

        btnSiguiente.setOnClickListener(v -> {
            if (preguntaActual < totalPreguntas) {
                preguntaActual++;
                cargarPregunta();
            } else {
                Toast.makeText(this, "¡Juego finalizado!", Toast.LENGTH_SHORT).show();
                // Redirige a resultado o pantalla final
            }
        });
    }

    private void cargarPregunta() {
        rgOpciones.clearCheck();
        tvPregunta.setText(preguntas[preguntaActual - 1]);
        tvProgresoPregunta.setText("Pregunta " + preguntaActual + "/" + totalPreguntas);

        RadioButton opcion1 = findViewById(R.id.opcion1);
        RadioButton opcion2 = findViewById(R.id.opcion2);
        RadioButton opcion3 = findViewById(R.id.opcion3);
        RadioButton opcion4 = findViewById(R.id.opcion4);

        String[] opcionesActuales = opciones[preguntaActual - 1];
        opcion1.setText(opcionesActuales[0]);
        opcion2.setText(opcionesActuales[1]);
        opcion3.setText(opcionesActuales[2]);
        opcion4.setText(opcionesActuales[3]);

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvContador.setText("00:" + (millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvContador.setText("00:00");
                Toast.makeText(QuestionActivity.this, "¡Tiempo agotado!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }
}
