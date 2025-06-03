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
import java.util.Date;
import java.util.Locale;

public class HotelDetailReservaFragment extends Fragment {

    private String hotelId;
    private TextView hotelDescription, hotelPhone, hotelLocation;

    private TextView txtCheckIn, txtCheckOut, txtGuests;

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

        // Referencias a los TextView del layout
        hotelDescription = view.findViewById(R.id.hotelDescription);
        hotelPhone = view.findViewById(R.id.hotelPhone);
        hotelLocation = view.findViewById(R.id.hotelLocation2);

        // Referencias a TextViews para fechas y huéspedes
        txtCheckIn = view.findViewById(R.id.txtCheckIn);
        txtCheckOut = view.findViewById(R.id.txtCheckOut);
        txtGuests = view.findViewById(R.id.txtGuests);

        // Referencia al botón de reserva
        Button viewRooms = view.findViewById(R.id.viewRooms);

        // Crear canal de notificación (solo para Android 8.0+)
        createNotificationChannel();

        // Listener para mostrar notificación al hacer click
        viewRooms.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HotelHabitacionActivity.class);
            // Si quieres enviar datos (como hotelId), usa:
            intent.putExtra("hotelId", hotelId);
            startActivity(intent);
        });


        // Obtener el hotelId desde la actividad
        if (getActivity() != null && getActivity().getIntent() != null) {
            hotelId = getActivity().getIntent().getStringExtra("hotelId");
            Log.d("HotelReserva", "hotelId recibido en fragmento: " + hotelId);

            if (hotelId != null) {
                cargarHotelDesdeRepositorio(hotelId);
            } else {
                Log.e("HotelReserva", "hotelId es null");
            }
        }

        // Cargar datos guardados de fechas y huéspedes
        cargarDatosReservaGuardados();

        return view;
    }

    private void cargarHotelDesdeRepositorio(String hotelId) {
        HotelRepository.getHotelById(hotelId, hotel -> {
            Log.d("HotelReserva", "Hotel cargado: " + hotel.getNombre());
            hotelDescription.setText(hotel.getDescripcion());

            if (hotel.getTelefono() != null) {
                hotelPhone.setText(hotel.getTelefono());
            }

            if (hotel.getUbicacion() != null) {
                hotelLocation.setText(hotel.getUbicacion().getCiudad());
            }
        }, error -> {
            Log.e("HotelReserva", "Error consultando hotel", error);
            Toast.makeText(getContext(), "No se pudo cargar el hotel", Toast.LENGTH_SHORT).show();
        });
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
                .setSmallIcon(R.drawable.ic_check_circle) // Cambia por un ícono válido en tu drawable
                .setContentTitle("Reserva Confirmada")
                .setContentText("¡Reserva realizada con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
