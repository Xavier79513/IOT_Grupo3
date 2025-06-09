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

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarHotelPaso3Fragment extends Fragment {

    private TextInputEditText etNombreTaxista, etPlacaTaxi, etTelefonoTaxista;
    private MaterialButton btnRegistrarTaxista;
    private CircularProgressIndicator progressIndicator;

    private FirebaseFirestore db;
    private String hotelId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registrar_taxistas, container, false);

        etNombreTaxista = view.findViewById(R.id.etNombreTaxista);
        etPlacaTaxi = view.findViewById(R.id.etPlacaTaxi);
        etTelefonoTaxista = view.findViewById(R.id.etTelefonoTaxista);
        btnRegistrarTaxista = view.findViewById(R.id.btnRegistrarTaxista);
        progressIndicator = view.findViewById(R.id.progressTaxista);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            hotelId = getArguments().getString("hotelId");
        }

        btnRegistrarTaxista.setOnClickListener(v -> registrarTaxista());

        return view;
    }

    private void registrarTaxista() {
        String nombre = etNombreTaxista.getText().toString().trim();
        String placa = etPlacaTaxi.getText().toString().trim();
        String telefono = etTelefonoTaxista.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(placa) || TextUtils.isEmpty(telefono)) {
            Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hotelId == null) {
            Toast.makeText(getContext(), "No se encontró el hotel asociado", Toast.LENGTH_SHORT).show();
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);
        btnRegistrarTaxista.setEnabled(false);

        Map<String, Object> taxistaData = new HashMap<>();
        taxistaData.put("nombre", nombre);
        taxistaData.put("placa", placa);
        taxistaData.put("telefono", telefono);
        taxistaData.put("hotelId", hotelId);
        taxistaData.put("rol", "taxista");

        db.collection("usuarios")
                .add(taxistaData)
                .addOnSuccessListener(documentReference -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnRegistrarTaxista.setEnabled(true);
                    Toast.makeText(getContext(), "Taxista registrado correctamente", Toast.LENGTH_SHORT).show();

                    // Limpiar campos para registrar otro taxista si desea
                    etNombreTaxista.setText("");
                    etPlacaTaxi.setText("");
                    etTelefonoTaxista.setText("");
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnRegistrarTaxista.setEnabled(true);
                    Toast.makeText(getContext(), "Error al registrar taxista: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}