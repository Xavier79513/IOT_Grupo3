package com.example.telehotel.features.superadmin.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;

public class StatisticsFragment extends Fragment {

    private EditText editTextNombre, editTextCorreo, editTextTelefono, editTextDireccion;
    private Button btnRegistrarAdminHotel;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        editTextNombre = view.findViewById(R.id.editTextNombreAdmin);
        editTextCorreo = view.findViewById(R.id.editTextCorreoAdmin);
        editTextTelefono = view.findViewById(R.id.editTextTelefonoAdmin);
        editTextDireccion = view.findViewById(R.id.editTextDireccionHotel);
        btnRegistrarAdminHotel = view.findViewById(R.id.btnRegistrarAdminHotel);

        btnRegistrarAdminHotel.setOnClickListener(v -> registrarAdministradorHotel());

        return view;
    }

    private void registrarAdministradorHotel() {
        String nombre = editTextNombre.getText().toString().trim();
        String correo = editTextCorreo.getText().toString().trim();
        String telefono = editTextTelefono.getText().toString().trim();
        String direccion = editTextDireccion.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo)
                || TextUtils.isEmpty(telefono) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(getContext(), "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí podrías luego guardar en Firebase, base de datos, etc.
        Toast.makeText(getContext(), "Administrador registrado exitosamente.", Toast.LENGTH_LONG).show();

        // Limpiar los campos
        editTextNombre.setText("");
        editTextCorreo.setText("");
        editTextTelefono.setText("");
        editTextDireccion.setText("");
    }
}
