package com.example.telehotel.data.repository;

import com.example.telehotel.data.model.User;

public class AuthRepository {

    public User login(String email, String password) {
        // Simulación de login con credenciales válidas
        if (email.equalsIgnoreCase("admin@telehotel.com") && password.equals("123")) {
            return new User("uid_admin", email, "Admin", "Admin");
        }
        if (email.equalsIgnoreCase("taxista@telehotel.com") && password.equals("123")) {
            return new User("uid_taxista", email, "Taxista", "Taxista");
        }
        if (email.equalsIgnoreCase("cliente@telehotel.com") && password.equals("123")) {
            return new User("uid_cliente", email, "Cliente", "Cliente");
        }
        if (email.equalsIgnoreCase("superadmin@telehotel.com") && password.equals("123")) {
            return new User("uid_superadmin", email, "SuperAdmin", "SuperAdmin");
        }

        // No coincide
        return null;
    }

    public boolean register(User user, String password) {
        return true;
    }

    public boolean recoverPassword(String email) {
        return true;
    }
}
