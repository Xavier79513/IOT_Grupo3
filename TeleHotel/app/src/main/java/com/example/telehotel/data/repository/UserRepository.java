package com.example.telehotel.data.repository;

import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Usuario;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserRepository {

    public interface UserCallback {
        void onSuccess(Usuario user);
        void onFailure(String errorMessage);
    }

    public void getUserByEmail(String email, final UserCallback callback) {
        FirebaseUtil.getFirestore().collection("usuarios")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Usuario user = doc.toObject(Usuario.class);
                        user.setUid(doc.getId()); // para que tenga UID si es el ID del doc
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error al consultar: " + e.getMessage()));
    }
}
