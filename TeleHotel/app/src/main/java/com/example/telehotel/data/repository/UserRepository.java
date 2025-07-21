package com.example.telehotel.data.repository;

import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.data.model.Usuario;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class UserRepository {

    public interface UserCallback {
        void onSuccess(Usuario user);
        void onFailure(String errorMessage);
    }

    // Ahora definimos dos interfaces funcionales (funciones lambda)
    public interface SuccessCallback {
        void onSuccess(Usuario user);
    }

    public interface FailureCallback {
        void onFailure(String errorMessage);
    }

    public void getUserByEmail(String email, SuccessCallback onSuccess, FailureCallback onFailure) {
        FirebaseUtil.getFirestore().collection("usuarios")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Usuario user = doc.toObject(Usuario.class);
                        user.setUid(doc.getId());
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> onFailure.onFailure("Error al consultar: " + e.getMessage()));
    }
    public static ListenerRegistration  escucharEstadoUsuario(String uid, UserEstadoListener listener) {
        return FirebaseUtil.getFirestore()
                .collection("usuarios")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        listener.onError("Error al escuchar el estado del usuario.");
                        return;
                    }

                    String estado = snapshot.getString("estadoViaje");
                    listener.onEstadoChanged(estado);
                });
    }
    public interface UserEstadoListener {
        void onEstadoChanged(String estado);
        void onError(String error);
    }

    public static void getUserByUid(String uidBuscado, SuccessCallback onSuccess, FailureCallback onFailure) {
        FirebaseUtil.getFirestore().collection("usuarios")
                .whereEqualTo("uid", uidBuscado)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Usuario user = doc.toObject(Usuario.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                            onSuccess.onSuccess(user);
                        } else {
                            onFailure.onFailure("Usuario mapeado como null");
                        }
                    } else {
                        onFailure.onFailure("Usuario no encontrado con uid: " + uidBuscado);
                    }
                })
                .addOnFailureListener(e -> onFailure.onFailure("Error al consultar: " + e.getMessage()));
    }

}
