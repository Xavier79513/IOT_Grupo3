package com.example.telehotel.features.cliente.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
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
        return inflater.inflate(R.layout.cliente_taxi_fragment, container, false); // reemplaza por el nombre correcto
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Mapa
        mapView = view.findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null
                ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
                : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // QR dinámico
        qrCodeImage = view.findViewById(R.id.qr_code);
        generarCodigoQR("CRN: #854HG23 | Destino: Hotel Royal Inka");
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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        LatLng destino = new LatLng(-13.516, -71.978); // Hotel Royal Inka (Cusco)
        googleMap.addMarker(new MarkerOptions().position(destino).title("Hotel Royal Inka"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15));
    }

    // Ciclo de vida del MapView
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onStop() { mapView.onStop(); super.onStop(); }
    @Override public void onDestroyView() { mapView.onDestroy(); super.onDestroyView(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
