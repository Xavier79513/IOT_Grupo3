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
import android.widget.CheckBox;
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
import com.google.android.material.textfield.TextInputLayout;
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
    private CheckBox cbSaveCard;

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

        cargarDatosTarjetaGuardada();

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
        cbSaveCard = findViewById(R.id.cbSaveCard);
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

        // Primero necesitas tener referencias a los TextInputLayout
        TextInputLayout tilCardNumber = findViewById(R.id.tilCardNumber);
        TextInputLayout tilCardHolder = findViewById(R.id.tilCardHolder);
        TextInputLayout tilExpiryDate = findViewById(R.id.tilExpiryDate);
        TextInputLayout tilCvv = findViewById(R.id.tilCvv);

        // Validar número de tarjeta
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            tilCardNumber.setError("Ingresa un número de tarjeta válido");
            etCardNumber.requestFocus();
            return false;
        } else {
            tilCardNumber.setError(null); // Limpiar error si es válido
        }

        // Validar nombre del titular
        String cardHolder = etCardHolder.getText().toString().trim();
        if (cardHolder.isEmpty()) {
            tilCardHolder.setError("Ingresa el nombre del titular");
            etCardHolder.requestFocus();
            return false;
        } else {
            tilCardHolder.setError(null); // Limpiar error si es válido
        }

        // Validar fecha de vencimiento
        String expiryDate = etExpiryDate.getText().toString().trim();
        if (expiryDate.isEmpty() || !expiryDate.matches("\\d{2}/\\d{2}")) {
            tilExpiryDate.setError("Formato MM/YY");
            etExpiryDate.requestFocus();
            return false;
        } else {
            tilExpiryDate.setError(null); // Limpiar error si es válido
        }

        // Validar CVV
        String cvv = etCvv.getText().toString().trim();
        if (cvv.isEmpty() || cvv.length() < 3) {
            tilCvv.setError("Ingresa un CVV válido");
            etCvv.requestFocus();
            return false;
        } else {
            tilCvv.setError(null); // Limpiar error si es válido
        }

        return true;
    }

    /*private void procesarPago() {
        Log.d(TAG, "=== INICIANDO PROCESO DE PAGO ===");

        // Mostrar loading
        btnProcessPayment.setEnabled(false);
        btnProcessPayment.setText("Procesando...");

        // AGREGAR: Verificar si debe guardar/actualizar la tarjeta
        if (cbSaveCard.isChecked()) {
            guardarDatosTarjeta();
        }

        // Simular procesamiento del pago (2 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Guardar la reserva en Firebase
            guardarReservaEnFirebase();
        }, 2000);
    }*/
    private void procesarPago() {
        Log.d(TAG, "=== INICIANDO PROCESO DE PAGO ===");

        // Mostrar loading
        btnProcessPayment.setEnabled(false);
        btnProcessPayment.setText("Procesando...");

        // ⭐ VERIFICAR DISPONIBILIDAD ANTES DE PROCESAR
        verificarDisponibilidadHabitacion(hotelId, habitacionId,
                () -> {
                    // Habitación disponible - continuar con el pago
                    Log.d(TAG, "Habitación disponible, continuando con el pago");

                    // Verificar si debe guardar/actualizar la tarjeta
                    if (cbSaveCard.isChecked()) {
                        guardarDatosTarjeta();
                    }

                    // Simular procesamiento del pago (2 segundos)
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        guardarReservaEnFirebase();
                    }, 2000);
                },
                () -> {
                    // Habitación no disponible - mostrar error
                    Log.e(TAG, "Habitación ya no está disponible");

                    // Restaurar botón
                    btnProcessPayment.setEnabled(true);
                    btnProcessPayment.setText(String.format(Locale.getDefault(), "Realizar Pago - S/ %.2f", totalAmount));

                    // Mostrar error al usuario
                    new AlertDialog.Builder(this)
                            .setTitle("Habitación no disponible")
                            .setMessage("Lo sentimos, esta habitación ya fue reservada por otro usuario. Por favor, selecciona otra habitación.")
                            .setPositiveButton("Entendido", (dialog, which) -> {
                                // Regresar a la pantalla anterior
                                onBackPressed();
                            })
                            .setCancelable(false)
                            .show();
                }
        );
    }

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
                    // ⭐ ACTUALIZAR ESTADO DE LA HABITACIÓN DESPUÉS DE GUARDAR LA RESERVA
                    actualizarEstadoHabitacion(hotelId, habitacionId, "ocupada");

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
                                // Limpiar datos de búsqueda del PrefsManager
                                prefsManager.clearSearchData();

                                pagoExitoso(reserva); // Continuar aunque falle la actualización del ID
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error guardando reserva: " + e.getMessage(), e);
                    mostrarErrorPago("Error al guardar la reserva: " + e.getMessage());
                });
    }
    /*private void actualizarEstadoHabitacion(String hotelId, String habitacionId, String nuevoEstado) {
        Log.d(TAG, "=== ACTUALIZANDO ESTADO DE HABITACIÓN ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "Habitación ID: " + habitacionId);
        Log.d(TAG, "Nuevo estado: " + nuevoEstado);

        if (hotelId == null || habitacionId == null) {
            Log.e(TAG, "❌ Hotel ID o Habitación ID son nulos");
            return;
        }

        // Crear map con el nuevo estado
        java.util.Map<String, Object> actualizacion = new java.util.HashMap<>();
        actualizacion.put("estado", nuevoEstado);
        actualizacion.put("fechaUltimaActualizacion", System.currentTimeMillis());

        // Actualizar el estado de la habitación en Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Primero obtener el hotel para actualizar la habitación específica
        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(hotelDoc -> {
                    if (hotelDoc.exists()) {
                        // Obtener la lista de habitaciones
                        @SuppressWarnings("unchecked")
                        java.util.List<java.util.Map<String, Object>> habitaciones =
                                (java.util.List<java.util.Map<String, Object>>) hotelDoc.get("habitaciones");

                        if (habitaciones != null) {
                            boolean habitacionEncontrada = false;

                            // Buscar y actualizar la habitación específica
                            for (java.util.Map<String, Object> habitacion : habitaciones) {
                                String id = (String) habitacion.get("id");
                                if (habitacionId.equals(id)) {
                                    habitacion.put("estado", nuevoEstado);
                                    habitacion.put("fechaUltimaActualizacion", System.currentTimeMillis());
                                    habitacionEncontrada = true;
                                    Log.d(TAG, "✅ Habitación encontrada y actualizada en memoria");
                                    break;
                                }
                            }

                            if (habitacionEncontrada) {
                                // Guardar la lista actualizada en Firebase
                                db.collection("hoteles")
                                        .document(hotelId)
                                        .update("habitaciones", habitaciones)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "✅ Estado de habitación actualizado exitosamente");
                                            Log.d(TAG, "🏨 Habitación " + habitacionId + " ahora está: " + nuevoEstado);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "❌ Error actualizando estado de habitación: " + e.getMessage());
                                            // El pago ya se procesó, así que solo logueamos el error
                                        });
                            } else {
                                Log.e(TAG, "❌ No se encontró la habitación con ID: " + habitacionId);
                            }
                        } else {
                            Log.e(TAG, "❌ El hotel no tiene habitaciones definidas");
                        }
                    } else {
                        Log.e(TAG, "❌ No se encontró el hotel con ID: " + hotelId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo hotel para actualizar habitación: " + e.getMessage());
                });
    }*/
    private void actualizarEstadoHabitacion(String hotelId, String habitacionId, String nuevoEstado) {
        Log.d(TAG, "=== ACTUALIZANDO ESTADO DE HABITACIÓN ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "Habitación ID: " + habitacionId);
        Log.d(TAG, "Nuevo estado: " + nuevoEstado);

        if (hotelId == null || habitacionId == null) {
            Log.e(TAG, "❌ Hotel ID o Habitación ID son nulos");
            return;
        }

        // Crear map con el nuevo estado
        java.util.Map<String, Object> actualizacion = new java.util.HashMap<>();
        actualizacion.put("estado", nuevoEstado);
        actualizacion.put("fechaUltimaActualizacion", System.currentTimeMillis());

        // ACTUALIZAR DIRECTAMENTE EN LA COLECCIÓN "habitaciones"
        FirebaseFirestore.getInstance()
                .collection("habitaciones")
                .document(habitacionId.trim())
                .update(actualizacion)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Estado de habitación actualizado exitosamente");
                    Log.d(TAG, "🏨 Habitación " + habitacionId + " ahora está: " + nuevoEstado);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando estado de habitación: " + e.getMessage());

                    // Intentar verificar si la habitación existe
                    FirebaseFirestore.getInstance()
                            .collection("habitaciones")
                            .document(habitacionId.trim())
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Log.e(TAG, "La habitación existe, pero falló la actualización");
                                    Log.e(TAG, "Datos actuales: " + doc.getData());
                                } else {
                                    Log.e(TAG, "La habitación no existe en la colección");
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error verificando existencia de habitación: " + e2.getMessage());
                            });
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
    /// //////////////////////////////////////////////////////////////////////////////// ////////////////////////////////////////////////////////////////////////////////
    private void cargarDatosTarjetaGuardada() {
        Log.d(TAG, "=== CARGANDO DATOS DE TARJETA GUARDADA ===");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No hay usuario autenticado");
            return;
        }

        String userId = currentUser.getUid();

        // Cargar datos desde Firebase
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Intentar obtener los datos de la tarjeta guardada
                        String numeroTarjeta = documentSnapshot.getString("tarjetaNumero");
                        String titular = documentSnapshot.getString("tarjetaTitular");
                        String fechaVencimiento = documentSnapshot.getString("tarjetaVencimiento");
                        String tipoTarjeta = documentSnapshot.getString("tarjetaTipo");

                        if (numeroTarjeta != null && !numeroTarjeta.isEmpty()) {
                            Log.d(TAG, "Datos de tarjeta encontrados, pre-llenando formulario");
                            prellenarFormularioTarjeta(numeroTarjeta, titular, fechaVencimiento, tipoTarjeta);
                        } else {
                            Log.d(TAG, "ℹ️ No hay tarjeta guardada para este usuario");
                        }
                    } else {
                        Log.d(TAG, "Documento de usuario no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando datos de tarjeta: " + e.getMessage());
                    // No mostrar error al usuario, simplemente no pre-llenar
                });
    }

    private void prellenarFormularioTarjeta(String numeroTarjeta, String titular, String fechaVencimiento, String tipoTarjeta) {
        try {
            // Obtener referencias a los campos
            TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
            TextInputEditText etCardHolder = findViewById(R.id.etCardHolder);
            TextInputEditText etExpiryDate = findViewById(R.id.etExpiryDate);
            TextInputEditText etCvv = findViewById(R.id.etCvv);

            // Pre-llenar los campos (excepto CVV)
            if (etCardNumber != null && numeroTarjeta != null) {
                etCardNumber.setText(numeroTarjeta);
                Log.d(TAG, "📱 Número de tarjeta pre-llenado");
            }

            if (etCardHolder != null && titular != null) {
                etCardHolder.setText(titular);
                Log.d(TAG, "👤 Titular pre-llenado: " + titular);
            }

            if (etExpiryDate != null && fechaVencimiento != null) {
                etExpiryDate.setText(fechaVencimiento);
                Log.d(TAG, "📅 Fecha de vencimiento pre-llenada: " + fechaVencimiento);
            }

            // CVV siempre queda vacío por seguridad
            if (etCvv != null) {
                etCvv.setText("");
                etCvv.setHint("CVV");
            }

            // Marcar checkbox como marcado ya que hay datos guardados
            if (cbSaveCard != null) {
                cbSaveCard.setChecked(true);
            }

            // Mostrar mensaje informativo
            Toast.makeText(this, "Datos de tarjeta cargados. Solo ingresa tu CVV", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error pre-llenando formulario: " + e.getMessage());
        }
    }
    // 6. AGREGAR método para guardar los datos de la tarjeta:
    private void guardarDatosTarjeta() {
        Log.d(TAG, "=== GUARDANDO DATOS DE TARJETA ===");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para guardar tarjeta");
            return;
        }

        String userId = currentUser.getUid();

        try {
            // Obtener datos del formulario
            TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
            TextInputEditText etCardHolder = findViewById(R.id.etCardHolder);
            TextInputEditText etExpiryDate = findViewById(R.id.etExpiryDate);

            String numeroTarjeta = etCardNumber.getText().toString().trim();
            String titular = etCardHolder.getText().toString().trim();
            String fechaVencimiento = etExpiryDate.getText().toString().trim();
            String tipoTarjeta = detectarTipoTarjeta(numeroTarjeta);

            Log.d(TAG, "Datos a guardar:");
            Log.d(TAG, "- Titular: " + titular);
            Log.d(TAG, "- Tipo: " + tipoTarjeta);
            Log.d(TAG, "- Vencimiento: " + fechaVencimiento);
            Log.d(TAG, "- Últimos 4 dígitos: ****" +
                    (numeroTarjeta.length() >= 4 ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : ""));

            // Crear map con los datos a guardar (SIN CVV por seguridad)
            java.util.Map<String, Object> datosTarjeta = new java.util.HashMap<>();
            datosTarjeta.put("tarjetaNumero", numeroTarjeta);
            datosTarjeta.put("tarjetaTitular", titular);
            datosTarjeta.put("tarjetaVencimiento", fechaVencimiento);
            datosTarjeta.put("tarjetaTipo", tipoTarjeta);
            datosTarjeta.put("tarjetaUltimosDigitos", numeroTarjeta.length() >= 4 ?
                    numeroTarjeta.substring(numeroTarjeta.length() - 4) : numeroTarjeta);
            datosTarjeta.put("tarjetaFechaActualizacion", System.currentTimeMillis());

            // Guardar en el documento del usuario
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(userId)
                    .update(datosTarjeta)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Datos de tarjeta guardados exitosamente");
                        // No mostrar mensaje para no interrumpir el flujo de pago
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error guardando datos de tarjeta: " + e.getMessage());

                        // Si falla el update, intentar crear el documento
                        FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(userId)
                                .set(datosTarjeta, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "✅ Datos de tarjeta creados exitosamente (fallback)");
                                })
                                .addOnFailureListener(e2 -> {
                                    Log.e(TAG, "❌ Error en fallback guardando tarjeta: " + e2.getMessage());
                                });
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Excepción guardando datos de tarjeta: " + e.getMessage());
        }
    }

    // 7. AGREGAR método auxiliar para detectar tipo de tarjeta:
    private String detectarTipoTarjeta(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return "Desconocida";
        }

        // Eliminar espacios y guiones
        numeroTarjeta = numeroTarjeta.replaceAll("[\\s-]", "");

        if (numeroTarjeta.startsWith("4")) {
            return "VISA";
        } else if (numeroTarjeta.startsWith("5") || numeroTarjeta.startsWith("2")) {
            return "MasterCard";
        } else if (numeroTarjeta.startsWith("34") || numeroTarjeta.startsWith("37")) {
            return "American Express";
        } else if (numeroTarjeta.startsWith("6")) {
            return "Discover";
        } else {
            return "Desconocida";
        }
    }

    // 8. OPCIONAL - Agregar método para limpiar datos guardados:
    private void limpiarDatosTarjetaGuardada() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        java.util.Map<String, Object> datosALimpiar = new java.util.HashMap<>();
        datosALimpiar.put("tarjetaNumero", com.google.firebase.firestore.FieldValue.delete());
        datosALimpiar.put("tarjetaTitular", com.google.firebase.firestore.FieldValue.delete());
        datosALimpiar.put("tarjetaVencimiento", com.google.firebase.firestore.FieldValue.delete());
        datosALimpiar.put("tarjetaTipo", com.google.firebase.firestore.FieldValue.delete());
        datosALimpiar.put("tarjetaUltimosDigitos", com.google.firebase.firestore.FieldValue.delete());

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update(datosALimpiar)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos de tarjeta eliminados");
                    Toast.makeText(this, "Datos de tarjeta eliminados", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando datos de tarjeta: " + e.getMessage());
                });
    }
    // OPCIONAL - Agregar método para liberar habitación (cuando se cancela una reserva)

    private void liberarHabitacion(String hotelId, String habitacionId) {
        Log.d(TAG, "=== LIBERANDO HABITACIÓN ===");
        actualizarEstadoHabitacion(hotelId, habitacionId, "disponible");
    }

