package com.example.telehotel.features.cliente.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HotelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Hotel> listaHoteles = new ArrayList<>();
    private HotelAdapter adapter;
    private FirebaseFirestore db;

    public HotelsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerHotelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelAdapter(listaHoteles);
        recyclerView.setAdapter(adapter);

        db = FirebaseUtil.getFirestore();

        db.collection("hoteles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Consulta ejecutada con éxito. Total documentos: " + querySnapshot.size());

                    listaHoteles.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Log.d(TAG, "Documento recibido con ID: " + doc.getId());

                        try {
                            Hotel hotel = doc.toObject(Hotel.class);
                            if (hotel != null) {
                                listaHoteles.add(hotel);
                                Log.d(TAG, "Hotel agregado: " + hotel.getNombre());
                            } else {
                                Log.w(TAG, "Documento convertido a null: " + doc.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al convertir el documento: " + doc.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Total hoteles listos para mostrar: " + listaHoteles.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener hoteles desde Firestore", e);
                    Toast.makeText(getContext(), "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
                });

    }
}
