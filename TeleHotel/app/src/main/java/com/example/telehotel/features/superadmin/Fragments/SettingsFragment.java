package com.example.telehotel.features.superadmin.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.features.auth.LoginActivity; // Asegúrate de que sea el nombre correcto de tu login
import com.example.telehotel.R;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            // Aquí puedes borrar datos del usuario si es necesario (ej: SharedPreferences)

            // Redirige al Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Borra el back stack
            startActivity(intent);
        });

        return view;
    }
}