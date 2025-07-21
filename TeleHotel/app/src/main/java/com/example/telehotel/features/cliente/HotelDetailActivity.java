package com.example.telehotel.features.cliente;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.fragments.HotelDetailCercaFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailFotosFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailResenaFragment;
import com.example.telehotel.features.cliente.fragments.HotelDetailReservaFragment;
import com.example.telehotel.data.repository.HotelRepository;

public class HotelDetailActivity extends AppCompatActivity {

    private Button bookingButton, reviewButton, photosButton, nearbyButton;
    private Button currentSelectedButton;

    private TextView hotelNameTextView, hotelLocationTextView;
    private ImageView hotelImage;
    private ProgressBar imageLoadingSpinner;
    private FrameLayout loadingOverlay;

    // ✅ AGREGADO: Variable para almacenar el hotelId
    private String hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_detail_base);

        initViews();
        setupToolbar();
        setupButtonListeners();

        // ✅ MODIFICADO: Guardar hotelId como variable de instancia
        hotelId = getIntent().getStringExtra("hotelId");

        if (hotelId != null) {
            cargarDatosDelHotel(hotelId);
        } else {
            ocultarLoadingOverlay();
        }

        // ✅ MODIFICADO: Pasar hotelId al fragmento inicial
        loadFragment(createReservaFragment());
        setSelectedButton(bookingButton);
    }

    private void initViews() {
        bookingButton = findViewById(R.id.bookingButton);
        reviewButton = findViewById(R.id.reviewButton);
        photosButton = findViewById(R.id.photosButton);
        nearbyButton = findViewById(R.id.nearbyButton);

        hotelNameTextView = findViewById(R.id.hotelName);
        hotelLocationTextView = findViewById(R.id.hotelLocation);
        hotelImage = findViewById(R.id.hotelImage);
        imageLoadingSpinner = findViewById(R.id.imageLoadingSpinner);
        loadingOverlay = findViewById(R.id.loadingOverlay);
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
        bookingButton.setOnClickListener(v -> {
            loadFragment(createReservaFragment());
            setSelectedButton(bookingButton);
        });

        reviewButton.setOnClickListener(v -> {
            loadFragment(createResenaFragment());
            setSelectedButton(reviewButton);
        });

        photosButton.setOnClickListener(v -> {
            loadFragment(createFotosFragment());
            setSelectedButton(photosButton);
        });

        nearbyButton.setOnClickListener(v -> {
            // ✅ MODIFICADO: Usar el nuevo método que pasa hotelId
            loadFragment(createCercaFragment());
            setSelectedButton(nearbyButton);
        });
    }

    // ✅ AGREGADO: Métodos factory para crear fragmentos con hotelId
    private Fragment createReservaFragment() {
        // Si HotelDetailReservaFragment necesita hotelId, actualizarlo también
        return new HotelDetailReservaFragment();
    }

    private Fragment createResenaFragment() {
        // Si HotelDetailResenaFragment necesita hotelId, actualizarlo también
        return new HotelDetailResenaFragment();
    }

    private Fragment createFotosFragment() {
        // Si HotelDetailFotosFragment necesita hotelId, actualizarlo también
        return new HotelDetailFotosFragment();
    }

    private Fragment createCercaFragment() {
        // ✅ CLAVE: Usar el factory method con hotelId
        if (hotelId != null) {
            return HotelDetailCercaFragment.newInstance(hotelId);
        } else {
            Log.w("HotelDetailActivity", "hotelId es null, creando fragmento sin datos");
            return new HotelDetailCercaFragment();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void setSelectedButton(Button selectedButton) {
        resetButtonStyles();
        currentSelectedButton = selectedButton;
        selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#1E88E5") // azul primario
        ));
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void resetButtonStyles() {
        Button[] buttons = {bookingButton, reviewButton, photosButton, nearbyButton};
        for (Button btn : buttons) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E0E0E0") // gris claro
            ));
            btn.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void cargarDatosDelHotel(String hotelId) {
        HotelRepository.getHotelById(hotelId, hotel -> {
            // Nombre
            hotelNameTextView.setText(hotel.getNombre());
            hotelNameTextView.animate().alpha(1f).setDuration(300).start();

            // Ciudad
            if (hotel.getUbicacion() != null) {
                hotelLocationTextView.setText(hotel.getUbicacion().getCiudad());
            }
            hotelLocationTextView.animate().alpha(1f).setDuration(300).start();

            // Imagen
            if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                Glide.with(this)
                        .load(hotel.getImagenes().get(0))
                        .into(hotelImage);

                hotelImage.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .withEndAction(this::ocultarLoadingOverlay)
                        .start();
            } else {
                hotelImage.setAlpha(1f);
                ocultarLoadingOverlay();
            }

        }, error -> {
            Log.e("HotelDetail", "Error al cargar hotel", error);
            ocultarLoadingOverlay(); // en caso de error, ocultamos igual
        });
    }

    private void ocultarLoadingOverlay() {
        if (loadingOverlay.getVisibility() == View.VISIBLE) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(300);
            fadeOut.setFillAfter(true);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    loadingOverlay.setVisibility(View.GONE);
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
            loadingOverlay.startAnimation(fadeOut);
        }
        imageLoadingSpinner.setVisibility(View.GONE);
    }

    // ✅ AGREGADO: Método público para obtener el hotelId (útil para fragmentos)
    public String getHotelId() {
        return hotelId;
    }

    // ✅ AGREGADO: Método para actualizar fragmentos si cambia el hotel
    public void updateHotelId(String newHotelId) {
        this.hotelId = newHotelId;

        // Si tienes un fragmento activo que necesita actualización
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HotelDetailCercaFragment) {
            ((HotelDetailCercaFragment) currentFragment).updateHotelId(newHotelId);
        }
        // Agregar más fragmentos aquí si necesitan actualización
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}