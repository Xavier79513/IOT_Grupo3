package com.example.telehotel.features.superadmin.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarHotelPaso1Fragment extends Fragment {

    private TextInputEditText etNombre, etTelefono, etDireccion, etDescripcion, etServicios;
    private MaterialButton btnCrear;
    private CircularProgressIndicator progressIndicator;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registrar_hotel, container, false);

        etNombre = view.findViewById(R.id.etNombreHotel);
        etTelefono = view.findViewById(R.id.etTelefonoHotel);
        etDireccion = view.findViewById(R.id.etDireccionHotel);
        etDescripcion = view.findViewById(R.id.etDescripcionHotel);
        etServicios = view.findViewById(R.id.etServiciosHotel);
        btnCrear = view.findViewById(R.id.btnCrearHotel);
        progressIndicator = view.findViewById(R.id.progressHotel);

        db = FirebaseFirestore.getInstance();

        btnCrear.setOnClickListener(v -> crearHotel());

        return view;
    }

    private void crearHotel() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String servicios = etServicios.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(telefono) ||
                TextUtils.isEmpty(direccion) || TextUtils.isEmpty(descripcion) ||
                TextUtils.isEmpty(servicios)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);
        btnCrear.setEnabled(false);

        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("nombre", nombre);
        hotelData.put("telefono", telefono);
        hotelData.put("direccion", direccion);
        hotelData.put("descripcion", descripcion);
        hotelData.put("servicios", servicios); // puedes separar luego con split(",") si necesitas lista

        db.collection("hoteles")
                .add(hotelData)
                .addOnSuccessListener(documentReference -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnCrear.setEnabled(true);
                    Toast.makeText(getContext(), "Hotel registrado", Toast.LENGTH_SHORT).show();

                    // Guardar ID del hotel para el siguiente paso
                    Bundle args = new Bundle();
                    args.putString("hotelId", documentReference.getId());

                    // Pasar al paso 2
                    Fragment paso2Fragment = new RegistrarHotelPaso2Fragment();
                    paso2Fragment.setArguments(args);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, paso2Fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnCrear.setEnabled(true);
                    Toast.makeText(getContext(), "Error al registrar hotel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}