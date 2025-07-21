/*package com.example.telehotel.features.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;

public class HotelDetailCercaFragment extends Fragment {

    public HotelDetailCercaFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_hotel_detail_cerca, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Puedes configurar lógica aquí si es necesario
    }
}*/
package com.example.telehotel.features.cliente.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telehotel.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HotelDetailCercaFragment extends Fragment {

    private static final String TAG = "HotelDetailCerca";
    private static final String ARG_HOTEL_ID = "hotel_id";

    private LinearLayout containerLugares;
    private LinearLayout emptyStateLugares;
    private View loadingView;

    private FirebaseFirestore db;
    private String hotelId;

    public HotelDetailCercaFragment() {
        // Constructor vacío requerido
    }

    // Factory method para crear instancia con hotelId
    public static HotelDetailCercaFragment newInstance(String hotelId) {
        HotelDetailCercaFragment fragment = new HotelDetailCercaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_ID, hotelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_hotel_detail_cerca, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();
        getArgumentsData();
        loadLugaresCercanos();
    }

    private void initViews(View view) {
        containerLugares = view.findViewById(R.id.containerLugaresCercanos);
        emptyStateLugares = view.findViewById(R.id.emptyStateLugares);
        loadingView = view.findViewById(R.id.loadingView);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void getArgumentsData() {
        if (getArguments() != null) {
            hotelId = getArguments().getString(ARG_HOTEL_ID);
        }

        Log.d(TAG, "Hotel ID recibido: " + hotelId);
    }

    @SuppressWarnings("unchecked")
    private void loadLugaresCercanos() {
        if (hotelId == null || hotelId.isEmpty()) {
            Log.e(TAG, "Hotel ID es null o vacío");
            mostrarError("Error: Hotel no identificado");
            return;
        }

        mostrarLoading(true);

        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    mostrarLoading(false);

                    if (!document.exists()) {
                        Log.w(TAG, "Hotel no encontrado: " + hotelId);
                        mostrarEmptyState(true, "Hotel no encontrado");
                        return;
                    }

                    // Obtener lugares históricos
                    List<Map<String, Object>> lugares =
                            (List<Map<String, Object>>) document.get("lugaresHistoricos");

                    if (lugares == null || lugares.isEmpty()) {
                        Log.d(TAG, "No hay lugares históricos para este hotel");
                        mostrarEmptyState(true, "No hay lugares históricos registrados para este hotel");
                        return;
                    }

                    Log.d(TAG, "Lugares encontrados: " + lugares.size());
                    mostrarEmptyState(false, null);
                    mostrarLugares(lugares);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando lugares", e);
                    mostrarLoading(false);
                    mostrarError("Error cargando lugares cercanos");
                });
    }

    private void mostrarLugares(List<Map<String, Object>> lugares) {
        // Limpiar contenedor
        containerLugares.removeAllViews();

        for (Map<String, Object> lugar : lugares) {
            View itemView = crearItemLugar(lugar);
            containerLugares.addView(itemView);
        }
    }

    private View crearItemLugar(Map<String, Object> lugar) {
        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_lugar_cercano_cliente, containerLugares, false);

        // Referencias a las vistas
        TextView tvIcon = itemView.findViewById(R.id.tvLugarIcon);
        TextView tvNombre = itemView.findViewById(R.id.tvLugarNombre);
        TextView tvInfo = itemView.findViewById(R.id.tvLugarInfo);

        // Llenar datos
        String nombre = (String) lugar.get("nombre");
        String descripcion = (String) lugar.get("descripcion");

        // Manejar distancia
        Object distanciaObj = lugar.get("distancia");
        String distanciaStr = "-- km";

        if (distanciaObj != null) {
            try {
                double distancia = Double.parseDouble(distanciaObj.toString());
                distanciaStr = String.format(Locale.getDefault(), "%.1f km", distancia);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error parseando distancia para " + nombre, e);
            }
        }

        // Asignar valores
        tvIcon.setText(getIconoParaLugar(nombre));
        tvNombre.setText(nombre != null ? nombre : "Lugar sin nombre");
        tvInfo.setText(distanciaStr + " • " + (descripcion != null ? descripcion : "Sin descripción"));

        return itemView;
    }

    private String getIconoParaLugar(String nombre) {
        if (nombre == null) return "📍";

        String nombreLower = nombre.toLowerCase();

        // Categorizar por palabras clave
        if (nombreLower.contains("museo")) return "🏛️";
        if (nombreLower.contains("iglesia") || nombreLower.contains("catedral") || nombreLower.contains("basilica")) return "⛪";
        if (nombreLower.contains("mezquita")) return "🕌";
        if (nombreLower.contains("palacio") || nombreLower.contains("casa")) return "🏰";
        if (nombreLower.contains("puente")) return "🌉";
        if (nombreLower.contains("parque") || nombreLower.contains("jardín")) return "🌳";
        if (nombreLower.contains("mercado") || nombreLower.contains("plaza")) return "🏪";
        if (nombreLower.contains("teatro") || nombreLower.contains("cinema")) return "🎭";
        if (nombreLower.contains("universidad") || nombreLower.contains("escuela")) return "🏫";
        if (nombreLower.contains("hospital") || nombreLower.contains("clínica")) return "🏥";
        if (nombreLower.contains("monumento") || nombreLower.contains("estatua")) return "🗿";
        if (nombreLower.contains("centro") || nombreLower.contains("mall")) return "🏢";
        if (nombreLower.contains("playa") || nombreLower.contains("costa")) return "🏖️";
        if (nombreLower.contains("montaña") || nombreLower.contains("cerro")) return "⛰️";

        // Icono por defecto
        return "📍";
    }

    private void mostrarLoading(boolean mostrar) {
        if (loadingView != null) {
            loadingView.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        if (containerLugares != null) {
            containerLugares.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }

        if (emptyStateLugares != null) {
            emptyStateLugares.setVisibility(View.GONE);
        }
    }

    private void mostrarEmptyState(boolean mostrar, String mensaje) {
        if (emptyStateLugares != null) {
            emptyStateLugares.setVisibility(mostrar ? View.VISIBLE : View.GONE);

            if (mostrar && mensaje != null) {
                TextView tvEmptyMessage = emptyStateLugares.findViewById(R.id.tvEmptyMessage);
                if (tvEmptyMessage != null) {
                    tvEmptyMessage.setText(mensaje);
                }
            }
        }

        if (containerLugares != null) {
            containerLugares.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    private void mostrarError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
        mostrarEmptyState(true, mensaje);
    }

    // Método público para actualizar datos cuando cambie el hotel
    public void updateHotelId(String newHotelId) {
        this.hotelId = newHotelId;
        if (containerLugares != null) {
            loadLugaresCercanos();
        }
    }

    // Método para refrescar datos
    public void refresh() {
        if (hotelId != null && !hotelId.isEmpty()) {
            loadLugaresCercanos();
        }
    }
}
