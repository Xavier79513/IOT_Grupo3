package com.example.telehotel.features.superadmin.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
import com.example.telehotel.core.utils.LogUtils;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.features.auth.LoginActivity;
import com.example.telehotel.features.superadmin.ui.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<Usuario> usuarioList = new ArrayList<>();
    private Spinner spinnerFiltroRoles;
    private ImageView ivLogout;

    // ✅ CORREGIDO: Roles para mostrar en el spinner
    private final String[] rolesDisplay = {"Todos","Administrador", "Taxista", "Cliente"};

    // ✅ AGREGADO: Mapeo de roles display a valores de Firebase
    private final String[] rolesFirebase = {"todos", "admin", "taxista", "cliente"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> cerrarSesion());

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerFiltroRoles = view.findViewById(R.id.spinnerFiltroRoles);

        // CORREGIDO: Usar rolesDisplay
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, rolesDisplay);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerFiltroRoles.setAdapter(spinnerAdapter);

        spinnerFiltroRoles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String rolFirebase = rolesFirebase[position];
                filtrarUsuarios(rolFirebase);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada
            }
        });

        loadUsersFromFirestore();

        return view;
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /*private void loadUsersFromFirestore() {
        FirebaseUtil.getFirestore().collection("usuarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    usuarioList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        if (usuario != null && usuario.getUid() == null) {
                            usuario.setUid(doc.getId());
                        }
                        usuarioList.add(usuario);
                    }

                    //  AGREGADO: Log para debug
                    Log.d("UserListDebug", "Total usuarios cargados: " + usuarioList.size());
                    for (Usuario u : usuarioList) {
                        Log.d("UserListDebug", "Usuario: " + u.getCorreo() + ", Rol: " + u.getRole() + ", Estado: " + u.getEstado());
                    }

                    // Filtrar con "todos" por defecto
                    filtrarUsuarios("todos");
                })
                .addOnFailureListener(e -> {
                    Log.e("UserListDebug", "Error al cargar usuarios: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void filtrarUsuarios(String rolFiltro) {
        List<Usuario> filtrados = new ArrayList<>();

        Log.d("UserListDebug", "Filtrando por rol: " + rolFiltro);

        if (rolFiltro.equals("todos")) {
            filtrados.addAll(usuarioList);
        } else {
            for (Usuario usuario : usuarioList) {
                // ✅ CORREGIDO: Comparación más robusta
                String userRole = usuario.getRole();
                if (userRole != null && userRole.equalsIgnoreCase(rolFiltro)) {
                    filtrados.add(usuario);
                }
            }
        }

        Log.d("UserListDebug", "Usuarios filtrados: " + filtrados.size());

        adapter = new UserAdapter(filtrados, this);
        recyclerView.setAdapter(adapter);
    }*/
    private void loadUsersFromFirestore() {
        FirebaseUtil.getFirestore().collection("usuarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    usuarioList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        if (usuario != null && usuario.getUid() == null) {
                            usuario.setUid(doc.getId());
                        }

                        // ✅ AGREGADO: Excluir usuarios superadmin
                        if (usuario != null && !"superadmin".equalsIgnoreCase(usuario.getRole())) {
                            usuarioList.add(usuario);
                        }
                    }

                    Log.d("UserListDebug", "Total usuarios cargados (sin superadmin): " + usuarioList.size());
                    for (Usuario u : usuarioList) {
                        Log.d("UserListDebug", "Usuario: " + u.getCorreo() + ", Rol: " + u.getRole() + ", Estado: " + u.getEstado());
                    }

                    // Filtrar con "todos" por defecto
                    filtrarUsuarios("todos");
                })
                .addOnFailureListener(e -> {
                    Log.e("UserListDebug", "Error al cargar usuarios: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void filtrarUsuarios(String rolFiltro) {
        List<Usuario> filtrados = new ArrayList<>();

        Log.d("UserListDebug", "Filtrando por rol: " + rolFiltro);

        if (rolFiltro.equals("todos")) {
            // ✅ MEJORADO: Agregar todos EXCEPTO superadmin (doble verificación)
            for (Usuario usuario : usuarioList) {
                if (!"superadmin".equalsIgnoreCase(usuario.getRole())) {
                    filtrados.add(usuario);
                }
            }
        } else {
            for (Usuario usuario : usuarioList) {
                String userRole = usuario.getRole();
                // ✅ MEJORADO: Comparación más robusta y excluir superadmin
                if (userRole != null &&
                        userRole.equalsIgnoreCase(rolFiltro) &&
                        !"superadmin".equalsIgnoreCase(userRole)) {
                    filtrados.add(usuario);
                }
            }
        }

        Log.d("UserListDebug", "Usuarios filtrados: " + filtrados.size());

        adapter = new UserAdapter(filtrados, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onToggleEstadoClick(Usuario usuario) {
        boolean activar = !"activo".equalsIgnoreCase(usuario.getEstado());
        mostrarDialogoConfirmacion(usuario, activar);
    }

    private void mostrarDialogoConfirmacion(Usuario usuario, boolean activar) {
        String accion = activar ? "Activó" : "Desactivó";
        String nuevoEstado = activar ? "activo" : "inactivo";
        String mensaje = activar
                ? "¿Estás seguro de que deseas ACTIVAR este usuario?"
                : "¿Estás seguro de que deseas DESACTIVAR este usuario?";

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(activar ? "Activar Usuario" : "Desactivar Usuario")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialogInterface, which) -> {
                    FirebaseUtil.getFirestore().collection("usuarios")
                            .document(usuario.getUid())
                            .update("estado", nuevoEstado)
                            .addOnSuccessListener(aVoid -> {
                                usuario.setEstado(nuevoEstado);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
                                // ✅ AGREGAR ESTAS LÍNEAS:
                                String nombreUsuario = usuario.getNombres();
                                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                                    nombreUsuario = usuario.getEmail();
                                }
                                LogUtils.logUserManagement(accion, usuario.getEmail(), nombreUsuario);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        // ✅ Cambiar colores de forma más concisa
        TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        TextView messageView = dialog.findViewById(android.R.id.message);

        if (titleView != null) titleView.setTextColor(Color.BLACK);
        if (messageView != null) messageView.setTextColor(Color.BLACK);
    }
}