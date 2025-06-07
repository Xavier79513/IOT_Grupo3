package com.example.telehotel.features.cliente;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.telehotel.R;
import com.google.android.material.textfield.TextInputEditText;

/*public class PagoActivity extends AppCompatActivity {

    private final String CHANNEL_ID = "payment_channel";
    private final int NOTIFICATION_ID = 202;

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Payment Channel";
            String description = "Canal para notificaciones de pago";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void mostrarNotificacionPagoExitoso() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle) // Cambia por un ícono válido en tu drawable
                .setContentTitle("Pago Confirmado")
                .setContentText("¡Tu pago fue realizado con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_pago);

        Button finishButton = findViewById(R.id.finishOrderButton);
        finishButton.setOnClickListener(v -> {
            crearCanalNotificacion();
            mostrarNotificacionPagoExitoso();

            Intent intent = new Intent(PagoActivity.this, PagoExitosoActivity.class);
            startActivity(intent);
        });

        LinearLayout backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Regresa a la actividad anterior
            }
        });

    }
}*/
public class PagoActivity extends AppCompatActivity {

    private final String CHANNEL_ID = "payment_channel";
    private final int NOTIFICATION_ID = 202;
    private EditText etExpiryDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_pago);

        // Configurar toolbar
        setupToolbar();

        // Configurar botones
        setupButtons();

        // Crear canal de notificación
        crearCanalNotificacion();
        etExpiryDate = findViewById(R.id.etExpiryDate);

        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String input = s.toString().replace("/", "");
                if (input.length() >= 2) {
                    String month = input.substring(0, 2);
                    String year = input.length() > 2 ? input.substring(2) : "";
                    String formatted = month + "/" + year;

                    etExpiryDate.setText(formatted);
                    etExpiryDate.setSelection(formatted.length());  // Mueve el cursor al final
                }

                isFormatting = false;
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Habilitar botón de navegación
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Manejar clic del botón back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupButtons() {
        // Botón de procesar pago (nuevo ID del layout actualizado)
        Button processPaymentButton = findViewById(R.id.btnProcessPayment);
        processPaymentButton.setOnClickListener(v -> {
            if (validarDatosTarjeta()) {
                procesarPago();
            }
        });

        // Botón de cancelar
        Button cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> {
            mostrarDialogoCancelacion();
        });

        // Si todavía tienes el botón anterior, mantenlo como respaldo
        /*Button finishButton = findViewById(R.id.finishOrderButton);
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> procesarPago());
        }*/
    }

    private boolean validarDatosTarjeta() {
        TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
        TextInputEditText etCardHolder = findViewById(R.id.etCardHolder);
        TextInputEditText etExpiryDate = findViewById(R.id.etExpiryDate);
        TextInputEditText etCvv = findViewById(R.id.etCvv);

        // Validar número de tarjeta
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            etCardNumber.setError("Ingresa un número de tarjeta válido");
            etCardNumber.requestFocus();
            return false;
        }

        // Validar nombre del titular
        String cardHolder = etCardHolder.getText().toString().trim();
        if (cardHolder.isEmpty()) {
            etCardHolder.setError("Ingresa el nombre del titular");
            etCardHolder.requestFocus();
            return false;
        }


        // Validar fecha de vencimiento
        String expiryDate = etExpiryDate.getText().toString().trim();
        if (expiryDate.isEmpty() || !expiryDate.matches("\\d{2}/\\d{2}")) {
            etExpiryDate.setError("Formato MM/YY");
            etExpiryDate.requestFocus();
            return false;
        }

        // Validar CVV
        String cvv = etCvv.getText().toString().trim();
        if (cvv.isEmpty() || cvv.length() < 3) {
            etCvv.setError("Ingresa un CVV válido");
            etCvv.requestFocus();
            return false;
        }

        return true;
    }

    private void procesarPago() {
        // Mostrar loading
        Button processButton = findViewById(R.id.btnProcessPayment);
        processButton.setEnabled(false);
        processButton.setText("Procesando...");

        // Simular procesamiento del pago (2 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Restaurar botón
            processButton.setEnabled(true);
            processButton.setText("Realizar Pago - S/.406.00");

            // Mostrar notificación y navegar
            mostrarNotificacionPagoExitoso();

            Intent intent = new Intent(PagoActivity.this, PagoExitosoActivity.class);
            startActivity(intent);
            finish(); // Cerrar esta activity
        }, 2000);
    }

    private void mostrarDialogoCancelacion() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar pago")
                .setMessage("¿Estás seguro de que deseas cancelar el pago?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Payment Channel";
            String description = "Canal para notificaciones de pago";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void mostrarNotificacionPagoExitoso() {
        // Verificar permisos de notificación (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Pago Confirmado")
                .setContentText("¡Tu pago de S/.406.00 fue realizado con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.borderFocused)); // Color verde

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarNotificacionPagoExitoso();
            } else {
                Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}