package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;       // <-- import cambiado
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminLugaresFragment extends Fragment {

    private Toolbar         toolbar;
    private EditText        etNombreLugar, etDescripcionLugar;
    private Button          btnAgregarLugar;
    private LinearLayout    containerLugares;

    private FirebaseFirestore db;
    private String            hotelId, adminName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_lugares, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI
        toolbar            = view.findViewById(R.id.toolbar_lugares);
        etNombreLugar      = view.findViewById(R.id.etNombreLugar);
        etDescripcionLugar = view.findViewById(R.id.etDescripcionLugar);
        btnAgregarLugar    = view.findViewById(R.id.btnAgregarLugar);
        containerLugares   = view.findViewById(R.id.containerLugares);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Leer argumentos
        if (getArguments() != null) {
            hotelId   = getArguments().getString("hotel_id");
            adminName = getArguments().getString("admin_name");
        }

        // Flecha atrás
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Cargar lista existente
        loadLugaresDesdeFirestore();

        // Configurar botón Agregar
        btnAgregarLugar.setOnClickListener(v -> agregarLugar());
    }

    @SuppressWarnings("unchecked")
    private void loadLugaresDesdeFirestore() {
        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    // Vaciar el contenedor
                    containerLugares.removeAllViews();

                    List<Map<String, Object>> lugares =
                            (List<Map<String, Object>>) doc.get("lugaresHistoricos");
                    if (lugares == null) return;

                    for (Map<String, Object> lugar : lugares) {
                        View item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_lugar, containerLugares, false);

                        TextView  tvN  = item.findViewById(R.id.tvLugarNombre);
                        TextView  tvD  = item.findViewById(R.id.tvLugarDescripcion);
                        ImageView btnDel = item.findViewById(R.id.btnDeleteLugar);  // <-- ImageView

                        tvN.setText((String) lugar.get("nombre"));
                        tvD.setText((String) lugar.get("descripcion"));

                        btnDel.setOnClickListener(v -> {
                            db.collection("hoteles")
                                    .document(hotelId)
                                    .update("lugaresHistoricos", FieldValue.arrayRemove(lugar))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(),
                                                "Lugar eliminado", Toast.LENGTH_SHORT).show();
                                        loadLugaresDesdeFirestore();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(),
                                                "Error al eliminar: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });
                        });

                        containerLugares.addView(item);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error cargando lugares: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void agregarLugar() {
        String nombre = etNombreLugar.getText().toString().trim();
        String desc   = etDescripcionLugar.getText().toString().trim();

        if (nombre.isEmpty() || desc.isEmpty()) {
            Toast.makeText(getContext(),
                    "Por favor completa ambos campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> lugar = new HashMap<>();
        lugar.put("nombre", nombre);
        lugar.put("descripcion", desc);

        db.collection("hoteles")
                .document(hotelId)
                .update("lugaresHistoricos", FieldValue.arrayUnion(lugar))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            "Lugar agregado correctamente",
                            Toast.LENGTH_SHORT).show();
                    etNombreLugar.setText("");
                    etDescripcionLugar.setText("");
                    loadLugaresDesdeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error al agregar: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
