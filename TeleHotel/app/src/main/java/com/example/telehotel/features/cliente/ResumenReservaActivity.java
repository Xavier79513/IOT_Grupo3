package com.example.telehotel.features.cliente;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.telehotel.R;
import com.example.telehotel.core.utils.PrefsManager;

public class ResumenReservaActivity extends AppCompatActivity {

    // Referencias a vistas
    private TextView tvHotelName, tvHotelLocation;
    private TextView tvNumberRooms, tvRoomType, tvRoomDescription, tvRoomNumber;
    private TextView tvDays, tvTaxes, tvTotalPrice, tvGuests;

    private Button btnConfirmOrder;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_resumen_reserva);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Resumen de Reserva");
        }

        prefsManager = new PrefsManager(this);

        // Inicializar vistas (usa los IDs que definiste en el layout XML)
        tvHotelName = findViewById(R.id.hotelName);
        tvHotelLocation = findViewById(R.id.hotelLocation);
        tvNumberRooms = findViewById(R.id.tvNumberRooms);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvRoomDescription = findViewById(R.id.tvRoomDescription);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvDays = findViewById(R.id.tvDays);
        //tvTaxes = findViewById(R.id.tvTaxes);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvGuests = findViewById(R.id.tvGuests);

        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        cargarDatosResumen();

        /*btnConfirmOrder.setOnClickListener(v -> {
            // Aquí el flujo para confirmar reserva o avanzar
            Toast.makeText(this, "Reserva confirmada. Gracias!", Toast.LENGTH_SHORT).show();
            // Puedes ir a otra pantalla o cerrar
            finish();
        });*/
        btnConfirmOrder.setOnClickListener(v -> {
            // Navegar a la vista de pago
            Intent intent = new Intent(this, PagoActivity.class);

            // Opcional: Pasar datos de la reserva si los necesitas
            /*intent.putExtra("total_amount", "S/.406.00");
            intent.putExtra("reservation_id", "12345");
            intent.putExtra("hotel_name", "Bristol Marriot Hotel Royale");*/

            startActivity(intent);

            // Opcional: si quieres cerrar esta activity después de ir al pago
            // finish();
        });
    }

    private void cargarDatosResumen() {
        // Obtener datos guardados en SharedPreferences
        String hotelName = prefsManager.getHotelName();
        String hotelLocation = prefsManager.getHotelLocation();
        String roomType = prefsManager.getRoomType();
        String roomDescription = prefsManager.getRoomDescription();
        String roomNumber = prefsManager.getRoomNumber();
        int roomQuantity = prefsManager.getRoomQuantity();
        int totalDays = prefsManager.getTotalDays();
        //float taxes = prefsManager.getTotalTaxes();
        float totalPrice = prefsManager.getTotalPrice();
        String guests = prefsManager.getPeopleString();

        // Validar datos mínimos para evitar crash
        if (hotelName == null || hotelName.isEmpty()) hotelName = "Hotel no seleccionado";
        if (hotelLocation == null) hotelLocation = "-";
        if (roomType == null) roomType = "-";
        if (roomDescription == null) roomDescription = "-";
        if (roomNumber == null) roomNumber = "-";
        if (guests == null) guests = "-";

        tvHotelName.setText(hotelName);
        tvHotelLocation.setText(hotelLocation);

        tvNumberRooms.setText(String.valueOf(roomQuantity));
        tvRoomType.setText(roomType);
        tvRoomDescription.setText(roomDescription);
        tvRoomNumber.setText(roomNumber);

        tvDays.setText(totalDays + " noches");

        //tvTaxes.setText(String.format("$%.2f", taxes));
        tvTotalPrice.setText(String.format("$%.2f", totalPrice));
        tvGuests.setText(guests);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
