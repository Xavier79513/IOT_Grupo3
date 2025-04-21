package com.example.telehotel.features.cliente;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.telehotel.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/*public class HotelDetailActivity extends AppCompatActivity {

    private TextView txtCheckIn, txtCheckOut, txtGuests;
    private LinearLayout btnCheckIn, btnCheckOut, btnGuests;
    private int numAdultos = 0, numNinos = 0, numHabitaciones = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_detail);

        txtCheckIn = findViewById(R.id.txtCheckIn);
        txtCheckOut = findViewById(R.id.txtCheckOut);
        txtGuests = findViewById(R.id.txtGuests);

        // 🔴 Referenciamos los LinearLayout clicables
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnGuests = findViewById(R.id.btnGuests);

        // 🔴 Asignamos los clickListener a los LinearLayout (no a los TextView)
        btnCheckIn.setOnClickListener(v -> mostrarDateTimePicker(txtCheckIn));
        btnCheckOut.setOnClickListener(v -> mostrarDateTimePicker(txtCheckOut));
        btnGuests.setOnClickListener(v -> mostrarDialogoGuests());
    }

    private void mostrarDateTimePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                String fechaHora = String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d",
                        dayOfMonth, month + 1, year, hourOfDay, minute);
                textView.setText(fechaHora);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void mostrarDialogoGuests() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_guests, null);

        NumberPicker npAdultos = view.findViewById(R.id.npAdultos);
        NumberPicker npNinos = view.findViewById(R.id.npNinos);
        NumberPicker npHabitaciones = view.findViewById(R.id.npHabitaciones);

        npAdultos.setMinValue(0);
        npAdultos.setMaxValue(10);
        npNinos.setMinValue(0);
        npNinos.setMaxValue(10);
        npHabitaciones.setMinValue(0);
        npHabitaciones.setMaxValue(5);

        npAdultos.setValue(numAdultos);
        npNinos.setValue(numNinos);
        npHabitaciones.setValue(numHabitaciones);

        builder.setView(view);
        builder.setTitle("Select Guests")
                .setPositiveButton("OK", (dialog, which) -> {
                    numAdultos = npAdultos.getValue();
                    numNinos = npNinos.getValue();
                    numHabitaciones = npHabitaciones.getValue();
                    txtGuests.setText(numAdultos + " Adults. " + numNinos + " Children. " + numHabitaciones + " room(s)");
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }
}*/
public class HotelDetailActivity extends AppCompatActivity {

    private TextView txtCheckIn, txtCheckOut, txtGuests;
    private Calendar checkInDate, checkOutDate;
    private int adultos = 0, ninos = 0, habitaciones = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_detail);

        txtCheckIn = findViewById(R.id.txtCheckIn);
        txtCheckOut = findViewById(R.id.txtCheckOut);
        txtGuests = findViewById(R.id.txtGuests);

        //findViewById(R.id.btnCheckIn).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.btnCheckIn).setOnClickListener(v -> {
            Toast.makeText(this, "Click en Check-in", Toast.LENGTH_SHORT).show();
            showDatePicker(true);
        });
        findViewById(R.id.btnCheckOut).setOnClickListener(v -> showDatePicker(false));
        findViewById(R.id.btnGuests).setOnClickListener(v -> showGuestPicker());
    }

    private void showDatePicker(boolean isCheckIn) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
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
        datePickerDialog.show();
    }

    private void showGuestPicker() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_guests, null);

        NumberPicker npAdultos = view.findViewById(R.id.npAdultos);
        NumberPicker npNinos = view.findViewById(R.id.npNinos);
        NumberPicker npHabitaciones = view.findViewById(R.id.npHabitaciones);

        npAdultos.setMinValue(0); npAdultos.setMaxValue(10); npAdultos.setValue(adultos);
        npNinos.setMinValue(0); npNinos.setMaxValue(10); npNinos.setValue(ninos);
        npHabitaciones.setMinValue(0); npHabitaciones.setMaxValue(5); npHabitaciones.setValue(habitaciones);

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

