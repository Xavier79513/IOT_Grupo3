package com.example.telehotel.features.admin.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.features.admin.ServiciosAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ServiciosFragment extends Fragment {

    // Vistas del formulario
    private TextInputEditText etNombre, etDescripcion, etPrecio;
    private CheckBox cbGratuito;
    private MaterialButton btnRegistrar, btnLimpiar;
    private ProgressBar progressBar;

    // RecyclerView para lista de servicios
    private RecyclerView recyclerServicios;
    private ServiciosAdapter serviciosAdapter;
    private List<Servicio> serviciosList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String hotelId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        obtenerHotelDelAdmin();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        etNombre = view.findViewById(R.id.etNombre);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etPrecio = view.findViewById(R.id.etPrecio);
        cbGratuito = view.findViewById(R.id.cbGratuito);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerServicios = view.findViewById(R.id.recyclerServicios);
    }

    private void setupRecyclerView() {
        serviciosAdapter = new ServiciosAdapter(serviciosList);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerServicios.setAdapter(serviciosAdapter);
        recyclerServicios.setNestedScrollingEnabled(true);
        recyclerServicios.setHasFixedSize(false);
    }

    private void setupClickListeners() {
        btnRegistrar.setOnClickListener(v -> registrarServicio());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());

        // Listener para checkbox gratuito
        cbGratuito.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPrecio.setEnabled(!isChecked);
            if (isChecked) {
                etPrecio.setText("0");
            } else {
                etPrecio.setText("");
            }
        });
    }

    private void obtenerHotelDelAdmin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hotelId = documentSnapshot.getString("hotelAsignado");
                        if (hotelId != null && !hotelId.isEmpty()) {
                            cargarServicios();
                        } else {
                            Toast.makeText(getContext(), "Error: Administrador sin hotel asignado", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosFragment", "Error al obtener hotel del admin: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                });
    }

    private void registrarServicio() {
        if (!validarCampos()) {
            return;
        }

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Hotel no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarProgreso(true);

        // Obtener datos del formulario
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        Double precio = null;

        // Determinar el precio
        if (!cbGratuito.isChecked()) {
            try {
                precio = Double.parseDouble(etPrecio.getText().toString().trim());
            } catch (NumberFormatException e) {
                precio = 0.0;
            }
        }

        // Crear servicio
        Servicio servicio = new Servicio(nombre, descripcion, precio,
                hotelId, mAuth.getCurrentUser().getUid());

        // Verificar que no exista servicio con el mismo nombre
        db.collection("servicios")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        mostrarProgreso(false);
                        Toast.makeText(getContext(), "Ya existe un servicio con el nombre: " + nombre, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Guardar en Firestore
                    db.collection("servicios")
                            .add(servicio)
                            .addOnSuccessListener(documentReference -> {
                                mostrarProgreso(false);
                                Toast.makeText(getContext(), "Servicio registrado exitosamente", Toast.LENGTH_SHORT).show();
                                limpiarFormulario();
                                cargarServicios(); // Recargar lista
                            })
                            .addOnFailureListener(e -> {
                                mostrarProgreso(false);
                                Log.e("ServiciosFragment", "Error al registrar servicio: " + e.getMessage());
                                Toast.makeText(getContext(), "Error al registrar servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e("ServiciosFragment", "Error al verificar servicio: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al verificar servicio", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validarCampos() {
        // Validar nombre
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre del servicio es requerido");
            etNombre.requestFocus();
            return false;
        }

        String nombre = etNombre.getText().toString().trim();
        if (nombre.length() < 3) {
            etNombre.setError("El nombre debe tener al menos 3 caracteres");
            etNombre.requestFocus();
            return false;
        }

        // Validar descripción
        if (TextUtils.isEmpty(etDescripcion.getText())) {
            etDescripcion.setError("La descripción es requerida");
            etDescripcion.requestFocus();
            return false;
        }

        String descripcion = etDescripcion.getText().toString().trim();
        if (descripcion.length() < 10) {
            etDescripcion.setError("La descripción debe tener al menos 10 caracteres");
            etDescripcion.requestFocus();
            return false;
        }

        // Validar precio si no es gratuito
        if (!cbGratuito.isChecked()) {
            if (TextUtils.isEmpty(etPrecio.getText())) {
                etPrecio.setError("El precio es requerido o marque como gratuito");
                etPrecio.requestFocus();
                return false;
            }

            try {
                double precio = Double.parseDouble(etPrecio.getText().toString().trim());
                if (precio < 0) {
                    etPrecio.setError("El precio no puede ser negativo");
                    etPrecio.requestFocus();
                    return false;
                }
                if (precio > 1000) {
                    etPrecio.setError("El precio no puede exceder S/ 1000");
                    etPrecio.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                etPrecio.setError("Ingrese un precio válido");
                etPrecio.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void cargarServicios() {
        if (hotelId == null || hotelId.isEmpty()) {
            return;
        }

        db.collection("servicios")
                .whereEqualTo("hotelId", hotelId)
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    serviciosList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Servicio servicio = doc.toObject(Servicio.class);
                        if (servicio != null) {
                            servicio.setId(doc.getId());
                            serviciosList.add(servicio);
                        }
                    }

                    serviciosAdapter.notifyDataSetChanged();
                    Log.d("ServiciosFragment", "Servicios cargados: " + serviciosList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosFragment", "Error al cargar servicios: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etDescripcion.setText("");
        etPrecio.setText("");
        cbGratuito.setChecked(false);
        etPrecio.setEnabled(true);

        // Limpiar errores
        etNombre.setError(null);
        etDescripcion.setError(null);
        etPrecio.setError(null);
    }

    private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrar.setEnabled(!mostrar);
        btnRegistrar.setText(mostrar ? "Registrando..." : "Registrar Servicio");
    }
}