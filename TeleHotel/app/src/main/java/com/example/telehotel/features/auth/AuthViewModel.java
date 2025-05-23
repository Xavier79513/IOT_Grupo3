package com.example.telehotel.features.auth;

import androidx.lifecycle.ViewModel;
import com.example.telehotel.data.model.Usuario;
import com.example.telehotel.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private Usuario currentUsuario;

    public boolean login(String email, String password) {
        Usuario usuario = authRepository.login(email, password);
        if (usuario != null) {
            currentUsuario = usuario;
            return true;
        }
        return false;
    }

    public boolean register(String email, String name, String password) {
        Usuario usuario = new Usuario(null, email, name, null);
        return authRepository.register(usuario, password);
    }

    public boolean recoverPassword(String email) {
        return authRepository.recoverPassword(email);
    }

    public Usuario getCurrentUser() {
        return currentUsuario;
    }
}
