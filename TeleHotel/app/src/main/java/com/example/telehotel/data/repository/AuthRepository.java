package com.example.telehotel.data.repository;

import com.example.telehotel.data.model.Usuario;

/*public class AuthRepository {

    public Usuario login(String email, String password) {
        // Simulación de login con credenciales válidas
        if (email.equalsIgnoreCase("admin@telehotel.com") && password.equals("123")) {
            return new Usuario("uid_admin", email, "Admin", "Admin");
        }
        if (email.equalsIgnoreCase("taxista@telehotel.com") && password.equals("123")) {
            return new Usuario("uid_taxista", email, "Taxista", "Taxista");
        }
        if (email.equalsIgnoreCase("cliente@telehotel.com") && password.equals("123")) {
            return new Usuario("uid_cliente", email, "Cliente", "Cliente");
        }
        if (email.equalsIgnoreCase("superadmin@telehotel.com") && password.equals("123")) {
            return new Usuario("uid_superadmin", email, "SuperAdmin", "SuperAdmin");
        }

        // No coincide
        return null;
    }

    public boolean register(Usuario usuario, String password) {
        return true;
    }

    public boolean recoverPassword(String email) {
        return true;
    }
}*/
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public void login(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Usuario usuario = new Usuario(
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail(),
                                    firebaseUser.getDisplayName(),
                                    "Cliente" // Puedes recuperar el rol de Firebase si lo guardas en Firestore
                            );
                            callback.onSuccess(usuario);
                        } else {
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure();
                    }
                });
    }

    public void register(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Usuario usuario = new Usuario(
                                    firebaseUser.getUid(),
                                    email,
                                    firebaseUser.getDisplayName(),
                                    "Cliente"
                            );
                            callback.onSuccess(usuario);
                        } else {
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure();
                    }
                });
    }

    public void recoverPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email);
    }

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onFailure();
    }
}

