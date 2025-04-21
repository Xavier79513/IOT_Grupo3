package com.example.telehotel.features.taxista;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class TaxistaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_activity);
        setupViewSolicitudesButton();
        setupAceptarButton();
//Navegación
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);  // Aquí debes poner el ID del ítem que estaba seleccionado.

        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaActivity.this, item.getItemId());
            return true;
        });
    }
    private void setupViewSolicitudesButton() {
        Button viewSolicitudes = findViewById(R.id.viewSolicitudes);
        viewSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(TaxistaActivity.this, TaxistaTodasSolicitudes.class);
            startActivity(intent);
        });
    }
    private void setupAceptarButton() {
        Button btnAceptar = findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(v -> {
            showBottomSheetDialog("Luisa Pérez"); // 👈 puedes cambiar el nombre dinámicamente
        });
    }

    private void showBottomSheetDialog(String nombreSolicitante) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.taxista_bottom_sheet_confirmacion, null);
        bottomSheetDialog.setContentView(view);

        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        Button btnAceptar = view.findViewById(R.id.btnAceptarSolicitud);
        Button btnDenegar = view.findViewById(R.id.btnDenegarSolicitud);

        tvMensaje.setText("¿Estás seguro de que quieres aceptar la solicitud de " + nombreSolicitante + "?");

        btnAceptar.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(TaxistaActivity.this, TaxistaViajeAceptadoActivity.class);
            startActivity(intent);
        });

        btnDenegar.setOnClickListener(v -> {
            // Acción al denegar
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

}


