package com.example.telehotel.features.auth;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telehotel.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button recoverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.recoverEmail);
        recoverButton = findViewById(R.id.recoverButton);

        recoverButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí puedes integrar Firebase Auth para recuperar contraseña
            Toast.makeText(this, "Instrucciones enviadas a tu correo", Toast.LENGTH_SHORT).show();
        });
    }
}
