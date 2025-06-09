package com.example.telehotel.features.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.databinding.ActivityHabitacionesBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HabitacionesActivity extends AppCompatActivity {
    private ActivityHabitacionesBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private static final int PICK_IMAGE = 100;
    private Uri selectedImageUri;
    private String hotelId = "0T3ZhLGp6fEOirFeqAUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar + Drawer
        setSupportActionBar(binding.topAppBar);
        drawerToggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.topAppBar,
                R.string.drawer_open, R.string.drawer_close
        );
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Botón “Agregar Habitación”
        binding.btnAgregarHabitacion.setOnClickListener(v -> mostrarDialogoNuevaHabitacion());

        // Cargar lista
        cargarHabitaciones();
    }

    private void mostrarDialogoNuevaHabitacion() {
        View view = getLayoutInflater().inflate(R.layout.dialog_nueva_habitacion, null);

        Spinner spinnerTipo             = view.findViewById(R.id.spinnerTipo);
        NumberPicker npAdultos          = view.findViewById(R.id.npAdultos);
        NumberPicker npNinos            = view.findViewById(R.id.npNinos);
        TextInputEditText etTamano      = view.findViewById(R.id.etTamano);
        TextInputEditText etDescripcion = view.findViewById(R.id.etDescripcion);
        ImageView ivPreview             = view.findViewById(R.id.ivPreview);
        Button btnSelectImg             = view.findViewById(R.id.btnSelectImage);

        // Configurar rangos en código
        npAdultos.setMinValue(1);
        npAdultos.setMaxValue(10);
        npNinos.setMinValue(0);
        npNinos.setMaxValue(10);

        btnSelectImg.setOnClickListener(v -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            startActivityForResult(pick, PICK_IMAGE);
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nueva Habitación")
                .setView(view)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String tipo = spinnerTipo.getSelectedItem().toString();
                    int adultos = npAdultos.getValue();
                    int ninos   = npNinos.getValue();
                    String tamanoStr      = etTamano.getText().toString().trim();
                    String descripcionStr = etDescripcion.getText().toString().trim();

                    if (tamanoStr.isEmpty()) {
                        Toast.makeText(this, "Ingresa el tamaño en m²", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newId = UUID.randomUUID().toString();
                    if (selectedImageUri != null) {
                        subirImagenYCrearDocumento(
                                tipo, descripcionStr, tamanoStr,
                                adultos, ninos, newId
                        );
                    } else {
                        crearDocumentoHabitacion(
                                tipo, descripcionStr, tamanoStr,
                                adultos, ninos, null, newId
                        );
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }

    private void subirImagenYCrearDocumento(
            String tipo, String descripcion, String tamano,
            int adultos, int ninos, String habitacionId
    ) {
        StorageReference ref = FirebaseStorage
                .getInstance()
                .getReference("hoteles/" + hotelId + "/habitaciones/" + habitacionId + ".jpg");

        ref.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri ->
                        crearDocumentoHabitacion(
                                tipo, descripcion, tamano,
                                adultos, ninos, uri.toString(), habitacionId
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void crearDocumentoHabitacion(
            String tipo, String descripcion, String tamanoStr,
            int adultos, int ninos, @Nullable String imageUrl,
            String habitacionId
    ) {
        Map<String,Object> data = new HashMap<>();
        data.put("tipo", tipo);
        data.put("descripcion", descripcion);
        data.put("tamaño", Integer.parseInt(tamanoStr));
        data.put("estado", "disponible");

        Map<String,Object> capacidad = new HashMap<>();
        capacidad.put("adulto", adultos);
        capacidad.put("ninos", ninos);
        data.put("capacidad", capacidad);

        if (imageUrl != null) data.put("imageUrl", imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones_hotel")
                .document(habitacionId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Habitación creada", Toast.LENGTH_SHORT).show();
                    cargarHabitaciones();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void cargarHabitaciones() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones_hotel")
                .get()
                .addOnSuccessListener(query -> {
                    LinearLayout container = binding.llHabitacionesContainer;
                    container.removeAllViews();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        View item = getLayoutInflater()
                                .inflate(R.layout.item_habitacion, container, false);

                        ImageView ivFoto       = item.findViewById(R.id.ivFoto);
                        TextView tvTitulo      = item.findViewById(R.id.tvTitulo);
                        TextView tvEstado      = item.findViewById(R.id.tvEstado);
                        TextView tvPrecio      = item.findViewById(R.id.tvPrecio);
                        TextView tvCapacidad   = item.findViewById(R.id.tvCapacidad);
                        TextView tvTamano      = item.findViewById(R.id.tvTamano);
                        TextView tvInformacion = item.findViewById(R.id.tvInformacion);

                        tvTitulo.setText(doc.getString("tipo"));
                        tvEstado.setText("Estado: " + doc.getString("estado"));
                        Long price = doc.getLong("precio");
                        if (price != null) tvPrecio.setText("Precio: S/ " + price);
                        Long size = doc.getLong("tamaño");
                        if (size != null) tvTamano.setText("Tamaño: " + size + " m²");
                        tvInformacion.setText(doc.getString("descripcion"));

                        @SuppressWarnings("unchecked")
                        Map<String,Object> cap = (Map<String,Object>) doc.get("capacidad");
                        if (cap != null) {
                            Number ad = (Number) cap.get("adulto");
                            Number ni = (Number) cap.get("ninos");
                            tvCapacidad.setText(
                                    "Capacidad: " +
                                            (ad != null ? ad.intValue() + " adultos" : "N/D") +
                                            ", " +
                                            (ni != null ? ni.intValue() + " niños" : "N/D")
                            );
                        }

                        String url = doc.getString("imageUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this).load(url).into(ivFoto);
                        }

                        container.addView(item);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
