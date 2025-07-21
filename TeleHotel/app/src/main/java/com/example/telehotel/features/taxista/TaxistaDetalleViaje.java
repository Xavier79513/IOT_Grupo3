package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.SolicitudRepository;
import com.example.telehotel.data.repository.AeropuertoRepository;  // Asegúrate de importar
import com.example.telehotel.data.repository.HotelRepository;      // Asegúrate de importar
import com.example.telehotel.data.repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxistaDetalleViaje extends AppCompatActivity {
    SolicitudRepository solicitudRepository = new SolicitudRepository();
    private TextView userName, destino, fechaInicio, inicio, recorrido, horaInicio, horaFinal, idViaje;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_viaje_detalle);
        profileImage = findViewById(R.id.profileImage);

        userName = findViewById(R.id.userName);
        destino = findViewById(R.id.destino);
        fechaInicio = findViewById(R.id.fechaInicio);
        inicio = findViewById(R.id.inicio);
        horaInicio = findViewById(R.id.horaInicio);
        horaFinal = findViewById(R.id.horaFinal);
        idViaje = findViewById(R.id.idViaje);

        String solicitudTaxiId = getIntent().getStringExtra("solicitudTaxiId");
        if (solicitudTaxiId != null) {
            Log.d("TaxistaDetalleViaje", "SolicitudTaxi ID recibida: " + solicitudTaxiId);

            solicitudRepository.getById(solicitudTaxiId, new SolicitudRepository.OnViajeLoadedListener() {
                @Override
                public void onViajeLoaded(ServicioTaxi servicioTaxi) {
                    if (servicioTaxi != null) {
                        mostrarDatos(servicioTaxi);
                    } else {
                        Log.e("TaxistaDetalleViaje", "ServicioTaxi no encontrado para ID: " + solicitudTaxiId);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("TaxistaDetalleViaje", "Error al obtener ServicioTaxi: " + errorMessage);
                }
            });
        } else {
            Log.e("TaxistaDetalleViaje", "No se recibió ID de SolicitudTaxi");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_stats);

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaDetalleViaje.this, item.getItemId());
            return true;
        });
    }

    private void mostrarDatos(ServicioTaxi servicioTaxi) {



        if (servicioTaxi.getClienteId() != null && !servicioTaxi.getClienteId().isEmpty()) {
            UserRepository.getUserByUid(servicioTaxi.getClienteId(),
                    usuario -> {
                        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                        userName.setText("Cliente: " + nombreCompleto);

                        // Cargar foto con Glide desde la URL del usuario
                        String fotoUrl = usuario.getFotoUrl(); // o getFotoUrl() si así se llama
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(TaxistaDetalleViaje.this)
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.user1)
                                    .error(R.drawable.user1)
                                    .circleCrop()
                                    .into(profileImage);
                        } else {
                            // Si no hay URL, usar imagen por defecto
                            Glide.with(TaxistaDetalleViaje.this)
                                    .load(R.drawable.user1)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    },
                    error -> {
                        userName.setText("Cliente: (Nombre no disponible)");
                        // Imagen por defecto si falla la carga del usuario
                        Glide.with(TaxistaDetalleViaje.this)
                                .load(R.drawable.user1)
                                .circleCrop()
                                .into(profileImage);
                    }
            );
        } else {
            userName.setText("Cliente: (ID no disponible)");
            Glide.with(this)
                    .load(R.drawable.user1)
                    .circleCrop()
                    .into(profileImage);
        }


        // Obtener nombre del aeropuerto por su ID
        if (servicioTaxi.getAeropuertoId() != null && !servicioTaxi.getAeropuertoId().isEmpty()) {
            AeropuertoRepository.getByAeropuertoId(
                    servicioTaxi.getAeropuertoId(),
                    aeropuerto -> destino.setText(aeropuerto.getNombreAeropuerto()),
                    error -> destino.setText("(Error cargando aeropuerto)")
            );
        } else {
            destino.setText("(ID de aeropuerto no disponible)");
        }

        fechaInicio.setText(servicioTaxi.getFechaInicio() != null ? servicioTaxi.getFechaInicio() : "(No disponible)");

        // Obtener nombre del hotel por su ID
        if (servicioTaxi.getHotelId() != null && !servicioTaxi.getHotelId().isEmpty()) {
            HotelRepository.getHotelByIdPorCampo(
                    servicioTaxi.getHotelId(),
                    hotel -> inicio.setText(hotel.getNombre()),
                    error -> inicio.setText("(Error cargando hotel)")
            );
        } else {
            inicio.setText("(ID de hotel no disponible)");
        }

        horaInicio.setText(servicioTaxi.getHoraInicio() != null ? servicioTaxi.getHoraInicio() : "N/A");
        horaFinal.setText(servicioTaxi.getHoraFin() != null ? servicioTaxi.getHoraFin() : "N/A");
        idViaje.setText(servicioTaxi.getId() != null ? servicioTaxi.getId() : "(No disponible)");
    }
}
