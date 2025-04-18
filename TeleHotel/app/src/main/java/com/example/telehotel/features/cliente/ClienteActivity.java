package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;

import android.view.View;

import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    RecyclerView recyclerHoteles;
    List<Hotel> listaHoteles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup); // Tu layout principal
    }
    public void crearCuenta(View view) {
        Intent intent = new Intent(ClienteActivity.this, OtpActivity.class);
        startActivity(intent);
    }
}
