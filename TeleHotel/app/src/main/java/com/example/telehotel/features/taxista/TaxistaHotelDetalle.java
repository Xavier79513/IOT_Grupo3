package com.example.telehotel.features.taxista;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.features.taxista.adapter.SolicitudTaxiAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaxistaHotelDetalle extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SolicitudTaxiAdapter solicitudAdapter;
    private List<ServicioTaxi> listaSolicitudes = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxista_hotel_detalle);

        // Recuperar datos del Intent
        String hotelId = getIntent().getStringExtra("hotel_id");
        String hotelNombre = getIntent().getStringExtra("hotel_nombre");
        String hotelDireccion = getIntent().getStringExtra("hotel_direccion");

        // Verificar si los datos del Intent existen
        if (hotelId == null || hotelNombre == null || hotelDireccion == null) {
            Log.e("TaxistaHotelDetalle", "Faltan datos en el Intent");
            return;
        }
        Log.d("Verificar",hotelId);

        // Mostrar los datos del hotel
        TextView titulo = findViewById(R.id.textHotelTitulo);
        TextView direccion = findViewById(R.id.textHotelDireccion);
        if (titulo != null) titulo.setText(hotelNombre);
        if (direccion != null) direccion.setText(hotelDireccion);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adapter con listener
        solicitudAdapter = new SolicitudTaxiAdapter(listaSolicitudes, new SolicitudTaxiAdapter.OnSolicitudActionListener() {
            @Override
            public void onAceptar(ServicioTaxi solicitud) {
                if (solicitud.getId() != null) {
                    db.collection("solicitudTaxi")
                            .document(solicitud.getId())
                            .update("estado", "en curso")
                            .addOnSuccessListener(aVoid -> Log.d("Adapter", "Solicitud aceptada"))
                            .addOnFailureListener(e -> Log.e("Adapter", "Error al aceptar", e));
                }
            }

            @Override
            public void onRechazar(ServicioTaxi solicitud) {
                if (solicitud.getId() != null) {
                    db.collection("solicitudTaxi")
                            .document(solicitud.getId())
                            .update("estado", "cancelado")
                            .addOnSuccessListener(aVoid -> Log.d("Adapter", "Solicitud rechazada"))
                            .addOnFailureListener(e -> Log.e("Adapter", "Error al rechazar", e));
                }
            }
        });
        String authUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("usuarios")
                .document(authUserId)  // asumimos que el id del doc es el mismo que el de Authentication
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String uid = userDoc.getString("uid");
                        if (uid != null) {
                            // Ahora sí busca en estadoViaje por ese UID
                            FirebaseFirestore.getInstance().collection("estadoViaje")
                                    .whereEqualTo("taxista_uid", uid)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot estadoDoc = querySnapshot.getDocuments().get(0);
                                            Boolean estaViajando = estadoDoc.getBoolean("estaViajando");
                                            Log.d("EstadoViaje", "estaViajando: " + estaViajando);

                                            if (Boolean.TRUE.equals(estaViajando)) {
                                                TextView emptyTextView = findViewById(R.id.textViewEmpty);
                                                recyclerView.setVisibility(View.GONE);
                                                emptyTextView.setText("No puedes buscar más, actualmente estás en un viaje");
                                                emptyTextView.setVisibility(View.VISIBLE);
                                            } else {
                                                escucharSolicitudes(hotelId);
                                            }
                                        } else {
                                            escucharSolicitudes(hotelId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EstadoViaje", "Error al consultar estadoViaje", e);
                                        escucharSolicitudes(hotelId);
                                    });
                        } else {
                            Log.e("Usuario", "Campo uid es null");
                            escucharSolicitudes(hotelId);
                        }
                    } else {
                        Log.e("Usuario", "No se encontró el usuario en la colección");
                        escucharSolicitudes(hotelId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Usuario", "Error al obtener documento del usuario", e);
                    escucharSolicitudes(hotelId);
                });

        recyclerView.setAdapter(solicitudAdapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("AuthCheck", "Usuario autenticado: " + user.getUid());

        } else {
            Log.e("AuthCheck", "Usuario NO autenticado, no se puede consultar Firestore");
            // Opcional: mostrar mensaje o redirigir a login
        }

        // Obtener solicitudes en tiempo real

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_hotels);
        bottomNav.setOnItemSelectedListener(item -> {
            TaxistaNavigationHelper.navigate(TaxistaHotelDetalle.this, item.getItemId());
            return true;
        });
    }

    private void escucharSolicitudes(String hotelId) {
        TextView emptyTextView = findViewById(R.id.textViewEmpty);

        db.collection("solicitudTaxi")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("estado", "Buscando")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("TaxistaHotelDetalle", "Error en listener de Firestore", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<ServicioTaxi> nuevasSolicitudes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Log.d("FirestoreDocumento", "ID: " + document.getId() + " Datos: " + document.getData());
                            ServicioTaxi solicitud = document.toObject(ServicioTaxi.class);
                            solicitud.setId(document.getId());

                            // Log del estado de la solicitud
                            if (solicitud.getEstado() != null) {
                                Log.d("SolicitudEstado", "Solicitud ID: " + solicitud.getId() + " Estado: " + solicitud.getEstado());
                            } else {
                                Log.d("SolicitudEstado", "Solicitud ID: " + solicitud.getId() + " Estado: null");
                            }

                            nuevasSolicitudes.add(solicitud);
                        }


                        Log.d("TaxistaHotelDetalle", "Solicitudes actualizadas: " + nuevasSolicitudes.size());
                        solicitudAdapter.updateSolicitudes(nuevasSolicitudes);

                        // Mostrar u ocultar mensaje vacío
                        if (nuevasSolicitudes.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

}
