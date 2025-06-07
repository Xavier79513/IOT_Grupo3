package com.example.telehotel.data.repository;

import androidx.annotation.NonNull;

import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Aeropuerto;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.function.Consumer;

public class AeropuertoRepository {

    private static final String COLLECTION_NAME = "aeropuerto"; // nombre de tu colección

    private static CollectionReference getCollection() {
        return FirebaseUtil.getFirestore().collection(COLLECTION_NAME);
    }

    public static void getByAeropuertoId(@NonNull String aeropuertoId,
                                         @NonNull Consumer<Aeropuerto> onSuccess,
                                         @NonNull Consumer<Exception> onError) {
        getCollection()
                .whereEqualTo("aeropuertoId", aeropuertoId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Aeropuerto aeropuerto = doc.toObject(Aeropuerto.class);
                        assert aeropuerto != null;
                        onSuccess.accept(aeropuerto);
                    } else {
                        onError.accept(new Exception("No se encontró aeropuerto con ID: " + aeropuertoId));
                    }
                })
                .addOnFailureListener(onError::accept);
    }
}
