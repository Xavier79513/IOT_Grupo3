package com.example.telehotel.data.repository;

import com.example.telehotel.data.model.ServicioTaxi;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SolicitudRepository {

    private FirebaseFirestore db;
    private CollectionReference viajesRef;

    public SolicitudRepository() {
        db = FirebaseFirestore.getInstance();
        viajesRef = db.collection("solicitudTaxi");
    }

    public void getAllByUidTaxista(String uidTaxista, final OnViajesLoadedListener listener) {
        viajesRef.whereEqualTo("taxistaId", uidTaxista)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServicioTaxi> solicitudTaxiList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ServicioTaxi solicitudTaxi = doc.toObject(ServicioTaxi.class);
                        solicitudTaxiList.add(solicitudTaxi);
                    }
                    listener.onViajesLoaded(solicitudTaxiList);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }
    public  void getUltimosViajesByTaxista(String uidTaxista, int limit, final OnViajesLoadedListener listener) {
        viajesRef.whereEqualTo("taxistaId", uidTaxista)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServicioTaxi> solicitudTaxiList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ServicioTaxi solicitudTaxi = doc.toObject(ServicioTaxi.class);
                        solicitudTaxiList.add(solicitudTaxi);
                    }
                    listener.onViajesLoaded(solicitudTaxiList);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }
    public void getSolicitudesBuscando(final OnViajesLoadedListener listener) {
        viajesRef.whereEqualTo("estado", "Buscando")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServicioTaxi> solicitudTaxiList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ServicioTaxi solicitudTaxi = doc.toObject(ServicioTaxi.class);
                        solicitudTaxiList.add(solicitudTaxi);
                    }
                    listener.onViajesLoaded(solicitudTaxiList);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }


    public void listenSolicitudesBuscando(EventListener<QuerySnapshot> listener) {
        FirebaseFirestore.getInstance()
                .collection("solicitudTaxi")
                .whereEqualTo("estado", "Buscando")
                .addSnapshotListener(listener);
    }


    public interface OnViajesLoadedListener {
        void onViajesLoaded(List<ServicioTaxi> viajes);
        void onError(String errorMessage);
    }
}
