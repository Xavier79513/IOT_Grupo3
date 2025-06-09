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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarHotelPaso2Fragment extends Fragment {

    private TextInputEditText etNombreAdmin, etCorreoAdmin, etTelefonoAdmin;
    private MaterialButton btnRegistrarAdmin;
    private CircularProgressIndicator progressIndicator;

    private FirebaseFirestore db;
    private String hotelId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registrar_admin, container, false);

        etNombreAdmin = view.findViewById(R.id.etNombreAdmin);
        etCorreoAdmin = view.findViewById(R.id.etCorreoAdmin);
        etTelefonoAdmin = view.findViewById(R.id.etTelefonoAdmin);
        btnRegistrarAdmin = view.findViewById(R.id.btnRegistrarAdmin);
        progressIndicator = view.findViewById(R.id.progressAdmin);

        db = FirebaseFirestore.getInstance();

        // Obtener hotelId del argumento
        if (getArguments() != null) {
            hotelId = getArguments().getString("hotelId");
        }

        btnRegistrarAdmin.setOnClickListener(v -> registrarAdministrador());

        return view;
    }

    private void registrarAdministrador() {
        String nombre = etNombreAdmin.getText().toString().trim();
        String correo = etCorreoAdmin.getText().toString().trim();
        String telefono = etTelefonoAdmin.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo) || TextUtils.isEmpty(telefono)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hotelId == null) {
            Toast.makeText(getContext(), "No se encontró el hotel asociado", Toast.LENGTH_SHORT).show();
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);
        btnRegistrarAdmin.setEnabled(false);

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("nombre", nombre);
        adminData.put("correo", correo);
        adminData.put("telefono", telefono);
        adminData.put("hotelId", hotelId);
        adminData.put("rol", "administrador");

        // Guardar administrador
        db.collection("usuarios")
                .add(adminData)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar el hotel con el ID del administrador
                    DocumentReference hotelRef = db.collection("hoteles").document(hotelId);
                    hotelRef.update("administradorId", documentReference.getId())
                            .addOnSuccessListener(aVoid -> {
                                progressIndicator.setVisibility(View.GONE);
                                btnRegistrarAdmin.setEnabled(true);
                                Toast.makeText(getContext(), "Administrador registrado correctamente", Toast.LENGTH_SHORT).show();

                                // Pasar al siguiente paso: registrar taxistas
                                Bundle args = new Bundle();
                                args.putString("hotelId", hotelId);

                                Fragment paso3Fragment = new RegistrarHotelPaso3Fragment();
                                paso3Fragment.setArguments(args);

                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, paso3Fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            })
                            .addOnFailureListener(e -> {
                                progressIndicator.setVisibility(View.GONE);
                                btnRegistrarAdmin.setEnabled(true);
                                Toast.makeText(getContext(), "Error actualizando hotel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnRegistrarAdmin.setEnabled(true);
                    Toast.makeText(getContext(), "Error registrando administrador: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}