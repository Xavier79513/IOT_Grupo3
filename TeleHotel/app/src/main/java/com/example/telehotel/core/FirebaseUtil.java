package com.example.telehotel.core;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseUtil {

    private static FirebaseFirestore firestoreInstance;
    private static FirebaseAuth authInstance;
    private static FirebaseStorage storageInstance;
    private static FirebaseDatabase realtimeDatabaseInstance;

    // Constructor privado para evitar instanciación
    private FirebaseUtil() {}

    // Firestore
    public static FirebaseFirestore getFirestore() {
        if (firestoreInstance == null) {
            firestoreInstance = FirebaseFirestore.getInstance();
        }
        return firestoreInstance;
    }

    // Auth
    public static FirebaseAuth getAuth() {
        if (authInstance == null) {
            authInstance = FirebaseAuth.getInstance();
        }
        return authInstance;
    }

    // Storage
    public static FirebaseStorage getStorage() {
        if (storageInstance == null) {
            storageInstance = FirebaseStorage.getInstance();
        }
        return storageInstance;
    }

    // Realtime Database
    public static FirebaseDatabase getRealtimeDatabase() {
        if (realtimeDatabaseInstance == null) {
            realtimeDatabaseInstance = FirebaseDatabase.getInstance();
        }
        return realtimeDatabaseInstance;
    }
}