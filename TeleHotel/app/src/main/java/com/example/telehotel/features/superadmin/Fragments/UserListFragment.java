package com.example.telehotel.features.superadmin.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.FirebaseUtil;
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

    private final String[] roles = {"Todos", "Administrador", "Taxista", "Cliente"};

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

        // Configurar spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerFiltroRoles.setAdapter(spinnerAdapter);

        spinnerFiltroRoles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarUsuarios(roles[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
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

                        usuarioList.add(usuario);
                    }

                    filtrarUsuarios(spinnerFiltroRoles.getSelectedItem().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void filtrarUsuarios(String rol) {
        List<Usuario> filtrados = new ArrayList<>();

        if (rol.equals("Todos")) {
            filtrados.addAll(usuarioList);
        } else {
            for (Usuario usuario : usuarioList) {
                if (usuario.getRole() != null && usuario.getRole().equalsIgnoreCase(rol)) {
                    filtrados.add(usuario);
                }
            }
        }

        adapter = new UserAdapter(filtrados, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivateClick(Usuario usuario) {
        mostrarDialogoConfirmacion(usuario, true);
    }

    @Override
    public void onDeactivateClick(Usuario usuario) {
        mostrarDialogoConfirmacion(usuario, false);
    }

    private void mostrarDialogoConfirmacion(Usuario usuario, boolean activar) {
        String mensaje = activar
                ? "¿Estás seguro de que deseas ACTIVAR este usuario?"
                : "¿Estás seguro de que deseas DESACTIVAR este usuario?";
        String titulo = activar ? "Activar Usuario" : "Desactivar Usuario";

        new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> {
                    String nuevoEstado = activar ? "Activo" : "Inactivo";

                    FirebaseUtil.getFirestore().collection("usuarios")
                            .document(usuario.getUid())
                            .update("estado", nuevoEstado)
                            .addOnSuccessListener(aVoid -> {
                                usuario.setEstado(nuevoEstado);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}