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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import com.example.telehotel.data.model.ServicioTaxi;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
        ScrollView vistaConTaxi = view.findViewById(R.id.vista_con_taxi);

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
        } else if (reserva.getServicioTaxi() == null) {
            vistaSinReserva.setVisibility(View.GONE);
            vistaSinTaxi.setVisibility(View.VISIBLE);
            vistaConTaxi.setVisibility(View.GONE);

            // Inflar la vista personalizada dentro del contenedor
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View vistaPersonalizada = inflater.inflate(R.layout.cliente_taxi_sin_servicio, vistaSinTaxi, false);
            vistaSinTaxi.removeAllViews();
            vistaSinTaxi.addView(vistaPersonalizada);

            // Configurar botón dentro de la vista inflada
            vistaPersonalizada.findViewById(R.id.btn_agregar_taxi).setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.telehotel.features.cliente.AgregarTaxiActivity.class);
                startActivity(intent);
            });


            // Configurar ícono de logout si lo deseas
            vistaPersonalizada.findViewById(R.id.ivLogout).setOnClickListener(v -> {
                // Acción de logout o navegación
            });
        } else {
            vistaSinReserva.setVisibility(View.GONE);
            vistaSinTaxi.setVisibility(View.GONE);
            vistaConTaxi.setVisibility(View.VISIBLE);

            ServicioTaxi taxi = reserva.getServicioTaxi();
            ((TextView) view.findViewById(R.id.tv_title)).setText("Taxi reservado para " + reserva.getNombreHotel());
            generarCodigoQR(taxi.getQrCodigo());

            // Puedes mostrar más info como el modelo del auto si tienes un TextView con id correspondiente
            // ((TextView) view.findViewById(R.id.tv_modelo)).setText("Modelo: " + taxi.getModelo());
        }

        view.findViewById(R.id.btn_ir_reservar).setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_taxiFragment_to_hotelesFragment);
        });
    }

    private Reserva obtenerReservaActual() {
        Reserva reserva = new Reserva();
        reserva.setNombreHotel("Hotel Royal Inka");
        reserva.setFechaCheckin("2025-05-05");
        reserva.setFechaCheckout("2025-05-07");
        reserva.setServicioTaxi(null); // Simula que NO tiene taxi
        return reserva;
    }

    private boolean yaHizoCheckout(Reserva reserva) {
        return false; // Simula que la reserva está activa
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
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}
