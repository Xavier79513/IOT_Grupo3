/*package com.example.telehotel.features.cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;
import com.example.telehotel.core.utils.PrefsManager;

public class ResumenReservaActivity extends AppCompatActivity {

    // Referencias a vistas
    private TextView tvHotelName, tvHotelLocation;
    private TextView tvNumberRooms, tvRoomType, tvRoomDescription, tvRoomNumber;
    private TextView tvDays, tvTaxes, tvTotalPrice, tvGuests;

    private Button btnConfirmOrder;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_resumen_reserva);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Resumen de Reserva");
        }

        prefsManager = new PrefsManager(this);

        // Inicializar vistas (usa los IDs que definiste en el layout XML)
        tvHotelName = findViewById(R.id.hotelName);
        tvHotelLocation = findViewById(R.id.hotelLocation);
        tvNumberRooms = findViewById(R.id.tvNumberRooms);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvRoomDescription = findViewById(R.id.tvRoomDescription);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvDays = findViewById(R.id.tvDays);
        //tvTaxes = findViewById(R.id.tvTaxes);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvGuests = findViewById(R.id.tvGuests);

        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        cargarDatosResumen();


        btnConfirmOrder.setOnClickListener(v -> {
            // Navegar a la vista de pago
            Intent intent = new Intent(this, PagoActivity.class);



            startActivity(intent);

        });
    }

    private void cargarDatosResumen() {
        // Obtener datos guardados en SharedPreferences
        String hotelName = prefsManager.getHotelName();
        String hotelLocation = prefsManager.getHotelLocation();
        String roomType = prefsManager.getRoomType();
        String roomDescription = prefsManager.getRoomDescription();
        String roomNumber = prefsManager.getRoomNumber();
        int roomQuantity = prefsManager.getRoomQuantity();
        int totalDays = prefsManager.getTotalDays();
        //float taxes = prefsManager.getTotalTaxes();
        float totalPrice = prefsManager.getTotalPrice();
        String guests = prefsManager.getPeopleString();

        // Validar datos mínimos para evitar crash
        if (hotelName == null || hotelName.isEmpty()) hotelName = "Hotel no seleccionado";
        if (hotelLocation == null) hotelLocation = "-";
        if (roomType == null) roomType = "-";
        if (roomDescription == null) roomDescription = "-";
        if (roomNumber == null) roomNumber = "-";
        if (guests == null) guests = "-";

        tvHotelName.setText(hotelName);
        tvHotelLocation.setText(hotelLocation);

        tvNumberRooms.setText(String.valueOf(roomQuantity));
        tvRoomType.setText(roomType);
        tvRoomDescription.setText(roomDescription);
        tvRoomNumber.setText(roomNumber);

        tvDays.setText(totalDays + " noches");

        //tvTaxes.setText(String.format("$%.2f", taxes));
        tvTotalPrice.setText(String.format("$%.2f", totalPrice));
        tvGuests.setText(guests);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}*/
