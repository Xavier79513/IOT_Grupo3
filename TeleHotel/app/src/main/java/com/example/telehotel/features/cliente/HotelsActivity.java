package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HotelsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotels);

        LinearLayout container = findViewById(R.id.linearHotelContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < 5; i++) {
            View hotelView = inflater.inflate(R.layout.cliente_item_hotel, container, false);

            TextView hotelName = hotelView.findViewById(R.id.hotelName);
            hotelName.setText("Hotel #" + (i + 1));

            MaterialButton btn = hotelView.findViewById(R.id.btnBookNow);
            btn.setOnClickListener(v -> {
                Toast.makeText(this, "Clickeaste " + hotelName.getText(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, BookingActivity.class);
                startActivity(intent);
            });

            container.addView(hotelView);
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_rooms);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_rooms) {
                return true;
            } else if (id == R.id.nav_car) {
                startActivity(new Intent(HotelsActivity.this, ReservationAutos.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HotelsActivity.this, PerfilActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_history_reservation) {
                startActivity(new Intent(HotelsActivity.this, HistorialReservaActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(HotelsActivity.this, AjustesActivity.class));
                finish();
                return true;
            }



            return false;
        });

    }
}

