package com.example.telehotel.features.cliente;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Reserva;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class PagoActivity extends AppCompatActivity {

    private static final String TAG = "PagoActivity";
    private final String CHANNEL_ID = "payment_channel";
    private final int NOTIFICATION_ID = 202;

    private EditText etExpiryDate;
    private TextView tvTotalAmount;
    private Button btnProcessPayment;

    // Datos recibidos del Intent
    private String hotelId;
    private String habitacionId;
    private double totalAmount;
    private int totalDays;

    // PrefsManager para obtener datos adicionales
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_pago);

        // Inicializar PrefsManager
        prefsManager = new PrefsManager(this);

        verificarDatosPrefsManager();

        // Obtener datos del Intent
        obtenerDatosIntent();

        // Inicializar vistas
        inicializarVistas();

        // Configurar precio dinámico
        configurarPrecioDinamico();

        // Configurar toolbar
        setupToolbar();

        // Configurar botones
        setupButtons();

        // Crear canal de notificación
        crearCanalNotificacion();

        // Configurar formato de fecha
        configurarFormatoFecha();
    }

    private void obtenerDatosIntent() {
        hotelId = getIntent().getStringExtra("hotelId");
        habitacionId = getIntent().getStringExtra("habitacionId");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        totalDays = getIntent().getIntExtra("totalDays", 1);

        Log.d(TAG, "Datos recibidos - Hotel: " + hotelId + ", Habitación: " + habitacionId + ", Total: " + totalAmount + ", Días: " + totalDays);
    }
    // AGREGA este método después de obtenerDatosIntent() para debug:

    private void verificarDatosPrefsManager() {
        Log.d(TAG, "=== VERIFICANDO DATOS EN PREFSMANAGER ===");
        Log.d(TAG, "StartDate: " + prefsManager.getStartDate());
        Log.d(TAG, "EndDate: " + prefsManager.getEndDate());
        Log.d(TAG, "UserName: " + prefsManager.getUserName());
        Log.d(TAG, "HotelName: " + prefsManager.getHotelName());
        Log.d(TAG, "HotelLocation: " + prefsManager.getHotelLocation());
        Log.d(TAG, "RoomType: " + prefsManager.getRoomType());
        Log.d(TAG, "RoomNumber: " + prefsManager.getRoomNumber());
        Log.d(TAG, "RoomDescription: " + prefsManager.getRoomDescription());
        Log.d(TAG, "RoomPrice: " + prefsManager.getRoomPrice());
        Log.d(TAG, "PeopleString: " + prefsManager.getPeopleString());
    }

    private void inicializarVistas() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnProcessPayment = findViewById(R.id.btnProcessPayment);
        etExpiryDate = findViewById(R.id.etExpiryDate);
    }

    private void configurarPrecioDinamico() {
        // Actualizar precio en la vista principal
        tvTotalAmount.setText(String.format(Locale.getDefault(), "S/ %.2f", totalAmount));

        // Actualizar texto del botón
        btnProcessPayment.setText(String.format(Locale.getDefault(), "Realizar Pago - S/ %.2f", totalAmount));

        Log.d(TAG, "Precio configurado: S/ " + String.format("%.2f", totalAmount));
    }

    private void configurarFormatoFecha() {
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String input = s.toString().replace("/", "");
                if (input.length() >= 2) {
                    String month = input.substring(0, 2);
                    String year = input.length() > 2 ? input.substring(2) : "";
                    String formatted = month + "/" + year;

                    etExpiryDate.setText(formatted);
                    etExpiryDate.setSelection(formatted.length());
                }

                isFormatting = false;
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupButtons() {
        btnProcessPayment.setOnClickListener(v -> {
            if (validarDatosTarjeta()) {
                procesarPago();
            }
        });

        Button cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> {
            mostrarDialogoCancelacion();
        });
    }

    private boolean validarDatosTarjeta() {
        TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
        TextInputEditText etCardHolder = findViewById(R.id.etCardHolder);
        TextInputEditText etExpiryDate = findViewById(R.id.etExpiryDate);
        TextInputEditText etCvv = findViewById(R.id.etCvv);

        // Validar número de tarjeta
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            etCardNumber.setError("Ingresa un número de tarjeta válido");
            etCardNumber.requestFocus();
            return false;
        }

        // Validar nombre del titular
        String cardHolder = etCardHolder.getText().toString().trim();
        if (cardHolder.isEmpty()) {
            etCardHolder.setError("Ingresa el nombre del titular");
            etCardHolder.requestFocus();
            return false;
        }

        // Validar fecha de vencimiento
        String expiryDate = etExpiryDate.getText().toString().trim();
        if (expiryDate.isEmpty() || !expiryDate.matches("\\d{2}/\\d{2}")) {
            etExpiryDate.setError("Formato MM/YY");
            etExpiryDate.requestFocus();
            return false;
        }

        // Validar CVV
        String cvv = etCvv.getText().toString().trim();
        if (cvv.isEmpty() || cvv.length() < 3) {
            etCvv.setError("Ingresa un CVV válido");
            etCvv.requestFocus();
            return false;
        }

        return true;
    }

    private void procesarPago() {
        Log.d(TAG, "=== INICIANDO PROCESO DE PAGO ===");

        // Mostrar loading
        btnProcessPayment.setEnabled(false);
        btnProcessPayment.setText("Procesando...");

        // Simular procesamiento del pago (2 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Guardar la reserva en Firebase
            guardarReservaEnFirebase();
        }, 2000);
    }


    /*private void guardarReservaEnFirebase() {
        Log.d(TAG, "=== GUARDANDO RESERVA EN FIREBASE ===");

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarErrorPago("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        // Obtener datos adicionales del PrefsManager
        String userName = prefsManager.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = "Usuario";
        }

        String hotelName = prefsManager.getHotelName();
        String hotelLocation = prefsManager.getHotelLocation();
        String roomType = prefsManager.getRoomType();
        String roomNumber = prefsManager.getRoomNumber();
        String roomDescription = prefsManager.getRoomDescription();
        double roomPrice = prefsManager.getRoomPrice();
        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();
        String guests = prefsManager.getPeopleString();

        Log.d(TAG, "Datos para la reserva:");
        Log.d(TAG, "- Usuario: " + userName + " (" + userEmail + ")");
        Log.d(TAG, "- Hotel: " + hotelName + " (" + hotelLocation + ")");
        Log.d(TAG, "- Habitación: " + roomNumber + " - " + roomType);
        Log.d(TAG, "- Fechas: " + startDate + " a " + endDate);
        Log.d(TAG, "- Total: S/ " + totalAmount);

        // Crear objeto Reserva
        Reserva reserva = new Reserva();

        // Datos del cliente
        reserva.setClienteId(userId);
        reserva.setClienteNombre(userName);
        reserva.setClienteEmail(userEmail);

        // Datos del hotel
        reserva.setHotelId(hotelId);
        reserva.setHotelNombre(hotelName != null ? hotelName : "Hotel no especificado");
        reserva.setHotelUbicacion(hotelLocation);

        // Datos de la habitación
        reserva.setHabitacionId(habitacionId);
        reserva.setHabitacionNumero(roomNumber != null ? roomNumber : "N/A");
        reserva.setHabitacionTipo(roomType != null ? roomType : "Habitación estándar");
        reserva.setHabitacionDescripcion(roomDescription);
        reserva.setHabitacionPrecio(roomPrice); // Double acepta double automáticamente

        // Datos de la estadía - CORREGIDO
        reserva.setFechaInicio(startDate); // Esto actualiza tanto timestamp como LocalDate
        reserva.setFechaFin(endDate); // Esto actualiza tanto timestamp como LocalDate
        reserva.setTotalDias(totalDays); // Integer acepta int automáticamente
        reserva.setHuespedes(guests != null ? guests : "No especificado");

        // Datos del pago
        reserva.setMontoTotal(totalAmount); // Double acepta double automáticamente
        reserva.setMetodoPago("Tarjeta de crédito");
        reserva.setEstadoPago("pagado");
        reserva.setEstado("activa");

        // Obtener últimos dígitos de la tarjeta para guardar
        TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.length() >= 4) {
            reserva.setTarjetaUltimosDigitos(cardNumber.substring(cardNumber.length() - 4));
        }
        reserva.setTipoTarjeta("VISA"); // Por defecto, podrías detectar el tipo

        // Guardar en Firebase
        FirebaseFirestore.getInstance()
                .collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    String reservaId = documentReference.getId();
                    reserva.setId(reservaId);

                    // Actualizar el documento con el ID
                    documentReference.update("id", reservaId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Reserva guardada exitosamente con ID: " + reservaId);
                                Log.d(TAG, "📋 Código de reserva: " + reserva.getCodigoReserva());

                                // Limpiar datos de búsqueda del PrefsManager
                                prefsManager.clearSearchData();

                                // Mostrar éxito
                                pagoExitoso(reserva);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error actualizando ID de reserva", e);
                                pagoExitoso(reserva); // Continuar aunque falle la actualización del ID
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando reserva: " + e.getMessage(), e);
                    mostrarErrorPago("Error al guardar la reserva: " + e.getMessage());
                });
    }*/
    // REEMPLAZA el método guardarReservaEnFirebase() con este:

    private void guardarReservaEnFirebase() {
        Log.d(TAG, "=== GUARDANDO RESERVA EN FIREBASE ===");

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado");
            mostrarErrorPago("Error: Usuario no autenticado");
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        // Obtener datos adicionales del PrefsManager
        String userName = prefsManager.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = "Usuario";
        }

        String hotelName = prefsManager.getHotelName();
        String hotelLocation = prefsManager.getHotelLocation();
        String roomType = prefsManager.getRoomType();
        String roomNumber = prefsManager.getRoomNumber();
        String roomDescription = prefsManager.getRoomDescription();
        double roomPrice = prefsManager.getRoomPrice();
        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();
        String guests = prefsManager.getPeopleString();

        // VALIDAR FECHAS CRÍTICAS
        if (startDate == 0 || endDate == 0) {
            Log.e(TAG, "❌ FECHAS NO VÁLIDAS - StartDate: " + startDate + ", EndDate: " + endDate);
            mostrarErrorPago("Error: Las fechas de reserva no están disponibles");
            return;
        }

        Log.d(TAG, "=== DATOS PARA LA RESERVA ===");
        Log.d(TAG, "- Usuario: " + userName + " (" + userEmail + ")");
        Log.d(TAG, "- Hotel: " + hotelName + " (" + hotelLocation + ")");
        Log.d(TAG, "- Habitación: " + roomNumber + " - " + roomType);
        Log.d(TAG, "- Descripción habitación: " + roomDescription);
        Log.d(TAG, "- Fechas: " + startDate + " a " + endDate);
        Log.d(TAG, "- Huéspedes: " + guests);
        Log.d(TAG, "- Total: S/ " + totalAmount);

        // Crear objeto Reserva
        Reserva reserva = new Reserva();

        // Datos del cliente
        reserva.setClienteId(userId);
        reserva.setClienteNombre(userName);
        reserva.setClienteEmail(userEmail);

        // Datos del hotel
        reserva.setHotelId(hotelId);
        reserva.setHotelNombre(hotelName != null ? hotelName : "Hotel no especificado");
        reserva.setHotelUbicacion(hotelLocation);

        // Datos de la habitación
        reserva.setHabitacionId(habitacionId);
        reserva.setHabitacionNumero(roomNumber != null ? roomNumber : "N/A");
        reserva.setHabitacionTipo(roomType != null ? roomType : "Habitación estándar");

        // MEJORAR DESCRIPCIÓN
        if (roomDescription != null && !roomDescription.trim().isEmpty()) {
            reserva.setHabitacionDescripcion(roomDescription);
        } else {
            // Generar descripción por defecto
            String descripcionGenerada = "Habitación " + roomType + " con excelentes comodidades";
            reserva.setHabitacionDescripcion(descripcionGenerada);
            Log.d(TAG, "Descripción generada: " + descripcionGenerada);
        }

        reserva.setHabitacionPrecio(roomPrice);

        // DATOS DE LA ESTADÍA - CORREGIDOS
        Log.d(TAG, "=== CONFIGURANDO FECHAS ===");
        Log.d(TAG, "StartDate timestamp: " + startDate);
        Log.d(TAG, "EndDate timestamp: " + endDate);

        reserva.setFechaInicio(startDate);
        reserva.setFechaFin(endDate);
        reserva.setTotalDias(totalDays);

        // MEJORAR HUÉSPEDES
        if (guests != null && !guests.trim().isEmpty() && !guests.equals("No especificado")) {
            reserva.setHuespedes(guests);
        } else {
            reserva.setHuespedes("1 adulto"); // Valor por defecto más realista
            Log.d(TAG, "Huéspedes por defecto asignados: 1 adulto");
        }

        // Datos del pago
        reserva.setMontoTotal(totalAmount);
        reserva.setMetodoPago("Tarjeta de crédito");
        reserva.setEstadoPago("pagado");
        reserva.setEstado("activa");
        // AGREGAR ESTA LÍNEA - Fecha de reserva como timestamp para ordenamiento
        reserva.setFechaReservaTimestamp(System.currentTimeMillis());

        // Obtener últimos dígitos de la tarjeta para guardar
        TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.length() >= 4) {
            reserva.setTarjetaUltimosDigitos(cardNumber.substring(cardNumber.length() - 4));
        }
        reserva.setTipoTarjeta("VISA");

        Log.d(TAG, "=== RESERVA CREADA ===");
        Log.d(TAG, "Código de reserva: " + reserva.getCodigoReserva());
        Log.d(TAG, "Fecha inicio en reserva: " + reserva.getFechaInicio());
        Log.d(TAG, "Fecha fin en reserva: " + reserva.getFechaFin());

        // Guardar en Firebase
        FirebaseFirestore.getInstance()
                .collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    String reservaId = documentReference.getId();
                    reserva.setId(reservaId);

                    // Actualizar el documento con el ID
                    documentReference.update("id", reservaId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Reserva guardada exitosamente con ID: " + reservaId);
                                Log.d(TAG, "📋 Código de reserva: " + reserva.getCodigoReserva());

                                // Limpiar datos de búsqueda del PrefsManager
                                prefsManager.clearSearchData();

                                // Mostrar éxito
                                pagoExitoso(reserva);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error actualizando ID de reserva", e);
                                pagoExitoso(reserva); // Continuar aunque falle la actualización del ID
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando reserva: " + e.getMessage(), e);
                    mostrarErrorPago("Error al guardar la reserva: " + e.getMessage());
                });
    }
    private void pagoExitoso(Reserva reserva) {
        Log.d(TAG, "=== PAGO EXITOSO ===");

        // Restaurar botón
        btnProcessPayment.setEnabled(true);
        btnProcessPayment.setText(String.format(Locale.getDefault(), "Realizar Pago - S/ %.2f", totalAmount));

        // Mostrar notificación
        mostrarNotificacionPagoExitoso();

        // Navegar a pantalla de éxito pasando el código de reserva
        Intent intent = new Intent(PagoActivity.this, PagoExitosoActivity.class);
        intent.putExtra("codigoReserva", reserva.getCodigoReserva());
        intent.putExtra("montoTotal", totalAmount);
        intent.putExtra("hotelNombre", reserva.getHotelNombre());
        intent.putExtra("totalDias", totalDays);
        startActivity(intent);
        finish();
    }

    private void mostrarErrorPago(String mensaje) {
        // Restaurar botón
        btnProcessPayment.setEnabled(true);
        btnProcessPayment.setText(String.format(Locale.getDefault(), "Realizar Pago - S/ %.2f", totalAmount));

        // Mostrar error
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error en el pago: " + mensaje);
    }

    private void mostrarDialogoCancelacion() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar pago")
                .setMessage("¿Estás seguro de que deseas cancelar el pago?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Payment Channel";
            String description = "Canal para notificaciones de pago";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void mostrarNotificacionPagoExitoso() {
        // Verificar permisos de notificación (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Pago Confirmado")
                .setContentText(String.format("¡Tu pago de S/ %.2f fue realizado con éxito!", totalAmount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.borderFocused));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarNotificacionPagoExitoso();
            } else {
                Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}