package com.example.telehotel.data.repository;

import com.example.telehotel.data.model.User;

public class AuthRepository {

    public User login(String email, String password) {
        // Simulación de login. Aquí luego irá Firebase Auth
        if (email.equals("admin@example.com") && password.equals("123")) {
            return new User("uid123", email, "Admin");
        }
        return null;
    }

    public boolean register(User user, String password) {
        // Simulación de registro
        return true;
    }

    public boolean recoverPassword(String email) {
        // Simulación de recuperación
        return true;
    }
}
