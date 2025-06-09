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

    private void cargarServiciosDelHotel() {
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
