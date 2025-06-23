package com.example.telehotel.features.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.features.cliente.adapters.HabitacionAdapter;
import com.example.telehotel.data.repository.HotelRepository;

import java.util.ArrayList;
import java.util.List;

/*public class HotelHabitacionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitacionAdapter adapter;
    private List<Habitacion> listaHabitaciones = new ArrayList<>();

    private String hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_habitacion);

        hotelId = getIntent().getStringExtra("hotelId");
        if (hotelId == null) {
            Toast.makeText(this, "Hotel inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewHabitaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
            // Navegar a la pantalla de resumen de reserva
            Intent intent = new Intent(this, ResumenReservaActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacion.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Cargar habitaciones
        cargarHabitacionesDelHotel();
    }

    private void cargarHabitacionesDelHotel() {
        // Obtener parámetros de búsqueda desde PrefsManager
        PrefsManager prefsManager = new PrefsManager(this);

        // Parsear string de personas para obtener adultos y niños
        String peopleString = prefsManager.getPeopleString();

        // 🔍 DEBUG: Mostrar exactamente lo que hay en PrefsManager
        Log.d("HotelHabitacion", "=== DEBUG COMPLETO ===");
        Log.d("HotelHabitacion", "PeopleString desde PrefsManager: '" + peopleString + "'");

        // Verificar si el string es correcto
        if (peopleString == null) {
            Log.e("HotelHabitacion", "⚠️ PeopleString es NULL!");
        } else if (peopleString.trim().isEmpty()) {
            Log.e("HotelHabitacion", "⚠️ PeopleString está VACÍO!");
        }

        int[] capacidad = parsearCapacidad(peopleString);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        // 🔍 DEBUG: Verificar valores finales
        Log.d("HotelHabitacion", "Valores finales parseados:");
        Log.d("HotelHabitacion", "- Adultos: " + adultos);
        Log.d("HotelHabitacion", "- Niños: " + ninos);

        // 🔍 VERIFICACIÓN: ¿Debería la habitación 300 estar incluida?
        boolean habitacion300DeberiaEstar = (2 >= adultos) && (0 >= ninos);
        Log.d("HotelHabitacion", "¿Habitación 300 [2 adultos, 0 niños] debería estar con criterio [" +
                adultos + "+, " + ninos + "+]? " + (habitacion300DeberiaEstar ? "SÍ" : "NO"));

        if (habitacion300DeberiaEstar && ninos > 0) {
            Log.e("HotelHabitacion", "🚨 ERROR: Habitación 300 va a aparecer incorrectamente!");
        }

        Log.d("HotelHabitacion", "Hotel ID: " + hotelId);
        Log.d("HotelHabitacion", "Buscando habitaciones para: " + adultos + "+ adultos, " + ninos + "+ niños");

        HotelRepository.getHabitacionesByHotelIdAndCapacity(hotelId, adultos, ninos,
                habitaciones -> {
                    runOnUiThread(() -> {
                        // 🔍 DEBUG: Verificar habitaciones recibidas
                        Log.d("HotelHabitacion", "=== HABITACIONES RECIBIDAS ===");
                        Log.d("HotelHabitacion", "Total: " + habitaciones.size());

                        boolean habitacion300Presente = false;

                        for (int i = 0; i < habitaciones.size(); i++) {
                            Habitacion hab = habitaciones.get(i);
                            int adultosHab = hab.getCapacidadAdultos() != null ? hab.getCapacidadAdultos() : 0;
                            int ninosHab = hab.getCapacidadNinos() != null ? hab.getCapacidadNinos() : 0;

                            boolean deberiaEstar = (adultosHab >= adultos) && (ninosHab >= ninos);

                            if ("300".equals(hab.getNumero())) {
                                habitacion300Presente = true;
                                Log.e("HotelHabitacion", "🎯 HABITACIÓN 300 DETECTADA:");
                                Log.e("HotelHabitacion", "   Capacidad: [" + adultosHab + " adultos, " + ninosHab + " niños]");
                                Log.e("HotelHabitacion", "   Criterio: [" + adultos + "+ adultos, " + ninos + "+ niños]");
                                Log.e("HotelHabitacion", "   ¿Debería estar? " + (deberiaEstar ? "SÍ" : "NO"));
                            }

                            Log.d("HotelHabitacion", String.format(
                                    "Habitación %s: [%d adultos, %d niños] - ¿Debería estar? %s",
                                    hab.getNumero(), adultosHab, ninosHab, deberiaEstar ? "SÍ" : "NO ❌"
                            ));

                            if (!deberiaEstar) {
                                Log.e("HotelHabitacion", "⚠️ HABITACIÓN QUE NO DEBERÍA ESTAR: " + hab.getNumero());
                            }
                        }

                        if (habitacion300Presente && ninos > 0) {
                            Log.e("HotelHabitacion", "🚨 CONFIRMADO: Habitación 300 no debería estar presente!");
                        }

                        Log.d("HotelHabitacion", "===============================");

                        listaHabitaciones.clear();
                        listaHabitaciones.addAll(habitaciones);
                        adapter.notifyDataSetChanged();

                        if (habitaciones.isEmpty()) {
                            Toast.makeText(this,
                                    "No hay habitaciones disponibles para " + adultos + " adultos y " + ninos + " niños",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String mensaje = habitaciones.size() + " habitaciones encontradas para " +
                                    adultos + "+ adultos, " + ninos + "+ niños";
                            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e("HotelHabitacion", "Error cargando habitaciones: " + error.getMessage());
                        Toast.makeText(this, "Error cargando habitaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    private int[] parsearCapacidad(String peopleString) {
        int adultos = 2; // Valor por defecto
        int ninos = 0;   // Valor por defecto

        if (peopleString != null && !peopleString.trim().isEmpty()) {
            try {
                String[] parts = peopleString.split(" · ");

                for (String part : parts) {
                    part = part.trim().toLowerCase();

                    if (part.contains("adulto")) {
                        String[] adultParts = part.split(" ");
                        if (adultParts.length > 0) {
                            adultos = Integer.parseInt(adultParts[0]);
                        }
                    } else if (part.contains("niño")) {
                        String[] childParts = part.split(" ");
                        if (childParts.length > 0) {
                            ninos = Integer.parseInt(childParts[0]);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("HotelHabitacion", "Error parseando capacidad: " + peopleString);
                // Usar valores por defecto
            }
        }

        return new int[]{adultos, ninos};
    }

    private void testParsearCapacidad() {
        String[] testCases = {
                "1 habitación · 2 adultos · 1 niño",
                "2 habitaciones · 3 adultos · 2 niños",
                "1 habitación · 1 adulto · 0 niños",
                "3 habitaciones · 4 adultos · 1 niño"
        };

        Log.d("HotelHabitacion", "=== TESTING PARSEO ===");
        for (String test : testCases) {
            int[] result = parsearCapacidad(test);
            Log.d("HotelHabitacion", String.format("'%s' → [%d adultos, %d niños]",
                    test, result[0], result[1]));
        }
        Log.d("HotelHabitacion", "=====================");
    }

}*/
public class HotelHabitacionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitacionAdapter adapter;
    private List<Habitacion> listaHabitaciones = new ArrayList<>();

    private String hotelId;

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_habitacion);

        hotelId = getIntent().getStringExtra("hotelId");
        if (hotelId == null) {
            Toast.makeText(this, "Hotel inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewHabitaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
            // Navegar a la pantalla de resumen de reserva
            Intent intent = new Intent(this, ResumenReservaActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacion.getId()); // Asegúrate de que getId() existe en Habitacion
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        //cargarHotelYHabitaciones();
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_activity_hotel_habitacion);

        hotelId = getIntent().getStringExtra("hotelId");
        if (hotelId == null) {
            Toast.makeText(this, "Hotel inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewHabitaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
            // Navegar a la pantalla de resumen de reserva
            Intent intent = new Intent(this, ResumenReservaActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacion.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Cargar habitaciones
        cargarHabitacionesDelHotel();
    }

    /**
     * Carga habitaciones del hotel con filtro de capacidad
     */
    private void cargarHabitacionesDelHotel() {
        // Obtener parámetros de búsqueda desde PrefsManager
        PrefsManager prefsManager = new PrefsManager(this);

        // Parsear string de personas para obtener adultos y niños
        String peopleString = prefsManager.getPeopleString();
        int[] capacidad = parsearCapacidad(peopleString);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        Log.d("HotelHabitacion", "Cargando habitaciones para " + adultos + " adultos y " + ninos + " niños");

        // Mostrar loading
        // TODO: Agregar ProgressBar si quieres

        HotelRepository.getHabitacionesByHotelIdAndCapacity(hotelId, adultos, ninos,
                habitaciones -> {
                    runOnUiThread(() -> {
                        listaHabitaciones.clear();
                        listaHabitaciones.addAll(habitaciones);
                        adapter.notifyDataSetChanged();

                        Log.d("HotelHabitacion", "Habitaciones cargadas: " + habitaciones.size());

                        if (habitaciones.isEmpty()) {
                            Toast.makeText(this,
                                    "No hay habitaciones disponibles para " + adultos + " adultos y " + ninos + " niños",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e("HotelHabitacion", "Error cargando habitaciones: " + error.getMessage());
                        Toast.makeText(this, "Error cargando habitaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    /**
     * Parsea el string de personas para extraer adultos y niños
     * Formato: "2 habitaciones · 3 adultos · 1 niño"
     */
    private int[] parsearCapacidad(String peopleString) {
        int adultos = 2; // Valor por defecto
        int ninos = 0;   // Valor por defecto

        if (peopleString != null && !peopleString.trim().isEmpty()) {
            try {
                String[] parts = peopleString.split(" · ");

                for (String part : parts) {
                    part = part.trim().toLowerCase();

                    if (part.contains("adulto")) {
                        String[] adultParts = part.split(" ");
                        if (adultParts.length > 0) {
                            adultos = Integer.parseInt(adultParts[0]);
                        }
                    } else if (part.contains("niño")) {
                        String[] childParts = part.split(" ");
                        if (childParts.length > 0) {
                            ninos = Integer.parseInt(childParts[0]);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("HotelHabitacion", "Error parseando capacidad: " + peopleString);
                // Usar valores por defecto
            }
        }

        return new int[]{adultos, ninos};
    }

}
