package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.adapters.FullscreenPhotoAdapter;

import java.util.List;

/*public class FullscreenPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        setContentView(imageView);

        int resId = getIntent().getIntExtra("photoResId", -1);
        if (resId != -1) {
            imageView.setImageResource(resId);
        }
    }
}*/
public class FullscreenPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_fullscreen_photo);

        List<Integer> photos = (List<Integer>) getIntent().getSerializableExtra("photos");
        int startPosition = getIntent().getIntExtra("position", 0);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        FullscreenPhotoAdapter adapter = new FullscreenPhotoAdapter(photos);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);  // Mostrar imagen tocada

        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish());
    }
}
