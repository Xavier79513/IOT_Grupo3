package com.example.telehotel.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Hotel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HotelRepository {

    private static final String TAG = "HotelRepository";

    // 🔁 Accede a la colección "hoteles"
    public static CollectionReference getCollection() {
        return FirebaseUtil.getFirestore().collection("hoteles");
    }

    // 📥 Obtener todos los hoteles
    public static void getAllHotels(@NonNull Consumer<List<Hotel>> onSuccess,
                                    @NonNull Consumer<Exception> onError) {

        getCollection()
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId()); // 💡 ID del documento Firestore
                            hoteles.add(hotel);
                        }
                    }
                    onSuccess.accept(hoteles);
                })
                .addOnFailureListener(onError::accept);
    }

    // 🔍 Buscar hotel por ID de documento
    public static void getHotelById(String hotelId,
                                    @NonNull Consumer<Hotel> onSuccess,
                                    @NonNull Consumer<Exception> onError) {

        getCollection()
                .document(hotelId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hotel.setId(doc.getId());
                            onSuccess.accept(hotel);
                        } else {
                            onError.accept(new Exception("Hotel mapeado como null"));
                        }
                    } else {
                        onError.accept(new Exception("No existe hotel con ID: " + hotelId));
                    }
                })
                .addOnFailureListener(onError::accept);
    }
}
