package com.example.telehotel.features.superadmin.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.User;
import com.example.telehotel.features.superadmin.ui.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private Spinner spinnerFiltroRoles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerFiltroRoles = view.findViewById(R.id.spinnerFiltroRoles);

        loadUsers(); // Cargamos usuarios simulados

        // Configurar spinner
        String[] roles = {"Todos", "Administrador", "Taxista", "Cliente"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerFiltroRoles.setAdapter(spinnerAdapter);

        spinnerFiltroRoles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filtroSeleccionado = roles[position];
                filtrarUsuarios(filtroSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadUsers() {
        userList = new ArrayList<>();
        userList.add(new User("1", "correo1@example.com", "Ana Martínez", "Administrador"));
        userList.add(new User("2", "correo2@example.com", "Luis Gómez", "Cliente"));
        userList.add(new User("3", "correo3@example.com", "María Pérez", "Recepcionista"));
        userList.add(new User("4", "correo4@example.com", "Pedro Sánchez", "Cliente"));
        userList.add(new User("5", "correo5@example.com", "Carlos Ruiz", "Taxista"));
        userList.add(new User("6", "correo6@example.com", "Lucía Herrera", "Cliente"));
        userList.add(new User("7", "correo7@example.com", "Fernando Torres", "Administrador"));
        userList.add(new User("8", "correo8@example.com", "Isabel Mendoza", "Taxista"));
        userList.add(new User("9", "correo9@example.com", "Alonso Martínez", "Cliente"));
        userList.add(new User("10", "correo10@example.com", "Pamela Díaz", "Taxista"));
        userList.add(new User("11", "correo11@example.com", "Miguel Soto", "Administrador"));
        userList.add(new User("12", "correo12@example.com", "Carmen Lara", "Cliente"));
        userList.add(new User("13", "correo13@example.com", "Jorge Ríos", "Taxista"));
        userList.add(new User("14", "correo14@example.com", "Andrea Campos", "Cliente"));
        userList.add(new User("15", "correo15@example.com", "Sebastián Reyes", "Administrador"));

        // Estados simulados
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).setEstado(i % 2 == 0 ? "Activo" : "Inactivo");
        }
    }

    private void filtrarUsuarios(String rol) {
        List<User> filtrados = new ArrayList<>();

        if (rol.equals("Todos")) {
            filtrados.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getRole() != null && user.getRole().equalsIgnoreCase(rol)) {
                    filtrados.add(user);
                }
            }
        }

        adapter = new UserAdapter(filtrados, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivateClick(User user) {
        user.setEstado("Activo");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeactivateClick(User user) {
        user.setEstado("Inactivo");
        adapter.notifyDataSetChanged();
    }
}