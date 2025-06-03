package com.example.telehotel.features.cliente.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.EditarPerfilActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView tvNombre, tvDni;
    private EditText etNombre, etDni;
    private ImageButton btnEditName, btnEditDni;
    private MaterialButton btnSaveChanges;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvDni = view.findViewById(R.id.tvDni);
        etNombre = view.findViewById(R.id.etNombre);
        etDni = view.findViewById(R.id.etDni);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnEditDni = view.findViewById(R.id.btnEditDni);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        prefs = requireContext().getSharedPreferences("PerfilPrefs", Context.MODE_PRIVATE);

        // Cargar datos guardados o valores por defecto
        cargarDatos();

        // Configurar botones de edición
        btnEditName.setOnClickListener(v -> {
            tvNombre.setVisibility(View.GONE);
            etNombre.setVisibility(View.VISIBLE);
            etNombre.requestFocus();
            etNombre.setSelection(etNombre.getText().length());
        });

        btnEditDni.setOnClickListener(v -> {
            tvDni.setVisibility(View.GONE);
            etDni.setVisibility(View.VISIBLE);
            etDni.requestFocus();
            etDni.setSelection(etDni.getText().length());
        });

        // Guardar cambios al pulsar botón
        btnSaveChanges.setOnClickListener(v -> {
            String nombreNuevo = etNombre.getText().toString().trim();
            String dniNuevo = etDni.getText().toString().trim();

            if (nombreNuevo.isEmpty() || dniNuevo.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar en SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nombre", nombreNuevo);
            editor.putString("dni", dniNuevo);
            editor.apply();

            // Actualizar vistas
            tvNombre.setText(nombreNuevo);
            tvDni.setText(dniNuevo);

            // Mostrar TextViews y ocultar EditTexts
            tvNombre.setVisibility(View.VISIBLE);
            etNombre.setVisibility(View.GONE);

            tvDni.setVisibility(View.VISIBLE);
            etDni.setVisibility(View.GONE);

            Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatos() {
        String nombre = prefs.getString("nombre", "John Smith");
        String dni = prefs.getString("dni", "71030722");

        tvNombre.setText(nombre);
        etNombre.setText(nombre);

        tvDni.setText(dni);
        etDni.setText(dni);

        // Asegura que los EditText estén ocultos al iniciar
        etNombre.setVisibility(View.GONE);
        etDni.setVisibility(View.GONE);

        tvNombre.setVisibility(View.VISIBLE);
        tvDni.setVisibility(View.VISIBLE);
    }
}
