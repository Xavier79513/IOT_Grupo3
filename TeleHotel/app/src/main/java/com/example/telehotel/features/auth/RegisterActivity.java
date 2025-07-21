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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDni, etFechaNacimiento,
            etEmail, etTelefono, etDomicilio, etPassword, etConfirmPassword;
    private ProgressBar pbPasswordStrength;
    private TextView tvPasswordStrength, tvLogin;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;

    // ✅ NUEVOS ELEMENTOS PARA FOTO DE PERFIL
    private ImageView ivFotoPerfil;
    private MaterialButton btnSeleccionarFoto;
    private String fotoPerfilUrl = ""; // URL de Cloudinary
    private boolean fotoPerfilSubida = false;
    private Uri fotoPerfilUri;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    // ✅ CONSTANTES PARA MANEJO DE FOTOS Y PERMISOS
    private static final int REQUEST_FOTO_PERFIL = 100;
    private static final int REQUEST_CAMERA_PERFIL = 101;
    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_STORAGE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ✅ INICIALIZAR CLOUDINARY
        CloudinaryManager.initialize(this);

        // Inicializar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);

        // Referencias a las vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etDomicilio = findViewById(R.id.etDomicilio);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // ✅ INICIALIZAR ELEMENTOS DE FOTO
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
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

        // Habilitar botón solo si se aceptan los términos
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked && validarCamposBasico());
        });

        // Listener del botón de registro
        btnRegister.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuarioFirebase();
            }
        });

        // Listener para ir al login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar DatePicker para fecha de nacimiento
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        etFechaNacimiento.setFocusable(false);
        etFechaNacimiento.setClickable(true);

        // ✅ LISTENER PARA SELECCIONAR FOTO DE PERFIL
        btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
    }

    // ✅ MÉTODO PARA MOSTRAR OPCIONES DE FOTO (GALERÍA O CÁMARA)
    private void mostrarOpcionesFoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Foto de Perfil")
                .setItems(new String[]{"📷 Tomar foto", "🖼️ Elegir de galería"},
                        (dialog, which) -> {
                            if (which == 0) {
                                abrirCamara();
                            } else {
                                abrirGaleria();
                            }
                        })
                .show();
    }

    // ✅ ABRIR CÁMARA CON VERIFICACIÓN DE PERMISOS
    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
            return;
        }

        abrirCamaraDirectamente();
    }

    private void abrirCamaraDirectamente() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA_PERFIL);
        } else {
            Toast.makeText(this, "No se puede acceder a la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ ABRIR GALERÍA CON VERIFICACIÓN DE PERMISOS
    private void abrirGaleria() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE);
                return;
            }
        }

        abrirGaleriaDirectamente();
    }

    private void abrirGaleriaDirectamente() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_FOTO_PERFIL);
    }

    // ✅ MANEJAR RESULTADO DE SELECCIÓN DE FOTOS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        try {
            switch (requestCode) {
                case REQUEST_FOTO_PERFIL:
                    if (data != null && data.getData() != null) {
                        fotoPerfilUri = data.getData();
                        ivFotoPerfil.setImageURI(fotoPerfilUri);
                        subirFotoCloudinary(fotoPerfilUri);
                    }
                    break;

                case REQUEST_CAMERA_PERFIL:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        ivFotoPerfil.setImageBitmap(bitmap);
                        subirBitmapCloudinary(bitmap);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error procesando imagen", e);
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ SUBIR FOTO DESDE URI A CLOUDINARY
    private void subirFotoCloudinary(Uri imageUri) {
        if (!CloudinaryManager.isInitialized()) {
            Toast.makeText(this, "Error: Cloudinary no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = System.currentTimeMillis() + "_" + etDni.getText().toString().trim();

        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Subiendo foto de perfil...");
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        try {
            MediaManager.get().upload(imageUri)
                    .unsigned(CloudinaryManager.getUploadPreset())
                    .option("folder", "clientes")
                    .option("public_id", fileName)
                    .option("resource_type", "image")
                    .option("quality", "auto:good")
                    .option("format", "jpg")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("Cloudinary", "🚀 Iniciando subida foto perfil: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            long progress = (bytes * 100) / totalBytes;
                            Log.d("Cloudinary", "📊 Progreso: " + progress + "%");
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadDialog.dismiss();
                            String url = (String) resultData.get("secure_url");

                            fotoPerfilUrl = url;
                            fotoPerfilSubida = true;
                            actualizarBotonFoto(true);

                            Log.d("Cloudinary", "✅ Foto de perfil subida: " + url);
                            Toast.makeText(RegisterActivity.this,
                                    "✅ Foto de perfil subida correctamente",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            uploadDialog.dismiss();
                            Log.e("Cloudinary", "❌ Error subiendo foto: " + error.getDescription());
                            Toast.makeText(RegisterActivity.this,
                                    "❌ Error subiendo foto de perfil",
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
    private void subirBitmapCloudinary(Bitmap bitmap) {
        if (!CloudinaryManager.isInitialized()) {
            Toast.makeText(this, "Error: Cloudinary no inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = System.currentTimeMillis() + "_" + etDni.getText().toString().trim();

        ProgressDialog uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Subiendo foto de perfil...");
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        try {
            // Convertir Bitmap a bytes
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            byte[] byteArray = stream.toByteArray();

            MediaManager.get().upload(byteArray)
                    .unsigned(CloudinaryManager.getUploadPreset())
                    .option("folder", "clientes")
                    .option("public_id", fileName)
                    .option("resource_type", "image")
                    .option("quality", "auto:good")
                    .option("format", "jpg")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("Cloudinary", "🚀 Iniciando subida bitmap: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            long progress = (bytes * 100) / totalBytes;
                            Log.d("Cloudinary", "📊 Progreso bitmap: " + progress + "%");
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadDialog.dismiss();
                            String url = (String) resultData.get("secure_url");

                            fotoPerfilUrl = url;
                            fotoPerfilSubida = true;
                            actualizarBotonFoto(true);

                            Log.d("Cloudinary", "✅ Bitmap subido exitosamente: " + url);
                            Toast.makeText(RegisterActivity.this,
                                    "✅ Foto de perfil subida correctamente",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            uploadDialog.dismiss();
                            Log.e("Cloudinary", "❌ Error subiendo bitmap: " + error.getDescription());
                            Toast.makeText(RegisterActivity.this,
                                    "❌ Error subiendo foto de perfil",
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
    private void actualizarBotonFoto(boolean subida) {
        if (subida) {
            btnSeleccionarFoto.setText("✅ Foto subida");
            btnSeleccionarFoto.setIconResource(android.R.drawable.ic_menu_gallery);
            btnSeleccionarFoto.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_green_light)));
        } else {
            btnSeleccionarFoto.setText("Seleccionar Foto");
            btnSeleccionarFoto.setIconResource(R.drawable.ic_camera);
        }
    }

    // ✅ MANEJAR RESPUESTA DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_CAMERA:
                    abrirCamaraDirectamente();
                    break;
                case PERMISSION_STORAGE:
                    Toast.makeText(this, "Ahora puedes seleccionar fotos de la galería", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            String mensaje = requestCode == PERMISSION_CAMERA ?
                    "Se necesita permiso de cámara para tomar fotos" :
                    "Se necesita permiso de almacenamiento para acceder a la galería";
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -25);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerTheme,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fecha = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFechaNacimiento.setText(fecha);
                }, year, month, day);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -80);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.setOnShowListener(dialog -> {
            try {
                int titleId = getResources().getIdentifier("date_picker_header_title", "id", "android");
                if (titleId != 0) {
                    TextView title = datePickerDialog.findViewById(titleId);
                    if (title != null) {
                        title.setTextColor(Color.BLACK);
                    }
                }

                DatePicker datePicker = datePickerDialog.getDatePicker();
                setDatePickerTextColor(datePicker);
            } catch (Exception e) {
                Log.e("DatePicker", "Error aplicando colores", e);
            }
        });

        datePickerDialog.show();
    }

    private void setDatePickerTextColor(ViewGroup viewGroup) {
        if (viewGroup == null) return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                textView.setTextColor(Color.BLACK);
                textView.setAlpha(1.0f);
            } else if (child instanceof ViewGroup) {
                setDatePickerTextColor((ViewGroup) child);
            }
        }
    }

    private void registrarUsuarioFirebase() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            actualizarPerfilUsuario(user);
                            enviarEmailVerificacion(user);
                            guardarDatosUsuarioFirestore(user.getUid());
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
                        mostrarDialogoVerificacion();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error al enviar email de verificación",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarDatosUsuarioFirestore(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombres", etNombre.getText().toString().trim());
        usuario.put("apellido", etApellido.getText().toString().trim());
        usuario.put("dni", etDni.getText().toString().trim());
        usuario.put("fechaNacimiento", etFechaNacimiento.getText().toString().trim());
        usuario.put("email", etEmail.getText().toString().trim());
        usuario.put("telefono", etTelefono.getText().toString().trim());
        usuario.put("domicilio", etDomicilio.getText().toString().trim());
        usuario.put("fechaRegistro", new Date());
        usuario.put("role", "cliente");

        // ✅ AGREGAR URL DE FOTO DE PERFIL
        usuario.put("fotoUrl", fotoPerfilUrl);

        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Datos del usuario guardados exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error al guardar datos del usuario", e);
                });
    }

    private void mostrarDialogoVerificacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificación de Email")
                .setMessage("Se ha enviado un email de verificación a " + etEmail.getText().toString() +
                        "\n\nPor favor revisa tu bandeja de entrada y haz clic en el enlace de verificación.")
                .setIcon(R.drawable.ic_email)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", etEmail.getText().toString());
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void manejarErrorRegistro(Exception exception) {
        String mensaje = "Error al crear la cuenta";

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
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
                fechaNacimiento.isEmpty() || email.isEmpty() || telefono.isEmpty() ||
                domicilio.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dni.length() != 8 || !dni.matches("\\d{8}")) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 dígitos", Toast.LENGTH_SHORT).show();
            etDni.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (telefono.length() != 9 || !telefono.matches("\\d{9}")) {
            Toast.makeText(this, "El teléfono debe tener exactamente 9 dígitos", Toast.LENGTH_SHORT).show();
            etTelefono.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}