package com.example.telehotel.features.cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ResumenReservaActivity extends AppCompatActivity {

    private static final String TAG = "ResumenReserva";

    // Referencias a vistas - HOTEL
    private TextView tvHotelName, tvHotelLocation;

    // Referencias a vistas - HABITACIÓN
    private TextView tvNumberRooms, tvRoomType, tvRoomDescription, tvRoomNumber;

    // Referencias a vistas - ESTADÍA
    private TextView tvDays, tvGuests;

    // Referencias a vistas - PRECIOS
    private TextView tvTotalPrice;

    // Referencias a vistas - CLIENTE
    private TextView tvNombre, tvEmail, tvTelefono;

    private Button btnConfirmOrder;
    private PrefsManager prefsManager;

    // Datos recibidos
    private String hotelId;
    private String habitacionId;

    // Datos cargados
    private Hotel hotelActual;
    private Habitacion habitacionActual;
    private long fechaInicio;
    private long fechaFin;
    private int totalDias;
    private double precioTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_resumen_reserva);

        // Configurar toolbar
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Resumen de Reserva");
        }

        // Inicializar PrefsManager
        prefsManager = new PrefsManager(this);

        // Obtener parámetros del Intent
        obtenerParametros();

        // Inicializar vistas
        inicializarVistas();

        // Cargar datos
        if (hotelId != null && habitacionId != null) {
            cargarDatosCompletos();
        } else {
            mostrarError("Datos de reserva incompletos");
        }

        // Configurar botón
        configurarBotonConfirmar();
    }

    private void obtenerParametros() {
        hotelId = getIntent().getStringExtra("hotelId");
        habitacionId = getIntent().getStringExtra("habitacionId");

        Log.d(TAG, "Parámetros recibidos - HotelId: " + hotelId + ", HabitacionId: " + habitacionId);
    }

    private void inicializarVistas() {
        // Hotel
        tvHotelName = findViewById(R.id.hotelName);
        tvHotelLocation = findViewById(R.id.hotelLocation);

        // Habitación
        tvNumberRooms = findViewById(R.id.tvNumberRooms);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvRoomDescription = findViewById(R.id.tvRoomDescription);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);

        // Estadía
        tvDays = findViewById(R.id.tvDays);
        tvGuests = findViewById(R.id.tvGuests);

        // Precios
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        // Cliente
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);

        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
    }

    private void configurarBotonConfirmar() {
        btnConfirmOrder.setOnClickListener(v -> {
            // Guardar datos de la reserva en PrefsManager antes de ir al pago
            guardarDatosReserva();

            // Navegar a la vista de pago
            Intent intent = new Intent(this, PagoActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacionId);
            intent.putExtra("totalAmount", precioTotal);
            intent.putExtra("totalDays", totalDias);
            startActivity(intent);
        });
    }

    private void cargarDatosCompletos() {
        // Cargar datos de fechas desde PrefsManager
        cargarFechas();

        // Cargar hotel
        cargarDatosHotel();

        // Cargar información del cliente
        cargarDatosCliente();
    }

    private void cargarFechas() {
        fechaInicio = prefsManager.getStartDate();
        fechaFin = prefsManager.getEndDate();

        if (fechaInicio > 0 && fechaFin > 0) {
            // Calcular días de diferencia
            long diferenciaMilis = fechaFin - fechaInicio;
            totalDias = (int) TimeUnit.MILLISECONDS.toDays(diferenciaMilis);

            // Asegurar que sea al menos 1 día
            if (totalDias <= 0) {
                totalDias = 1;
            }

            Log.d(TAG, "Fechas cargadas - Días: " + totalDias);
        } else {
            Log.e(TAG, "No hay fechas válidas en PrefsManager");
            totalDias = 1; // Valor por defecto
        }
    }

    private void cargarDatosHotel() {
        Log.d(TAG, "Cargando datos del hotel: " + hotelId);

        HotelRepository.getHotelById(hotelId,
                hotel -> {
                    runOnUiThread(() -> {
                        hotelActual = hotel;
                        actualizarDatosHotel(hotel);
                        // Después de cargar el hotel, cargar la habitación
                        cargarDatosHabitacion();
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error cargando hotel: " + error.getMessage());
                        mostrarError("Error cargando información del hotel");
                    });
                }
        );
    }

    private void cargarDatosHabitacion() {
        Log.d(TAG, "Cargando datos de la habitación: " + habitacionId);

        HotelRepository.getHabitacionesByHotelId(hotelId,
                habitaciones -> {
                    runOnUiThread(() -> {
                        // Buscar la habitación específica
                        Habitacion habitacionEncontrada = null;
                        for (Habitacion hab : habitaciones) {
                            if (habitacionId.equals(hab.getId())) {
                                habitacionEncontrada = hab;
                                break;
                            }
                        }

                        if (habitacionEncontrada != null) {
                            habitacionActual = habitacionEncontrada;
                            actualizarDatosHabitacion(habitacionEncontrada);
                            calcularPrecios(habitacionEncontrada);
                        } else {
                            Log.e(TAG, "Habitación no encontrada: " + habitacionId);
                            mostrarError("Habitación no encontrada");
                        }
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error cargando habitación: " + error.getMessage());
                        mostrarError("Error cargando información de la habitación");
                    });
                }
        );
    }

    private void actualizarDatosHotel(Hotel hotel) {
        // Nombre del hotel
        tvHotelName.setText(hotel.getNombre() != null ? hotel.getNombre() : "Hotel sin nombre");

        // Ubicación del hotel
        String ubicacion = "Ubicación no disponible";
        if (hotel.getUbicacion() != null) {
            if (hotel.getUbicacion().getDireccion() != null && !hotel.getUbicacion().getDireccion().isEmpty()) {
                ubicacion = hotel.getUbicacion().getDireccion();
            } else if (hotel.getUbicacion().getCiudad() != null && hotel.getUbicacion().getPais() != null) {
                ubicacion = hotel.getUbicacion().getCiudad() + ", " + hotel.getUbicacion().getPais();
            }
        }
        tvHotelLocation.setText(ubicacion);

        Log.d(TAG, "Datos del hotel actualizados: " + hotel.getNombre());
    }

    private void actualizarDatosHabitacion(Habitacion habitacion) {
        // Número de habitaciones (siempre 1 por ahora)
        tvNumberRooms.setText("1 habitación");

        // Tipo de habitación
        String tipo = habitacion.getTipoFormateado();
        tvRoomType.setText(tipo);

        // Descripción de la habitación
        String descripcion = habitacion.getDescripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            // Generar descripción basada en capacidad y tamaño
            StringBuilder desc = new StringBuilder();
            if (habitacion.getCapacidadAdultos() != null || habitacion.getCapacidadNinos() != null) {
                int adultos = habitacion.getCapacidadAdultos() != null ? habitacion.getCapacidadAdultos() : 0;
                int ninos = habitacion.getCapacidadNinos() != null ? habitacion.getCapacidadNinos() : 0;
                desc.append("Capacidad para ").append(adultos).append(" adultos");
                if (ninos > 0) {
                    desc.append(" y ").append(ninos).append(" niños");
                }
            }
            if (habitacion.getTamaño() != null && habitacion.getTamaño() > 0) {
                if (desc.length() > 0) {
                    desc.append(" - ");
                }
                desc.append(String.format("%.0f m²", habitacion.getTamaño()));
            }
            descripcion = desc.toString();
        }
        tvRoomDescription.setText(descripcion);

        // Número de habitación
        String numero = habitacion.getNumero() != null ? habitacion.getNumero() : "N/A";
        tvRoomNumber.setText(numero);

        // Días de estadía
        tvDays.setText(totalDias + (totalDias == 1 ? " noche" : " noches"));

        // Huéspedes (desde PrefsManager)
        String huespedes = prefsManager.getPeopleString();
        if (huespedes == null || huespedes.isEmpty()) {
            huespedes = "No especificado";
        }
        tvGuests.setText(huespedes);

        Log.d(TAG, "Datos de la habitación actualizados: " + habitacion.getNumero());
    }

    private void calcularPrecios(Habitacion habitacion) {
        double precioPorNoche = habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0;
        precioTotal = precioPorNoche * totalDias;

        // Actualizar precio total
        tvTotalPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", precioTotal));

        Log.d(TAG, String.format("Precios calculados - Precio por noche: S/ %.2f, Total (%d días): S/ %.2f",
                precioPorNoche, totalDias, precioTotal));
    }

    private void cargarDatosCliente() {
        Log.d(TAG, "=== INICIANDO CARGA DE DATOS DEL CLIENTE ===");

        // Verificar si PrefsManager tiene userId
        String userId = prefsManager.getUserId();
        Log.d(TAG, "UserID desde PrefsManager: " + userId);

        // También intentar obtener email directamente del PrefsManager
        String emailPrefs = prefsManager.getUserEmail();
        String namePrefs = prefsManager.getUserName();
        Log.d(TAG, "Email desde PrefsManager: " + emailPrefs);
        Log.d(TAG, "Name desde PrefsManager: " + namePrefs);

        if (userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Buscando usuario en Firebase con ID: " + userId);

            FirebaseUtil.getFirestore()
                    .collection("usuarios")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "Respuesta de Firebase recibida");
                        Log.d(TAG, "Documento existe: " + documentSnapshot.exists());

                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Datos del documento: " + documentSnapshot.getData());

                            try {
                                String nombres = documentSnapshot.getString("nombres");
                                String apellido = documentSnapshot.getString("apellido");
                                String email = documentSnapshot.getString("email");
                                String telefono = documentSnapshot.getString("telefono");

                                Log.d(TAG, "Nombres extraído: " + nombres);
                                Log.d(TAG, "Apellido extraído: " + apellido);
                                Log.d(TAG, "Email extraído: " + email);
                                Log.d(TAG, "Teléfono extraído: " + telefono);

                                String nombreCompleto = "";
                                if (nombres != null && !nombres.isEmpty()) {
                                    nombreCompleto = nombres;
                                    if (apellido != null && !apellido.isEmpty()) {
                                        nombreCompleto += " " + apellido;
                                    }
                                } else {
                                    nombreCompleto = "Usuario sin nombre";
                                }

                                Log.d(TAG, "Nombre completo construido: " + nombreCompleto);

                                // Actualizar UI en el hilo principal
                                String finalNombreCompleto = nombreCompleto;
                                runOnUiThread(() -> {
                                    tvNombre.setText(finalNombreCompleto);
                                    tvEmail.setText(email != null && !email.isEmpty() ? email : "email@example.com");
                                    tvTelefono.setText(telefono != null && !telefono.isEmpty() ? telefono : "+51 999 999 999");

                                    Log.d(TAG, "UI actualizada con datos de Firebase");
                                });

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando datos del usuario", e);
                                runOnUiThread(() -> usarDatosPorDefecto());
                            }
                        } else {
                            Log.w(TAG, "Documento de usuario no existe en Firebase");
                            runOnUiThread(() -> usarDatosPorDefecto());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error consultando Firebase: " + e.getMessage(), e);
                        runOnUiThread(() -> usarDatosPorDefecto());
                    });
        } else {
            Log.w(TAG, "No hay userId válido, usando datos por defecto");
            usarDatosPorDefecto();
        }
    }

    private void usarDatosPorDefecto() {
        Log.d(TAG, "=== USANDO DATOS POR DEFECTO ===");

        // Datos hardcodeados temporalmente para testing
        tvNombre.setText("Valeria Sanchez");
        tvEmail.setText("vale.sr.2002@gmail.com");
        tvTelefono.setText("965120886");

        Log.d(TAG, "Datos por defecto establecidos");
    }

    private void guardarDatosReserva() {
        try {
            // Guardar datos básicos de la reserva en PrefsManager
            if (hotelActual != null) {
                prefsManager.saveHotel(hotelActual.getId(), hotelActual.getNombre(),
                        hotelActual.getUbicacion() != null ? hotelActual.getUbicacion().getCiudad() : "");
            }

            if (habitacionActual != null) {
                prefsManager.saveRoom(
                        habitacionActual.getTipo(),
                        habitacionActual.getDescripcion(),
                        habitacionActual.getNumero(),
                        habitacionActual.getPrecio() != null ? habitacionActual.getPrecio() : 0.0,
                        1 // cantidad de habitaciones
                );
            }

            // Guardar totales
            prefsManager.saveTotals(totalDias, 0.0, precioTotal); // Sin impuestos por ahora

            Log.d(TAG, "Datos de la reserva guardados en PrefsManager");

        } catch (Exception e) {
            Log.e(TAG, "Error guardando datos de la reserva", e);
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Log.e(TAG, mensaje);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
