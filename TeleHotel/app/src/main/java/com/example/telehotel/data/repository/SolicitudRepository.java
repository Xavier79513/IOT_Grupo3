package com.example.telehotel.data.repository;

import android.util.Log;

import com.example.telehotel.data.model.ServicioTaxi;

import com.example.telehotel.features.taxista.TaxistaStatsActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
                        assert solicitudTaxi != null;
                        solicitudTaxi.setId(doc.getId());  // SETEAR EL ID DEL DOCUMENTO

                        solicitudTaxiList.add(solicitudTaxi);
                    }
                    listener.onViajesLoaded(solicitudTaxiList);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }
    public void getEstadisticasViajes(String taxistaId, SolicitudRepository.OnEstadisticasLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("solicitudTaxi");

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        Date inicioDia = hoy.getTime();

        Calendar inicioMes = Calendar.getInstance();
        inicioMes.set(Calendar.DAY_OF_MONTH, 1);
        inicioMes.set(Calendar.HOUR_OF_DAY, 0);
        inicioMes.set(Calendar.MINUTE, 0);
        inicioMes.set(Calendar.SECOND, 0);
        inicioMes.set(Calendar.MILLISECOND, 0);
        Date inicioMesDate = inicioMes.getTime();

        // Formato esperado: "yyyy-MM-dd"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Log.d("ESTADISTICAS", "Inicio del día: " + inicioDia);
        Log.d("ESTADISTICAS", "Inicio del mes: " + inicioMesDate);

        ref.whereEqualTo("taxistaId", taxistaId)
                .whereEqualTo("estado", "Terminado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalHoy = 0;
                    int totalMes = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String fechaFinStr = doc.getString("fechaFin");

                        Log.d("ESTADISTICAS", "Documento ID: " + doc.getId());
                        Log.d("ESTADISTICAS", "  fechaFin: " + fechaFinStr);

                        if (fechaFinStr != null) {
                            try {
                                Date fechaFin = sdf.parse(fechaFinStr);
                                if (fechaFin != null) {
                                    if (fechaFin.compareTo(inicioMesDate) >= 0) {
                                        totalMes++;
                                        if (fechaFin.compareTo(inicioDia) >= 0) {
                                            totalHoy++;
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                Log.e("ESTADISTICAS", "Error al parsear fecha: " + fechaFinStr, e);
                            }
                        }
                    }

                    Log.d("ESTADISTICAS", "Total HOY: " + totalHoy);
                    Log.d("ESTADISTICAS", "Total MES: " + totalMes);
                    listener.onEstadisticasLoaded(totalHoy, totalMes);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }



    public interface OnEstadisticasLoadedListener {
        void onEstadisticasLoaded(int viajesHoy, int viajesMes);
        void onError(String errorMessage);
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
    public void getById(String id, final OnViajeLoadedListener listener) {
        viajesRef.document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ServicioTaxi servicioTaxi = documentSnapshot.toObject(ServicioTaxi.class);
                        assert servicioTaxi != null;
                        servicioTaxi.setId(documentSnapshot.getId());  // SETEAR EL ID DEL DOCUMENTO

                        listener.onViajeLoaded(servicioTaxi);
                    } else {
                        listener.onViajeLoaded(null); // No existe documento
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    // Interfaz para un solo objeto
    public interface OnViajeLoadedListener {
        void onViajeLoaded(ServicioTaxi viaje);
        void onError(String errorMessage);
    }



    public interface OnViajesLoadedListener {
        void onViajesLoaded(List<ServicioTaxi> viajes);
        void onError(String errorMessage);
    }
}
