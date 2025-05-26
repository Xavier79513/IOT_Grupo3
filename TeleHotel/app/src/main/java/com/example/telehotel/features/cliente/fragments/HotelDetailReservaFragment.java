package com.example.telehotel.features.cliente.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.example.telehotel.core.utils.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HotelDetailReservaFragment extends Fragment {

    private String hotelId;
    private TextView hotelDescription, hotelPhone, hotelLocation;

    private TextView txtCheckIn, txtCheckOut, txtGuests;

    private PrefsManager prefsManager;

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
}