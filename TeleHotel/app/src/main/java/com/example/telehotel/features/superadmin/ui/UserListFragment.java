package com.example.telehotel.features.superadmin.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUsers(); // Simulamos carga de usuarios

        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadUsers() {
        userList = new ArrayList<>();
        // Aquí cargas los usuarios reales de Firestore o lo que uses.
        userList.add(new User("1", "correo1@example.com", "Nombre1"));
        userList.add(new User("2", "correo2@example.com", "Nombre2"));
        userList.get(0).setEstado("Activo");
        userList.get(1).setEstado("Inactivo");
    }

    @Override
    public void onActivateClick(User user) {
        // Lógica para activar
        user.setEstado("Activo");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeactivateClick(User user) {
        // Lógica para desactivar
        user.setEstado("Inactivo");
        adapter.notifyDataSetChanged();
    }
}