package com.example.telehotel.features.cliente.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.adapters.PhotoAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HotelDetailFotosFragment extends Fragment {

    private static final String TAG = "HotelDetailFotosFragment";
    private static final String ARG_HOTEL_ID = "hotel_id";

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;

    // Firebase
    private FirebaseFirestore db;
    private PrefsManager prefsManager;

    // Data
    private String hotelId;
    private List<String> hotelImages;

    public HotelDetailFotosFragment() {
        // Constructor vacío requerido
    }

    /**
     * Método estático para crear una nueva instancia del fragment con parámetros
     */
    public static HotelDetailFotosFragment newInstance(String hotelId) {
        HotelDetailFotosFragment fragment = new HotelDetailFotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOTEL_ID, hotelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase y PrefsManager
        db = FirebaseFirestore.getInstance();
        prefsManager = new PrefsManager(requireContext());

        // SOLUCIÓN: Obtener hotelId igual que HotelDetailReservaFragment
        obtenerHotelId();
    }

    /**
     * MÉTODO CRÍTICO: Obtener hotelId de la misma manera que HotelDetailReservaFragment
     */
    private void obtenerHotelId() {
        // 1. Intentar obtener hotelId desde la actividad padre (método principal)
        if (getActivity() != null && getActivity().getIntent() != null) {
            hotelId = getActivity().getIntent().getStringExtra("hotelId");
            Log.d(TAG, "HotelId obtenido desde actividad: " + hotelId);
        }

        // 2. Si no se encuentra, intentar desde argumentos del fragment
        if ((hotelId == null || hotelId.isEmpty()) && getArguments() != null) {
            hotelId = getArguments().getString(ARG_HOTEL_ID);
            Log.d(TAG, "HotelId obtenido desde argumentos: " + hotelId);
        }

        // 3. Fallback: intentar desde PrefsManager
        if (hotelId == null || hotelId.isEmpty()) {
            hotelId = prefsManager.getHotelId();
            Log.d(TAG, "HotelId obtenido desde PrefsManager: " + hotelId);
        }

        // Log final para debugging
        if (hotelId == null || hotelId.isEmpty()) {
            Log.e(TAG, "ERROR CRÍTICO: No se pudo obtener hotelId de ninguna fuente");
        } else {
            Log.d(TAG, "HotelId final: " + hotelId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_hotel_detail_fotos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        cargarImagenesHotel();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.photoGalleryRecyclerView);

        // Verificar que el RecyclerView se encontró
        if (recyclerView == null) {
            Log.e(TAG, "ERROR: RecyclerView no encontrado en el layout");
        }
    }

    private void setupRecyclerView() {
        if (recyclerView == null) {
            Log.e(TAG, "No se puede configurar RecyclerView porque es null");
            return;
        }

        // Configurar GridLayoutManager con 3 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Inicializar con lista vacía
        hotelImages = new ArrayList<>();
        adapter = new PhotoAdapter(requireContext(), hotelImages, this::onImageClick);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "RecyclerView configurado correctamente");
    }

    private void cargarImagenesHotel() {
        // Verificar nuevamente si tenemos hotelId válido antes de cargar
        if (hotelId == null || hotelId.trim().isEmpty()) {
            Log.e(TAG, "Hotel ID no disponible para cargar imágenes");
            cargarImagenesDefault();
            return;
        }

        Log.d(TAG, "Cargando imágenes para hotel: " + hotelId);

        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Hotel hotel = documentSnapshot.toObject(Hotel.class);
                        if (hotel != null && hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                            mostrarImagenesHotel(hotel.getImagenes());
                        } else {
                            Log.w(TAG, "Hotel sin imágenes disponibles");
                            cargarImagenesDefault();
                        }
                    } else {
                        Log.w(TAG, "Hotel no encontrado con ID: " + hotelId);
                        cargarImagenesDefault();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando hotel: " + hotelId, e);
                    cargarImagenesDefault();
                    Toast.makeText(getContext(), "Error cargando imágenes", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarImagenesHotel(List<String> imagenes) {
        if (hotelImages == null) {
            hotelImages = new ArrayList<>();
        }

        hotelImages.clear();

        // Filtrar URLs válidas
        for (String imagen : imagenes) {
            if (imagen != null && !imagen.trim().isEmpty() && esUrlValida(imagen)) {
                hotelImages.add(imagen);
            }
        }

        if (!hotelImages.isEmpty()) {
            if (adapter != null) {
                adapter.updateImages(hotelImages);
            }
            Log.d(TAG, "Imágenes cargadas: " + hotelImages.size());
        } else {
            Log.w(TAG, "No hay imágenes válidas disponibles");
            cargarImagenesDefault();
        }
    }

    private void cargarImagenesDefault() {
        // Imágenes por defecto si no hay imágenes del hotel
        List<String> imagenesDefault = Arrays.asList(
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=400",
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=400",
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=400",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=400",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=400",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400",
                "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=400",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=400",
                "https://images.unsplash.com/photo-1549294413-26f195200c16?w=400",
                "https://images.unsplash.com/photo-1596178065887-1198b6148b2b?w=400",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400",
                "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        );

        if (hotelImages == null) {
            hotelImages = new ArrayList<>();
        }

        hotelImages.clear();
        hotelImages.addAll(imagenesDefault);

        if (adapter != null) {
            adapter.updateImages(hotelImages);
        }

        Log.d(TAG, "Cargadas imágenes por defecto: " + hotelImages.size());
    }

    private boolean esUrlValida(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void onImageClick(String imageUrl, int position) {
        // Por ahora solo mostrar un toast, puedes implementar vista en pantalla completa después
        Toast.makeText(getContext(), "Imagen " + (position + 1) + " clickeada", Toast.LENGTH_SHORT).show();

        // TODO: Implementar vista en pantalla completa
        Log.d(TAG, "Imagen clickeada: " + imageUrl + " en posición: " + position);
    }

    /**
     * Método para actualizar las imágenes desde el fragment padre
     */
    public void actualizarImagenes(List<String> nuevasImagenes) {
        if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
            mostrarImagenesHotel(nuevasImagenes);
        } else {
            cargarImagenesDefault();
        }
    }

    /**
     * Método para actualizar el hotel ID
     */
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
        Log.d(TAG, "HotelId actualizado a: " + hotelId);
        if (isAdded()) {
            cargarImagenesHotel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar imágenes si es necesario
        if (hotelImages == null || hotelImages.isEmpty()) {
            // Intentar obtener hotelId nuevamente si no lo tenemos
            if (hotelId == null || hotelId.isEmpty()) {
                obtenerHotelId();
            }
            cargarImagenesHotel();
        }
    }

    /**
     * Método de debugging para verificar el estado del fragment
     */
    public void debugInfo() {
        Log.d(TAG, "=== DEBUG INFO ===");
        Log.d(TAG, "HotelId: " + hotelId);
        Log.d(TAG, "Adapter: " + (adapter != null ? "OK" : "NULL"));
        Log.d(TAG, "RecyclerView: " + (recyclerView != null ? "OK" : "NULL"));
        Log.d(TAG, "Images count: " + (hotelImages != null ? hotelImages.size() : "NULL"));
        Log.d(TAG, "Fragment added: " + isAdded());
        Log.d(TAG, "Activity: " + (getActivity() != null ? "OK" : "NULL"));
        Log.d(TAG, "================");
    }
}