// OPCIONAL - Agregar método para verificar disponibilidad antes del pago

    /*private void verificarDisponibilidadHabitacion(String hotelId, String habitacionId, Runnable onDisponible, Runnable onNoDisponible) {
        Log.d(TAG, "=== VERIFICANDO DISPONIBILIDAD DE HABITACIÓN ===");

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(hotelDoc -> {
                    if (hotelDoc.exists()) {
                        @SuppressWarnings("unchecked")
                        java.util.List<java.util.Map<String, Object>> habitaciones =
                                (java.util.List<java.util.Map<String, Object>>) hotelDoc.get("habitaciones");

                        if (habitaciones != null) {
                            for (java.util.Map<String, Object> habitacion : habitaciones) {
                                String id = (String) habitacion.get("id");
                                if (habitacionId.equals(id)) {
                                    String estado = (String) habitacion.get("estado");

                                    if ("disponible".equals(estado)) {
                                        Log.d(TAG, "✅ Habitación disponible");
                                        onDisponible.run();
                                    } else {
                                        Log.d(TAG, "❌ Habitación no disponible. Estado actual: " + estado);
                                        onNoDisponible.run();
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    Log.e(TAG, "❌ No se pudo verificar la disponibilidad");
                    onNoDisponible.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error verificando disponibilidad: " + e.getMessage());
                    onNoDisponible.run();
                });
    }*/
    private void verificarDisponibilidadHabitacion(String hotelId, String habitacionId, Runnable onDisponible, Runnable onNoDisponible) {
        Log.d(TAG, "=== VERIFICANDO DISPONIBILIDAD DE HABITACIÓN ===");
        Log.d(TAG, "HotelId recibido: '" + hotelId + "'");
        Log.d(TAG, "HabitacionId recibido: '" + habitacionId + "'");

        if (hotelId == null || hotelId.trim().isEmpty()) {
            Log.e(TAG, "❌ Hotel ID es nulo o vacío");
            onNoDisponible.run();
            return;
        }

        if (habitacionId == null || habitacionId.trim().isEmpty()) {
            Log.e(TAG, "❌ Habitación ID es nulo o vacío");
            onNoDisponible.run();
            return;
        }

        // BUSCAR DIRECTAMENTE EN LA COLECCIÓN "habitaciones" por el ID del documento
        FirebaseFirestore.getInstance()
                .collection("habitaciones")
                .document(habitacionId.trim())
                .get()
                .addOnSuccessListener(habitacionDoc -> {
                    Log.d(TAG, "Consulta de habitación completada");
                    Log.d(TAG, "Documento de habitación existe: " + habitacionDoc.exists());

                    if (habitacionDoc.exists()) {
                        Log.d(TAG, "✅ Habitación encontrada");

                        // Imprimir todos los datos de la habitación
                        Log.d(TAG, "Datos de la habitación: " + habitacionDoc.getData());

                        // Verificar que pertenece al hotel correcto
                        String hotelIdEnHabitacion = habitacionDoc.getString("hotelId");
                        Log.d(TAG, "Hotel ID en habitación: '" + hotelIdEnHabitacion + "'");

                        if (hotelIdEnHabitacion != null && hotelId.trim().equals(hotelIdEnHabitacion.trim())) {
                            Log.d(TAG, "✅ La habitación pertenece al hotel correcto");

                            String estado = habitacionDoc.getString("estado");
                            Log.d(TAG, "Estado de la habitación: '" + estado + "'");

                            if (estado != null && "disponible".equals(estado.trim())) {
                                Log.d(TAG, "✅ Habitación disponible para reservar");
                                onDisponible.run();
                            } else {
                                Log.d(TAG, "❌ Habitación no disponible. Estado actual: '" + estado + "'");
                                onNoDisponible.run();
                            }
                        } else {
                            Log.e(TAG, "❌ La habitación no pertenece al hotel especificado");
                            Log.e(TAG, "Hotel esperado: '" + hotelId + "'");
                            Log.e(TAG, "Hotel en habitación: '" + hotelIdEnHabitacion + "'");
                            onNoDisponible.run();
                        }
                    } else {
                        Log.e(TAG, "❌ No se encontró la habitación con ID: '" + habitacionId + "'");
                        onNoDisponible.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo habitación: " + e.getMessage());
                    Log.e(TAG, "Habitación ID usado: '" + habitacionId + "'");
                    e.printStackTrace();
                    onNoDisponible.run();
                });
    }
}