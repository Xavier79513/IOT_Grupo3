package com.example.telehotel.features.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.databinding.ActivityServiciosBinding;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*public class ServiciosActivity extends AppCompatActivity {
    private ActivityServiciosBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema Admin (sin ActionBar nativo)
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // Inflar layout con ViewBinding
        binding = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ─── CONFIGURAR DRAWER ───────────────────────────
        drawerLayout = binding.drawerLayout;      // @+id/drawer_layout
        NavigationView navView = binding.navView; // @+id/nav_view

        // Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        MaterialToolbar toolbar = binding.topAppBar;

        // Toggle para vincular toolbar ↔ drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Manejar clicks en el menú lateral
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: navegar a Estado de taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: navegar a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // ─── CONFIGURAR TOOLBAR ──────────────────────────
        toolbar.setBackgroundColor(Color.BLACK);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);

        // ─── DATOS DE EJEMPLO ────────────────────────────
        List<Service> datos = Arrays.asList(
                new Service(R.drawable.ic_breakfast,    "Desayuno Buffet",
                        "Desayuno variado con jugos, pan, frutas, café, etc.",
                        "Precio: S/50.00"),
                new Service(R.drawable.ic_parking,      "Estacionamiento",
                        "Por noche.",
                        "Precio: S/30.00"),
                new Service(R.drawable.ic_lavanderia,   "Lavandería",
                        "Servicio de lavado y planchado.",
                        "Precio: S/25.00"),
                new Service(R.drawable.ic_spa,          "Spa",
                        "Masajes y tratamientos relajantes.",
                        "Precio: S/120.00"),
                new Service(R.drawable.ic_room_service, "Room Service",
                        "Servicio a la habitación 24/7.",
                        "Precio: S/70.00")
        );

        // ─── CONFIGURAR RECYCLER VIEW ───────────────────
        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(new ServiceAdapter(datos));

        // ─── BOTTOM NAV ─────────────────────────────────
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.servicios_fragment);
    }


    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: cerrar sesión
            return true;
        }
        return false;
    }


    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.inicio_fragment) {
            intent = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            intent = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            // Ya estamos aquí
            return true;
        } else if (id == R.id.reportes_fragment) {
            intent = new Intent(this, ReportesActivity.class);
        } else if (id == R.id.perfil_fragment) {
            intent = new Intent(this, PerfilActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, lo cerramos primero
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}*/
public class ServiciosActivity extends AppCompatActivity implements ServiciosAdapter.OnServicioClickListener {

    private ActivityServiciosBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String hotelId;

    // RecyclerView
    private ServiciosAdapter serviciosAdapter;
    private List<Servicio> serviciosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema Admin (sin ActionBar nativo)
        setTheme(R.style.Theme_TeleHotel_Admin);
        super.onCreate(savedInstanceState);

        // Inflar layout con ViewBinding
        binding = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        initFirebase();

        // Inicializar UI
        initUI();

        // Configurar Drawer
        setupDrawer();

        // Configurar Toolbar
        setupToolbar();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar Bottom Navigation
        setupBottomNavigation();

        // Cargar datos
        obtenerHotelDelAdministrador();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initUI() {
        serviciosList = new ArrayList<>();
    }

    private void setupDrawer() {
        drawerLayout = binding.drawerLayout;
        NavigationView navView = binding.navView;

        // Toolbar como ActionBar
        setSupportActionBar(binding.topAppBar);
        MaterialToolbar toolbar = binding.topAppBar;

        // Toggle para vincular toolbar ↔ drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Manejar clicks en el menú lateral
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_estado_taxi) {
                // TODO: navegar a Estado de taxi
            } else if (id == R.id.nav_checkout) {
                // TODO: navegar a Checkout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = binding.topAppBar;
        toolbar.setBackgroundColor(Color.BLACK);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_hotel_detail);
        toolbar.setOnMenuItemClickListener(this::onLogoutItem);
    }

    private void setupRecyclerView() {
        serviciosAdapter = new ServiciosAdapter(serviciosList);
        serviciosAdapter.setOnServicioClickListener(this);

        binding.rvServices.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServices.setAdapter(serviciosAdapter);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView
                .setOnItemSelectedListener(this::onBottomItem);
        binding.bottomNavigationView
                .setSelectedItemId(R.id.servicios_fragment);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS DE FIRESTORE
    // ═══════════════════════════════════════════════════════════

    private void obtenerHotelDelAdministrador() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminUid = currentUser.getUid();
        Log.d("ServiciosActivity", "Buscando hotel para admin: " + adminUid);

        db.collection("hoteles")
                .whereEqualTo("administradorId", adminUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener el primer (y debería ser único) hotel
                        DocumentSnapshot hotelDoc = queryDocumentSnapshots.getDocuments().get(0);
                        hotelId = hotelDoc.getId();

                        String hotelNombre = hotelDoc.getString("nombre");
                        Log.d("ServiciosActivity", "Hotel encontrado: " + hotelNombre + " (ID: " + hotelId + ")");

                        // Actualizar título con nombre del hotel
                        binding.tvScreenTitle.setText("Servicios de " + hotelNombre);

                        // Cargar servicios del hotel
                        cargarServiciosDelHotel();

                    } else {
                        Log.w("ServiciosActivity", "No se encontró hotel para el administrador: " + adminUid);
                        Toast.makeText(this, "No tienes un hotel asignado", Toast.LENGTH_LONG).show();
                        mostrarEstadoSinHotel();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosActivity", "Error al obtener hotel del administrador", e);
                    Toast.makeText(this, "Error al cargar información del hotel", Toast.LENGTH_SHORT).show();
                });
    }

    /*private void cargarServiciosDelHotel() {
        if (hotelId == null) {
            Log.w("ServiciosActivity", "hotelId es null, no se pueden cargar servicios");
            return;
        }

        Log.d("ServiciosActivity", "Cargando servicios para hotel: " + hotelId);

        db.collection("servicios")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviciosList.clear();

                    Log.d("ServiciosActivity", "Servicios encontrados: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Servicio servicio = document.toObject(Servicio.class);
                        servicio.setId(document.getId());
                        serviciosList.add(servicio);

                        Log.d("ServiciosActivity", "Servicio cargado: " + servicio.getNombre());
                    }

                    // Actualizar el adaptador
                    serviciosAdapter.updateServicios(serviciosList);

                    // Mostrar estado según cantidad de servicios
                    if (serviciosList.isEmpty()) {
                        mostrarEstadoVacio();
                    } else {
                        ocultarEstadoVacio();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosActivity", "Error al cargar servicios del hotel", e);
                    Toast.makeText(this, "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                });
    }*/
    /*private void cargarServiciosDelHotel() {
        if (hotelId == null) {
            Log.w("ServiciosActivity", "❌ hotelId es null, no se pueden cargar servicios");
            return;
        }

        Log.d("ServiciosActivity", "🔍 DEBUGGING - Iniciando carga de servicios...");
        Log.d("ServiciosActivity", "🏨 Hotel ID: " + hotelId);

        // ✅ PASO 1: Verificar que existen servicios en la colección
        db.collection("servicios")
                .get()
                .addOnSuccessListener(allServicesSnapshot -> {
                    int totalServicios = allServicesSnapshot.size();
                    Log.d("ServiciosActivity", "📊 TOTAL servicios en colección: " + totalServicios);

                    if (totalServicios == 0) {
                        Log.e("ServiciosActivity", "❌ La colección 'servicios' está VACÍA");
                        mostrarEstadoVacio();
                        return;
                    }

                    // Mostrar TODOS los servicios para debugging
                    for (QueryDocumentSnapshot doc : allServicesSnapshot) {
                        String servicioHotelId = doc.getString("hotelId");
                        String servicioNombre = doc.getString("nombre");
                        String servicioId = doc.getId();

                        Log.d("ServiciosActivity", "🔸 Servicio encontrado:");
                        Log.d("ServiciosActivity", "   📋 ID: " + servicioId);
                        Log.d("ServiciosActivity", "   🏷️ Nombre: " + servicioNombre);
                        Log.d("ServiciosActivity", "   🏨 Hotel ID: " + servicioHotelId);
                        Log.d("ServiciosActivity", "   ✅ ¿Coincide? " + hotelId.equals(servicioHotelId));
                    }

                    // PASO 2: Hacer la consulta filtrada
                    consultarServiciosPorHotel();
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosActivity", "❌ ERROR al verificar servicios totales", e);
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }*/
    private void cargarServiciosDelHotel() {
        if (hotelId == null) {
            Log.w("ServiciosActivity", "❌ hotelId es null, no se pueden cargar servicios");
            return;
        }

        Log.d("ServiciosActivity", "🔍 Cargando servicios para hotel: " + hotelId);

        // ✅ Usar arrayContains para buscar en arrays
        db.collection("servicios")
                .whereArrayContains("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviciosList.clear();

                    int totalEncontrados = queryDocumentSnapshots.size();
                    Log.d("ServiciosActivity", "✅ Servicios encontrados para este hotel: " + totalEncontrados);

                    if (totalEncontrados == 0) {
                        Log.w("ServiciosActivity", "⚠️ No se encontraron servicios para hotelId: " + hotelId);
                        mostrarEstadoVacio();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Servicio servicio = document.toObject(Servicio.class);
                            servicio.setId(document.getId());
                            serviciosList.add(servicio);

                            Log.d("ServiciosActivity", "✅ Servicio cargado: " + servicio.getNombre() +
                                    " | Hoteles: " + servicio.getCantidadHoteles() +
                                    " | Precio: " + servicio.getPrecioFormateado());

                        } catch (Exception e) {
                            Log.e("ServiciosActivity", "❌ Error al deserializar servicio: " + document.getId(), e);
                        }
                    }

                    // Actualizar el adaptador
                    Log.d("ServiciosActivity", "🔄 Actualizando adaptador con " + serviciosList.size() + " servicios");
                    serviciosAdapter.updateServicios(serviciosList);

                    if (serviciosList.isEmpty()) {
                        mostrarEstadoVacio();
                    } else {
                        ocultarEstadoVacio();
                        Log.d("ServiciosActivity", "🎉 Servicios mostrados correctamente");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosActivity", "❌ Error al cargar servicios del hotel", e);
                    Toast.makeText(this, "Error al cargar servicios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void consultarServiciosPorHotel() {
        Log.d("ServiciosActivity", "🔍 PASO 2: Consultando servicios específicos del hotel: " + hotelId);

        db.collection("servicios")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviciosList.clear();

                    int totalEncontrados = queryDocumentSnapshots.size();
                    Log.d("ServiciosActivity", "✅ Servicios encontrados para ESTE hotel: " + totalEncontrados);

                    if (totalEncontrados == 0) {
                        Log.w("ServiciosActivity", "⚠️ NO se encontraron servicios para hotelId: " + hotelId);
                        Log.w("ServiciosActivity", "💡 Posibles causas:");
                        Log.w("ServiciosActivity", "   1. Los servicios no tienen el campo 'hotelId'");
                        Log.w("ServiciosActivity", "   2. El hotelId no coincide exactamente");
                        Log.w("ServiciosActivity", "   3. Los servicios están en otra colección");
                        mostrarEstadoVacio();
                        return;
                    }

                    // PASO 3: Procesar cada servicio encontrado
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d("ServiciosActivity", "🔄 Procesando servicio: " + document.getId());

                            Servicio servicio = document.toObject(Servicio.class);
                            servicio.setId(document.getId());
                            serviciosList.add(servicio);

                            Log.d("ServiciosActivity", "✅ Servicio procesado exitosamente:");
                            Log.d("ServiciosActivity", "   📋 ID: " + servicio.getId());
                            Log.d("ServiciosActivity", "   🏷️ Nombre: " + servicio.getNombre());
                            Log.d("ServiciosActivity", "   💰 Precio: " + servicio.getPrecioFormateado());
                            Log.d("ServiciosActivity", "   🏨 Hotel: " + servicio.getHotelId());
                            Log.d("ServiciosActivity", "   📂 Categoría: " + servicio.getCategoria());

                        } catch (Exception e) {
                            Log.e("ServiciosActivity", "❌ Error al deserializar servicio: " + document.getId(), e);
                        }
                    }

                    // PASO 4: Actualizar UI
                    Log.d("ServiciosActivity", "🔄 Actualizando adaptador con " + serviciosList.size() + " servicios");
                    serviciosAdapter.updateServicios(serviciosList);

                    // PASO 5: Verificar estado final
                    if (serviciosList.isEmpty()) {
                        Log.w("ServiciosActivity", "⚠️ Lista de servicios vacía después del procesamiento");
                        mostrarEstadoVacio();
                    } else {
                        Log.d("ServiciosActivity", "🎉 ÉXITO: " + serviciosList.size() + " servicios mostrados correctamente");
                        ocultarEstadoVacio();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ServiciosActivity", "❌ ERROR en consulta filtrada", e);
                    Toast.makeText(this, "Error al cargar servicios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    // ✅ Método adicional para obtener todos los servicios (opcional)
    private void cargarTodosLosServicios() {
        Log.d("ServiciosActivity", "🔍 Cargando TODOS los servicios disponibles");

        db.collection("servicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Servicio> todosLosServicios = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Servicio servicio = document.toObject(Servicio.class);
                            servicio.setId(document.getId());
                            todosLosServicios.add(servicio);

                            Log.d("ServiciosActivity", "📋 Servicio: " + servicio.getNombre() +
                                    " | Disponible en " + servicio.getCantidadHoteles() + " hoteles");

                        } catch (Exception e) {
                            Log.e("ServiciosActivity", "❌ Error al procesar servicio: " + document.getId(), e);
                        }
                    }

                    // Aquí puedes mostrar todos los servicios si lo necesitas
                    Log.d("ServiciosActivity", "📊 Total servicios cargados: " + todosLosServicios.size());
                });
    }

    // ✅ Método para verificar si un servicio pertenece al hotel actual
    private boolean servicioPertenece(Servicio servicio) {
        return servicio.perteneceAlHotel(hotelId);
    }

    private void mostrarEstadoSinHotel() {
        binding.tvScreenTitle.setText("Sin hotel asignado");
        // TODO: Mostrar mensaje de que no tiene hotel asignado
    }

    private void mostrarEstadoVacio() {
        // TODO: Mostrar estado vacío (sin servicios)
        Toast.makeText(this, "Este hotel no tiene servicios registrados", Toast.LENGTH_LONG).show();
    }

    private void ocultarEstadoVacio() {
        // TODO: Ocultar estado vacío si existe
    }

    // ═══════════════════════════════════════════════════════════
    // IMPLEMENTACIÓN DE INTERFACES
    // ═══════════════════════════════════════════════════════════

    @Override
    public void onServicioClick(Servicio servicio) {
        // TODO: Manejar click en servicio (abrir detalle, editar, etc.)
        Toast.makeText(this, "Servicio: " + servicio.getNombre(), Toast.LENGTH_SHORT).show();
    }

    /** Clic en "Cerrar sesión" (overflow) */
    private boolean onLogoutItem(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // TODO: cerrar sesión
            auth.signOut();
            // Redirigir al login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    /** Navegación del BottomNavigationView */
    private boolean onBottomItem(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.inicio_fragment) {
            intent = new Intent(this, AdminActivity.class);
        } else if (id == R.id.habitaciones_fragment) {
            intent = new Intent(this, HabitacionesActivity.class);
        } else if (id == R.id.servicios_fragment) {
            // Ya estamos aquí
            return true;
        } else if (id == R.id.reportes_fragment) {
            intent = new Intent(this, ReportesActivity.class);
        } else if (id == R.id.perfil_fragment) {
            intent = new Intent(this, PerfilActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, lo cerramos primero
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar servicios al volver a la actividad
        if (hotelId != null) {
            cargarServiciosDelHotel();
        }
    }
}
