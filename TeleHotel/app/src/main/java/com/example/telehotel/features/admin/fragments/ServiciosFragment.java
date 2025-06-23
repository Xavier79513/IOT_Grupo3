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
import com.example.telehotel.core.storage.PrefsManager; // ✅ AGREGAR IMPORT
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.features.admin.ServiciosAdapter;
import com.example.telehotel.core.utils.LogUtils; // ✅ AGREGAR IMPORT
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

    // ✅ AGREGAR para logs
    private String adminId;
    private String adminName;

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

        // ✅ OBTENER DATOS DEL ADMIN PARA LOGS
        PrefsManager prefsManager = new PrefsManager(requireContext());
        adminId = prefsManager.getUserId();
        adminName = prefsManager.getUserName();

        initFirebase();
        initViews(view);
        inicializarCloudinary(); // *** NUEVO: Usar CloudinaryManager ***
        setupImagePicker(); // *** NUEVO ***
        setupRecyclerView();
        setupClickListeners();
        obtenerHotelDelAdmin();

        // ✅ LOG: Acceso al módulo de servicios
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Accedió al módulo de gestión de servicios (Admin: " + adminName + ")"
        );
    }

    // *** NUEVO MÉTODO: Inicializar Cloudinary como en AdminImagenesFragment ***
    private void inicializarCloudinary() {
        try {
            if (!CloudinaryManager.isInitialized()) {
                Log.d(TAG, "Inicializando Cloudinary...");
                CloudinaryManager.initialize(requireContext());
                Log.d(TAG, "✅ Cloudinary inicializado correctamente");

                // ✅ LOG: Cloudinary inicializado exitosamente
                LogUtils.logSistema("Cloudinary inicializado correctamente para gestión de servicios");
            } else {
                Log.d(TAG, "✅ Cloudinary ya estaba inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Cloudinary: " + e.getMessage());
            Toast.makeText(getContext(),
                    "Error: No se pudo inicializar el servicio de imágenes.",
                    Toast.LENGTH_LONG).show();

            // ✅ LOG: Error inicializando Cloudinary
            LogUtils.logError("Error inicializando Cloudinary para servicios: " + e.getMessage(), adminId);

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

                            // ✅ LOG: Imagen seleccionada para servicio
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Seleccionó imagen para servicio desde galería"
                            );

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

                // ✅ LOG: Marcó servicio como gratuito
                LogUtils.registrarActividad(
                        LogUtils.ActionType.SYSTEM,
                        adminId,
                        "Marcó servicio como gratuito"
                );
            } else {
                etPrecio.setText("");

                // ✅ LOG: Desmarcó servicio gratuito
                LogUtils.registrarActividad(
                        LogUtils.ActionType.SYSTEM,
                        adminId,
                        "Desmarcó opción de servicio gratuito"
                );
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

            // ✅ LOG: Error - datos inválidos para subir imagen
            LogUtils.logError("Datos inválidos para subir imagen de servicio - Hotel: " + hotelId, adminId);
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

                // ✅ LOG: Error reinicializando Cloudinary
                LogUtils.logError("Error reinicializando Cloudinary para subir imagen de servicio: " + e.getMessage(), adminId);
                return;
            }
        }

        // Mostrar estado de carga
        imagenSubiendose = true;
        mostrarEstadoCargaImagen(true);
        Toast.makeText(getContext(), "📤 Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // ✅ LOG: Inicio de subida de imagen
        LogUtils.registrarActividad(
                LogUtils.ActionType.CREATE,
                adminId,
                "Inició subida de imagen para servicio - Hotel: " + hotelId
        );

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

                                // ✅ LOG: Imagen subida exitosamente
                                LogUtils.registrarActividad(
                                        LogUtils.ActionType.CREATE,
                                        adminId,
                                        "Subió imagen de servicio exitosamente a Cloudinary - Hotel: " + hotelId +
                                                " - URL: " + imageUrl.substring(0, Math.min(50, imageUrl.length())) + "..."
                                );

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

                            // ✅ LOG: Error procesando respuesta
                            LogUtils.logError("Error procesando respuesta de Cloudinary para imagen de servicio: " + e.getMessage(), adminId);
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

                        // ✅ LOG: Error subiendo imagen
                        LogUtils.logError("Error subiendo imagen de servicio a Cloudinary - Hotel: " + hotelId + " - Error: " + error.getDescription(), adminId);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Subida reagendada: " + requestId);

                        // ✅ LOG: Subida reagendada
                        LogUtils.registrarActividad(
                                LogUtils.ActionType.SYSTEM,
                                adminId,
                                "Subida de imagen de servicio reagendada - Hotel: " + hotelId + " - RequestId: " + requestId
                        );
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

                // ✅ LOG: Error mostrando imagen
                LogUtils.logError("Error mostrando imagen de servicio: " + e.getMessage(), adminId);
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

        // ✅ LOG: Imagen eliminada del formulario
        LogUtils.registrarActividad(
                LogUtils.ActionType.DELETE,
                adminId,
                "Eliminó imagen seleccionada del formulario de servicio"
        );
    }

    private void obtenerHotelDelAdmin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - usuario no autenticado
            LogUtils.logError("Usuario no autenticado al acceder a gestión de servicios", adminId);
            return;
        }

        db.collection("usuarios").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hotelId = documentSnapshot.getString("hotelAsignado");
                        if (hotelId != null && !hotelId.isEmpty()) {
                            // ✅ LOG: Hotel asignado identificado
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Hotel asignado identificado para gestión de servicios: " + hotelId
                            );
                            cargarServicios();
                        } else {
                            Toast.makeText(getContext(), "Error: Administrador sin hotel asignado", Toast.LENGTH_LONG).show();

                            // ✅ LOG: Error - admin sin hotel asignado
                            LogUtils.logError("Administrador sin hotel asignado al acceder a gestión de servicios", adminId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener hotel del admin: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar datos del administrador", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al obtener datos del admin
                    LogUtils.logError("Error al obtener hotel del admin en gestión de servicios: " + e.getMessage(), adminId);
                });
    }

    // *** MÉTODO MODIFICADO: Registrar servicio con imagen de Cloudinary ***
    private void registrarServicio() {
        if (!validarCampos()) {
            return;
        }

        if (imagenSubiendose) {
            Toast.makeText(getContext(), "⏳ Espera a que termine de subirse la imagen", Toast.LENGTH_SHORT).show();

            // ✅ LOG: Intentó registrar mientras se sube imagen
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó registrar servicio mientras se sube la imagen"
            );
            return;
        }

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Hotel no identificado", Toast.LENGTH_SHORT).show();

            // ✅ LOG: Error - hotel no identificado
            LogUtils.logError("Intentó registrar servicio sin hotel identificado", adminId);
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

        // ✅ LOG: Inicio del proceso de registro
        LogUtils.registrarActividad(
                LogUtils.ActionType.CREATE,
                adminId,
                "Inició registro de servicio - Hotel: " + hotelId +
                        " - Nombre: " + nombre + " - Precio: " + (precio != null ? "S/" + precio : "Gratuito") +
                        " - Con imagen: " + (imagenUrlActual != null ? "Sí" : "No")
        );

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

                        // ✅ LOG: Error - servicio duplicado
                        LogUtils.registrarActividad(
                                LogUtils.ActionType.ERROR,
                                adminId,
                                "Intentó registrar servicio con nombre duplicado: " + nombre + " en hotel: " + hotelId
                        );
                        return;
                    }

                    // Guardar servicio (con imagen de Cloudinary si existe)
                    guardarServicio(nombre, descripcion, finalPrecio, imagenUrlActual);
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e(TAG, "Error al verificar servicio: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al verificar servicio", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al verificar servicio
                    LogUtils.logError("Error al verificar servicio duplicado " + nombre + " en hotel " + hotelId + ": " + e.getMessage(), adminId);
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

                    // ✅ LOG: Servicio registrado exitosamente
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.CREATE,
                            adminId,
                            "Registró servicio exitosamente - ID: " + documentReference.getId() +
                                    " - Hotel: " + hotelId + " - Nombre: " + nombre +
                                    " - Precio: " + (precio != null ? "S/" + precio : "Gratuito") +
                                    " - Con imagen: " + (imagenUrl != null ? "Sí" : "No")
                    );

                    limpiarFormulario();
                    cargarServicios(); // Recargar lista
                })
                .addOnFailureListener(e -> {
                    mostrarProgreso(false);
                    Log.e(TAG, "Error al registrar servicio: " + e.getMessage());
                    Toast.makeText(getContext(), "❌ Error al registrar servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // ✅ LOG: Error al registrar servicio
                    LogUtils.logError("Error al registrar servicio " + nombre + " en hotel " + hotelId + ": " + e.getMessage(), adminId);
                });
    }

    private boolean validarCampos() {
        List<String> errores = new ArrayList<>();

        // Validar nombre
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre del servicio es requerido");
            etNombre.requestFocus();
            errores.add("nombre vacío");
        } else {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.length() < 3) {
                etNombre.setError("El nombre debe tener al menos 3 caracteres");
                etNombre.requestFocus();
                errores.add("nombre muy corto");
            }
        }

        // Validar descripción
        if (TextUtils.isEmpty(etDescripcion.getText())) {
            etDescripcion.setError("La descripción es requerida");
            etDescripcion.requestFocus();
            errores.add("descripción vacía");
        } else {
            String descripcion = etDescripcion.getText().toString().trim();
            if (descripcion.length() < 10) {
                etDescripcion.setError("La descripción debe tener al menos 10 caracteres");
                etDescripcion.requestFocus();
                errores.add("descripción muy corta");
            }
        }

        // Validar precio si no es gratuito
        if (!cbGratuito.isChecked()) {
            if (TextUtils.isEmpty(etPrecio.getText())) {
                etPrecio.setError("El precio es requerido o marque como gratuito");
                etPrecio.requestFocus();
                errores.add("precio vacío");
            } else {
                try {
                    double precio = Double.parseDouble(etPrecio.getText().toString().trim());
                    if (precio < 0) {
                        etPrecio.setError("El precio no puede ser negativo");
                        etPrecio.requestFocus();
                        errores.add("precio negativo");
                    }
                    if (precio > 1000) {
                        etPrecio.setError("El precio no puede exceder S/ 1000");
                        etPrecio.requestFocus();
                        errores.add("precio excesivo");
                    }
                } catch (NumberFormatException e) {
                    etPrecio.setError("Ingrese un precio válido");
                    etPrecio.requestFocus();
                    errores.add("precio inválido");
                }
            }
        }

        // ✅ LOG: Errores de validación si existen
        if (!errores.isEmpty()) {
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Errores de validación al registrar servicio: " + String.join(", ", errores)
            );
        }

        return errores.isEmpty();
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

                    // ✅ LOG: Servicios cargados exitosamente
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.SYSTEM,
                            adminId,
                            "Cargó lista de servicios - Hotel: " + hotelId + " - Total: " + serviciosList.size() + " servicios"
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar servicios: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar servicios", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error al cargar servicios
                    LogUtils.logError("Error al cargar servicios del hotel " + hotelId + ": " + e.getMessage(), adminId);
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

        // ✅ LOG: Formulario limpiado
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Limpió formulario de registro de servicios"
        );
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