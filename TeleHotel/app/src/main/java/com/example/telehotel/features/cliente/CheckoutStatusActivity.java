package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class CheckoutStatusActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutStatus";

    // Views
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout layoutEsperando, layoutListo, layoutCompletado;

    // Estado: Esperando
    private TextView tvMensajeEspera;

    // Estado: Listo para pagar
    private TextView tvMontoTotal, tvCargosAdicionales;
    private Button btnPagarAhora, btnSolicitarTaxi;

    // Estado: Completado
    private TextView tvCheckoutCompletado;
    private Button btnVolverInicio;

    // Datos
    private String reservaId;
    private Reserva reserva;
    private FirebaseFirestore db;
    private ListenerRegistration listenerReserva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_status);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        reservaId = getIntent().getStringExtra("reservaId");
        if (reservaId == null) {
            finish();
            return;
        }

        // Inicializar views
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar listeners
        setupListeners();

        // Escuchar cambios en la reserva
        escucharCambiosReserva();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        // Estados
        layoutEsperando = findViewById(R.id.layoutEsperando);
        layoutListo = findViewById(R.id.layoutListo);
        layoutCompletado = findViewById(R.id.layoutCompletado);

        // Esperando
        tvMensajeEspera = findViewById(R.id.tvMensajeEspera);

        // Listo para pagar
        tvMontoTotal = findViewById(R.id.tvMontoTotal);
        tvCargosAdicionales = findViewById(R.id.tvCargosAdicionales);
        btnPagarAhora = findViewById(R.id.btnPagarAhora);
        btnSolicitarTaxi = findViewById(R.id.btnSolicitarTaxi);

        // Completado
        tvCheckoutCompletado = findViewById(R.id.tvCheckoutCompletado);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Estado del Checkout");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnPagarAhora.setOnClickListener(v -> procesarPago());

        btnSolicitarTaxi.setOnClickListener(v -> solicitarTaxi());

        btnVolverInicio.setOnClickListener(v -> {
            // Volver al inicio o historial
            finish();
        });
    }

    private void escucharCambiosReserva() {
        mostrarLoading(true);

        DocumentReference docRef = db.collection("reservas").document(reservaId);

        listenerReserva = docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Error escuchando cambios de reserva", e);
                mostrarError("Error obteniendo estado del checkout");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                reserva = snapshot.toObject(Reserva.class);
                if (reserva != null) {
                    reserva.setId(snapshot.getId());
                    actualizarUI();
                }
            } else {
                Log.d(TAG, "Reserva no encontrada");
                mostrarError("Reserva no encontrada");
            }

            mostrarLoading(false);
        });
    }

    private void actualizarUI() {
        String estado = reserva.getEstado();
        Log.d(TAG, "Estado actual de la reserva: " + estado);

        // Ocultar todos los layouts
        layoutEsperando.setVisibility(View.GONE);
        layoutListo.setVisibility(View.GONE);
        layoutCompletado.setVisibility(View.GONE);

        switch (estado) {
            case "checkout_solicitado":
            case "checkout_procesando":
                mostrarEstadoEsperando();
                break;

            case "checkout_listo":
                mostrarEstadoListo();
                break;

            case "completada":
                mostrarEstadoCompletado();
                break;

            default:
                mostrarError("Estado de checkout desconocido: " + estado);
                break;
        }
    }

    private void mostrarEstadoEsperando() {
        layoutEsperando.setVisibility(View.VISIBLE);

        String mensaje = "El administrador del hotel está revisando tu habitación.\n\n" +
                "Te notificaremos cuando el checkout esté listo para procesar.";
        tvMensajeEspera.setText(mensaje);
    }

    private void mostrarEstadoListo() {
        layoutListo.setVisibility(View.VISIBLE);

        // Mostrar monto total
        double total = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        tvMontoTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", total));

        // Mostrar si hay cargos adicionales
        if (reserva.getServiciosAdicionales() != null && !reserva.getServiciosAdicionales().isEmpty()) {
            tvCargosAdicionales.setVisibility(View.VISIBLE);
            tvCargosAdicionales.setText("Se han agregado " +
                    reserva.getServiciosAdicionales().size() + " cargo(s) adicional(es)");
        } else {
            tvCargosAdicionales.setVisibility(View.GONE);
        }

        // Verificar si cumple requisitos para taxi
        double montoMinimo = 0.0; // Obtener del hotel si es necesario
        btnSolicitarTaxi.setEnabled(total >= montoMinimo);
    }

    private void mostrarEstadoCompletado() {
        layoutCompletado.setVisibility(View.VISIBLE);

        String mensaje = "¡Checkout completado exitosamente!\n\n" +
                "Gracias por hospedarte con nosotros.";
        tvCheckoutCompletado.setText(mensaje);
    }

    private void procesarPago() {
        // Aquí iría la integración con el sistema de pagos
        // Por ahora, simular el pago exitoso

        Toast.makeText(this, "Procesando pago...", Toast.LENGTH_SHORT).show();

        // Actualizar estado a completada
        db.collection("reservas")
                .document(reservaId)
                .update("estado", "completada",
                        "fechaPagoFinal", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pago procesado exitosamente");
                    Toast.makeText(this, "¡Pago realizado exitosamente!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error procesando pago", e);
                    Toast.makeText(this, "Error procesando pago: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void solicitarTaxi() {
        // Abrir actividad o fragment para solicitar taxi
        Intent intent = new Intent(this, SolicitarTaxiActivity.class);
        intent.putExtra("reservaId", reservaId);
        startActivity(intent);
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerReserva != null) {
            listenerReserva.remove();
        }
    }

    @Override
    public void onBackPressed() {
        // Solo permitir volver si el checkout está completado
        if (reserva != null && "completada".equals(reserva.getEstado())) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Debes esperar a que se complete el checkout", Toast.LENGTH_SHORT).show();
        }
    }
}