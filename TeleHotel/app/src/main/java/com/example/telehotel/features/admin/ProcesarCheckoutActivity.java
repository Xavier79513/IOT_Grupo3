/*package com.example.telehotel.features.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.CobroAdicional;
import com.example.telehotel.data.model.NotificacionCheckout;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.admin.adapters.CobrosAdicionalesAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProcesarCheckoutActivity extends AppCompatActivity implements CobrosAdicionalesAdapter.OnCobroClickListener {

    private static final String TAG = "ProcesarCheckout";

    // Views
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Información de la reserva
    private TextView tvClienteNombre, tvClienteEmail, tvClienteTelefono;
    private TextView tvHabitacionNumero, tvHabitacionTipo;
    private TextView tvFechaEstadia, tvMontoOriginal;

    // Sección de cargos adicionales
    private CardView layoutCargosAdicionales;
    private RecyclerView recyclerCobrosAdicionales;
    private Button btnAgregarCargo;
    private EditText etConceptoCargo, etMontoCargo;

    // Totales
    private TextView tvSubtotal, tvImpuestos, tvTotal;

    // Acciones
    private Button btnProcesarCheckout, btnRechazar;

    // Datos
    private String notificacionId, reservaId, hotelId;
    private Reserva reserva;
    private NotificacionCheckout notificacion;
    private List<CobroAdicional> cobrosAdicionales = new ArrayList<>();
    private CobrosAdicionalesAdapter cobrosAdapter;
    private FirebaseFirestore db;

    // Cálculos
    private double montoOriginal = 0.0;
    private double subtotal = 0.0;
    private double impuestos = 0.0;
    private double total = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procesar_checkout);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        obtenerDatosIntent();

        // Inicializar views
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerView de cargos
        setupRecyclerViewCargos();

        // Configurar listeners
        setupListeners();

        // Cargar datos de la reserva
        cargarDatosReserva();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        notificacionId = intent.getStringExtra("notificacionId");
        reservaId = intent.getStringExtra("reservaId");
        hotelId = intent.getStringExtra("hotelId");

        Log.d(TAG, "Procesando checkout - ReservaId: " + reservaId + ", NotificacionId: " + notificacionId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        // Información de la reserva
        tvClienteNombre = findViewById(R.id.tvClienteNombre);
        tvClienteEmail = findViewById(R.id.tvClienteEmail);
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono);
        tvHabitacionNumero = findViewById(R.id.tvHabitacionNumero);
        tvHabitacionTipo = findViewById(R.id.tvHabitacionTipo);
        tvFechaEstadia = findViewById(R.id.tvFechaEstadia);
        tvMontoOriginal = findViewById(R.id.tvMontoOriginal);

        // Cargos adicionales
        layoutCargosAdicionales = findViewById(R.id.layoutCargosAdicionales);
        recyclerCobrosAdicionales = findViewById(R.id.recyclerCobrosAdicionales);
        btnAgregarCargo = findViewById(R.id.btnAgregarCargo);
        etConceptoCargo = findViewById(R.id.etConceptoCargo);
        etMontoCargo = findViewById(R.id.etMontoCargo);

        // Totales
        tvSubtotal = findViewById(R.id.tvSubtotal);
        //tvImpuestos = findViewById(R.id.tvImpuestos);
        tvTotal = findViewById(R.id.tvTotal);

        // Acciones
        btnProcesarCheckout = findViewById(R.id.btnProcesarCheckout);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Procesar Checkout");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViewCargos() {
        cobrosAdapter = new CobrosAdicionalesAdapter(cobrosAdicionales, this, this);
        recyclerCobrosAdicionales.setLayoutManager(new LinearLayoutManager(this));
        recyclerCobrosAdicionales.setAdapter(cobrosAdapter);
    }

    private void setupListeners() {
        btnAgregarCargo.setOnClickListener(v -> agregarCargoAdicional());

        btnProcesarCheckout.setOnClickListener(v -> mostrarConfirmacionProcesar());

        btnRechazar.setOnClickListener(v -> mostrarConfirmacionRechazar());

        // Listener para recalcular totales cuando cambie el monto
        etMontoCargo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Podríamos validar en tiempo real aquí si es necesario
            }
        });
    }

    private void cargarDatosReserva() {
        mostrarLoading(true);

        Log.d(TAG, "Cargando datos de la reserva: " + reservaId);

        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        reserva = documentSnapshot.toObject(Reserva.class);
                        if (reserva != null) {
                            reserva.setId(documentSnapshot.getId());
                            mostrarInformacionReserva();
                            cargarNotificacion();
                        } else {
                            Log.e(TAG, "Error: reserva es null");
                            mostrarError("Error cargando datos de la reserva");
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Reserva no encontrada");
                        mostrarError("Reserva no encontrada");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reserva", e);
                    mostrarError("Error cargando reserva: " + e.getMessage());
                    finish();
                });
    }

    private void cargarNotificacion() {
        if (notificacionId == null || notificacionId.isEmpty()) {
            mostrarLoading(false);
            return;
        }

        db.collection("notificaciones_admin")
                .document(notificacionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        notificacion = documentSnapshot.toObject(NotificacionCheckout.class);
                        if (notificacion != null) {
                            notificacion.setId(documentSnapshot.getId());
                        }
                    }
                    mostrarLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando notificación", e);
                    mostrarLoading(false);
                });
    }

    private void mostrarInformacionReserva() {
        if (reserva == null) return;

        // Información del cliente
        tvClienteNombre.setText(reserva.getClienteNombre() != null ?
                reserva.getClienteNombre() : "No especificado");
        tvClienteEmail.setText(reserva.getClienteEmail() != null ?
                reserva.getClienteEmail() : "No especificado");
        tvClienteTelefono.setText(reserva.getClienteTelefono() != null ?
                reserva.getClienteTelefono() : "No especificado");

        // Información de la habitación
        tvHabitacionNumero.setText("Habitación " +
                (reserva.getHabitacionNumero() != null ? reserva.getHabitacionNumero() : "N/A"));
        tvHabitacionTipo.setText(reserva.getHabitacionTipo() != null ?
                reserva.getHabitacionTipo() : "No especificado");

        // Fechas de estadía
        String fechas = formatearFechasEstadia();
        tvFechaEstadia.setText(fechas);

        // Monto original
        montoOriginal = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        tvMontoOriginal.setText(String.format(Locale.getDefault(), "S/ %.2f", montoOriginal));

        // Calcular totales iniciales
        calcularTotales();
    }

    private String formatearFechasEstadia() {
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            try {
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String inicio = formatter.format(new java.util.Date(reserva.getFechaInicio()));
                String fin = formatter.format(new java.util.Date(reserva.getFechaFin()));
                return inicio + " - " + fin;
            } catch (Exception e) {
                Log.e(TAG, "Error formateando fechas", e);
            }
        }
        return "Fechas no disponibles";
    }

    private void agregarCargoAdicional() {
        String concepto = etConceptoCargo.getText().toString().trim();
        String montoStr = etMontoCargo.getText().toString().trim();

        if (concepto.isEmpty()) {
            etConceptoCargo.setError("Ingresa el concepto del cargo");
            etConceptoCargo.requestFocus();
            return;
        }

        if (montoStr.isEmpty()) {
            etMontoCargo.setError("Ingresa el monto del cargo");
            etMontoCargo.requestFocus();
            return;
        }

        try {
            double monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                etMontoCargo.setError("El monto debe ser mayor a 0");
                etMontoCargo.requestFocus();
                return;
            }

            // Crear el cargo adicional
            CobroAdicional cargo = new CobroAdicional();
            cargo.concepto = concepto;
            cargo.monto = monto;

            // Agregar a la lista
            cobrosAdicionales.add(cargo);
            cobrosAdapter.notifyItemInserted(cobrosAdicionales.size() - 1);

            // Limpiar campos
            etConceptoCargo.setText("");
            etMontoCargo.setText("");

            // Recalcular totales
            calcularTotales();

            Log.d(TAG, "Cargo adicional agregado: " + concepto + " - S/" + monto);

        } catch (NumberFormatException e) {
            etMontoCargo.setError("Monto inválido");
            etMontoCargo.requestFocus();
        }
    }

    @Override
    public void onEliminarCargo(int position) {
        if (position >= 0 && position < cobrosAdicionales.size()) {
            cobrosAdicionales.remove(position);
            cobrosAdapter.notifyItemRemoved(position);
            calcularTotales();
        }
    }

    private void calcularTotales() {
        // Calcular subtotal (monto original + cargos adicionales)
        double totalCargos = 0.0;
        for (CobroAdicional cargo : cobrosAdicionales) {
            totalCargos += cargo.monto != null ? cargo.monto : 0.0;
        }

        subtotal = montoOriginal + totalCargos;

        // Calcular impuestos (18% sobre el subtotal)
        //impuestos = subtotal * 0.18;

        // Calcular total
        //total = subtotal + impuestos;
        total = subtotal;

        // Actualizar UI
        tvSubtotal.setText(String.format(Locale.getDefault(), "S/ %.2f", subtotal));
        //tvImpuestos.setText(String.format(Locale.getDefault(), "S/ %.2f", impuestos));
        tvTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", total));

        //Log.d(TAG, "Totales calculados - Subtotal: S/" + subtotal + ", Impuestos: S/" + impuestos + ", Total: S/" + total);
        Log.d(TAG, "Totales calculados - Subtotal: S/" + subtotal +  ", Total: S/" + total);

    }

    private void mostrarConfirmacionProcesar() {
        String mensaje = "¿Confirmas el procesamiento del checkout?\n\n" +
                "Cliente: " + reserva.getClienteNombre() + "\n" +
                "Habitación: " + reserva.getHabitacionNumero() + "\n" +
                "Total a cobrar: S/ " + String.format(Locale.getDefault(), "%.2f", total);

        if (!cobrosAdicionales.isEmpty()) {
            mensaje += "\n\nCargos adicionales: " + cobrosAdicionales.size();
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Checkout")
                .setMessage(mensaje)
                .setPositiveButton("Sí, procesar", (dialog, which) -> procesarCheckout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarConfirmacionRechazar() {
        new AlertDialog.Builder(this)
                .setTitle("Rechazar Checkout")
                .setMessage("¿Estás seguro de que quieres rechazar esta solicitud de checkout?")
                .setPositiveButton("Sí, rechazar", (dialog, which) -> rechazarCheckout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void procesarCheckout() {
        mostrarLoading(true);
        btnProcesarCheckout.setEnabled(false);
        btnRechazar.setEnabled(false);

        Log.d(TAG, "Iniciando procesamiento de checkout");

        // 1. Actualizar la reserva
        actualizarReserva();
    }

    private void actualizarReserva() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "completada");
        updates.put("montoTotal", total);
        //updates.put("impuestos", impuestos);
        updates.put("fechaCheckoutProcesado", System.currentTimeMillis());

        // Agregar cargos adicionales si los hay
        if (!cobrosAdicionales.isEmpty()) {
            updates.put("cobrosAdicionales", cobrosAdicionales);
        }

        db.collection("reservas")
                .document(reservaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva actualizada exitosamente");
                    // 2. Actualizar la notificación
                    actualizarNotificacion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando reserva", e);
                    mostrarError("Error procesando checkout: " + e.getMessage());
                    restablecerBotones();
                });
    }

    private void actualizarNotificacion() {
        if (notificacionId == null || notificacionId.isEmpty()) {
            Log.w(TAG, "No hay notificación para actualizar");
            finalizarProcesamiento();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("procesada", true);
        updates.put("estadoProcesamiento", "completada");
        updates.put("fechaProcesamiento", System.currentTimeMillis());

        db.collection("notificaciones_admin")
                .document(notificacionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación actualizada exitosamente");
                    // 3. Liberar la habitación
                    liberarHabitacion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando notificación", e);
                    // Continuar aunque falle la notificación
                    liberarHabitacion();
                });
    }

    private void liberarHabitacion() {
        // Actualizar estado de la habitación a "disponible"
        String habitacionId = reserva.getHabitacionId();
        if (habitacionId != null && !habitacionId.isEmpty()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", "disponible");

            db.collection("hoteles")
                    .document(hotelId)
                    .collection("habitaciones")
                    .document(habitacionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Habitación liberada exitosamente");
                        finalizarProcesamiento();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error liberando habitación", e);
                        // Continuar aunque falle
                        finalizarProcesamiento();
                    });
        } else {
            finalizarProcesamiento();
        }
    }

    private void finalizarProcesamiento() {
        mostrarLoading(false);

        Toast.makeText(this, "Checkout procesado exitosamente", Toast.LENGTH_LONG).show();

        // Retornar resultado y cerrar actividad
        setResult(RESULT_OK);
        finish();
    }

    private void rechazarCheckout() {
        mostrarLoading(true);
        btnProcesarCheckout.setEnabled(false);
        btnRechazar.setEnabled(false);

        // Actualizar estado de la reserva a "activa" (volver al estado anterior)
        Map<String, Object> reservaUpdates = new HashMap<>();
        reservaUpdates.put("estado", "activa");

        db.collection("reservas")
                .document(reservaId)
                .update(reservaUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar notificación como rechazada
                    if (notificacionId != null && !notificacionId.isEmpty()) {
                        Map<String, Object> notifUpdates = new HashMap<>();
                        notifUpdates.put("procesada", true);
                        notifUpdates.put("estadoProcesamiento", "rechazada");
                        notifUpdates.put("fechaProcesamiento", System.currentTimeMillis());

                        db.collection("notificaciones_admin")
                                .document(notificacionId)
                                .update(notifUpdates);
                    }

                    mostrarLoading(false);
                    Toast.makeText(this, "Solicitud de checkout rechazada", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rechazando checkout", e);
                    mostrarError("Error rechazando solicitud: " + e.getMessage());
                    restablecerBotones();
                });
    }

    private void restablecerBotones() {
        mostrarLoading(false);
        btnProcesarCheckout.setEnabled(true);
        btnRechazar.setEnabled(true);
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Salir sin procesar?")
                .setMessage("Si sales ahora, la solicitud de checkout quedará pendiente.")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }
}*/
package com.example.telehotel.features.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.CobroAdicional;
import com.example.telehotel.data.model.NotificacionCheckout;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.features.admin.adapters.CobrosAdicionalesAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProcesarCheckoutActivity extends AppCompatActivity implements CobrosAdicionalesAdapter.OnCobroClickListener {

    private static final String TAG = "ProcesarCheckout";

    // Views
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Información de la reserva
    private TextView tvClienteNombre, tvClienteEmail, tvClienteTelefono;
    private TextView tvHabitacionNumero, tvHabitacionTipo;
    private TextView tvFechaEstadia, tvMontoOriginal;

    // Sección de cargos adicionales
    private CardView layoutCargosAdicionales;
    private RecyclerView recyclerCobrosAdicionales;
    private Button btnAgregarCargo;
    private EditText etConceptoCargo, etMontoCargo;

    // Totales
    private TextView tvSubtotal, tvImpuestos, tvTotal;

    // Acciones
    private Button btnProcesarCheckout, btnRechazar;

    // Datos
    private String notificacionId, reservaId, hotelId;
    private Reserva reserva;
    private NotificacionCheckout notificacion;
    private List<CobroAdicional> cobrosAdicionales = new ArrayList<>();
    private CobrosAdicionalesAdapter cobrosAdapter;
    private FirebaseFirestore db;

    // Cálculos
    private double montoOriginal = 0.0;
    private double subtotal = 0.0;
    private double impuestos = 0.0;
    private double total = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procesar_checkout);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        obtenerDatosIntent();

        // Inicializar views
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerView de cargos
        setupRecyclerViewCargos();

        // Configurar listeners
        setupListeners();

        // Cargar datos de la reserva
        cargarDatosReserva();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        notificacionId = intent.getStringExtra("notificacionId");
        reservaId = intent.getStringExtra("reservaId");
        hotelId = intent.getStringExtra("hotelId");

        Log.d(TAG, "Procesando checkout - ReservaId: " + reservaId + ", NotificacionId: " + notificacionId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        // Información de la reserva
        tvClienteNombre = findViewById(R.id.tvClienteNombre);
        tvClienteEmail = findViewById(R.id.tvClienteEmail);
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono);
        tvHabitacionNumero = findViewById(R.id.tvHabitacionNumero);
        tvHabitacionTipo = findViewById(R.id.tvHabitacionTipo);
        tvFechaEstadia = findViewById(R.id.tvFechaEstadia);
        tvMontoOriginal = findViewById(R.id.tvMontoOriginal);

        // Cargos adicionales
        layoutCargosAdicionales = findViewById(R.id.layoutCargosAdicionales);
        recyclerCobrosAdicionales = findViewById(R.id.recyclerCobrosAdicionales);
        btnAgregarCargo = findViewById(R.id.btnAgregarCargo);
        etConceptoCargo = findViewById(R.id.etConceptoCargo);
        etMontoCargo = findViewById(R.id.etMontoCargo);

        // Totales
        tvSubtotal = findViewById(R.id.tvSubtotal);
        //tvImpuestos = findViewById(R.id.tvImpuestos);
        tvTotal = findViewById(R.id.tvTotal);

        // Acciones
        btnProcesarCheckout = findViewById(R.id.btnProcesarCheckout);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Procesar Checkout");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViewCargos() {
        cobrosAdapter = new CobrosAdicionalesAdapter(cobrosAdicionales, this, this);
        recyclerCobrosAdicionales.setLayoutManager(new LinearLayoutManager(this));
        recyclerCobrosAdicionales.setAdapter(cobrosAdapter);
    }

    private void setupListeners() {
        btnAgregarCargo.setOnClickListener(v -> agregarCargoAdicional());

        btnProcesarCheckout.setOnClickListener(v -> mostrarConfirmacionProcesar());

        btnRechazar.setOnClickListener(v -> mostrarConfirmacionRechazar());

        // Listener para recalcular totales cuando cambie el monto
        etMontoCargo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Podríamos validar en tiempo real aquí si es necesario
            }
        });
    }

    private void cargarDatosReserva() {
        mostrarLoading(true);

        Log.d(TAG, "Cargando datos de la reserva: " + reservaId);

        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        reserva = documentSnapshot.toObject(Reserva.class);
                        if (reserva != null) {
                            reserva.setId(documentSnapshot.getId());
                            mostrarInformacionReserva();
                            cargarNotificacion();
                        } else {
                            Log.e(TAG, "Error: reserva es null");
                            mostrarError("Error cargando datos de la reserva");
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Reserva no encontrada");
                        mostrarError("Reserva no encontrada");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reserva", e);
                    mostrarError("Error cargando reserva: " + e.getMessage());
                    finish();
                });
    }

    private void cargarNotificacion() {
        if (notificacionId == null || notificacionId.isEmpty()) {
            mostrarLoading(false);
            return;
        }

        db.collection("notificaciones_admin")
                .document(notificacionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        notificacion = documentSnapshot.toObject(NotificacionCheckout.class);
                        if (notificacion != null) {
                            notificacion.setId(documentSnapshot.getId());
                        }
                    }
                    mostrarLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando notificación", e);
                    mostrarLoading(false);
                });
    }

    private void mostrarInformacionReserva() {
        if (reserva == null) return;

        // Información del cliente
        tvClienteNombre.setText(reserva.getClienteNombre() != null ?
                reserva.getClienteNombre() : "No especificado");
        tvClienteEmail.setText(reserva.getClienteEmail() != null ?
                reserva.getClienteEmail() : "No especificado");
        tvClienteTelefono.setText(reserva.getClienteTelefono() != null ?
                reserva.getClienteTelefono() : "No especificado");

        // Información de la habitación
        tvHabitacionNumero.setText("Habitación " +
                (reserva.getHabitacionNumero() != null ? reserva.getHabitacionNumero() : "N/A"));
        tvHabitacionTipo.setText(reserva.getHabitacionTipo() != null ?
                reserva.getHabitacionTipo() : "No especificado");

        // Fechas de estadía
        String fechas = formatearFechasEstadia();
        tvFechaEstadia.setText(fechas);

        // Monto original
        montoOriginal = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        tvMontoOriginal.setText(String.format(Locale.getDefault(), "S/ %.2f", montoOriginal));

        // Calcular totales iniciales
        calcularTotales();
    }

    private String formatearFechasEstadia() {
        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            try {
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String inicio = formatter.format(new java.util.Date(reserva.getFechaInicio()));
                String fin = formatter.format(new java.util.Date(reserva.getFechaFin()));
                return inicio + " - " + fin;
            } catch (Exception e) {
                Log.e(TAG, "Error formateando fechas", e);
            }
        }
        return "Fechas no disponibles";
    }

    private void agregarCargoAdicional() {
        String concepto = etConceptoCargo.getText().toString().trim();
        String montoStr = etMontoCargo.getText().toString().trim();

        if (concepto.isEmpty()) {
            etConceptoCargo.setError("Ingresa el concepto del cargo");
            etConceptoCargo.requestFocus();
            return;
        }

        if (montoStr.isEmpty()) {
            etMontoCargo.setError("Ingresa el monto del cargo");
            etMontoCargo.requestFocus();
            return;
        }

        try {
            double monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                etMontoCargo.setError("El monto debe ser mayor a 0");
                etMontoCargo.requestFocus();
                return;
            }

            // Crear el cargo adicional
            CobroAdicional cargo = new CobroAdicional();
            cargo.concepto = concepto;
            cargo.monto = monto;

            // Agregar a la lista
            cobrosAdicionales.add(cargo);
            cobrosAdapter.notifyItemInserted(cobrosAdicionales.size() - 1);

            // Limpiar campos
            etConceptoCargo.setText("");
            etMontoCargo.setText("");

            // Recalcular totales
            calcularTotales();

            Log.d(TAG, "Cargo adicional agregado: " + concepto + " - S/" + monto);

        } catch (NumberFormatException e) {
            etMontoCargo.setError("Monto inválido");
            etMontoCargo.requestFocus();
        }
    }

    @Override
    public void onEliminarCargo(int position) {
        if (position >= 0 && position < cobrosAdicionales.size()) {
            cobrosAdicionales.remove(position);
            cobrosAdapter.notifyItemRemoved(position);
            calcularTotales();
        }
    }

    private void calcularTotales() {
        // Calcular subtotal (monto original + cargos adicionales)
        double totalCargos = 0.0;
        for (CobroAdicional cargo : cobrosAdicionales) {
            totalCargos += cargo.monto != null ? cargo.monto : 0.0;
        }

        subtotal = montoOriginal + totalCargos;

        // Calcular impuestos (18% sobre el subtotal)
        //impuestos = subtotal * 0.18;

        // Calcular total
        total = subtotal;
        //total = subtotal + impuestos;

        // Actualizar UI
        tvSubtotal.setText(String.format(Locale.getDefault(), "S/ %.2f", subtotal));
        //tvImpuestos.setText(String.format(Locale.getDefault(), "S/ %.2f", impuestos));
        tvTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", total));

        //Log.d(TAG, "Totales calculados - Subtotal: S/" + subtotal + ", Impuestos: S/" + impuestos + ", Total: S/" + total);
        Log.d(TAG, "Totales calculados - Subtotal: S/" + subtotal  + ", Total: S/" + total);

    }

    private void mostrarConfirmacionProcesar() {
        String mensaje = "¿Confirmas el procesamiento del checkout?\n\n" +
                "Cliente: " + reserva.getClienteNombre() + "\n" +
                "Habitación: " + reserva.getHabitacionNumero() + "\n" +
                "Total a cobrar: S/ " + String.format(Locale.getDefault(), "%.2f", total);

        if (!cobrosAdicionales.isEmpty()) {
            mensaje += "\n\nCargos adicionales: " + cobrosAdicionales.size();
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Checkout")
                .setMessage(mensaje)
                .setPositiveButton("Sí, procesar", (dialog, which) -> procesarCheckout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarConfirmacionRechazar() {
        new AlertDialog.Builder(this)
                .setTitle("Rechazar Checkout")
                .setMessage("¿Estás seguro de que quieres rechazar esta solicitud de checkout?")
                .setPositiveButton("Sí, rechazar", (dialog, which) -> rechazarCheckout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void procesarCheckout() {
        mostrarLoading(true);
        btnProcesarCheckout.setEnabled(false);
        btnRechazar.setEnabled(false);

        Log.d(TAG, "Iniciando procesamiento de checkout");

        // 1. Actualizar la reserva
        actualizarReserva();
    }

    private void actualizarReserva() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "checkout_listo"); // ✅ CAMBIO: checkout_listo en lugar de completada
        updates.put("montoTotal", total);
        //updates.put("impuestos", impuestos);
        updates.put("fechaCheckoutProcesado", System.currentTimeMillis());

        // Agregar cargos adicionales si los hay
        if (!cobrosAdicionales.isEmpty()) {
            updates.put("cobrosAdicionales", cobrosAdicionales);
        }

        db.collection("reservas")
                .document(reservaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva actualizada exitosamente");
                    // 2. Actualizar la notificación
                    actualizarNotificacion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando reserva", e);
                    mostrarError("Error procesando checkout: " + e.getMessage());
                    restablecerBotones();
                });
    }

    private void actualizarNotificacion() {
        if (notificacionId == null || notificacionId.isEmpty()) {
            Log.w(TAG, "No hay notificación para actualizar");
            finalizarProcesamiento();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("procesada", true);
        updates.put("estadoProcesamiento", "completada");
        updates.put("fechaProcesamiento", System.currentTimeMillis());

        db.collection("notificaciones_admin")
                .document(notificacionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación actualizada exitosamente");
                    // 3. Liberar la habitación
                    liberarHabitacion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando notificación", e);
                    // Continuar aunque falle la notificación
                    liberarHabitacion();
                });
    }

    private void liberarHabitacion() {
        // Actualizar estado de la habitación a "disponible"
        String habitacionId = reserva.getHabitacionId();
        if (habitacionId != null && !habitacionId.isEmpty()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("estado", "disponible");

            db.collection("hoteles")
                    .document(hotelId)
                    .collection("habitaciones")
                    .document(habitacionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Habitación liberada exitosamente");
                        finalizarProcesamiento();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error liberando habitación", e);
                        // Continuar aunque falle
                        finalizarProcesamiento();
                    });
        } else {
            finalizarProcesamiento();
        }
    }

    private void finalizarProcesamiento() {
        mostrarLoading(false);

        Toast.makeText(this, "Checkout procesado exitosamente", Toast.LENGTH_LONG).show();

        // Retornar resultado y cerrar actividad
        setResult(RESULT_OK);
        finish();
    }

    private void rechazarCheckout() {
        mostrarLoading(true);
        btnProcesarCheckout.setEnabled(false);
        btnRechazar.setEnabled(false);

        // Actualizar estado de la reserva a "activa" (volver al estado anterior)
        Map<String, Object> reservaUpdates = new HashMap<>();
        reservaUpdates.put("estado", "activa");

        db.collection("reservas")
                .document(reservaId)
                .update(reservaUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar notificación como rechazada
                    if (notificacionId != null && !notificacionId.isEmpty()) {
                        Map<String, Object> notifUpdates = new HashMap<>();
                        notifUpdates.put("procesada", true);
                        notifUpdates.put("estadoProcesamiento", "rechazada");
                        notifUpdates.put("fechaProcesamiento", System.currentTimeMillis());

                        db.collection("notificaciones_admin")
                                .document(notificacionId)
                                .update(notifUpdates);
                    }

                    mostrarLoading(false);
                    Toast.makeText(this, "Solicitud de checkout rechazada", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rechazando checkout", e);
                    mostrarError("Error rechazando solicitud: " + e.getMessage());
                    restablecerBotones();
                });
    }

    private void restablecerBotones() {
        mostrarLoading(false);
        btnProcesarCheckout.setEnabled(true);
        btnRechazar.setEnabled(true);
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Salir sin procesar?")
                .setMessage("Si sales ahora, la solicitud de checkout quedará pendiente.")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }
}
