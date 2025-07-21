package com.example.telehotel.features.admin.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminLugaresFragment extends Fragment {

    private Toolbar toolbar;
    private TextInputEditText etNombreLugar, etDescripcionLugar, etDistanciaLugar;
    private MaterialButton btnAgregarLugar;
    private LinearLayout containerLugares, emptyStateLugares;

    private FirebaseFirestore db;
    private String hotelId, adminName;

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
        toolbar = view.findViewById(R.id.toolbar_lugares);
        etNombreLugar = view.findViewById(R.id.etNombreLugar);
        etDescripcionLugar = view.findViewById(R.id.etDescripcionLugar);
        etDistanciaLugar = view.findViewById(R.id.etDistanciaLugar);
        btnAgregarLugar = view.findViewById(R.id.btnAgregarLugar);
        containerLugares = view.findViewById(R.id.containerLugares);
        emptyStateLugares = view.findViewById(R.id.emptyStateLugares);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Leer argumentos
        if (getArguments() != null) {
            hotelId = getArguments().getString("hotel_id");
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
                    if (!doc.exists()) {
                        mostrarEmptyState(true);
                        return;
                    }

                    // Vaciar el contenedor
                    containerLugares.removeAllViews();

                    List<Map<String, Object>> lugares =
                            (List<Map<String, Object>>) doc.get("lugaresHistoricos");

                    if (lugares == null || lugares.isEmpty()) {
                        mostrarEmptyState(true);
                        return;
                    }

                    mostrarEmptyState(false);

                    for (Map<String, Object> lugar : lugares) {
                        View item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_lugar, containerLugares, false);

                        TextView tvNombre = item.findViewById(R.id.tvLugarNombre);
                        TextView tvDescripcion = item.findViewById(R.id.tvLugarDescripcion);
                        TextView tvDistancia = item.findViewById(R.id.tvLugarDistancia);
                        ImageView btnDelete = item.findViewById(R.id.btnDeleteLugar);

                        // Llenar datos
                        tvNombre.setText((String) lugar.get("nombre"));
                        tvDescripcion.setText((String) lugar.get("descripcion"));

                        // Manejar distancia
                        Object distanciaObj = lugar.get("distancia");
                        if (distanciaObj != null) {
                            try {
                                double distancia = Double.parseDouble(distanciaObj.toString());
                                tvDistancia.setText(String.format(Locale.getDefault(), "%.1f km", distancia));
                            } catch (NumberFormatException e) {
                                tvDistancia.setText("-- km");
                            }
                        } else {
                            tvDistancia.setText("-- km");
                        }

                        // Configurar botón de eliminar con confirmación
                        btnDelete.setOnClickListener(v -> mostrarConfirmacionEliminacion(lugar));

                        containerLugares.addView(item);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error cargando lugares: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mostrarEmptyState(true);
                });
    }

    private void mostrarEmptyState(boolean mostrar) {
        if (mostrar) {
            containerLugares.setVisibility(View.GONE);
            emptyStateLugares.setVisibility(View.VISIBLE);
        } else {
            containerLugares.setVisibility(View.VISIBLE);
            emptyStateLugares.setVisibility(View.GONE);
        }
    }

    private void mostrarConfirmacionEliminacion(Map<String, Object> lugar) {
        String nombreLugar = (String) lugar.get("nombre");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar lugar");
        builder.setMessage("¿Estás seguro que deseas eliminar \"" + nombreLugar + "\"?");
        builder.setIcon(R.drawable.ic_delete);

        builder.setPositiveButton("Sí, eliminar", (dialog, which) -> {
            eliminarLugar(lugar);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Cambiar colores del texto a negro
        try {
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            TextView messageView = dialog.findViewById(android.R.id.message);

            if (titleView != null) titleView.setTextColor(Color.BLACK);
            if (messageView != null) messageView.setTextColor(Color.BLACK);
        } catch (Exception e) {
            // Ignorar errores de coloración
        }
    }

    private void eliminarLugar(Map<String, Object> lugar) {
        db.collection("hoteles")
                .document(hotelId)
                .update("lugaresHistoricos", FieldValue.arrayRemove(lugar))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            "Lugar eliminado correctamente", Toast.LENGTH_SHORT).show();
                    loadLugaresDesdeFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error al eliminar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void agregarLugar() {
        String nombre = etNombreLugar.getText().toString().trim();
        String descripcion = etDescripcionLugar.getText().toString().trim();
        String distanciaStr = etDistanciaLugar.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            etNombreLugar.setError("El nombre es requerido");
            etNombreLugar.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            etDescripcionLugar.setError("La descripción es requerida");
            etDescripcionLugar.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(distanciaStr)) {
            etDistanciaLugar.setError("La distancia es requerida");
            etDistanciaLugar.requestFocus();
            return;
        }

        // Validar que la distancia sea un número válido
        double distancia;
        try {
            distancia = Double.parseDouble(distanciaStr);
            if (distancia < 0) {
                etDistanciaLugar.setError("La distancia no puede ser negativa");
                etDistanciaLugar.requestFocus();
                return;
            }
            if (distancia > 1000) {
                etDistanciaLugar.setError("La distancia parece muy grande (máx. 1000 km)");
                etDistanciaLugar.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etDistanciaLugar.setError("Ingresa un número válido");
            etDistanciaLugar.requestFocus();
            return;
        }

        // Crear el objeto lugar
        Map<String, Object> lugar = new HashMap<>();
        lugar.put("nombre", nombre);
        lugar.put("descripcion", descripcion);
        lugar.put("distancia", distancia);

        // Deshabilitar botón mientras se procesa
        btnAgregarLugar.setEnabled(false);
        btnAgregarLugar.setText("Agregando...");

        db.collection("hoteles")
                .document(hotelId)
                .update("lugaresHistoricos", FieldValue.arrayUnion(lugar))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            "Lugar agregado correctamente",
                            Toast.LENGTH_SHORT).show();

                    // Limpiar campos
                    etNombreLugar.setText("");
                    etDescripcionLugar.setText("");
                    etDistanciaLugar.setText("");

                    // Quitar errores
                    etNombreLugar.setError(null);
                    etDescripcionLugar.setError(null);
                    etDistanciaLugar.setError(null);

                    loadLugaresDesdeFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error al agregar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> {
                    // Rehabilitar botón
                    btnAgregarLugar.setEnabled(true);
                    btnAgregarLugar.setText("Agregar Lugar");
                });
    }
}