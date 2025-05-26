package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.EditarPerfilActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        return inflater.inflate(R.layout.cliente_fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Acceder al botón que ya existe en el layout
        MaterialButton btnGuardar = view.findViewById(R.id.btnSaveChanges);

        // Agregar listener para redirigir a EditarPerfilActivity
        btnGuardar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
            startActivity(intent);
        });

        // También puedes hacer lo mismo con otros botones si los defines
        MaterialButton btnCompartir = view.findViewById(R.id.btnShareProfile);
        btnCompartir.setOnClickListener(v -> {
            // Acción para compartir perfil
            // Por ejemplo: compartir datos por intent
        });
    }
}
