package com.example.telehotel.features.superadmin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.User;
import com.example.telehotel.features.superadmin.ui.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_activity);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers(); // carga de usuarios ficticios

        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        userList = new ArrayList<>();

        userList.add(new User("1", "admin1@hotel.com", "Admin1", "admin"));
        userList.get(0).setNombres("Carlos"); userList.get(0).setApellidos("Ramirez"); userList.get(0).setCorreo("admin1@hotel.com"); userList.get(0).setEstado("Activo");

        userList.add(new User("2", "cliente1@mail.com", "Cliente1","cliente"));
        userList.get(1).setNombres("Lucia"); userList.get(1).setApellidos("Fernandez"); userList.get(1).setCorreo("cliente1@mail.com"); userList.get(1).setEstado("Inactivo");

        userList.add(new User("3", "taxista1@mail.com", "Taxista1","taxista"));
        userList.get(2).setNombres("Miguel"); userList.get(2).setApellidos("Soto"); userList.get(2).setCorreo("taxista1@mail.com"); userList.get(2).setEstado("Activo");

        userList.add(new User("4", "cliente2@mail.com", "Cliente2","cliente"));
        userList.get(3).setNombres("Ana"); userList.get(3).setApellidos("Martinez"); userList.get(3).setCorreo("cliente2@mail.com"); userList.get(3).setEstado("Activo");

        userList.add(new User("5", "taxista2@mail.com", "Taxista2","taxista"));
        userList.get(4).setNombres("Pablo"); userList.get(4).setApellidos("Gomez"); userList.get(4).setCorreo("taxista2@mail.com"); userList.get(4).setEstado("Inactivo");
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