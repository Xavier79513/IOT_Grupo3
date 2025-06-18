package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MasOpcionesFragment extends Fragment {

    private LinearLayout layoutTaxista, layoutCheckout, layoutPerfil, layoutCerrarSesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mas_opciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        layoutTaxista = view.findViewById(R.id.layoutTaxista);
        layoutCheckout = view.findViewById(R.id.layoutCheckout);
        layoutPerfil = view.findViewById(R.id.layoutPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);
    }

    private void setupClickListeners() {
        layoutTaxista.setOnClickListener(v -> {
            // TODO: Navegar a TaxistaEstadoFragment
            // navController.navigate(R.id.taxistaEstadoFragment);
        });

        layoutCheckout.setOnClickListener(v -> {
            // TODO: Navegar a CheckoutFragment
            // navController.navigate(R.id.checkoutFragment);
        });

        layoutPerfil.setOnClickListener(v -> {
            // TODO: Navegar a ConfiguracionFragment
            // navController.navigate(R.id.configuracionFragment);
        });

        layoutCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}