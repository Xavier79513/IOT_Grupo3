package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.features.taxista.adapter.HotelAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxistaHoteles extends AppCompatActivity {

    private RecyclerView recyclerHoteles;
    private HotelAdapter adapter;

    private List<Hotel> listaHoteles = new ArrayList<>();    // Lista filtrada
    private List<Hotel> listaOriginal = new ArrayList<>();   // Lista completa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_activity_hoteles);

        // Configurar Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_hotels);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHoteles.this, item.getItemId());
            return true;
        });

        // Configurar RecyclerView
        recyclerHoteles = findViewById(R.id.recyclerViewHoteles);
        recyclerHoteles.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HotelAdapter(listaHoteles, this);
        recyclerHoteles.setAdapter(adapter);

        // Configurar el buscador
        EditText searchBox = findViewById(R.id.searchBox);

        // Escucha cuando se presiona "Buscar" en el teclado
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Cerrar teclado
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Escucha cambios de texto en tiempo real
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarHotelesPorNombre(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Cargar hoteles desde Firestore
        cargarHotelesDesdeFirestore();
    }

    private void cargarHotelesDesdeFirestore() {
        HotelRepository.getAllHotels(
                hoteles -> {
                    listaOriginal.clear();
                    listaOriginal.addAll(hoteles);

                    listaHoteles.clear();
                    listaHoteles.addAll(hoteles);

                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
    }

    private void filtrarHotelesPorNombre(String texto) {
        listaHoteles.clear();

        if (texto.isEmpty()) {
            listaHoteles.addAll(listaOriginal);
        } else {
            for (Hotel hotel : listaOriginal) {
                if (hotel.getNombre() != null &&
                        hotel.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    listaHoteles.add(hotel);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
