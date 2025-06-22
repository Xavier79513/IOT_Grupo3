package com.example.telehotel.features.admin.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.telehotel.R;
import com.example.telehotel.core.CloudinaryManager;
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.features.admin.ServiciosAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiciosFragment extends Fragment {

    private static final String TAG = "ServiciosFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // Vistas del formulario original
    private TextInputEditText etNombre, etDescripcion, etPrecio;
    private CheckBox cbGratuito;
    private MaterialButton btnRegistrar, btnLimpiar;
    private ProgressBar progressBar;

    // *** NUEVAS VISTAS PARA GESTIÓN DE IMÁGENES CON CLOUDINARY ***
    private MaterialCardView cardImagenServicio;
    private ImageView ivImagenServicio;
    private TextView tvImagenServicio;
    private MaterialButton btnEliminarImagen;
    private ProgressBar progressBarImagen; // Progress específico para la imagen

    // RecyclerView para lista de servicios
    private RecyclerView recyclerServicios;
    private ServiciosAdapter serviciosAdapter;
    private List<Servicio> serviciosList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String hotelId;

    // *** NUEVAS VARIABLES PARA GESTIÓN DE IMÁGENES CON CLOUDINARY ***
    private Uri imagenSeleccionada;
    private String imagenUrlActual; // URL de la imagen ya subida
    private boolean imagenSubiendose = false; // Control de estado de subida
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
        inicializarCloudinary(); // *** NUEVO: Usar CloudinaryManager ***
        setupImagePicker(); // *** NUEVO ***
        setupRecyclerView();
        setupClickListeners();
        obtenerHotelDelAdmin();
    }

    // *** NUEVO MÉTODO: Inicializar Cloudinary como en AdminImagenesFragment ***
    private void inicializarCloudinary() {
        try {
            if (!CloudinaryManager.isInitialized()) {
                Log.d(TAG, "Inicializando Cloudinary...");
                CloudinaryManager.initialize(requireContext());
                Log.d(TAG, "✅ Cloudinary inicializado correctamente");
            } else {
                Log.d(TAG, "✅ Cloudinary ya estaba inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Cloudinary: " + e.getMessage());
            Toast.makeText(getContext(),
                    "Error: No se pudo inicializar el servicio de imágenes.",
                    Toast.LENGTH_LONG).show();
            deshabilitarFuncionalidadImagenes();
        }
    }

    // *** NUEVO MÉTODO: Deshabilitar funcionalidad si Cloudinary falla ***
    private void deshabilitarFuncionalidadImagenes() {
        if (cardImagenServicio != null) {
            cardImagenServicio.setEnabled(false);
            cardImagenServicio.setAlpha(0.5f);
        }
        if (tvImagenServicio != null) {
            tvImagenServicio.setText("Servicio de imágenes no disponible");
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        // Vistas originales
        etNombre = view.findViewById(R.id.etNombre);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etPrecio = view.findViewById(R.id.etPrecio);
        cbGratuito = view.findViewById(R.id.cbGratuito);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);
        btnLimpiar = view.findViewById(R.id.btnLimpiar);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerServicios = view.findViewById(R.id.recyclerServicios);

        // *** NUEVAS VISTAS PARA IMÁGENES ***
        cardImagenServicio = view.findViewById(R.id.cardImagenServicio);
        ivImagenServicio = view.findViewById(R.id.ivImagenServicio);
        tvImagenServicio = view.findViewById(R.id.tvImagenServicio);
        btnEliminarImagen = view.findViewById(R.id.btnEliminarImagen);
        progressBarImagen = view.findViewById(R.id.progressBarImagen);
    }

    // *** NUEVO MÉTODO: Configurar el selector de imágenes ***
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            imagenSeleccionada = selectedImage;
                            subirImagenCloudinary(selectedImage);
                        }
                    }
                }
        );
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

        // *** NUEVOS LISTENERS PARA GESTIÓN DE IMÁGENES ***
        if (cardImagenServicio != null) {
            cardImagenServicio.setOnClickListener(v -> {
                if (!imagenSubiendose) {
                    seleccionarImagen();
                }
            });
        }

        if (btnEliminarImagen != null) {
            btnEliminarImagen.setOnClickListener(v -> eliminarImagenSeleccionada());
        }
    }

    // *** NUEVO MÉTODO: Seleccionar imagen ***
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // *** NUEVO MÉTODO: Subir imagen a Cloudinary (similar a AdminImagenesFragment) ***
    private void subirImagenCloudinary(Uri imageUri) {
        if (imageUri == null || hotelId == null) {
            Toast.makeText(getContext(), "Error: datos inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CloudinaryManager.isInitialized()) {
            Log.w(TAG, "Cloudinary no inicializado, inicializando antes de subir imagen...");
            try {
                CloudinaryManager.initialize(requireContext());
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando Cloudinary antes de subir", e);
                Toast.makeText(getContext(),
                        "Error: No se pudo inicializar el servicio de imágenes",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Mostrar estado de carga
        imagenSubiendose = true;
        mostrarEstadoCargaImagen(true);
        Toast.makeText(getContext(), "📤 Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // Configurar opciones para Cloudinary (similar a AdminImagenesFragment)
        Map<String, Object> options = new HashMap<>();
        options.put("folder", "telehotel/servicios/" + hotelId);
        options.put("resource_type", "image");
        options.put("quality", "auto:good");
        options.put("fetch_format", "auto");

        MediaManager.get().upload(imageUri)
                .unsigned(CloudinaryManager.getUploadPreset())
                .options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Subida iniciada: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Progreso subida imagen: " + progress + "%");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (tvImagenServicio != null) {
                                    tvImagenServicio.setText("Subiendo... " + progress + "%");
                                }
                            });
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        try {
                            String imageUrl = (String) resultData.get("secure_url");
                            if (imageUrl != null) {
                                Log.d(TAG, "Imagen subida exitosamente: " + imageUrl);
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        imagenUrlActual = imageUrl;
                                        mostrarImagenSubida(imageUrl);
                                        imagenSubiendose = false;
                                        mostrarEstadoCargaImagen(false);
                                        Toast.makeText(getContext(), "✅ Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                throw new Exception("URL de imagen no encontrada en respuesta");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando respuesta de Cloudinary", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    imagenSubiendose = false;
                                    mostrarEstadoCargaImagen(false);
                                    Toast.makeText(getContext(), "❌ Error procesando imagen", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Error subiendo imagen: " + error.getDescription());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                imagenSubiendose = false;
                                mostrarEstadoCargaImagen(false);

                                String errorMessage = "❌ Error subiendo imagen";
                                if (error.getDescription() != null) {
                                    errorMessage += ": " + error.getDescription();
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Subida reagendada: " + requestId);
                    }
                })
                .dispatch();
    }

    // *** NUEVO MÉTODO: Mostrar estado de carga de imagen ***
    private void mostrarEstadoCargaImagen(boolean cargando) {
        if (progressBarImagen != null) {
            progressBarImagen.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }

        if (cardImagenServicio != null) {
            cardImagenServicio.setEnabled(!cargando);
            cardImagenServicio.setAlpha(cargando ? 0.7f : 1.0f);
        }
    }

    // *** NUEVO MÉTODO: Mostrar imagen subida ***
    private void mostrarImagenSubida(String imageUrl) {
        if (ivImagenServicio != null && imageUrl != null) {
            try {
                Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_hhotel)
                        .error(R.drawable.ic_hhotel)
                        .into(ivImagenServicio);

                if (tvImagenServicio != null) {
                    tvImagenServicio.setText("✅ Imagen lista");
                }

                if (btnEliminarImagen != null) {
                    btnEliminarImagen.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error mostrando imagen: " + e.getMessage());
                Toast.makeText(getContext(), "Error al mostrar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // *** NUEVO MÉTODO: Eliminar imagen seleccionada ***
    private void eliminarImagenSeleccionada() {
        imagenSeleccionada = null;
        imagenUrlActual = null;

        if (ivImagenServicio != null) {
            ivImagenServicio.setImageResource(R.drawable.ic_hhotel);
        }

        if (tvImagenServicio != null) {
            tvImagenServicio.setText("Toca para agregar imagen del servicio");
        }

        if (btnEliminarImagen != null) {
            btnEliminarImagen.setVisibility(View.GONE);
        }

        Toast.makeText(getContext(), "🗑️ Imagen eliminada", Toast.LENGTH_SHORT).show();
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
                    Log.e(TAG, "Error al obtener hotel del admin: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();
                });
    }

    // *** MÉTODO MODIFICADO: Registrar servicio con imagen de Cloudinary ***
    private void registrarServicio() {
        if (!validarCampos()) {
            return;
        }

        if (imagenSubiendose) {
            Toast.makeText(getContext(), "⏳ Espera a que termine de subirse la imagen", Toast.LENGTH_SHORT).show();
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

        // Verificar que no exista servicio con el mismo nombre
        Double finalPrecio = precio;
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

                    // Guardar servicio (con imagen de Cloudinary si existe)
                    guardarServicio(nombre, descripcion, finalPrecio, imagenUrlActual);
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e(TAG, "Error al verificar servicio: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al verificar servicio", Toast.LENGTH_SHORT).show();
                });
    }

    // *** MÉTODO MODIFICADO: Guardar servicio con URL de Cloudinary ***
    private void guardarServicio(String nombre, String descripcion, Double precio, String imagenUrl) {
        // Crear servicio
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setHotelId(hotelId);
        servicio.setCreadoPor(mAuth.getCurrentUser().getUid());
        servicio.setDisponible(true);
        servicio.setFechaCreacion(com.google.firebase.Timestamp.now());

        // *** AGREGAR URL DE IMAGEN DE CLOUDINARY A LA LISTA ***
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            List<String> imagenes = new ArrayList<>();
            imagenes.add(imagenUrl);
            servicio.setImagenes(imagenes);
            Log.d(TAG, "Servicio creado con imagen de Cloudinary: " + imagenUrl);
        } else {
            Log.d(TAG, "Servicio creado sin imagen");
        }

        // Guardar en Firestore
        db.collection("servicios")
                .add(servicio)
                .addOnSuccessListener(documentReference -> {
                    mostrarProgreso(false);
                    Toast.makeText(getContext(), "✅ Servicio registrado exitosamente", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    cargarServicios(); // Recargar lista
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e(TAG, "Error al registrar servicio: " + e.getMessage());
                    Toast.makeText(getContext(), "❌ Error al registrar servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Log.d(TAG, "Servicios cargados: " + serviciosList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar servicios: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                });
    }

    // *** MÉTODO MODIFICADO: Limpiar formulario incluyendo imagen ***
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

        // *** LIMPIAR IMAGEN Y ESTADO ***
        eliminarImagenSeleccionada();
        imagenSubiendose = false;
        mostrarEstadoCargaImagen(false);
    }

    private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        btnRegistrar.setEnabled(!mostrar && !imagenSubiendose);
        btnRegistrar.setText(mostrar ? "Registrando..." : "Registrar Servicio");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Limpiar referencias para evitar memory leaks
        if (imagePickerLauncher != null) {
            imagePickerLauncher = null;
        }
    }
}