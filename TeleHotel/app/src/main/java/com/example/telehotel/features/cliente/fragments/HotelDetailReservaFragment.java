package com.example.telehotel.features.cliente.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.example.telehotel.core.utils.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.features.cliente.HotelHabitacionActivity;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelDetailReservaFragment extends Fragment {

    private static final String TAG = "HotelDetailReserva";

    private String hotelId;
    private Hotel hotelActual;

    // Views principales
    private TextView hotelDescription, hotelPhone, hotelLocation;
    private TextView txtCheckIn, txtCheckOut, txtGuests;
    private LinearLayout facilitiesLayout;

    // Iconos de servicios
    private LinearLayout wifiLayout, piscinaLayout, desayunoLayout, gymLayout;
    private ImageView wifiIcon, piscinaIcon, desayunoIcon, gymIcon;
    private TextView wifiText, piscinaText, desayunoText, gymText;

    private PrefsManager prefsManager;

    private final String CHANNEL_ID = "hotel_booking_channel";
    private final int NOTIFICATION_ID = 101;

    public HotelDetailReservaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cliente_fragment_hotel_detail_reserva, container, false);

        // Inicializar PrefsManager
        prefsManager = new PrefsManager(requireContext());

        // Inicializar vistas
        inicializarVistas(view);

        // Obtener hotelId de la actividad padre
        obtenerHotelId();

        // Configurar listeners
        configurarListeners(view);

        // Crear canal de notificación
        createNotificationChannel();

        // Cargar datos del hotel
        if (hotelId != null && !hotelId.isEmpty()) {
            cargarDatosDelHotel();
        } else {
            Log.e(TAG, "No se recibió hotelId");
            mostrarErrorCarga();
        }

        // Cargar datos guardados de fechas y huéspedes
        cargarDatosReservaGuardados();

        return view;
    }

    private void inicializarVistas(View view) {
        // Views principales
        hotelDescription = view.findViewById(R.id.hotelDescription);
        hotelPhone = view.findViewById(R.id.hotelPhone);
        hotelLocation = view.findViewById(R.id.hotelLocation2);

        // Views de reserva
        txtCheckIn = view.findViewById(R.id.txtCheckIn);
        txtCheckOut = view.findViewById(R.id.txtCheckOut);
        txtGuests = view.findViewById(R.id.txtGuests);

        // Layout de facilidades
        facilitiesLayout = view.findViewById(R.id.facilitiesLayout);


        Log.d(TAG, "Vistas inicializadas correctamente");
    }



    private void obtenerHotelId() {
        // Intentar obtener hotelId desde la actividad padre
        if (getActivity() != null && getActivity().getIntent() != null) {
            hotelId = getActivity().getIntent().getStringExtra("hotelId");
            Log.d(TAG, "HotelId obtenido: " + hotelId);
        }

        // Si no se encuentra, intentar desde argumentos del fragment
        if ((hotelId == null || hotelId.isEmpty()) && getArguments() != null) {
            hotelId = getArguments().getString("hotelId");
            Log.d(TAG, "HotelId desde argumentos: " + hotelId);
        }
    }

    private void configurarListeners(View view) {
        // Botón "Ver habitaciones"
        Button viewRooms = view.findViewById(R.id.viewRooms);
        viewRooms.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HotelHabitacionActivity.class);
            intent.putExtra("hotelId", hotelId);
            startActivity(intent);
        });
    }

    private void cargarDatosDelHotel() {
        Log.d(TAG, "Cargando datos del hotel: " + hotelId);

        HotelRepository.getHotelById(hotelId,
                hotel -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                hotelActual = hotel;
                                // Actualizar datos básicos del hotel
                                actualizarDatosBasicos(hotel);

                                // Cargar servicios desde la colección separada
                                cargarServiciosDelHotel();

                                Log.d(TAG, "Datos básicos del hotel cargados: " + hotel.getNombre());
                            } catch (Exception e) {
                                Log.e(TAG, "Error actualizando interfaz", e);
                                mostrarErrorCarga();
                            }
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Error cargando hotel: " + error.getMessage(), error);
                            mostrarErrorCarga();
                        });
                    }
                }
        );
    }

    /**
     * Carga servicios desde la colección separada
     */
    private void cargarServiciosDelHotel() {
        Log.d(TAG, "Cargando servicios para hotel: " + hotelId);

        HotelRepository.getServiciosByHotelId(hotelId,
                servicios -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Servicios cargados: " + servicios.size());
                            for (String servicio : servicios) {
                                Log.d(TAG, "- " + servicio);
                            }
                            actualizarServicios(servicios);
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Error cargando servicios: " + error.getMessage());
                            // Mostrar mensaje de que no hay servicios
                            actualizarServicios(new ArrayList<>());
                        });
                    }
                }
        );
    }

    /**
     * Actualiza solo los datos básicos del hotel (sin servicios)
     */
    private void actualizarDatosBasicos(Hotel hotel) {
        // Actualizar descripción
        if (hotel.getDescripcion() != null && !hotel.getDescripcion().isEmpty()) {
            hotelDescription.setText(hotel.getDescripcion());
        } else {
            hotelDescription.setText("Un excelente hotel con todas las comodidades para una estadía perfecta.");
        }

        // Actualizar teléfono
        if (hotel.getTelefono() != null && !hotel.getTelefono().isEmpty()) {
            hotelPhone.setText(hotel.getTelefono());
        } else {
            hotelPhone.setText("Teléfono no disponible");
        }

        // Actualizar ubicación
        if (hotel.getUbicacion() != null) {
            String ubicacionTexto = "";

            if (hotel.getUbicacion().getDireccion() != null && !hotel.getUbicacion().getDireccion().isEmpty()) {
                ubicacionTexto = hotel.getUbicacion().getDireccion();
            } else if (hotel.getUbicacion().getCiudad() != null && hotel.getUbicacion().getPais() != null) {
                ubicacionTexto = hotel.getUbicacion().getCiudad() + ", " + hotel.getUbicacion().getPais();
            } else {
                ubicacionTexto = "Ubicación no especificada";
            }

            hotelLocation.setText(ubicacionTexto);
        } else {
            hotelLocation.setText("Ubicación no disponible");
        }
    }
    private void actualizarInterfazConDatos(Hotel hotel) {
        // Actualizar descripción
        if (hotel.getDescripcion() != null && !hotel.getDescripcion().isEmpty()) {
            hotelDescription.setText(hotel.getDescripcion());
        } else {
            hotelDescription.setText("Un excelente hotel con todas las comodidades para una estadía perfecta.");
        }

        // Actualizar teléfono
        if (hotel.getTelefono() != null && !hotel.getTelefono().isEmpty()) {
            hotelPhone.setText(hotel.getTelefono());
        } else {
            hotelPhone.setText("Teléfono no disponible");
        }

        // Actualizar ubicación
        if (hotel.getUbicacion() != null) {
            String ubicacionTexto = "";

            if (hotel.getUbicacion().getDireccion() != null && !hotel.getUbicacion().getDireccion().isEmpty()) {
                ubicacionTexto = hotel.getUbicacion().getDireccion();
            } else if (hotel.getUbicacion().getCiudad() != null && hotel.getUbicacion().getPais() != null) {
                ubicacionTexto = hotel.getUbicacion().getCiudad() + ", " + hotel.getUbicacion().getPais();
            } else {
                ubicacionTexto = "Ubicación no especificada";
            }

            hotelLocation.setText(ubicacionTexto);
        } else {
            hotelLocation.setText("Ubicación no disponible");
        }

        // Actualizar servicios
        actualizarServicios(hotel.getServicios());

        Log.d(TAG, "Interfaz actualizada con datos del hotel");
    }

    private void actualizarServicios(List<String> servicios) {
        Log.d(TAG, "=== ACTUALIZANDO SERVICIOS ===");

        if (servicios == null || servicios.isEmpty()) {
            Log.d(TAG, "No hay servicios para este hotel");
            // Mostrar mensaje de no servicios
            if (facilitiesLayout != null) {
                facilitiesLayout.removeAllViews();
                TextView noServicios = new TextView(getContext());
                noServicios.setText("No hay servicios disponibles");
                noServicios.setTextSize(14);
                noServicios.setTextColor(getResources().getColor(android.R.color.darker_gray));
                noServicios.setPadding(16, 16, 16, 16);
                facilitiesLayout.addView(noServicios);
            }
            return;
        }

        Log.d(TAG, "Hotel tiene " + servicios.size() + " servicios:");

        // Limpiar el layout y agregar servicios
        if (facilitiesLayout != null) {
            facilitiesLayout.removeAllViews();
            facilitiesLayout.setOrientation(LinearLayout.VERTICAL);

            for (String servicio : servicios) {
                if (servicio != null && !servicio.trim().isEmpty()) {
                    TextView servicioView = new TextView(getContext());
                    servicioView.setText("✓ " + servicio.trim());
                    servicioView.setTextSize(14);
                    servicioView.setTextColor(getResources().getColor(android.R.color.black));
                    servicioView.setPadding(16, 8, 16, 8);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 4, 0, 4);
                    servicioView.setLayoutParams(params);

                    facilitiesLayout.addView(servicioView);
                    Log.d(TAG, "Servicio agregado: " + servicio);
                }
            }
        }
    }

    /**
     * Crea una vista simple para mostrar un servicio
     */
    private TextView crearVistaServicio(String servicio) {
        TextView servicioView = new TextView(getContext());

        // Configurar el TextView
        servicioView.setText("✓ " + servicio);
        servicioView.setTextSize(14);
        servicioView.setTextColor(getResources().getColor(android.R.color.black));
        servicioView.setPadding(16, 8, 16, 8);
        servicioView.setBackgroundColor(getResources().getColor(android.R.color.white));

        // Configurar layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 4, 8, 4);
        servicioView.setLayoutParams(params);

        return servicioView;
    }


    private void mostrarErrorCarga() {
        if (hotelDescription != null) {
            hotelDescription.setText("Error al cargar la información del hotel");
        }
        if (hotelPhone != null) {
            hotelPhone.setText("Información no disponible");
        }
        if (hotelLocation != null) {
            hotelLocation.setText("Ubicación no disponible");
        }

        Toast.makeText(getContext(), "Error cargando detalles del hotel", Toast.LENGTH_SHORT).show();
    }

    private void cargarDatosReservaGuardados() {
        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();
        String peopleString = prefsManager.getPeopleString();

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

        if (startDate != 0) {
            String formattedStart = formatter.format(new Date(startDate));
            txtCheckIn.setText(formattedStart);
        }

        if (endDate != 0) {
            String formattedEnd = formatter.format(new Date(endDate));
            txtCheckOut.setText(formattedEnd);
        }

        if (peopleString != null && !peopleString.isEmpty()) {
            txtGuests.setText(peopleString);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking Channel";
            String description = "Canal para notificaciones de reserva";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void mostrarNotificacionReservaExitosa() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Reserva Confirmada")
                .setContentText("¡Reserva realizada con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
