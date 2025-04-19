package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;

import java.util.Arrays;
import java.util.List;

public class FotosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_fotos);

        // Botones
        Button photosButton = findViewById(R.id.photosButton);
        Button nearbyButton = findViewById(R.id.nearbyButton);
        Button reviewButton = findViewById(R.id.reviewButton);

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FotosActivity.this, FotosActivity.class);
                startActivity(intent);
            }
        });

        nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FotosActivity.this, CercaActivity.class);
                startActivity(intent);
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FotosActivity.this, BookingActivity.class);
                startActivity(intent);
            }
        });
        Button bookNowButton = findViewById(R.id.bookNowButton);
        bookNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FotosActivity.this, ReservaActivity.class);
                startActivity(intent);
            }
        });

        // RecyclerView (GALERÍA DE FOTOS)
        RecyclerView recyclerView = findViewById(R.id.photoGalleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        List<Integer> photos = Arrays.asList(
                R.drawable.foto1, R.drawable.foto2, R.drawable.foto3,
                R.drawable.foto4, R.drawable.foto5, R.drawable.foto6,
                R.drawable.foto7, R.drawable.foto8, R.drawable.foto9,
                R.drawable.foto10, R.drawable.foto11, R.drawable.foto12
        );

        PhotoAdapter adapter = new PhotoAdapter(this, photos);
        recyclerView.setAdapter(adapter);
    }
}
