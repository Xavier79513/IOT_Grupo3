package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class AgregarTaxiActivity extends AppCompatActivity {

    private Spinner spinnerAeropuerto;
    private EditText editNumeroVuelo, editHoraLlegada;
    private Button btnConfirmarTaxi;

    // Datos simulados (pueden venir de una clase Reserva o base de datos)
    private TextView txtNombreHotel, txtFecha, txtPrecioTaxi, txtPrecio;
    private ImageView imagenHotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_agregar_taxi);

        // Inicializar vistas
        spinnerAeropuerto = findViewById(R.id.spinnerAeropuerto);
        editNumeroVuelo = findViewById(R.id.editNumeroVuelo);
        editHoraLlegada = findViewById(R.id.editHoraLlegada);
        btnConfirmarTaxi = findViewById(R.id.btnConfirmarTaxi);

        txtNombreHotel = findViewById(R.id.txtNombreHotel);
        txtFecha = findViewById(R.id.txtFecha);
        txtPrecio = findViewById(R.id.txtPrecio);
        txtPrecioTaxi = findViewById(R.id.txtPrecioTaxi);
        imagenHotel = findViewById(R.id.imagenHotel);

        // Simular datos de reserva
        txtNombreHotel.setText("Hotel San Miguel");
        txtFecha.setText("Check-in: 15/05/2025 - Check-out: 18/05/2025");
        txtPrecio.setText("Total: $150");
        txtPrecioTaxi.setText("Costo adicional: $20");
        imagenHotel.setImageResource(R.drawable.sample_hotel);  // Cambia por tu recurso real

        // Llenar spinner de aeropuertos
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.lista_aeropuertos,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAeropuerto.setAdapter(adapter);

        // Listener del botón
        btnConfirmarTaxi.setOnClickListener(v -> confirmarServicioTaxi());
    }

    private void confirmarServicioTaxi() {
        String aeropuerto = spinnerAeropuerto.getSelectedItem().toString();
        String vuelo = editNumeroVuelo.getText().toString();
        String horaLlegada = editHoraLlegada.getText().toString();

        if (horaLlegada.isEmpty()) {
            editHoraLlegada.setError("La hora estimada es obligatoria");
            editHoraLlegada.requestFocus();
            return;
        }

        // Aquí podrías guardar los datos o hacer un POST al backend
        Toast.makeText(this, "Servicio de taxi confirmado. Se realizará el cobro.", Toast.LENGTH_LONG).show();

        // Ir a la siguiente vista (pantalla de taxi confirmado)
        Intent intent = new Intent(this, VistaConTaxiActivity.class);
        // Puedes pasar datos si deseas: intent.putExtra("aeropuerto", aeropuerto);
        startActivity(intent);
        finish();
    }
}
