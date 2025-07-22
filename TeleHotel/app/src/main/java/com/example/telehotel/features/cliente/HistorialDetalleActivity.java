/*package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class HistorialDetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_detalle_reserva);

        // Inicializar componentes
        setupUI();
    }

    private void setupUI() {
        // Configurar el botón de regreso
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Regresa a la actividad anterior
            }
        });

        // Configurar el botón "Book again"
        Button btnBookAgain = findViewById(R.id.btnBookAgain);
        btnBookAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes implementar la lógica para reservar nuevamente
                // Por ejemplo, iniciar una nueva actividad de reserva
                Toast.makeText(HistorialDetalleActivity.this, "Iniciando nueva reserva...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void cargarDatosReserva(String reservaId) {
        // Implementa aquí la lógica para cargar los datos de la reserva
        // Esto podría incluir una consulta a una base de datos o una llamada a API

        // Ejemplo:
        // ReservaModel reserva = baseDeDatos.obtenerReserva(reservaId);
        // actualizarUI(reserva);
    }


    private void actualizarUI() {
        // Aquí puedes actualizar todos los TextView con los datos de la reserva
        // Por ejemplo:
        // TextView tvCheckin = findViewById(R.id.tvCheckinDate);
        // tvCheckin.setText(reserva.getFechaCheckin());

        // Y así con todos los demás campos...
    }


    private void hacerNuevaReserva() {
        // Implementa aquí la lógica para iniciar una nueva reserva
        // basada en los datos de la reserva actual
    }
}*/
package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistorialDetalleActivity extends AppCompatActivity {

    private static final String TAG = "HistorialDetalle";

    // === VIEWS INFORMACIÓN GENERAL ===
    private TextView tvCodigoReserva, tvFechaReserva;
    private ImageView ivCodigoQR;

    // === VIEWS INFORMACIÓN DEL HOTEL ===
    private TextView tvHotelNombre, tvHotelUbicacion;

    // === VIEWS INFORMACIÓN DE LA HABITACIÓN ===
    private TextView tvHabitacionNumero, tvHabitacionTipo, tvHabitacionPrecio;

    // === VIEWS DETALLES DE LA ESTADÍA ===
    private TextView tvCheckinDate, tvCheckoutDate, tvTotalDias, tvHuespedes;

    // === VIEWS INFORMACIÓN DEL CLIENTE ===
    private TextView tvClienteNombre, tvClienteEmail, tvClienteTelefono;
    private LinearLayout layoutClienteTelefono;

    // === VIEWS INFORMACIÓN DE PAGO ===
    private TextView tvMetodoPago, tvTipoTarjeta, tvTarjetaUltimosDigitos, tvEstadoPago, tvMontoTotal;

    // === VIEWS SERVICIOS ADICIONALES ===
    private CardView cardServiciosAdicionales;
    private LinearLayout layoutServiciosAdicionales;

    // === VIEWS INFORMACIÓN DEL TAXI ===
    private CardView cardSolicitudTaxi;
    private TextView tvTaxiSolicitado, tvAeropuertoDestino;
    private LinearLayout layoutAeropuertoDestino;

    // === BOTONES ===
    private Button btnBookAgain;
    private ImageButton btnBack;

    // === DATOS ===
    private Reserva reservaActual;
    private String reservaId;
    private String codigoReserva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_detalle_reserva);

        // Inicializar views
        initViews();

        // Obtener datos del Intent
        obtenerDatosIntent();

        // Configurar UI
        setupUI();

        // Cargar datos de la reserva
        if (reservaId != null && !reservaId.isEmpty()) {
            cargarDatosReserva(reservaId);
        } else {
            Toast.makeText(this, "Error: No se encontró la reserva", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        // === BOTONES ===
        btnBack = findViewById(R.id.btnBack);
        //btnBookAgain = findViewById(R.id.btnBookAgain);
        ivCodigoQR = findViewById(R.id.ivCodigoQR);
        // === INFORMACIÓN GENERAL ===
        tvCodigoReserva = findViewById(R.id.tvCodigoReserva);
        tvFechaReserva = findViewById(R.id.tvFechaReserva);

        // === INFORMACIÓN DEL HOTEL ===
        tvHotelNombre = findViewById(R.id.tvHotelNombre);
        tvHotelUbicacion = findViewById(R.id.tvHotelUbicacion);

        // === INFORMACIÓN DE LA HABITACIÓN ===
        tvHabitacionNumero = findViewById(R.id.tvHabitacionNumero);
        tvHabitacionTipo = findViewById(R.id.tvHabitacionTipo);
        tvHabitacionPrecio = findViewById(R.id.tvHabitacionPrecio);

        // === DETALLES DE LA ESTADÍA ===
        tvCheckinDate = findViewById(R.id.tvCheckinDate);
        tvCheckoutDate = findViewById(R.id.tvCheckoutDate);
        tvTotalDias = findViewById(R.id.tvTotalDias);
        tvHuespedes = findViewById(R.id.tvHuespedes);

        // === INFORMACIÓN DEL CLIENTE ===
        tvClienteNombre = findViewById(R.id.tvClienteNombre);
        tvClienteEmail = findViewById(R.id.tvClienteEmail);
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono);
        layoutClienteTelefono = findViewById(R.id.layoutClienteTelefono);

        // === INFORMACIÓN DE PAGO ===
        tvMetodoPago = findViewById(R.id.tvMetodoPago);
        tvTipoTarjeta = findViewById(R.id.tvTipoTarjeta);
        tvTarjetaUltimosDigitos = findViewById(R.id.tvTarjetaUltimosDigitos);
        tvEstadoPago = findViewById(R.id.tvEstadoPago);
        tvMontoTotal = findViewById(R.id.tvMontoTotal);

        // === SERVICIOS ADICIONALES ===
        cardServiciosAdicionales = findViewById(R.id.cardServiciosAdicionales);
        layoutServiciosAdicionales = findViewById(R.id.layoutServiciosAdicionales);

        // === INFORMACIÓN DEL TAXI ===
        cardSolicitudTaxi = findViewById(R.id.cardSolicitudTaxi);
        tvTaxiSolicitado = findViewById(R.id.tvTaxiSolicitado);
        tvAeropuertoDestino = findViewById(R.id.tvAeropuertoDestino);
        layoutAeropuertoDestino = findViewById(R.id.layoutAeropuertoDestino);
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            reservaId = intent.getStringExtra("reservaId");
            codigoReserva = intent.getStringExtra("codigoReserva");

            Log.d(TAG, "Recibido reservaId: " + reservaId);
            Log.d(TAG, "Recibido codigoReserva: " + codigoReserva);
        }
    }

    private void setupUI() {
        // Configurar el botón de regreso
        btnBack.setOnClickListener(v -> onBackPressed());

        /* Configurar el botón "Book again"
        btnBookAgain.setOnClickListener(v -> {
            if (reservaActual != null) {
                hacerNuevaReserva();
            } else {
                Toast.makeText(this, "No se pueden cargar los datos para reservar nuevamente", Toast.LENGTH_SHORT).show();
            }
        }); */
    }

    private void cargarDatosReserva(String reservaId) {
        Log.d(TAG, "Cargando datos de reserva: " + reservaId);

        FirebaseFirestore.getInstance()
                .collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            reservaActual = documentSnapshot.toObject(Reserva.class);
                            if (reservaActual != null) {
                                reservaActual.setId(documentSnapshot.getId());
                                actualizarUI(reservaActual);
                                Log.d(TAG, "Reserva cargada correctamente");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error convirtiendo documento a Reserva", e);
                            mostrarError("Error procesando los datos de la reserva");
                        }
                    } else {
                        Log.e(TAG, "No se encontró la reserva con ID: " + reservaId);
                        mostrarError("No se encontró la reserva");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando reserva", e);
                    mostrarError("Error cargando los datos: " + e.getMessage());
                });
    }

    private void actualizarUI(Reserva reserva) {
        try {
            Log.d(TAG, "Iniciando actualización de UI");

            // === INFORMACIÓN GENERAL DE LA RESERVA ===
            if (tvCodigoReserva != null && reserva.getCodigoReserva() != null) {
                tvCodigoReserva.setText(reserva.getCodigoReserva());
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(
                            reserva.getCodigoReserva(),
                            BarcodeFormat.QR_CODE,
                            400,
                            400
                    );
                    ivCodigoQR.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error generando código QR", e);
                }

            }


            if (tvFechaReserva != null && reserva.getFechaReservaTimestamp() != null) {
                String fechaReserva = formatearFechaSolo(reserva.getFechaReservaTimestamp());
                tvFechaReserva.setText(fechaReserva);
            }

            // === INFORMACIÓN DEL HOTEL ===
            if (tvHotelNombre != null && reserva.getHotelNombre() != null) {
                tvHotelNombre.setText(reserva.getHotelNombre());
            }

            if (tvHotelUbicacion != null && reserva.getHotelUbicacion() != null) {
                tvHotelUbicacion.setText(reserva.getHotelUbicacion());
            }

            // === INFORMACIÓN DE LA HABITACIÓN ===
            if (tvHabitacionNumero != null && reserva.getHabitacionNumero() != null) {
                tvHabitacionNumero.setText(reserva.getHabitacionNumero());
            }

            if (tvHabitacionTipo != null && reserva.getHabitacionTipo() != null) {
                String tipoCapitalizado = capitalizarPrimeraLetra(reserva.getHabitacionTipo());
                tvHabitacionTipo.setText(tipoCapitalizado);
            }

            if (tvHabitacionPrecio != null && reserva.getHabitacionPrecio() != null) {
                tvHabitacionPrecio.setText(String.format(Locale.getDefault(), "S/ %.2f", reserva.getHabitacionPrecio()));
            }

            // === DETALLES DE LA ESTADÍA ===
            if (tvCheckinDate != null && reserva.getFechaInicio() != null) {
                String checkinDate = formatearFecha(reserva.getFechaInicio());
                tvCheckinDate.setText(checkinDate);
            }

            if (tvCheckoutDate != null && reserva.getFechaFin() != null) {
                String checkoutDate = formatearFecha(reserva.getFechaFin());
                tvCheckoutDate.setText(checkoutDate);
            }

            if (tvTotalDias != null && reserva.getTotalDias() != null) {
                int dias = reserva.getTotalDias();
                String diasTexto = dias + (dias == 1 ? " noche" : " noches");
                tvTotalDias.setText(diasTexto);
            }

            if (tvHuespedes != null && reserva.getHuespedes() != null) {
                tvHuespedes.setText(reserva.getHuespedes());
            }

            // === INFORMACIÓN DEL CLIENTE ===
            if (tvClienteNombre != null && reserva.getClienteNombre() != null) {
                tvClienteNombre.setText(reserva.getClienteNombre());
            }

            if (tvClienteEmail != null && reserva.getClienteEmail() != null) {
                tvClienteEmail.setText(reserva.getClienteEmail());
            }

            // Teléfono del cliente (mostrar solo si existe)
            if (reserva.getClienteTelefono() != null && !reserva.getClienteTelefono().trim().isEmpty()) {
                if (tvClienteTelefono != null) {
                    tvClienteTelefono.setText(reserva.getClienteTelefono());
                }
                if (layoutClienteTelefono != null) {
                    layoutClienteTelefono.setVisibility(View.VISIBLE);
                }
            } else {
                if (layoutClienteTelefono != null) {
                    layoutClienteTelefono.setVisibility(View.GONE);
                }
            }

            // === INFORMACIÓN DE PAGO ===
            if (tvMetodoPago != null && reserva.getMetodoPago() != null) {
                tvMetodoPago.setText(reserva.getMetodoPago());
            }

            if (tvTipoTarjeta != null && reserva.getTipoTarjeta() != null) {
                tvTipoTarjeta.setText(reserva.getTipoTarjeta());
            }

            if (tvTarjetaUltimosDigitos != null && reserva.getTarjetaUltimosDigitos() != null) {
                tvTarjetaUltimosDigitos.setText("**** " + reserva.getTarjetaUltimosDigitos());
            }

            if (tvEstadoPago != null && reserva.getEstadoPago() != null) {
                String estadoPago = capitalizarPrimeraLetra(reserva.getEstadoPago());
                tvEstadoPago.setText(estadoPago);

                // Cambiar color según el estado
                if ("pagado".equalsIgnoreCase(reserva.getEstadoPago())) {
                    tvEstadoPago.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else if ("pendiente".equalsIgnoreCase(reserva.getEstadoPago())) {
                    tvEstadoPago.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvEstadoPago.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            if (tvMontoTotal != null && reserva.getMontoTotal() != null) {
                tvMontoTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", reserva.getMontoTotal()));
            }

            // === SERVICIOS ADICIONALES ===
            if (reserva.getServiciosAdicionales() != null && !reserva.getServiciosAdicionales().isEmpty()) {
                if (cardServiciosAdicionales != null) {
                    cardServiciosAdicionales.setVisibility(View.VISIBLE);
                }
                // Aquí puedes agregar lógica para mostrar servicios adicionales específicos
            } else {
                if (cardServiciosAdicionales != null) {
                    cardServiciosAdicionales.setVisibility(View.GONE);
                }
            }

            // === INFORMACIÓN DEL TAXI ===
            configurarInformacionTaxi(reserva);

            Log.d(TAG, "UI actualizada correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando UI", e);
            mostrarError("Error mostrando los datos de la reserva");
        }
    }

    private void configurarInformacionTaxi(Reserva reserva) {
        SolicitudTaxi solicitudTaxi = reserva.getSolicitudTaxi();

        if (solicitudTaxi != null && solicitudTaxi.getSolicitado() != null && solicitudTaxi.getSolicitado()) {
            // Mostrar card de taxi
            if (cardSolicitudTaxi != null) {
                cardSolicitudTaxi.setVisibility(View.VISIBLE);
            }

            if (tvTaxiSolicitado != null) {
                tvTaxiSolicitado.setText("Sí");
                tvTaxiSolicitado.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            // Mostrar aeropuerto destino si existe
            if (solicitudTaxi.getAeropuertoDestino() != null && !solicitudTaxi.getAeropuertoDestino().trim().isEmpty()) {
                if (tvAeropuertoDestino != null) {
                    tvAeropuertoDestino.setText(solicitudTaxi.getAeropuertoDestino());
                }
                if (layoutAeropuertoDestino != null) {
                    layoutAeropuertoDestino.setVisibility(View.VISIBLE);
                }
            } else {
                if (layoutAeropuertoDestino != null) {
                    layoutAeropuertoDestino.setVisibility(View.GONE);
                }
            }

        } else {
            // Ocultar card de taxi si no fue solicitado
            if (cardSolicitudTaxi != null) {
                cardSolicitudTaxi.setVisibility(View.GONE);
            }
        }
    }

    private String formatearFecha(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "Fecha no disponible";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e(TAG, "Error formateando fecha", e);
            return "Fecha inválida";
        }
    }

    private String formatearFechaSolo(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "Fecha no disponible";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e(TAG, "Error formateando fecha", e);
            return "Fecha inválida";
        }
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    private void hacerNuevaReserva() {
        Toast.makeText(this, "Iniciando nueva reserva para " + reservaActual.getHotelNombre(), Toast.LENGTH_SHORT).show();

        // Aquí puedes implementar la navegación a la pantalla de búsqueda o reserva
        // Intent intent = new Intent(this, BusquedaActivity.class);
        // intent.putExtra("hotel_nombre", reservaActual.getHotelNombre());
        // intent.putExtra("ubicacion", reservaActual.getHotelUbicacion());
        // startActivity(intent);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        finish();
    }
}