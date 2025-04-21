package com.example.telehotel.features.cliente;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class HotelDetailActivity extends AppCompatActivity {

    private TextView txtCheckIn, txtCheckOut, txtGuests;
    private Calendar checkInDate, checkOutDate;
    private int adultos = 0, ninos = 0, habitaciones = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_detail);

        // Referencias
        txtCheckIn = findViewById(R.id.txtCheckIn);
        txtCheckOut = findViewById(R.id.txtCheckOut);
        txtGuests = findViewById(R.id.txtGuests);

        LinearLayout btnCheckIn = findViewById(R.id.btnCheckIn);
        LinearLayout btnCheckOut = findViewById(R.id.btnCheckOut);
        LinearLayout btnGuests = findViewById(R.id.btnGuests);

        btnCheckIn.setOnClickListener(v -> {
            Toast.makeText(this, "Click en Check-in", Toast.LENGTH_SHORT).show();
            showDatePicker(true);
        });

        btnCheckOut.setOnClickListener(v -> showDatePicker(false));
        btnGuests.setOnClickListener(v -> showGuestPicker());
    }

    private void showDatePicker(boolean isCheckIn) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    HotelDetailActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,  // <-- Cambia el estilo
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(selectedDate.getTime());

                        if (isCheckIn) {
                            checkInDate = selectedDate;
                            txtCheckIn.setText(formattedDate);
                        } else {
                            checkOutDate = selectedDate;
                            txtCheckOut.setText(formattedDate);
                        }
                    },
                    year, month, day
            );
            Objects.requireNonNull(datePickerDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // opcional, si usas fondo personalizado
            datePickerDialog.show();

        } catch (Exception e) {
            Toast.makeText(this, "Error al mostrar DatePicker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showGuestPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_guests, null);

        NumberPicker npAdultos = view.findViewById(R.id.npAdultos);
        NumberPicker npNinos = view.findViewById(R.id.npNinos);
        NumberPicker npHabitaciones = view.findViewById(R.id.npHabitaciones);

        npAdultos.setMinValue(0);
        npAdultos.setMaxValue(10);
        npAdultos.setValue(adultos);

        npNinos.setMinValue(0);
        npNinos.setMaxValue(10);
        npNinos.setValue(ninos);

        npHabitaciones.setMinValue(0);
        npHabitaciones.setMaxValue(5);
        npHabitaciones.setValue(habitaciones);

        view.findViewById(R.id.btnGuardar).setOnClickListener(v -> {
            adultos = npAdultos.getValue();
            ninos = npNinos.getValue();
            habitaciones = npHabitaciones.getValue();
            txtGuests.setText(adultos + " Adultos. " + ninos + " Niños. " + habitaciones + " Habitaciones");
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
