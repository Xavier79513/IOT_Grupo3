package com.example.telehotel.features.auth;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.telehotel.R;
import com.example.telehotel.core.CloudinaryManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

// Importaciones para Cloudinary
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterTaxiActivity extends AppCompatActivity {

    // Campos básicos del usuario
    private TextInputEditText etNombre, etApellido, etDni, etFechaNacimiento,
            etEmail, etTelefono, etDomicilio, etPassword, etConfirmPassword;

    // Campos específicos del taxista
    private TextInputEditText etPlacaAuto;
    private ImageView ivFotoTaxista, ivFotoAuto;
    private MaterialButton btnSeleccionarFotoTaxista, btnSeleccionarFotoAuto;

    // Elementos de UI
    private ProgressBar pbPasswordStrength;
    private TextView tvPasswordStrength, tvLogin;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;

    // Firebase
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    // Variables para manejo de fotos
    private Uri fotoTaxistaUri;
    private Uri fotoAutoUri;
    private String fotoTaxistaUrl = ""; // URL de Cloudinary
    private String fotoAutoUrl = "";    // URL de Cloudinary
    private boolean fotoTaxistaSubida = false;
    private boolean fotoAutoSubida = false;

    private static final int REQUEST_FOTO_TAXISTA = 100;
    private static final int REQUEST_FOTO_AUTO = 101;
    private static final int REQUEST_CAMERA_TAXISTA = 102;
    private static final int REQUEST_CAMERA_AUTO = 103;

    // ✅ CÓDIGOS PARA PERMISOS
    private static final int PERMISSION_CAMERA_TAXISTA = 200;
    private static final int PERMISSION_CAMERA_AUTO = 201;
    private static final int PERMISSION_STORAGE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_taxi);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar Cloudinary
        CloudinaryManager.initialize(this);

        // Inicializar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta de taxista...");
        progressDialog.setCancelable(false);

        // Referencias a las vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        // Campos básicos
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etDomicilio = findViewById(R.id.etDomicilio);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Campos específicos del taxista
        etPlacaAuto = findViewById(R.id.etPlacaAuto);
        ivFotoTaxista = findViewById(R.id.ivFotoTaxista);
        ivFotoAuto = findViewById(R.id.ivFotoAuto);
        btnSeleccionarFotoTaxista = findViewById(R.id.btnSeleccionarFotoTaxista);
        btnSeleccionarFotoAuto = findViewById(R.id.btnSeleccionarFotoAuto);

        // Elementos de UI
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        // Listener para evaluar fuerza de contraseña
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    evaluarFortaleza(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Validador de placa en tiempo real
        etPlacaAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarPlaca(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Habilitar botón solo si se aceptan los términos
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked && validarCamposBasico());
        });

        // Listener del botón de registro
        btnRegister.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarTaxistaFirebase();
            }
        });

        // Listener para ir al login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterTaxiActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // DatePicker para fecha de nacimiento
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        etFechaNacimiento.setFocusable(false);
        etFechaNacimiento.setClickable(true);

        // ✅ FUNCIONALIDAD COMPLETA PARA FOTOS
        btnSeleccionarFotoTaxista.setOnClickListener(v -> mostrarOpcionesFoto(true));
        btnSeleccionarFotoAuto.setOnClickListener(v -> mostrarOpcionesFoto(false));
    }

    // ✅ MÉTODO PARA MOSTRAR OPCIONES DE FOTO (GALERÍA O CÁMARA)
    private void mostrarOpcionesFoto(boolean esFotoTaxista) {
        String titulo = esFotoTaxista ? "Foto del Taxista" : "Foto del Auto";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar " + titulo)
                .setItems(new String[]{"📷 Tomar foto", "🖼️ Elegir de galería"},
                        (dialog, which) -> {
                            if (which == 0) {
                                abrirCamara(esFotoTaxista);
                            } else {
                                abrirGaleria(esFotoTaxista);
                            }
                        })
                .show();
    }

    // ✅ ABRIR CÁMARA CON VERIFICACIÓN DE PERMISOS
    private void abrirCamara(boolean esFotoTaxista) {
        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // Solicitar permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    esFotoTaxista ? PERMISSION_CAMERA_TAXISTA : PERMISSION_CAMERA_AUTO);
            return;
        }

        // Si ya tiene permiso, abrir cámara
        abrirCamaraDirectamente(esFotoTaxista);
    }

    // ✅ ABRIR CÁMARA DIRECTAMENTE (SIN VERIFICACIÓN DE PERMISOS)
    private void abrirCamaraDirectamente(boolean esFotoTaxista) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent,
                    esFotoTaxista ? REQUEST_CAMERA_TAXISTA : REQUEST_CAMERA_AUTO);
        } else {
            Toast.makeText(this, "No se puede acceder a la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ ABRIR GALERÍA CON VERIFICACIÓN DE PERMISOS
    private void abrirGaleria(boolean esFotoTaxista) {
        // Verificar permiso de almacenamiento (Android 13+ no lo necesita para READ_MEDIA_IMAGES)
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE);
                return;
            }
        }

        // Si ya tiene permiso, abrir galería
        abrirGaleriaDirectamente(esFotoTaxista);
    }

    // ✅ ABRIR GALERÍA DIRECTAMENTE
    private void abrirGaleriaDirectamente(boolean esFotoTaxista) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent,
                esFotoTaxista ? REQUEST_FOTO_TAXISTA : REQUEST_FOTO_AUTO);
    }

    // ✅ MANEJAR RESULTADO DE SELECCIÓN DE FOTOS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        try {
            switch (requestCode) {
                case REQUEST_FOTO_TAXISTA:
                    if (data != null && data.getData() != null) {
                        fotoTaxistaUri = data.getData();
                        ivFotoTaxista.setImageURI(fotoTaxistaUri);
                        subirFotoCloudinary(fotoTaxistaUri, true);
                    }
                    break;

                case REQUEST_FOTO_AUTO:
                    if (data != null && data.getData() != null) {
                        fotoAutoUri = data.getData();
                        ivFotoAuto.setImageURI(fotoAutoUri);
                        subirFotoCloudinary(fotoAutoUri, false);
                    }
                    break;

                case REQUEST_CAMERA_TAXISTA:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        ivFotoTaxista.setImageBitmap(bitmap);
                        subirBitmapCloudinary(bitmap, true);
                    }
                    break;

                case REQUEST_CAMERA_AUTO:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        ivFotoAuto.setImageBitmap(bitmap);
                        subirBitmapCloudinary(bitmap, false);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("RegisterTaxi", "Error procesando imagen", e);
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ SUBIR FOTO DESDE URI A CLOUDINARY
    private void subirFotoCloudinary(Uri imageUri, boolean esFotoTaxista) {
        if (!CloudinaryManager.isInitialized()) {
            Toast.makeText(this, "Error: Cloudinary no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        String folder = esFotoTaxista ? "taxistas" : "autos";
        String fileName = System.currentTimeMillis() + "_" + etDni.getText().toString().trim();

        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Subiendo " + (esFotoTaxista ? "foto del taxista..." : "foto del auto..."));
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        try {
            // ✅ OPCIÓN A: Upload unsigned con preset por defecto
            MediaManager.get().upload(imageUri)
                    .unsigned("ml_default")
                    .option("folder", folder)
                    .option("public_id", fileName)
                    .option("resource_type", "image")

                    // ✅ OPCIÓN B: Upload con firma (más seguro)
                    /*
                    MediaManager.get().upload(imageUri)
                            .option("folder", folder)
                            .option("public_id", fileName)
                            .option("resource_type", "image")
                    */
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("Cloudinary", "Iniciando subida: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Opcional: mostrar progreso
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadDialog.dismiss();
                            String url = (String) resultData.get("secure_url");

                            if (esFotoTaxista) {
                                fotoTaxistaUrl = url;
                                fotoTaxistaSubida = true;
                                actualizarBotonFoto(btnSeleccionarFotoTaxista, true);
                            } else {
                                fotoAutoUrl = url;
                                fotoAutoSubida = true;
                                actualizarBotonFoto(btnSeleccionarFotoAuto, true);
                            }

                            Log.d("Cloudinary", "✅ Foto subida exitosamente: " + url);
                            Toast.makeText(RegisterTaxiActivity.this,
                                    "✅ " + (esFotoTaxista ? "Foto del taxista" : "Foto del auto") + " subida correctamente",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            uploadDialog.dismiss();
                            Log.e("Cloudinary", "❌ Error subiendo foto: " + error.getDescription());
                            Toast.makeText(RegisterTaxiActivity.this,
                                    "❌ Error subiendo " + (esFotoTaxista ? "foto del taxista" : "foto del auto"),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w("Cloudinary", "⏳ Reintentando subida: " + requestId);
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            uploadDialog.dismiss();
            Log.e("Cloudinary", "❌ Excepción al subir foto", e);
            Toast.makeText(this, "Error inesperado al subir la foto", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ SUBIR BITMAP DESDE CÁMARA A CLOUDINARY
    private void subirBitmapCloudinary(Bitmap bitmap, boolean esFotoTaxista) {
        if (!CloudinaryManager.isInitialized()) {
            Toast.makeText(this, "Error: Cloudinary no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        String folder = esFotoTaxista ? "taxistas" : "autos";
        String fileName = System.currentTimeMillis() + "_" + etDni.getText().toString().trim();

        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Subiendo " + (esFotoTaxista ? "foto del taxista..." : "foto del auto..."));
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        try {
            // Convertir Bitmap a bytes array
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // 80% de calidad
            byte[] byteArray = stream.toByteArray();

            // ✅ OPCIÓN A: Upload unsigned con preset por defecto
            MediaManager.get().upload(byteArray)
                    .unsigned("ml_default")
                    .option("folder", folder)
                    .option("public_id", fileName)
                    .option("resource_type", "image")

                    // ✅ OPCIÓN B: Upload con firma (más seguro)
                    /*
                    MediaManager.get().upload(byteArray)
                            .option("folder", folder)
                            .option("public_id", fileName)
                            .option("resource_type", "image")
                    */
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("Cloudinary", "Iniciando subida bitmap: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Opcional: mostrar progreso
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadDialog.dismiss();
                            String url = (String) resultData.get("secure_url");

                            if (esFotoTaxista) {
                                fotoTaxistaUrl = url;
                                fotoTaxistaSubida = true;
                                actualizarBotonFoto(btnSeleccionarFotoTaxista, true);
                            } else {
                                fotoAutoUrl = url;
                                fotoAutoSubida = true;
                                actualizarBotonFoto(btnSeleccionarFotoAuto, true);
                            }

                            Log.d("Cloudinary", "✅ Bitmap subido exitosamente: " + url);
                            Toast.makeText(RegisterTaxiActivity.this,
                                    "✅ " + (esFotoTaxista ? "Foto del taxista" : "Foto del auto") + " subida correctamente",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            uploadDialog.dismiss();
                            Log.e("Cloudinary", "❌ Error subiendo bitmap: " + error.getDescription());
                            Toast.makeText(RegisterTaxiActivity.this,
                                    "❌ Error subiendo " + (esFotoTaxista ? "foto del taxista" : "foto del auto"),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w("Cloudinary", "⏳ Reintentando subida bitmap: " + requestId);
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            uploadDialog.dismiss();
            Log.e("Cloudinary", "❌ Excepción al subir bitmap", e);
            Toast.makeText(this, "Error inesperado al subir la foto", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ ACTUALIZAR APARIENCIA DEL BOTÓN DESPUÉS DE SUBIR FOTO
    private void actualizarBotonFoto(MaterialButton boton, boolean subida) {
        if (subida) {
            boton.setText("✅ Foto subida");
            boton.setIconResource(android.R.drawable.ic_menu_gallery);
            boton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_green_light)));
        } else {
            if (boton == btnSeleccionarFotoTaxista) {
                boton.setText("Seleccionar Foto");
            } else {
                boton.setText("Seleccionar Foto del Auto");
            }
            boton.setIconResource(R.drawable.ic_camera);
        }
    }

    private void validarPlaca(String placa) {
        // Formato peruano: ABC-123 o ABC-1234
        String patron = "^[A-Z]{3}-[0-9]{3,4}$";

        if (placa.length() >= 7 && placa.matches(patron)) {
            etPlacaAuto.setError(null);
        } else if (!placa.isEmpty()) {
            etPlacaAuto.setError("Formato: ABC-123 o ABC-1234");
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        // Fecha por defecto: hace 25 años (edad razonable para un taxista)
        calendar.add(Calendar.YEAR, -25);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear DatePickerDialog con estilo personalizado
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerTheme, // Usar el tema personalizado
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fecha = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFechaNacimiento.setText(fecha);
                }, year, month, day);

        // Fecha máxima: hace 18 años (debe ser mayor de edad)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // Fecha mínima: hace 80 años (edad máxima razonable)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -80);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        // Forzar colores después de mostrar
        datePickerDialog.setOnShowListener(dialog -> {
            try {
                // Cambiar color del título
                int titleId = getResources().getIdentifier("date_picker_header_title", "id", "android");
                if (titleId != 0) {
                    TextView title = datePickerDialog.findViewById(titleId);
                    if (title != null) {
                        title.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                }

                // Cambiar color de los números
                DatePicker datePicker = datePickerDialog.getDatePicker();
                setDatePickerTextColor(datePicker);
            } catch (Exception e) {
                Log.e("DatePicker", "Error aplicando colores", e);
            }
        });

        datePickerDialog.show();
    }

    // Método recursivo para cambiar colores de texto
    private void setDatePickerTextColor(ViewGroup viewGroup) {
        if (viewGroup == null) return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                textView.setTextColor(ContextCompat.getColor(this, R.color.black));
                textView.setAlpha(1.0f); // Asegurar que no esté transparente
            } else if (child instanceof ViewGroup) {
                setDatePickerTextColor((ViewGroup) child);
            }
        }
    }

    private void registrarTaxistaFirebase() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();

        // Crear usuario con email y contraseña
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Actualizar perfil del usuario
                            actualizarPerfilUsuario(user);

                            // Enviar email de verificación
                            enviarEmailVerificacion(user);

                            // Guardar datos del taxista en Firestore
                            guardarDatosTaxistaFirestore(user.getUid());
                        }
                    } else {
                        progressDialog.dismiss();
                        manejarErrorRegistro(task.getException());
                    }
                });
    }

    private void actualizarPerfilUsuario(FirebaseUser user) {
        String nombreCompleto = etNombre.getText().toString().trim() + " " +
                etApellido.getText().toString().trim();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nombreCompleto)
                .build();

        user.updateProfile(profileUpdates);
    }

    private void enviarEmailVerificacion(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        mostrarDialogoAprobacion();
                    } else {
                        Toast.makeText(RegisterTaxiActivity.this,
                                "Error al enviar email de verificación",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarDatosTaxistaFirestore(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> taxista = new HashMap<>();
        // Datos básicos
        taxista.put("uid", uid);
        taxista.put("nombres", etNombre.getText().toString().trim());
        taxista.put("apellidos", etApellido.getText().toString().trim());
        taxista.put("tipoDocumento", "DNI");
        taxista.put("numeroDocumento", etDni.getText().toString().trim());
        taxista.put("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
        taxista.put("correo", etEmail.getText().toString().trim());
        taxista.put("email", etEmail.getText().toString().trim());
        taxista.put("telefono", etTelefono.getText().toString().trim());
        taxista.put("direccion", etDomicilio.getText().toString().trim());

        // Datos específicos del taxista
        taxista.put("placaAuto", etPlacaAuto.getText().toString().trim().toUpperCase());
        taxista.put("role", "taxista");
        taxista.put("estado", "pendiente"); // Pendiente de aprobación por superadmin

        // Datos iniciales del taxista
        taxista.put("viajesRealizadosHoy", 0);
        taxista.put("ingresosDiarios", 0.0);
        taxista.put("reputacion", 5.0); // Reputación inicial

        // Fechas
        taxista.put("fechaRegistro", new Date());

        // ✅ URLs de fotos subidas a Cloudinary
        taxista.put("fotoUrl", fotoTaxistaUrl); // URL de la foto del taxista
        taxista.put("fotoAuto", fotoAutoUrl);   // URL de la foto del auto

        db.collection("usuarios").document(uid)
                .set(taxista)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Datos del taxista guardados exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error al guardar datos del taxista", e);
                });
    }

    private void mostrarDialogoAprobacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registro de Taxista")
                .setMessage("Tu solicitud de registro como taxista ha sido enviada exitosamente.\n\n" +
                        "✅ Se ha enviado un email de verificación a " + etEmail.getText().toString() + "\n\n" +
                        "⏳ Tu cuenta será revisada y aprobada por el administrador del sistema.\n\n" +
                        "📧 Recibirás una notificación una vez que tu cuenta sea activada.")
                .setIcon(R.drawable.ic_taxi)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    // Regresar al login
                    Intent intent = new Intent(RegisterTaxiActivity.this, LoginActivity.class);
                    intent.putExtra("email", etEmail.getText().toString());
                    intent.putExtra("mensaje", "Registro de taxista completado. Espera la aprobación.");
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void manejarErrorRegistro(Exception exception) {
        String mensaje = "Error al crear la cuenta de taxista";

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            mensaje = "La contraseña es muy débil";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            mensaje = "El email no es válido";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            mensaje = "Ya existe una cuenta con este email";
        } else if (exception != null) {
            mensaje = exception.getLocalizedMessage();
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void evaluarFortaleza(String password) {
        int fuerza = 0;
        if (password.length() >= 8) fuerza += 25;
        if (password.length() >= 12) fuerza += 10;
        if (password.matches(".*[A-Z].*")) fuerza += 20;
        if (password.matches(".*[a-z].*")) fuerza += 10;
        if (password.matches(".*[0-9].*")) fuerza += 15;
        if (password.matches(".*[!@#\\$%\\^&\\*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\?].*")) fuerza += 20;

        pbPasswordStrength.setProgress(fuerza);

        if (fuerza < 40) {
            tvPasswordStrength.setText("Débil");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)));
        } else if (fuerza < 70) {
            tvPasswordStrength.setText("Media");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark)));
        } else {
            tvPasswordStrength.setText("Fuerte");
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            pbPasswordStrength.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)));
        }

        // Actualizar estado del botón
        btnRegister.setEnabled(cbTerms.isChecked() && validarCamposBasico());
    }

    private boolean validarCamposBasico() {
        return !etNombre.getText().toString().trim().isEmpty() &&
                !etApellido.getText().toString().trim().isEmpty() &&
                !etDni.getText().toString().trim().isEmpty() &&
                !etFechaNacimiento.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etTelefono.getText().toString().trim().isEmpty() &&
                !etDomicilio.getText().toString().trim().isEmpty() &&
                !etPlacaAuto.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().trim().isEmpty() &&
                !etConfirmPassword.getText().toString().trim().isEmpty();
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String domicilio = etDomicilio.getText().toString().trim();
        String placaAuto = etPlacaAuto.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar campos vacíos
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
                fechaNacimiento.isEmpty() || email.isEmpty() || telefono.isEmpty() ||
                domicilio.isEmpty() || placaAuto.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar DNI (8 dígitos)
        if (dni.length() != 8 || !dni.matches("\\d{8}")) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 dígitos", Toast.LENGTH_SHORT).show();
            etDni.requestFocus();
            return false;
        }

        // Validar email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        // Validar teléfono (9 dígitos para Perú)
        if (telefono.length() != 9 || !telefono.matches("\\d{9}")) {
            Toast.makeText(this, "El teléfono debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
            etTelefono.requestFocus();
            return false;
        }

        // Validar placa (formato peruano)
        if (!placaAuto.matches("^[A-Z]{3}-[0-9]{3,4}$")) {
            Toast.makeText(this, "Formato de placa inválido. Usa: ABC-123 o ABC-1234", Toast.LENGTH_SHORT).show();
            etPlacaAuto.requestFocus();
            return false;
        }

        // Validar contraseña mínima
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return false;
        }

        // Validar coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        // Validar términos y condiciones
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ OPCIONAL: Validar que las fotos estén subidas
        // Puedes descomentar esto si quieres hacer obligatorias las fotos
        /*
        if (!fotoTaxistaSubida) {
            Toast.makeText(this, "Debes subir una foto del taxista", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!fotoAutoSubida) {
            Toast.makeText(this, "Debes subir una foto del auto", Toast.LENGTH_SHORT).show();
            return false;
        }
        */

        return true;
    }

    // ✅ MANEJAR RESPUESTA DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_CAMERA_TAXISTA:
                    abrirCamaraDirectamente(true);
                    break;
                case PERMISSION_CAMERA_AUTO:
                    abrirCamaraDirectamente(false);
                    break;
                case PERMISSION_STORAGE:
                    // Reabrir el diálogo para que el usuario seleccione de nuevo
                    Toast.makeText(this, "Ahora puedes seleccionar fotos de la galería", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            // Permiso denegado
            String mensaje = "";
            switch (requestCode) {
                case PERMISSION_CAMERA_TAXISTA:
                case PERMISSION_CAMERA_AUTO:
                    mensaje = "Se necesita permiso de cámara para tomar fotos";
                    break;
                case PERMISSION_STORAGE:
                    mensaje = "Se necesita permiso de almacenamiento para acceder a la galería";
                    break;
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

            // Opcional: Mostrar diálogo explicativo
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                mostrarDialogoPermisos(requestCode);
            }
        }
    }

    // ✅ MOSTRAR DIÁLOGO EXPLICATIVO DE PERMISOS
    private void mostrarDialogoPermisos(int requestCode) {
        String titulo = "";
        String mensaje = "";

        switch (requestCode) {
            case PERMISSION_CAMERA_TAXISTA:
            case PERMISSION_CAMERA_AUTO:
                titulo = "Permiso de Cámara";
                mensaje = "La aplicación necesita acceso a la cámara para tomar fotos del taxista y del vehículo. Esto es necesario para completar el registro.";
                break;
            case PERMISSION_STORAGE:
                titulo = "Permiso de Almacenamiento";
                mensaje = "La aplicación necesita acceso al almacenamiento para seleccionar fotos de la galería.";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Permitir", (dialog, which) -> {
                    // Volver a solicitar el permiso
                    switch (requestCode) {
                        case PERMISSION_CAMERA_TAXISTA:
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    PERMISSION_CAMERA_TAXISTA);
                            break;
                        case PERMISSION_CAMERA_AUTO:
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    PERMISSION_CAMERA_AUTO);
                            break;
                        case PERMISSION_STORAGE:
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    PERMISSION_STORAGE);
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}