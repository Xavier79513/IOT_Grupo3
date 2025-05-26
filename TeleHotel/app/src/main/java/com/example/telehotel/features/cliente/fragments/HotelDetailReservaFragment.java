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
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;

public class HotelDetailReservaFragment extends Fragment {

    private String hotelId;
    private TextView hotelDescription, hotelPhone, hotelLocation;

    public HotelDetailReservaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cliente_fragment_hotel_detail_reserva, container, false);

        // Referencias a los TextView del layout
        hotelDescription = view.findViewById(R.id.hotelDescription);
        hotelPhone = view.findViewById(R.id.hotelPhone);
        hotelLocation = view.findViewById(R.id.hotelLocation2);

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
}
