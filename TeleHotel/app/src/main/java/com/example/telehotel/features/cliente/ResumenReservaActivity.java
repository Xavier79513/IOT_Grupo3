package com.example.telehotel.features.cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.data.model.ServicioAdicional;
import com.example.telehotel.data.repository.HotelRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ResumenReservaActivity extends AppCompatActivity {

    private static final String TAG = "ResumenReserva";

    // Referencias a vistas - HOTEL
    private TextView tvHotelName, tvHotelLocation;

    // Referencias a vistas - FECHAS
    private TextView tvCheckInDate, tvCheckOutDate;

    // Referencias a vistas - HABITACIÓN
    private TextView tvNumberRooms, tvRoomType, tvRoomDescription, tvRoomNumber;

    // Referencias a vistas - ESTADÍA
    private TextView tvDays, tvGuests;

    // Referencias a vistas - PRECIOS
    private TextView tvTotalPrice, tvSubtotal, tvServicesTotal;

    // Referencias a vistas - SERVICIOS ADICIONALES
    private LinearLayout servicesContainer;
    private TextView tvServicesTitle;

    // Referencias a vistas - TAXI
    private CheckBox cbTaxiService;
    private TextView tvTaxiDescription;

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
    private List<Servicio> serviciosDisponibles = new ArrayList<>();
    private List<ServicioAdicional> serviciosSeleccionados = new ArrayList<>();
    private long fechaInicio;
    private long fechaFin;
    private int totalDias;
    private double precioHabitacion;
    private double precioServicios = 0.0;
    private double precioTotal;
    private boolean taxiRequested = false;

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

        // Fechas
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);

        // Precios
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvServicesTotal = findViewById(R.id.tvServicesTotal);

        // Servicios adicionales
        servicesContainer = findViewById(R.id.servicesContainer);
        tvServicesTitle = findViewById(R.id.tvServicesTitle);

        // Taxi
        cbTaxiService = findViewById(R.id.cbTaxiService);
        tvTaxiDescription = findViewById(R.id.tvTaxiDescription);

        // Cliente
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);

        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        // Configurar listener del taxi
        if (cbTaxiService != null) {
            cbTaxiService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                taxiRequested = isChecked;
                Log.d(TAG, "Servicio de taxi " + (isChecked ? "solicitado" : "no solicitado"));
            });
        }
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
            intent.putExtra("taxiRequested", taxiRequested);

            // Pasar servicios seleccionados
            ArrayList<ServicioAdicional> serviciosList = new ArrayList<>(serviciosSeleccionados);
            intent.putParcelableArrayListExtra("serviciosSeleccionados", serviciosList);

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

        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());

        if (fechaInicio > 0 && fechaFin > 0) {
            // Calcular días de diferencia
            long diferenciaMilis = fechaFin - fechaInicio;
            totalDias = (int) TimeUnit.MILLISECONDS.toDays(diferenciaMilis);

            // Asegurar que sea al menos 1 día
            if (totalDias <= 0) {
                totalDias = 1;
            }

            // Mostrar fechas en la UI
            String fechaInicioFormateada = formatter.format(new Date(fechaInicio));
            String fechaFinFormateada = formatter.format(new Date(fechaFin));

            tvCheckInDate.setText(fechaInicioFormateada);
            tvCheckOutDate.setText(fechaFinFormateada);

            Log.d(TAG, "Fechas cargadas - Días: " + totalDias);
            Log.d(TAG, "Check-in: " + fechaInicioFormateada);
            Log.d(TAG, "Check-out: " + fechaFinFormateada);
        } else {
            Log.e(TAG, "No hay fechas válidas en PrefsManager");
            totalDias = 1; // Valor por defecto

            tvCheckInDate.setText("Fecha no disponible");
            tvCheckOutDate.setText("Fecha no disponible");
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
                        // Cargar servicios adicionales
                        cargarServiciosAdicionales();
                        // Configurar información del taxi
                        configurarInformacionTaxi();
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

    private void cargarServiciosAdicionales() {
        Log.d(TAG, "Cargando servicios adicionales para hotel: " + hotelId);

        HotelRepository.getServiciosObjetosByHotelId(hotelId,
                servicios -> {
                    runOnUiThread(() -> {
                        serviciosDisponibles = servicios;
                        mostrarServiciosAdicionales(servicios);
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error cargando servicios: " + error.getMessage());
                        ocultarSeccionServicios();
                    });
                }
        );
    }

    private void mostrarServiciosAdicionales(List<Servicio> servicios) {
        if (servicesContainer == null || servicios == null || servicios.isEmpty()) {
            ocultarSeccionServicios();
            return;
        }

        // Filtrar solo servicios con precio (no gratuitos)
        List<Servicio> serviciosPagos = new ArrayList<>();
        for (Servicio servicio : servicios) {
            if (servicio.getPrecio() != null && servicio.getPrecio() > 0) {
                serviciosPagos.add(servicio);
            }
        }

        if (serviciosPagos.isEmpty()) {
            ocultarSeccionServicios();
            return;
        }

        // Mostrar título de servicios
        if (tvServicesTitle != null) {
            tvServicesTitle.setVisibility(View.VISIBLE);
        }

        // Limpiar contenedor
        servicesContainer.removeAllViews();
        servicesContainer.setVisibility(View.VISIBLE);

        // Agregar cada servicio como checkbox
        for (Servicio servicio : serviciosPagos) {
            View servicioView = crearVistaServicio(servicio);
            if (servicioView != null) {
                servicesContainer.addView(servicioView);
            }
        }

        Log.d(TAG, "Servicios adicionales mostrados: " + serviciosPagos.size());
    }

    private View crearVistaServicio(Servicio servicio) {
        // Crear container principal
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.bottomMargin = 8;
        container.setLayoutParams(containerParams);

        // Crear checkbox
        CheckBox checkbox = new CheckBox(this);
        checkbox.setId(View.generateViewId());

        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        checkbox.setLayoutParams(checkboxParams);

        // Crear container de textos
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setPadding(16, 0, 0, 0);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textContainer.setLayoutParams(textParams);

        // Crear TextView para nombre
        TextView nombreText = new TextView(this);
        nombreText.setText(servicio.getNombre());
        nombreText.setTextSize(16);
        nombreText.setTypeface(null, android.graphics.Typeface.BOLD);
        nombreText.setTextColor(getResources().getColor(R.color.dark_text, null));

        // Crear TextView para descripción (si existe)
        TextView descripcionText = new TextView(this);
        if (servicio.getDescripcion() != null && !servicio.getDescripcion().isEmpty()) {
            descripcionText.setText(servicio.getDescripcion());
            descripcionText.setTextSize(14);
            descripcionText.setTextColor(getResources().getColor(R.color.gray_text, null));
            descripcionText.setVisibility(View.VISIBLE);
        } else {
            descripcionText.setVisibility(View.GONE);
        }

        // Crear TextView para precio
        TextView precioText = new TextView(this);
        precioText.setText(String.format(Locale.getDefault(), "S/ %.2f", servicio.getPrecio()));
        precioText.setTextSize(16);
        precioText.setTypeface(null, android.graphics.Typeface.BOLD);
        precioText.setTextColor(getResources().getColor(R.color.colorPrimary, null));

        // Agregar textos al container
        textContainer.addView(nombreText);
        if (descripcionText.getVisibility() == View.VISIBLE) {
            textContainer.addView(descripcionText);
        }

        // Agregar todo al container principal
        container.addView(checkbox);
        container.addView(textContainer);
        container.addView(precioText);

        // Configurar listener del checkbox
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Agregar servicio
                ServicioAdicional servicioAdicional = new ServicioAdicional();
                servicioAdicional.servicioId = servicio.getId();
                servicioAdicional.cantidad = 1;
                servicioAdicional.precio = servicio.getPrecio();
                serviciosSeleccionados.add(servicioAdicional);

                Log.d(TAG, "Servicio agregado: " + servicio.getNombre() + " - S/" + servicio.getPrecio());
            } else {
                // Remover servicio
                serviciosSeleccionados.removeIf(s -> s.servicioId.equals(servicio.getId()));
                Log.d(TAG, "Servicio removido: " + servicio.getNombre());
            }

            // Recalcular precios
            calcularPrecioTotal();
        });

        return container;
    }

    private void configurarInformacionTaxi() {
        if (hotelActual == null || cbTaxiService == null || tvTaxiDescription == null) {
            return;
        }

        Double montoMinimo = hotelActual.getMontoMinimoTaxi();
        if (montoMinimo != null && montoMinimo > 0) {
            String descripcion = String.format(Locale.getDefault(),
                    "Servicio de taxi gratuito al aeropuerto (requiere monto mínimo de reserva: S/ %.2f)",
                    montoMinimo);
            tvTaxiDescription.setText(descripcion);
            cbTaxiService.setVisibility(View.VISIBLE);
            tvTaxiDescription.setVisibility(View.VISIBLE);
        } else {
            cbTaxiService.setVisibility(View.GONE);
            tvTaxiDescription.setVisibility(View.GONE);
        }
    }

    private void ocultarSeccionServicios() {
        if (servicesContainer != null) {
            servicesContainer.setVisibility(View.GONE);
        }
        if (tvServicesTitle != null) {
            tvServicesTitle.setVisibility(View.GONE);
        }
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
        String tipo = habitacion.getTipo() != null ? habitacion.getTipo() : "Habitación";
        tvRoomType.setText(tipo);

        // Descripción de la habitación
        String descripcion = habitacion.getDescripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            // Generar descripción basada en capacidad y tamaño
            StringBuilder desc = new StringBuilder();
            if (habitacion.getCapacidad() != null) {
                Integer adultos = habitacion.getCapacidad().getAdultos();
                Integer ninos = habitacion.getCapacidad().getNinos();

                if (adultos != null) {
                    desc.append("Capacidad para ").append(adultos).append(" adultos");
                    if (ninos != null && ninos > 0) {
                        desc.append(" y ").append(ninos).append(" niños");
                    }
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
        precioHabitacion = (habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0) * totalDias;
        calcularPrecioTotal();
    }

    private void calcularPrecioTotal() {
        // Calcular precio de servicios
        precioServicios = 0.0;
        for (ServicioAdicional servicio : serviciosSeleccionados) {
            precioServicios += (servicio.precio != null ? servicio.precio : 0.0) *
                    (servicio.cantidad != null ? servicio.cantidad : 1);
        }

        // Calcular precio total
        precioTotal = precioHabitacion + precioServicios;

        // Actualizar UI
        if (tvSubtotal != null) {
            tvSubtotal.setText(String.format(Locale.getDefault(), "S/ %.2f", precioHabitacion));
        }

        if (tvServicesTotal != null) {
            tvServicesTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", precioServicios));
            tvServicesTotal.setVisibility(precioServicios > 0 ? View.VISIBLE : View.GONE);
        }

        tvTotalPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", precioTotal));

        Log.d(TAG, String.format("Precios calculados - Habitación: S/ %.2f, Servicios: S/ %.2f, Total: S/ %.2f",
                precioHabitacion, precioServicios, precioTotal));
    }

    private void cargarDatosCliente() {
        Log.d(TAG, "=== INICIANDO CARGA DE DATOS DEL CLIENTE ===");

        // Verificar si PrefsManager tiene userId
        String userId = prefsManager.getUserId();
        Log.d(TAG, "UserID desde PrefsManager: " + userId);

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
                                String apellido = documentSnapshot.getString("apellidos");
                                String email = documentSnapshot.getString("email");
                                String telefono = documentSnapshot.getString("telefono");

                                String nombreCompleto = "";
                                if (nombres != null && !nombres.isEmpty()) {
                                    nombreCompleto = nombres;
                                    if (apellido != null && !apellido.isEmpty()) {
                                        nombreCompleto += " " + apellido;
                                    }
                                } else {
                                    nombreCompleto = "Usuario sin nombre";
                                }

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

            // Guardar servicios seleccionados
            // TODO: Implementar método en PrefsManager para servicios adicionales

            // Guardar estado del taxi
            // TODO: Implementar método en PrefsManager para taxi

            // Guardar totales
            prefsManager.saveTotals(totalDias, precioServicios, precioTotal);

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