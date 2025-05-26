package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.model.SolicitudTaxi;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaxiReservationFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView qrCodeImage;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_taxi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout vistaSinReserva = view.findViewById(R.id.vista_sin_reserva);
        LinearLayout vistaSinTaxi = view.findViewById(R.id.vista_sin_taxi);
        NestedScrollView vistaConTaxi = view.findViewById(R.id.vista_con_taxi);

        mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            Bundle mapViewBundle = savedInstanceState != null
                    ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
                    : null;
            mapView.onCreate(mapViewBundle);
            mapView.getMapAsync(this);
        }

        qrCodeImage = view.findViewById(R.id.qr_code);

        Reserva reserva = obtenerReservaActual();

        if (reserva == null || yaHizoCheckout(reserva)) {
            vistaSinReserva.setVisibility(View.VISIBLE);
            vistaSinTaxi.setVisibility(View.GONE);
            vistaConTaxi.setVisibility(View.GONE);
        } else if (reserva.getSolicitudTaxi() == null || !reserva.getSolicitudTaxi().getSolicitado()) {
            vistaSinReserva.setVisibility(View.GONE);
            vistaSinTaxi.setVisibility(View.VISIBLE);
            vistaConTaxi.setVisibility(View.GONE);

            view.findViewById(R.id.btn_agregar_taxi).setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.telehotel.features.cliente.AgregarTaxiActivity.class);
                startActivity(intent);
            });

        } else {
            vistaSinReserva.setVisibility(View.GONE);
            vistaSinTaxi.setVisibility(View.GONE);
            vistaConTaxi.setVisibility(View.VISIBLE);

            SolicitudTaxi taxi = reserva.getSolicitudTaxi();

            // Aquí puedes cargar los datos reales del taxi
            TextView title = view.findViewById(R.id.tv_title);
            if (title != null) {
                title.setText("Taxi reservado para " + reserva.getFechaEntrada());
            }

            generarCodigoQR(taxi.getCodigoQR());
        }

        // Botón para ir a reservas
        view.findViewById(R.id.btn_ir_reservar).setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_taxiFragment_to_hotelesFragment);
        });
    }

    private Reserva obtenerReservaActual() {
        Reserva reserva = new Reserva();
        reserva.id = "reserva123";
        reserva.clienteId = "cliente456";
        reserva.hotelId = "hotel789";
        reserva.habitacionId = "hab101";
        reserva.fechaEntrada = LocalDate.of(2025, 5, 5);
        reserva.fechaSalida = LocalDate.of(2025, 5, 7);
        reserva.montoTotal = 250.0;
        reserva.estado = "CONFIRMADA";
        reserva.fechaReserva = LocalDateTime.now();

        // Simular taxi solicitado
        SolicitudTaxi taxi = new SolicitudTaxi();
        taxi.setSolicitado(true);
        taxi.setCodigoQR("TAXI123456");
        reserva.solicitudTaxi = taxi;

        return reserva;
    }

    private boolean yaHizoCheckout(Reserva reserva) {
        return false; // Simulación
    }

    private void generarCodigoQR(String texto) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(texto, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // Ciclo de vida del MapView
    @Override public void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onStop() { if (mapView != null) mapView.onStop(); super.onStop(); }
    @Override public void onDestroyView() { if (mapView != null) mapView.onDestroy(); super.onDestroyView(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
            if (mapViewBundle == null) {
                mapViewBundle = new Bundle();
                outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
            }
            mapView.onSaveInstanceState(mapViewBundle);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Ejemplo: marcar ubicación
        LatLng ubicacionHotel = new LatLng(-12.0464, -77.0428); // Lima
        googleMap.addMarker(new MarkerOptions().position(ubicacionHotel).title("Tu hotel"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionHotel, 15f));
    }
}
