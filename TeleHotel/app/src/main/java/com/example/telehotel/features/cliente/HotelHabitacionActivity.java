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

}*/
public class HotelHabitacionActivity extends AppCompatActivity {

    private static final String TAG = "HotelHabitacion";

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

        // ✅ CREAR ADAPTER CON DEBUGGING
        adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
            Log.d(TAG, "✅ Habitación seleccionada: " + habitacion.getNumero());

            // Navegar a la pantalla de resumen de reserva
            Intent intent = new Intent(this, ResumenReservaActivity.class);
            intent.putExtra("hotelId", hotelId);
            intent.putExtra("habitacionId", habitacion.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // ✅ DEBUGGING INICIAL
        Log.d(TAG, "=== ACTIVITY CONFIGURADA ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "Adapter item count inicial: " + adapter.getItemCount());

        // Cargar habitaciones
        cargarHabitacionesDelHotel();
    }

    /**
     * ✅ MÉTODO MEJORADO - Carga habitaciones DISPONIBLES del hotel con filtro de capacidad
     */
    /*private void cargarHabitacionesDelHotel() {
        Log.d(TAG, "=== CARGANDO HABITACIONES ===");

        // Obtener parámetros de búsqueda desde PrefsManager
        PrefsManager prefsManager = new PrefsManager(this);

        // Parsear string de personas para obtener adultos y niños
        String peopleString = prefsManager.getPeopleString();
        int[] capacidad = parsearCapacidad(peopleString);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        Log.d(TAG, "Buscando habitaciones para " + adultos + " adultos y " + ninos + " niños");
        Log.d(TAG, "People string original: '" + peopleString + "'");

        // ✅ OPCIÓN A: Modificar la consulta en HotelRepository (RECOMENDADO)
        // Si puedes modificar HotelRepository para que incluya filtro de estado:




        HotelRepository.getHabitacionesByHotelIdAndCapacity(hotelId, adultos, ninos,
                habitaciones -> {
                    Log.d(TAG, "=== HABITACIONES RECIBIDAS DEL REPOSITORY ===");
                    Log.d(TAG, "Total habitaciones: " + habitaciones.size());

                    // ✅ FILTRAR SOLO HABITACIONES DISPONIBLES AQUÍ
                    List<Habitacion> habitacionesDisponibles = filtrarHabitacionesDisponibles(habitaciones);

                    runOnUiThread(() -> {
                        Log.d(TAG, "=== ACTUALIZANDO UI ===");
                        Log.d(TAG, "Habitaciones disponibles: " + habitacionesDisponibles.size());

                        listaHabitaciones.clear();
                        listaHabitaciones.addAll(habitacionesDisponibles);

                        // ✅ DEBUGGING ANTES DE NOTIFICAR
                        Log.d(TAG, "Lista final size: " + listaHabitaciones.size());
                        Log.d(TAG, "Adapter getItemCount antes: " + adapter.getItemCount());

                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Adapter getItemCount después: " + adapter.getItemCount());
                        Log.d(TAG, "notifyDataSetChanged() llamado");

                        // ✅ VERIFICAR RECYCLERVIEW DESPUÉS DE UN MOMENTO
                        recyclerView.post(() -> {
                            Log.d(TAG, "=== ESTADO FINAL RECYCLERVIEW ===");
                            Log.d(TAG, "RecyclerView child count: " + recyclerView.getChildCount());
                            Log.d(TAG, "RecyclerView width: " + recyclerView.getWidth());
                            Log.d(TAG, "RecyclerView height: " + recyclerView.getHeight());
                            Log.d(TAG, "===================================");
                        });

                        // ✅ MENSAJES AL USUARIO
                        if (habitacionesDisponibles.isEmpty()) {
                            if (habitaciones.isEmpty()) {
                                Toast.makeText(this,
                                        "No hay habitaciones que cumplan con la capacidad requerida",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this,
                                        "No hay habitaciones disponibles en este momento. " +
                                                "Se encontraron " + habitaciones.size() + " habitaciones pero están ocupadas.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "✅ Mostrando " + habitacionesDisponibles.size() + " habitaciones disponibles");
                        }
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "❌ Error cargando habitaciones: " + error.getMessage());
                        Toast.makeText(this, "Error cargando habitaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }*/

    private void cargarHabitacionesDelHotel() {
        Log.d(TAG, "=== CARGANDO HABITACIONES ===");

        // Obtener parámetros de búsqueda desde PrefsManager
        PrefsManager prefsManager = new PrefsManager(this);

        // Parsear string de personas para obtener adultos y niños
        String peopleString = prefsManager.getPeopleString();
        int[] capacidad = parsearCapacidad(peopleString);
        int adultos = capacidad[0];
        int ninos = capacidad[1];

        Log.d(TAG, "Buscando habitaciones para " + adultos + " adultos y " + ninos + " niños");
        Log.d(TAG, "People string original: '" + peopleString + "'");

        // ✅ LLAMADA SIMPLIFICADA - HotelRepository ya filtra por estado disponible
        HotelRepository.getHabitacionesByHotelIdAndCapacity(hotelId, adultos, ninos,
                habitaciones -> {
                    Log.d(TAG, "=== HABITACIONES RECIBIDAS DEL REPOSITORY ===");
                    Log.d(TAG, "Habitaciones disponibles y con capacidad adecuada: " + habitaciones.size());

                    runOnUiThread(() -> {
                        Log.d(TAG, "=== ACTUALIZANDO UI ===");

                        // ✅ AQUÍ ESTÁ EL PROBLEMA - NECESITAS ACTUALIZAR EL ADAPTER, NO SOLO LA LISTA
                        listaHabitaciones.clear();
                        listaHabitaciones.addAll(habitaciones);

                        Log.d(TAG, "Lista final size: " + listaHabitaciones.size());

                        // ✅ OPCIÓN A: Usar el método actualizarHabitaciones del adapter
                        if (adapter != null) {
                            Log.d(TAG, "Adapter getItemCount antes: " + adapter.getItemCount());

                            // Esto debería actualizar la lista interna del adapter
                            adapter.actualizarHabitaciones(habitaciones);

                            Log.d(TAG, "Adapter getItemCount después: " + adapter.getItemCount());
                            Log.d(TAG, "actualizarHabitaciones() llamado");
                        } else {
                            Log.e(TAG, "❌ Adapter es null!");
                        }

                        // ✅ OPCIÓN B: Recrear el adapter (más seguro)
                    /*
                    Log.d(TAG, "Recreando adapter...");
                    adapter = new HabitacionAdapter(this, listaHabitaciones, habitacion -> {
                        Log.d(TAG, "✅ Habitación seleccionada: " + habitacion.getNumero());

                        Intent intent = new Intent(this, ResumenReservaActivity.class);
                        intent.putExtra("hotelId", hotelId);
                        intent.putExtra("habitacionId", habitacion.getId());
                        startActivity(intent);
                    });

                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "Adapter recreado y asignado");
                    */

                        // ✅ VERIFICAR RECYCLERVIEW DESPUÉS DE UN MOMENTO
                        recyclerView.post(() -> {
                            Log.d(TAG, "=== ESTADO FINAL RECYCLERVIEW ===");
                            Log.d(TAG, "RecyclerView child count: " + recyclerView.getChildCount());
                            Log.d(TAG, "RecyclerView width: " + recyclerView.getWidth());
                            Log.d(TAG, "RecyclerView height: " + recyclerView.getHeight());
                            Log.d(TAG, "Adapter final getItemCount: " + (adapter != null ? adapter.getItemCount() : "null"));
                            Log.d(TAG, "===================================");
                        });

                        // ✅ MENSAJES AL USUARIO MEJORADOS
                        if (habitaciones.isEmpty()) {
                            Toast.makeText(this,
                                    "No hay habitaciones disponibles para " + adultos + " adultos y " + ninos + " niños",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "✅ Mostrando " + habitaciones.size() + " habitaciones disponibles");
                        }
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "❌ Error cargando habitaciones: " + error.getMessage());
                        Toast.makeText(this, "Error cargando habitaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }


    /**
     * ✅ NUEVO MÉTODO - Filtrar habitaciones por estado disponible
     */
    /*private List<Habitacion> filtrarHabitacionesDisponibles(List<Habitacion> habitaciones) {
        List<Habitacion> disponibles = new ArrayList<>();

        Log.d(TAG, "=== FILTRANDO HABITACIONES POR ESTADO ===");

        if (habitaciones == null) {
            Log.w(TAG, "Lista de habitaciones es null");
            return disponibles;
        }

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion habitacion = habitaciones.get(i);

            if (habitacion == null) {
                Log.w(TAG, "Habitación " + i + " es null");
                continue;
            }

            String estado = habitacion.getEstado();
            Log.d(TAG, "Habitación " + habitacion.getNumero() + " - Estado: '" + estado + "'");

            if (estado != null && estado.equalsIgnoreCase("disponible")) {
                disponibles.add(habitacion);
                Log.d(TAG, "✅ Habitación " + habitacion.getNumero() + " agregada (disponible)");
            } else {
                Log.d(TAG, "❌ Habitación " + habitacion.getNumero() + " filtrada (estado: " + estado + ")");
            }
        }

        Log.d(TAG, "Resultado filtro: " + disponibles.size() + " de " + habitaciones.size() + " habitaciones disponibles");
        Log.d(TAG, "==========================================");

        return disponibles;
    }*/

    /**
     * Parsea el string de personas para extraer adultos y niños
     * Formato: "2 habitaciones · 3 adultos · 1 niño"
     */
    private int[] parsearCapacidad(String peopleString) {
        int adultos = 2; // Valor por defecto
        int ninos = 0;   // Valor por defecto

        Log.d(TAG, "=== PARSEANDO CAPACIDAD ===");
        Log.d(TAG, "Input: '" + peopleString + "'");

        if (peopleString != null && !peopleString.trim().isEmpty()) {
            try {
                String[] parts = peopleString.split(" · ");
                Log.d(TAG, "Parts: " + java.util.Arrays.toString(parts));

                for (String part : parts) {
                    part = part.trim().toLowerCase();
                    Log.d(TAG, "Procesando part: '" + part + "'");

                    if (part.contains("adulto")) {
                        String[] adultParts = part.split(" ");
                        if (adultParts.length > 0) {
                            try {
                                adultos = Integer.parseInt(adultParts[0]);
                                Log.d(TAG, "Adultos extraídos: " + adultos);
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Error parseando adultos: " + adultParts[0]);
                            }
                        }
                    } else if (part.contains("niño")) {
                        String[] childParts = part.split(" ");
                        if (childParts.length > 0) {
                            try {
                                ninos = Integer.parseInt(childParts[0]);
                                Log.d(TAG, "Niños extraídos: " + ninos);
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Error parseando niños: " + childParts[0]);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parseando capacidad: " + peopleString, e);
                // Usar valores por defecto
            }
        }

        Log.d(TAG, "Resultado final: " + adultos + " adultos, " + ninos + " niños");
        Log.d(TAG, "==========================");

        return new int[]{adultos, ninos};
    }

    /**
     * ✅ MÉTODO OPCIONAL - Para actualizar habitaciones cuando se regrese de pago
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== onResume - Recargando habitaciones ===");
        // Recargar habitaciones por si alguna cambió de estado
        cargarHabitacionesDelHotel();
    }

    /**
     * ✅ MÉTODO OPCIONAL - Para debugging manual
     */
    private void debugEstadoCompleto() {
        Log.d(TAG, "=== DEBUG ESTADO COMPLETO ===");
        Log.d(TAG, "Hotel ID: " + hotelId);
        Log.d(TAG, "Lista habitaciones size: " + listaHabitaciones.size());
        Log.d(TAG, "Adapter getItemCount: " + (adapter != null ? adapter.getItemCount() : "adapter null"));
        Log.d(TAG, "RecyclerView child count: " + (recyclerView != null ? recyclerView.getChildCount() : "recyclerView null"));

        if (listaHabitaciones != null) {
            for (int i = 0; i < listaHabitaciones.size(); i++) {
                Habitacion hab = listaHabitaciones.get(i);
                Log.d(TAG, "  " + i + ". " + (hab != null ? hab.getNumero() + " (" + hab.getEstado() + ")" : "null"));
            }
        }
        Log.d(TAG, "=============================");
    }
}
