package com.example.telehotel.features.admin.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.telehotel.R;
import com.example.telehotel.core.CloudinaryManager;
import com.example.telehotel.core.storage.PrefsManager; // ✅ AGREGAR IMPORT
import com.example.telehotel.features.admin.FullscreenPhotoActivity;
import com.example.telehotel.features.admin.adapters.PhotoAdapter;
import com.example.telehotel.core.utils.LogUtils; // ✅ AGREGAR IMPORT
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminImagenesFragment extends Fragment {

    private static final String TAG = "AdminImagenesFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;

    // UI Components
    private TextView tvTituloHotel, tvContadorImagenes;
    private Button btnAgregarImagen, btnTomarFoto, btnCambiarVista;
    private RecyclerView recyclerImagenes;

    // Data
    private String hotelId;
    private String adminName;
    private String adminId; // ✅ AGREGAR para logs
    private List<String> imagenesUrls;
    private ImagenesAdapter imagenesAdapter;
    private PhotoAdapter photoAdapter;

    // Control de modo de vista
    private boolean isGalleryMode = false;

    // Firebase
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_imagenes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener argumentos y datos del usuario
        if (getArguments() != null) {
            hotelId = getArguments().getString("hotel_id");
            adminName = getArguments().getString("admin_name");
        }

        // ✅ OBTENER ID DEL ADMIN DESDE PREFERENCIAS
        PrefsManager prefsManager = new PrefsManager(requireContext());
        adminId = prefsManager.getUserId();
        if (adminName == null) {
            adminName = prefsManager.getUserName();
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar Cloudinary
        inicializarCloudinary();

        // Inicializar UI
        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        // Cargar imágenes existentes
        loadHotelImages();

        // ✅ LOG: Acceso al módulo de gestión de imágenes
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Accedió al módulo de gestión de imágenes del hotel: " + hotelId + " (Admin: " + adminName + ")"
        );

        Log.d(TAG, "Fragment inicializado para hotel: " + hotelId);
    }

    private void inicializarCloudinary() {
        try {
            if (!CloudinaryManager.isInitialized()) {
                Log.d(TAG, "Inicializando Cloudinary...");
                CloudinaryManager.initialize(requireContext());
                Log.d(TAG, "✅ Cloudinary inicializado correctamente");

                // ✅ LOG: Cloudinary inicializado exitosamente
                LogUtils.logSistema("Cloudinary inicializado correctamente para gestión de imágenes");
            } else {
                Log.d(TAG, "✅ Cloudinary ya estaba inicializado");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Cloudinary: " + e.getMessage());
            Toast.makeText(getContext(),
                    "Error: No se pudo inicializar el servicio de imágenes.",
                    Toast.LENGTH_LONG).show();

            // ✅ LOG: Error inicializando Cloudinary
            LogUtils.logError("Error inicializando Cloudinary: " + e.getMessage(), adminId);

            deshabilitarFuncionalidadImagenes();
        }
    }

    private void deshabilitarFuncionalidadImagenes() {
        if (btnAgregarImagen != null) {
            btnAgregarImagen.setEnabled(false);
            btnAgregarImagen.setText("Servicio no disponible");
        }
        if (btnTomarFoto != null) {
            btnTomarFoto.setEnabled(false);
            btnTomarFoto.setText("Servicio no disponible");
        }
    }

    private void initViews(View view) {
        tvTituloHotel = view.findViewById(R.id.tvTituloHotel);
        tvContadorImagenes = view.findViewById(R.id.tvContadorImagenes);
        btnAgregarImagen = view.findViewById(R.id.btnAgregarImagen);
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        btnCambiarVista = view.findViewById(R.id.btnCambiarVista);
        recyclerImagenes = view.findViewById(R.id.recyclerImagenes);

        tvTituloHotel.setText("📷 Gestión de Imágenes del Hotel");

        // Configurar el botón de cambiar vista
        updateViewModeButton();
    }

    private void setupRecyclerView() {
        imagenesUrls = new ArrayList<>();

        // Crear ambos adapters
        imagenesAdapter = new ImagenesAdapter(imagenesUrls, this::eliminarImagen);
        photoAdapter = new PhotoAdapter(getContext(), imagenesUrls, this::abrirPantallaCompleta);

        // Iniciar en modo administración
        recyclerImagenes.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerImagenes.setAdapter(imagenesAdapter);
    }

    private void setupClickListeners() {
        btnAgregarImagen.setOnClickListener(v -> abrirGaleria());
        btnTomarFoto.setOnClickListener(v -> abrirCamara());
        btnCambiarVista.setOnClickListener(v -> toggleViewMode());
    }

    // Método para abrir pantalla completa
    private void abrirPantallaCompleta(int position, List<String> urls) {
        Intent intent = new Intent(getContext(), FullscreenPhotoActivity.class);
        intent.putStringArrayListExtra("image_urls", new ArrayList<>(urls));
        intent.putExtra("position", position);
        startActivity(intent);

        // ✅ LOG: Visualización de imagen en pantalla completa
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Visualizó imagen en pantalla completa - Hotel: " + hotelId + " - Posición: " + (position + 1)
        );
    }

    // Método para cambiar entre modo administración y modo galería
    private void toggleViewMode() {
        isGalleryMode = !isGalleryMode;

        if (isGalleryMode) {
            // Cambiar a modo galería (solo visualización)
            recyclerImagenes.setLayoutManager(new GridLayoutManager(getContext(), 3));
            recyclerImagenes.setAdapter(photoAdapter);

            // Ocultar botones de administración
            btnAgregarImagen.setVisibility(View.GONE);
            btnTomarFoto.setVisibility(View.GONE);

            // ✅ LOG: Cambio a modo galería
            LogUtils.registrarActividad(
                    LogUtils.ActionType.SYSTEM,
                    adminId,
                    "Cambió a modo galería en gestión de imágenes - Hotel: " + hotelId
            );

        } else {
            // Cambiar a modo administración
            recyclerImagenes.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerImagenes.setAdapter(imagenesAdapter);

            // Mostrar botones de administración
            btnAgregarImagen.setVisibility(View.VISIBLE);
            btnTomarFoto.setVisibility(View.VISIBLE);

            // ✅ LOG: Cambio a modo administración
            LogUtils.registrarActividad(
                    LogUtils.ActionType.SYSTEM,
                    adminId,
                    "Cambió a modo administración en gestión de imágenes - Hotel: " + hotelId
            );
        }

        updateViewModeButton();
    }

    private void updateViewModeButton() {
        if (btnCambiarVista != null) {
            if (isGalleryMode) {
                btnCambiarVista.setText("🔧 Modo Administración");
            } else {
                btnCambiarVista.setText("🖼️ Modo Galería");
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);

        // ✅ LOG: Inicio de selección de imagen desde galería
        LogUtils.registrarActividad(
                LogUtils.ActionType.SYSTEM,
                adminId,
                "Inició selección de imagen desde galería - Hotel: " + hotelId
        );
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

            // ✅ LOG: Solicitud de permisos de cámara
            LogUtils.registrarActividad(
                    LogUtils.ActionType.SYSTEM,
                    adminId,
                    "Solicitó permisos de cámara - Hotel: " + hotelId
            );
            return;
        }
        Toast.makeText(getContext(), "Funcionalidad de cámara en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void loadHotelImages() {
        if (hotelId == null || hotelId.isEmpty()) {
            Log.e(TAG, "Hotel ID no válido");
            return;
        }

        db.collection("hoteles").document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> imagenes = (List<String>) document.get("imagenes");
                        if (imagenes != null) {
                            imagenesUrls.clear();
                            imagenesUrls.addAll(imagenes);

                            // Notificar a ambos adapters
                            imagenesAdapter.notifyDataSetChanged();
                            photoAdapter.notifyDataSetChanged();

                            actualizarContador();

                            // ✅ LOG: Carga exitosa de imágenes
                            LogUtils.registrarActividad(
                                    LogUtils.ActionType.SYSTEM,
                                    adminId,
                                    "Cargó " + imagenes.size() + " imágenes del hotel: " + hotelId
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando imágenes: " + e.getMessage());
                    Toast.makeText(getContext(), "Error cargando imágenes del hotel", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error cargando imágenes
                    LogUtils.logError("Error cargando imágenes del hotel " + hotelId + ": " + e.getMessage(), adminId);
                });
    }

    private void subirImagen(Uri imageUri) {
        if (hotelId == null || imageUri == null) {
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

                // ✅ LOG: Error reinicializando Cloudinary
                LogUtils.logError("Error reinicializando Cloudinary para subir imagen: " + e.getMessage(), adminId);
                return;
            }
        }

        Toast.makeText(getContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // ✅ LOG: Inicio de subida de imagen
        LogUtils.registrarActividad(
                LogUtils.ActionType.CREATE,
                adminId,
                "Inició subida de nueva imagen al hotel: " + hotelId
        );

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "telehotel/hoteles/" + hotelId);
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
                        Log.d(TAG, "Progreso: " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        try {
                            String imageUrl = (String) resultData.get("secure_url");
                            if (imageUrl != null) {
                                Log.d(TAG, "Imagen subida exitosamente: " + imageUrl);

                                // ✅ LOG: Subida exitosa a Cloudinary
                                LogUtils.registrarActividad(
                                        LogUtils.ActionType.CREATE,
                                        adminId,
                                        "Subió imagen exitosamente a Cloudinary - Hotel: " + hotelId + " - URL: " + imageUrl.substring(0, Math.min(50, imageUrl.length())) + "..."
                                );

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> agregarImagenAHotel(imageUrl));
                                }
                            } else {
                                Log.e(TAG, "URL de imagen no encontrada en respuesta");
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Error: URL de imagen no válida", Toast.LENGTH_SHORT).show());
                                }

                                // ✅ LOG: Error URL no válida
                                LogUtils.logError("URL de imagen no válida en respuesta de Cloudinary - Hotel: " + hotelId, adminId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando respuesta de Cloudinary", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Error procesando imagen", Toast.LENGTH_SHORT).show());
                            }

                            // ✅ LOG: Error procesando respuesta
                            LogUtils.logError("Error procesando respuesta de Cloudinary: " + e.getMessage(), adminId);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Error subiendo imagen: " + error.getDescription());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String errorMessage = "Error subiendo imagen";
                                if (error.getDescription() != null) {
                                    errorMessage += ": " + error.getDescription();
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }

                        // ✅ LOG: Error subiendo imagen
                        LogUtils.logError("Error subiendo imagen a Cloudinary - Hotel: " + hotelId + " - Error: " + error.getDescription(), adminId);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Subida reagendada: " + requestId);

                        // ✅ LOG: Subida reagendada
                        LogUtils.registrarActividad(
                                LogUtils.ActionType.SYSTEM,
                                adminId,
                                "Subida de imagen reagendada - Hotel: " + hotelId + " - RequestId: " + requestId
                        );
                    }
                })
                .dispatch();
    }

    private void agregarImagenAHotel(String imageUrl) {
        imagenesUrls.add(imageUrl);

        // Notificar a ambos adapters
        imagenesAdapter.notifyItemInserted(imagenesUrls.size() - 1);
        photoAdapter.notifyItemInserted(imagenesUrls.size() - 1);

        actualizarContador();

        db.collection("hoteles").document(hotelId)
                .update("imagenes", imagenesUrls)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Imagen agregada exitosamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Imagen agregada a hotel: " + imageUrl);

                    // ✅ LOG: Imagen agregada exitosamente al hotel
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.CREATE,
                            adminId,
                            "Agregó imagen exitosamente al hotel: " + hotelId + " - Total de imágenes: " + imagenesUrls.size()
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando hotel: " + e.getMessage());
                    imagenesUrls.remove(imageUrl);

                    // Notificar a ambos adapters del error
                    imagenesAdapter.notifyDataSetChanged();
                    photoAdapter.notifyDataSetChanged();

                    actualizarContador();
                    Toast.makeText(getContext(), "❌ Error guardando imagen", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error guardando imagen en Firestore
                    LogUtils.logError("Error guardando imagen en Firestore - Hotel: " + hotelId + " - Error: " + e.getMessage(), adminId);
                });
    }

    private void eliminarImagen(int position) {
        if (position < 0 || position >= imagenesUrls.size()) {
            return;
        }

        if (imagenesUrls.size() <= 4) {
            Toast.makeText(getContext(), "⚠️ Debe mantener al menos 4 imágenes", Toast.LENGTH_LONG).show();

            // ✅ LOG: Intento de eliminar imagen violando regla mínima
            LogUtils.registrarActividad(
                    LogUtils.ActionType.ERROR,
                    adminId,
                    "Intentó eliminar imagen violando regla mínima (4 imágenes) - Hotel: " + hotelId + " - Imágenes actuales: " + imagenesUrls.size()
            );
            return;
        }

        String imageUrl = imagenesUrls.get(position);
        imagenesUrls.remove(position);

        // Notificar a ambos adapters
        imagenesAdapter.notifyItemRemoved(position);
        photoAdapter.notifyItemRemoved(position);

        actualizarContador();

        db.collection("hoteles").document(hotelId)
                .update("imagenes", imagenesUrls)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "🗑️ Imagen eliminada", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Imagen eliminada: " + imageUrl);

                    // ✅ LOG: Imagen eliminada exitosamente
                    LogUtils.registrarActividad(
                            LogUtils.ActionType.DELETE,
                            adminId,
                            "Eliminó imagen del hotel: " + hotelId + " - Posición: " + (position + 1) + " - Imágenes restantes: " + imagenesUrls.size()
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando imagen: " + e.getMessage());
                    imagenesUrls.add(position, imageUrl);

                    // Restaurar en ambos adapters
                    imagenesAdapter.notifyItemInserted(position);
                    photoAdapter.notifyItemInserted(position);

                    actualizarContador();
                    Toast.makeText(getContext(), "❌ Error eliminando imagen", Toast.LENGTH_SHORT).show();

                    // ✅ LOG: Error eliminando imagen
                    LogUtils.logError("Error eliminando imagen del hotel " + hotelId + ": " + e.getMessage(), adminId);
                });
    }

    private void actualizarContador() {
        int total = imagenesUrls.size();
        String contador = total + " imagen" + (total != 1 ? "es" : "");

        if (total < 4) {
            contador += " (mínimo 4 requeridas)";
            tvContadorImagenes.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvContadorImagenes.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        tvContadorImagenes.setText(contador);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // ✅ LOG: Imagen seleccionada desde galería
                LogUtils.registrarActividad(
                        LogUtils.ActionType.SYSTEM,
                        adminId,
                        "Seleccionó imagen desde galería para hotel: " + hotelId
                );
                subirImagen(selectedImage);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ✅ LOG: Permisos de cámara concedidos
                LogUtils.registrarActividad(
                        LogUtils.ActionType.SYSTEM,
                        adminId,
                        "Concedió permisos de cámara - Hotel: " + hotelId
                );
                abrirCamara();
            } else {
                Toast.makeText(getContext(), "📷 Permiso de cámara requerido", Toast.LENGTH_SHORT).show();

                // ✅ LOG: Permisos de cámara denegados
                LogUtils.registrarActividad(
                        LogUtils.ActionType.ERROR,
                        adminId,
                        "Denegó permisos de cámara - Hotel: " + hotelId
                );
            }
        }
    }

    // ✅ IMPLEMENTACIÓN COMPLETA DEL ADAPTER CON GLIDE
    private static class ImagenesAdapter extends RecyclerView.Adapter<ImagenesAdapter.ImageViewHolder> {
        private List<String> imagenes;
        private OnImageDeleteListener deleteListener;

        interface OnImageDeleteListener {
            void onDelete(int position);
        }

        public ImagenesAdapter(List<String> imagenes, OnImageDeleteListener deleteListener) {
            this.imagenes = imagenes;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.admin_item_imagen_hotel, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imagenes.get(position);
            holder.bind(imageUrl, position, deleteListener);
        }

        @Override
        public int getItemCount() {
            return imagenes.size();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivImagenHotel;
            private ImageButton btnEliminarImagen;
            private ProgressBar progressCarga;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImagenHotel = itemView.findViewById(R.id.ivImagenHotel);
                btnEliminarImagen = itemView.findViewById(R.id.btnEliminarImagen);
                progressCarga = itemView.findViewById(R.id.progressCarga);
            }

            public void bind(String imageUrl, int position, OnImageDeleteListener deleteListener) {
                // Mostrar progress bar mientras carga
                progressCarga.setVisibility(View.VISIBLE);

                // ✅ CARGAR IMAGEN CON GLIDE - IMPLEMENTACIÓN COMPLETA
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Imagen mientras carga
                        .error(R.drawable.error_image) // Imagen si hay error
                        .centerCrop() // Recortar para ajustar
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                                        Object model,
                                                        Target<Drawable> target,
                                                        boolean isFirstResource) {
                                progressCarga.setVisibility(View.GONE);
                                Log.e("ImageViewHolder", "Error cargando imagen: " + imageUrl, e);
                                return false; // Permite que Glide maneje el error
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource,
                                                           Object model,
                                                           Target<Drawable> target,
                                                           DataSource dataSource,
                                                           boolean isFirstResource) {
                                progressCarga.setVisibility(View.GONE);
                                Log.d("ImageViewHolder", "Imagen cargada exitosamente: " + imageUrl);
                                return false; // Permite que Glide maneje el recurso
                            }
                        })
                        .into(ivImagenHotel);

                // ✅ CONFIGURAR BOTÓN ELIMINAR CON CONFIRMACIÓN
                btnEliminarImagen.setOnClickListener(v -> {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("🗑️ Eliminar imagen")
                            .setMessage("¿Estás seguro de que quieres eliminar esta imagen?\n\nEsta acción no se puede deshacer.")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                if (deleteListener != null) {
                                    deleteListener.onDelete(position);
                                }
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });

                // ✅ LONG CLICK PARA OPCIONES ADICIONALES
                ivImagenHotel.setOnLongClickListener(v -> {
                    Toast.makeText(itemView.getContext(),
                            "🔗 URL: " + imageUrl.substring(0, Math.min(50, imageUrl.length())) + "...",
                            Toast.LENGTH_LONG).show();
                    return true;
                });
            }
        }
    }
}