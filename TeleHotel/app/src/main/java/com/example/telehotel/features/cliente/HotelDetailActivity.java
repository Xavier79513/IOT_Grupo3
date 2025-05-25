package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.fragments.HotelDetailCercaFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailFotosFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailResenaFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailReservaFragment;

public class HotelDetailActivity extends AppCompatActivity {

    private Button reviewButton, photosButton, nearbyButton, bookingButton;
    private Button currentSelectedButton;
    private TextView hotelNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_detail_base);

        initViews();

        String hotelId = getIntent().getStringExtra("hotelId");
        Log.d("HotelDetail", "hotelId recibido: " + hotelId);

        if (hotelId != null) {
            FirebaseUtil.getFirestore()
                    .collection("hoteles")
                    .document(hotelId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Hotel hotel = documentSnapshot.toObject(Hotel.class);
                            if (hotel != null) {
                                Log.d("HotelDetail", "Hotel cargado: " + hotel.getNombre());
                                hotelNameTextView.setText(hotel.getNombre());

                                // Cargar fragmento solo cuando los datos estén listos
                                loadFragment(new HotelDetailResenaFragment());
                                setSelectedButton(reviewButton);
                            } else {
                                Log.w("HotelDetail", "Hotel es null");
                            }
                        } else {
                            Log.w("HotelDetail", "Documento no existe en Firestore");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HotelDetail", "Error al consultar Firestore", e);
                        Toast.makeText(this, "Error al cargar datos del hotel", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("HotelDetail", "hotelId es null");
        }

        setupToolbar();
        setupButtonListeners();
    }

    private void initViews() {
        reviewButton = findViewById(R.id.reviewButton);
        photosButton = findViewById(R.id.photosButton);
        nearbyButton = findViewById(R.id.nearbyButton);
        bookingButton = findViewById(R.id.bookingButton);
        hotelNameTextView = findViewById(R.id.hotelName);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupButtonListeners() {
        reviewButton.setOnClickListener(v -> {
            loadFragment(new HotelDetailResenaFragment());
            setSelectedButton(reviewButton);
        });

        photosButton.setOnClickListener(v -> {
            loadFragment(new HotelDetailFotosFragment());
            setSelectedButton(photosButton);
        });

        nearbyButton.setOnClickListener(v -> {
            loadFragment(new HotelDetailCercaFragment());
            setSelectedButton(nearbyButton);
        });

        bookingButton.setOnClickListener(v -> {
            loadFragment(new HotelDetailReservaFragment());
            setSelectedButton(bookingButton);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void setSelectedButton(Button selectedButton) {
        resetButtonStyles();
        currentSelectedButton = selectedButton;
        selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4658B4")));
        selectedButton.setTextColor(Color.WHITE);
    }

    private void resetButtonStyles() {
        Button[] buttons = {reviewButton, photosButton, nearbyButton, bookingButton};
        for (Button button : buttons) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            button.setTextColor(Color.BLACK);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
