package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;

public class CercaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_cerca);
        Button photosButton = findViewById(R.id.photosButton);
        Button nearbyButton = findViewById(R.id.nearbyButton);
        Button reviewButton= findViewById(R.id.reviewButton);

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CercaActivity.this, FotosActivity.class);
                startActivity(intent);
            }
        });

        nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CercaActivity.this, CercaActivity.class);
                startActivity(intent);
            }
        });
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CercaActivity.this, HotelDetailActivity.class);
                startActivity(intent);
            }
        });
        Button bookNowButton = findViewById(R.id.bookNowButton);
        bookNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CercaActivity.this, ReservaActivity.class);
                startActivity(intent);
            }
        });
    }
}