package com.example.telehotel.features.auth;

import androidx.lifecycle.ViewModel;
import com.example.telehotel.data.model.User;
import com.example.telehotel.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private User currentUser;

    public boolean login(String email, String password) {
        User user = authRepository.login(email, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean register(String email, String name, String password) {
        User user = new User(null, email, name);
        return authRepository.register(user, password);
    }

    public boolean recoverPassword(String email) {
        return authRepository.recoverPassword(email);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
