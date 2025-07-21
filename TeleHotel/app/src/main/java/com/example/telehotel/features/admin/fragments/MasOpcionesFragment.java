/*package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MasOpcionesFragment extends Fragment {

    private LinearLayout layoutTaxista, layoutCheckout, layoutPerfil, layoutCerrarSesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mas_opciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        layoutTaxista = view.findViewById(R.id.layoutTaxista);
        layoutCheckout = view.findViewById(R.id.layoutCheckout);
        layoutPerfil = view.findViewById(R.id.layoutPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);
    }

    private void setupClickListeners() {
        layoutTaxista.setOnClickListener(v -> {
            // TODO: Navegar a TaxistaEstadoFragment
            // navController.navigate(R.id.taxistaEstadoFragment);
        });

        layoutCheckout.setOnClickListener(v -> {
            navegarACheckoutAdmin();
        });


        layoutPerfil.setOnClickListener(v -> {
            // TODO: Navegar a ConfiguracionFragment
            // navController.navigate(R.id.configuracionFragment);
        });

        layoutCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }

    private void navegarACheckoutAdmin() {
        try {
            // Crear instancia del fragment de checkout
            CheckoutAdminFragment checkoutFragment = new CheckoutAdminFragment();

            // Realizar la transición de fragments
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,  // Animación de entrada
                    R.anim.slide_out_left,  // Animación de salida
                    R.anim.slide_in_left,   // Animación al volver (pop enter)
                    R.anim.slide_out_right  // Animación al volver (pop exit)
            );

            // Reemplazar el fragment actual con el de checkout
            transaction.replace(R.id.fragment_container, checkoutFragment);

            // Agregar a la pila para poder volver con el botón atrás
            transaction.addToBackStack("checkout_admin");

            // Confirmar la transición
            transaction.commit();

        } catch (Exception e) {
            // Si hay error con la transición de fragments, usar fallback
            android.util.Log.e("MasOpcionesFragment", "Error navegando a checkout", e);

            // Fallback: mostrar mensaje o intentar otra aproximación
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(),
                        "Abriendo gestión de checkout...",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}*/
/*package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.telehotel.R;
import com.example.telehotel.features.admin.CheckoutAdminActivity;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MasOpcionesFragment extends Fragment {

    private LinearLayout layoutTaxista, layoutCheckout, layoutPerfil, layoutCerrarSesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mas_opciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        layoutTaxista = view.findViewById(R.id.layoutTaxista);
        layoutCheckout = view.findViewById(R.id.layoutCheckout);
        layoutPerfil = view.findViewById(R.id.layoutPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);
    }

    private void setupClickListeners() {
        layoutTaxista.setOnClickListener(v -> {
            // TODO: Navegar a TaxistaEstadoFragment
            // navegarConNavController(R.id.action_masOpciones_to_taxistaEstado);
        });

        layoutCheckout.setOnClickListener(v -> {
            // ✅ SOLUCIÓN: Usar Navigation Component correctamente
            navegarACheckoutAdmin();
        });

        layoutPerfil.setOnClickListener(v -> {
            // TODO: Navegar a ConfiguracionFragment
            // navegarConNavController(R.id.action_masOpciones_to_configuracion);
        });

        layoutCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }


    private void navegarACheckoutAdmin() {
        try {
            NavController navController = Navigation.findNavController(requireView());

            // Navegar usando el ID de la acción definida en nav_graph.xml
            navController.navigate(R.id.action_masOpciones_to_checkoutAdmin);

        } catch (Exception e) {
            android.util.Log.e("MasOpcionesFragment", "Error navegando con NavController", e);

            // ✅ SOLUCIÓN 2: Fallback - Usar Activity (si no tienes nav_graph configurado)
            usarActivityComoFallback();
        }
    }


    private void usarActivityComoFallback() {
        try {
            // Crear un Activity wrapper para el CheckoutAdminFragment
            Intent intent = new Intent(getContext(), CheckoutAdminActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            android.util.Log.e("MasOpcionesFragment", "Error con Activity fallback", e);

            // ✅ SOLUCIÓN 3: Última alternativa - Usar Fragment sin Navigation
            usarFragmentSinNavigation();
        }
    }

    /
    private void usarFragmentSinNavigation() {
        try {
            // Verificar si hay un contenedor disponible
            if (getActivity() != null && getActivity().findViewById(R.id.fragment_container) != null) {

                CheckoutAdminFragment checkoutFragment = new CheckoutAdminFragment();

                // Usar el FragmentManager de la Activity (no del parent)
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, checkoutFragment)
                        .addToBackStack("checkout_admin")
                        .commit();

            } else {
                // Si no hay contenedor, mostrar mensaje
                Toast.makeText(getContext(),
                        "Función de checkout en desarrollo",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            android.util.Log.e("MasOpcionesFragment", "Error con transición manual", e);
            Toast.makeText(getContext(),
                    "Error accediendo a checkout. Inténtalo nuevamente.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void navegarConNavController(int actionId) {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(actionId);
        } catch (Exception e) {
            android.util.Log.e("MasOpcionesFragment", "Error navegando", e);
            Toast.makeText(getContext(), "Error en navegación", Toast.LENGTH_SHORT).show();
        }
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}*/
package com.example.telehotel.features.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.telehotel.R;
import com.example.telehotel.features.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MasOpcionesFragment extends Fragment {

    private LinearLayout layoutTaxista, layoutCheckout, layoutPerfil, layoutCerrarSesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mas_opciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        //layoutTaxista = view.findViewById(R.id.layoutTaxista);
        layoutCheckout = view.findViewById(R.id.layoutCheckout);
        //layoutPerfil = view.findViewById(R.id.layoutPerfil);
        layoutCerrarSesion = view.findViewById(R.id.layoutCerrarSesion);
    }

    private void setupClickListeners() {
        /*layoutTaxista.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show();
        });*/

        layoutCheckout.setOnClickListener(v -> {
            // ✅ NAVEGAR A CHECKOUT ADMIN
            navegarACheckoutAdmin();
        });

        /*layoutPerfil.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show();
        });*/

        layoutCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    }

    /**
     * ✅ NAVEGAR A CHECKOUT ADMIN usando Navigation Component
     */
    private void navegarACheckoutAdmin() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_masOpciones_to_checkoutAdmin);

        } catch (Exception e) {
            android.util.Log.e("MasOpcionesFragment", "Error navegando a checkout", e);
            Toast.makeText(getContext(),
                    "Error accediendo a checkout: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}