package com.example.telehotel.data.repository;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.function.BiConsumer;

public class EstadoViajeRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration listenerRegistration;

    public void escucharViajePorUid(String taxistaUid, BiConsumer<DocumentSnapshot, Exception> callback) {
        // Buscar el último viaje activo del taxista
        listenerRegistration = db.collection("estadoViaje")
                .whereEqualTo("taxista_uid", taxistaUid)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.accept(null, error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        callback.accept(value.getDocuments().get(0), null);
                    } else {
                        callback.accept(null, null); // no hay viaje activo
                    }
                });
    }

    public void detenerEscucha() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
