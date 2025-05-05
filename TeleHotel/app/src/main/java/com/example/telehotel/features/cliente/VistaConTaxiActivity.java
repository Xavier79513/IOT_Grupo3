package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class VistaConTaxiActivity extends AppCompatActivity {

    private TextView txtNombreConductor, txtVehiculo, txtPlaca, txtAeropuerto, txtHoraLlegada;
    private ImageView qrReserva;
    private Button btnAsignarConductor, btnCambiarConductor, btnLlamarConductor;

    // Simulación de datos (en un escenario real estos vendrían de base de datos o API)
    private boolean conductorAsignado = false;
    private String nombreConductor = "Juan Pérez";
    private String telefonoConductor = "987654321";
    private String vehiculo = "Toyota Corolla";
    private String placa = "ABC-123";
    private String aeropuerto = "Jorge Chávez - Lima";
    private String horaLlegada = "08:30 AM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_vista_con_taxi);

        // Referencias
        txtNombreConductor = findViewById(R.id.txtNombreConductor);
        txtVehiculo = findViewById(R.id.txtVehiculo);
        txtPlaca = findViewById(R.id.txtPlaca);
        txtAeropuerto = findViewById(R.id.txtAeropuerto);
        txtHoraLlegada = findViewById(R.id.txtHoraLlegada);
        qrReserva = findViewById(R.id.qrReserva);

        btnAsignarConductor = findViewById(R.id.btnAsignarConductor);
        btnCambiarConductor = findViewById(R.id.btnCambiarConductor);
        btnLlamarConductor = findViewById(R.id.btnLlamarConductor);

        // Mostrar datos iniciales
        txtAeropuerto.setText("Aeropuerto: " + aeropuerto);
        txtHoraLlegada.setText("Hora estimada: " + horaLlegada);

        if (conductorAsignado) {
            mostrarConductorAsignado();
        } else {
            mostrarSinConductor();
        }

        // Acciones
        btnAsignarConductor.setOnClickListener(v -> asignarConductor());
        btnCambiarConductor.setOnClickListener(v -> cambiarConductor());
        btnLlamarConductor.setOnClickListener(v -> llamarConductor());
    }

    private void mostrarConductorAsignado() {
        txtNombreConductor.setText("Conductor: " + nombreConductor);
        txtVehiculo.setText("Vehículo: " + vehiculo);
        txtPlaca.setText("Placa: " + placa);
        qrReserva.setImageResource(R.drawable.qr_placeholder); // QR estático (luego puedes usar ZXing)

        btnAsignarConductor.setVisibility(View.GONE);
        btnCambiarConductor.setVisibility(View.VISIBLE);
        btnLlamarConductor.setVisibility(View.VISIBLE);
    }

    private void mostrarSinConductor() {
        txtNombreConductor.setText("Conductor: Aún no asignado");
        txtVehiculo.setText("Vehículo: -");
        txtPlaca.setText("Placa: -");

        qrReserva.setImageResource(R.drawable.qr_placeholder); // Puede estar invisible si deseas
        btnAsignarConductor.setVisibility(View.VISIBLE);
        btnCambiarConductor.setVisibility(View.GONE);
        btnLlamarConductor.setVisibility(View.GONE);
    }

    private void asignarConductor() {
        // Aquí puedes hacer la llamada a tu backend o lógica de asignación
        Toast.makeText(this, "Conductor asignado exitosamente", Toast.LENGTH_SHORT).show();
        conductorAsignado = true;
        mostrarConductorAsignado();
    }

    private void cambiarConductor() {
        Toast.makeText(this, "Buscando otro conductor disponible...", Toast.LENGTH_SHORT).show();
        // En producción deberías consultar al backend para reasignar
        nombreConductor = "Carlos López";
        telefonoConductor = "987123456";
        vehiculo = "Hyundai Accent";
        placa = "XYZ-789";
        mostrarConductorAsignado();
    }

    private void llamarConductor() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + telefonoConductor));
        startActivity(intent);
    }
}
