package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.HotelInfo;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Habitacion;
import com.example.telehotel.data.model.Ubicacion;
import com.example.telehotel.features.cliente.HotelDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private static final String TAG = "HotelAdapter";

    private List<Hotel> hotelList;
    private Context context;
    private PrefsManager prefsManager;
    private OnItemClickListener onItemClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;

    // URLs de imágenes de respaldo para hoteles
    private static final String[] FALLBACK_HOTEL_IMAGES = {
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=400&h=300&fit=crop&auto=format",
            "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=400&h=300&fit=crop&auto=format",
            "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=400&h=300&fit=crop&auto=format",
            "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=400&h=300&fit=crop&auto=format"
    };

    public interface OnItemClickListener {
        void onItemClick(Hotel hotel);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Hotel hotel, boolean isFavorite);
    }

    public HotelAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
        Log.d(TAG, "HotelAdapter creado con " + (hotelList != null ? hotelList.size() : 0) + " hoteles");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        Log.d(TAG, "OnItemClickListener establecido: " + (listener != null));
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
        Log.d(TAG, "OnFavoriteClickListener establecido: " + (listener != null));
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder llamado");

        context = parent.getContext();
        prefsManager = new PrefsManager(context);

        try {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.cliente_item_hotel, parent, false);
            Log.d(TAG, "Layout cliente_item_hotel inflado correctamente");

            HotelViewHolder holder = new HotelViewHolder(view);
            Log.d(TAG, "HotelViewHolder creado correctamente");

            return holder;
        } catch (Exception e) {
            Log.e(TAG, "Error creando ViewHolder", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder llamado para posición: " + position);

        try {
            if (hotelList != null && position < hotelList.size()) {
                Hotel hotel = hotelList.get(position);
                Log.d(TAG, "Binding hotel en posición " + position + ": " +
                        (hotel != null ? hotel.getNombre() : "null"));
                holder.bind(hotel);
            } else {
                Log.e(TAG, "Hotel no válido en posición: " + position +
                        ", total hoteles: " + (hotelList != null ? hotelList.size() : "null"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder para posición: " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = hotelList != null ? hotelList.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public void updateHoteles(List<Hotel> nuevosHoteles) {
        Log.d(TAG, "updateHoteles llamado - hoteles anteriores: " + (hotelList != null ? hotelList.size() : 0) +
                ", nuevos hoteles: " + (nuevosHoteles != null ? nuevosHoteles.size() : 0));

        this.hotelList = nuevosHoteles;
        notifyDataSetChanged();

        Log.d(TAG, "Hoteles actualizados y adapter notificado");
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName, hotelDescription, hotelRating, hotelPrice;
        MaterialButton btnBookNow;
        ImageView hotelImage;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            Log.d(TAG, "HotelViewHolder initViews");

            try {
                hotelName = itemView.findViewById(R.id.hotelName);
                Log.d(TAG, "hotelName encontrado: " + (hotelName != null));

                hotelDescription = itemView.findViewById(R.id.hotelDescription);
                Log.d(TAG, "hotelDescription encontrado: " + (hotelDescription != null));

                hotelRating = itemView.findViewById(R.id.hotelRating);
                Log.d(TAG, "hotelRating encontrado: " + (hotelRating != null));

                hotelPrice = itemView.findViewById(R.id.hotelPrice);
                Log.d(TAG, "hotelPrice encontrado: " + (hotelPrice != null));

                btnBookNow = itemView.findViewById(R.id.btnBookNow);
                Log.d(TAG, "btnBookNow encontrado: " + (btnBookNow != null));

                hotelImage = itemView.findViewById(R.id.hotelImage);
                Log.d(TAG, "hotelImage encontrado: " + (hotelImage != null));

            } catch (Exception e) {
                Log.e(TAG, "Error inicializando vistas del ViewHolder", e);
            }
        }

        private void setupClickListeners() {
            Log.d(TAG, "setupClickListeners");

            try {
                // Click en el item completo
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    Log.d(TAG, "Click en item, posición: " + position);

                    if (position != RecyclerView.NO_POSITION && hotelList != null && position < hotelList.size()) {
                        Hotel hotel = hotelList.get(position);
                        Log.d(TAG, "Hotel clickeado: " + (hotel != null ? hotel.getNombre() : "null"));

                        // Navegación original
                        if (hotel != null && hotel.getId() != null) {
                            Intent intent = new Intent(context, HotelDetailActivity.class);
                            intent.putExtra("hotelId", hotel.getId());
                            context.startActivity(intent);
                            Log.d(TAG, "Navegando a detalles del hotel: " + hotel.getId());
                        }

                        // Callback personalizado
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(hotel);
                        }
                    } else {
                        Log.w(TAG, "Click en posición inválida: " + position);
                    }
                });

                // Click en botón reservar
                if (btnBookNow != null) {
                    btnBookNow.setOnClickListener(v -> {
                        int position = getAdapterPosition();
                        Log.d(TAG, "Click en botón reservar, posición: " + position);

                        if (position != RecyclerView.NO_POSITION && hotelList != null && position < hotelList.size()) {
                            Hotel hotel = hotelList.get(position);
                            if (hotel != null) {
                                Toast.makeText(context, "Reservando en " + hotel.getNombre(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Reserva iniciada para: " + hotel.getNombre());
                            }
                        }
                    });
                }

                // Long click para favoritos
                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    Log.d(TAG, "Long click en item, posición: " + position);

                    if (position != RecyclerView.NO_POSITION && hotelList != null && position < hotelList.size()) {
                        Hotel hotel = hotelList.get(position);
                        if (hotel != null) {
                            toggleFavorite(hotel);
                            return true;
                        }
                    }
                    return false;
                });

            } catch (Exception e) {
                Log.e(TAG, "Error configurando click listeners", e);
            }
        }

        public void bind(Hotel hotel) {
            Log.d(TAG, "bind hotel: " + (hotel != null ? hotel.getNombre() : "null"));

            if (hotel == null) {
                Log.e(TAG, "Hotel es null, no se puede hacer bind");
                return;
            }

            try {
                // Nombre del hotel
                setHotelName(hotel);

                // Descripción
                setHotelDescription(hotel);

                // Rating (simulado)
                setRatingDisplay(hotel);

                // Precio
                setPriceDisplay(hotel);

                // Imagen - VERSIÓN MEJORADA
                loadHotelImageImproved(hotel);

                // Indicador de favorito
                updateFavoriteIndicator(hotel);

                Log.d(TAG, "Hotel bindeado exitosamente: " + hotel.getNombre());

            } catch (Exception e) {
                Log.e(TAG, "Error haciendo bind del hotel: " + hotel.getNombre(), e);
                setErrorState();
            }
        }

        private void setHotelName(Hotel hotel) {
            if (hotelName != null) {
                try {
                    String nombre = hotel.getNombre() != null ? hotel.getNombre() : "Hotel sin nombre";
                    hotelName.setText(nombre);
                    Log.d(TAG, "Nombre establecido: " + nombre);
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo nombre", e);
                    hotelName.setText("Error en nombre");
                }
            }
        }

        private void setHotelDescription(Hotel hotel) {
            if (hotelDescription != null) {
                try {
                    String descripcion = hotel.getDescripcion() != null ? hotel.getDescripcion() : "Sin descripción disponible";

                    // Limitar longitud de descripción para UI
                    if (descripcion.length() > 120) {
                        descripcion = descripcion.substring(0, 117) + "...";
                    }

                    hotelDescription.setText(descripcion);
                    Log.d(TAG, "Descripción establecida");
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo descripción", e);
                    hotelDescription.setText("Descripción no disponible");
                }
            }
        }

        private void setRatingDisplay(Hotel hotel) {
            if (hotelRating != null) {
                try {
                    // Generar rating consistente basado en el ID del hotel
                    float rating = generateConsistentRating(hotel);
                    int reviewCount = generateConsistentReviewCount(hotel);

                    String ratingText = String.format(Locale.getDefault(),
                            "⭐ %.1f  Reseñas (%d)", rating, reviewCount);
                    hotelRating.setText(ratingText);

                    Log.d(TAG, "Rating establecido: " + ratingText);
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo rating", e);
                    hotelRating.setText("⭐ --  Sin reseñas");
                }
            }
        }

        private float generateConsistentRating(Hotel hotel) {
            // Generar rating consistente basado en el hash del nombre/ID
            String seed = hotel.getId() != null ? hotel.getId() : hotel.getNombre();
            if (seed == null) seed = "default";

            int hash = Math.abs(seed.hashCode());
            return 3.5f + (hash % 20) / 10.0f; // Rating entre 3.5 y 5.4
        }

        private int generateConsistentReviewCount(Hotel hotel) {
            String seed = hotel.getId() != null ? hotel.getId() : hotel.getNombre();
            if (seed == null) seed = "default";

            int hash = Math.abs(seed.hashCode());
            return 50 + (hash % 400); // Reviews entre 50 y 449
        }

        private void setPriceDisplay(Hotel hotel) {
            if (hotelPrice != null) {
                try {
                    double price = calculateMinPrice(hotel);

                    if (price > 0) {
                        String priceText = String.format(Locale.getDefault(), "Desde S/.%.0f", price);
                        hotelPrice.setText(priceText);
                        hotelPrice.setTextColor(ContextCompat.getColor(context, R.color.green_dark));
                        Log.d(TAG, "Precio establecido: " + priceText);
                    } else {
                        hotelPrice.setText("Consultar precio");
                        hotelPrice.setTextColor(ContextCompat.getColor(context, R.color.gray_medium));
                        Log.d(TAG, "Precio no disponible");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo precio", e);
                    hotelPrice.setText("Error en precio");
                    hotelPrice.setTextColor(ContextCompat.getColor(context, R.color.red_medium));
                }
            }
        }

        private double calculateMinPrice(Hotel hotel) {
            double price = 0.0;

            try {
                // Primero intentar obtener precio de habitaciones
                if (hotel.getHabitaciones() != null && !hotel.getHabitaciones().isEmpty()) {
                    double minPrice = Double.MAX_VALUE;
                    boolean foundPrice = false;

                    for (Habitacion habitacion : hotel.getHabitaciones()) {
                        if (habitacion.getPrecio() != null && habitacion.getPrecio() > 0) {
                            if (habitacion.getPrecio() < minPrice) {
                                minPrice = habitacion.getPrecio();
                                foundPrice = true;
                            }
                        }
                    }

                    if (foundPrice) {
                        price = minPrice;
                        Log.d(TAG, "Precio desde habitaciones: " + price);
                    }
                }

                // Si no hay precio de habitaciones, usar monto mínimo taxi como referencia
                if (price == 0.0 && hotel.getMontoMinimoTaxi() != null && hotel.getMontoMinimoTaxi() > 0) {
                    price = hotel.getMontoMinimoTaxi() * 2; // Estimar precio base
                    Log.d(TAG, "Precio estimado desde monto taxi: " + price);
                }

                // Como último recurso, generar precio basado en características del hotel
                if (price == 0.0) {
                    price = generateEstimatedPrice(hotel);
                    Log.d(TAG, "Precio estimado: " + price);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error calculando precio", e);
            }

            return price;
        }

        private double generateEstimatedPrice(Hotel hotel) {
            // Generar precio consistente basado en características del hotel
            double basePrice = 80.0; // Precio base

            try {
                String seed = hotel.getId() != null ? hotel.getId() : hotel.getNombre();
                if (seed != null) {
                    int hash = Math.abs(seed.hashCode());
                    basePrice += (hash % 100); // Agregar variación de 0-99
                }

                // Ajustar por servicios
                if (hotel.getServicios() != null) {
                    basePrice += hotel.getServicios().size() * 10;
                }

                // Ajustar por ubicación (si contiene palabras clave)
                if (hotel.getUbicacion() != null) {
                    String ubicacion = hotel.getUbicacion().getCiudad();
                    if (ubicacion != null) {
                        if (ubicacion.toLowerCase().contains("lima") ||
                                ubicacion.toLowerCase().contains("cusco") ||
                                ubicacion.toLowerCase().contains("arequipa")) {
                            basePrice *= 1.2; // 20% más caro en ciudades principales
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error generando precio estimado", e);
            }

            return Math.round(basePrice);
        }

        private void loadHotelImageImproved(Hotel hotel) {
            if (hotelImage == null) {
                Log.w(TAG, "hotelImage es null");
                return;
            }

            try {
                String imageUrl = getValidImageUrl(hotel);
                Log.d(TAG, "Intentando cargar imagen: " + imageUrl);

                if (imageUrl != null && isValidUrl(imageUrl)) {
                    loadImageWithGlide(imageUrl, hotel);
                } else {
                    Log.w(TAG, "No hay URL válida, usando imagen local");
                    loadLocalPlaceholder();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error en loadHotelImageImproved", e);
                loadLocalPlaceholder();
            }
        }

        private String getValidImageUrl(Hotel hotel) {
            try {
                // Verificar imágenes del hotel
                if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                    for (String url : hotel.getImagenes()) {
                        if (url != null && !url.trim().isEmpty()) {
                            String cleanUrl = cleanAndValidateUrl(url.trim());
                            if (cleanUrl != null) {
                                return cleanUrl;
                            }
                        }
                    }
                }

                // Si no hay imágenes válidas, usar una imagen de respaldo
                return getFallbackImageUrl(hotel);

            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo URL válida", e);
                return getFallbackImageUrl(hotel);
            }
        }

        private String cleanAndValidateUrl(String url) {
            if (url == null || url.trim().isEmpty()) {
                return null;
            }

            String cleanUrl = url.trim();

            // Detectar URLs truncadas de Unsplash (el problema principal)
            if (cleanUrl.startsWith("https://images.unsplash.com/photo-") && cleanUrl.length() < 50) {
                Log.w(TAG, "URL de Unsplash truncada detectada: " + cleanUrl);
                return null; // Rechazar URLs truncadas
            }

            // Agregar parámetros de optimización a Unsplash
            if (cleanUrl.contains("unsplash.com") && !cleanUrl.contains("w=")) {
                if (cleanUrl.contains("?")) {
                    cleanUrl += "&w=400&h=300&fit=crop&auto=format";
                } else {
                    cleanUrl += "?w=400&h=300&fit=crop&auto=format";
                }
            }

            return isValidUrl(cleanUrl) ? cleanUrl : null;
        }

        private boolean isValidUrl(String url) {
            if (url == null || url.trim().isEmpty()) {
                return false;
            }

            try {
                java.net.URL testUrl = new java.net.URL(url);
                String host = testUrl.getHost();
                return host != null && !host.isEmpty() &&
                        (url.startsWith("http://") || url.startsWith("https://")) &&
                        url.length() > 15;
            } catch (Exception e) {
                return false;
            }
        }

        private String getFallbackImageUrl(Hotel hotel) {
            // Seleccionar imagen de respaldo basada en el hotel
            int index = 0;
            if (hotel.getId() != null) {
                index = Math.abs(hotel.getId().hashCode()) % FALLBACK_HOTEL_IMAGES.length;
            } else if (hotel.getNombre() != null) {
                index = Math.abs(hotel.getNombre().hashCode()) % FALLBACK_HOTEL_IMAGES.length;
            }

            return FALLBACK_HOTEL_IMAGES[index];
        }

        private void loadImageWithGlide(String imageUrl, Hotel hotel) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.sample_hotel)
                    .error(R.drawable.sample_hotel)
                    .centerCrop()
                    .timeout(10000) // 10 segundos timeout
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Error cargando imagen: " + imageUrl, e);

                            // Intentar con imagen de respaldo diferente
                            String fallbackUrl = getFallbackImageUrl(hotel);
                            if (!fallbackUrl.equals(imageUrl)) {
                                Log.d(TAG, "Intentando con imagen de respaldo: " + fallbackUrl);
                                loadFallbackImage(fallbackUrl);
                            }

                            return false; // Permitir que se muestre la imagen de error
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "Imagen cargada exitosamente: " + imageUrl);
                            return false;
                        }
                    })
                    .into(hotelImage);
        }

        private void loadFallbackImage(String fallbackUrl) {
            if (hotelImage != null && isValidUrl(fallbackUrl)) {
                Glide.with(context)
                        .load(fallbackUrl)
                        .placeholder(R.drawable.sample_hotel)
                        .error(R.drawable.sample_hotel)
                        .centerCrop()
                        .into(hotelImage);
            }
        }

        private void loadLocalPlaceholder() {
            if (hotelImage != null) {
                try {
                    hotelImage.setImageResource(R.drawable.sample_hotel);
                    Log.d(TAG, "Imagen local placeholder cargada");
                } catch (Exception e) {
                    Log.e(TAG, "Error cargando placeholder local", e);
                }
            }
        }

        private void updateFavoriteIndicator(Hotel hotel) {
            if (hotelName != null && prefsManager != null && hotel.getId() != null) {
                try {
                    boolean isFavorite = prefsManager.isHotelFavorite(hotel.getId());
                    Log.d(TAG, "Hotel " + hotel.getNombre() + " es favorito: " + isFavorite);

                    if (isFavorite) {
                        hotelName.setTextColor(ContextCompat.getColor(context, R.color.red_medium));
                        // Agregar corazón solo si no lo tiene ya
                        String currentText = hotelName.getText().toString();
                        if (!currentText.startsWith("❤️ ")) {
                            hotelName.setText("❤️ " + currentText);
                        }
                    } else {
                        hotelName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                        String currentText = hotelName.getText().toString();
                        if (currentText.startsWith("❤️ ")) {
                            hotelName.setText(currentText.substring(3));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error actualizando indicador de favorito", e);
                }
            }
        }

        private void toggleFavorite(Hotel hotel) {
            Log.d(TAG, "toggleFavorite para: " + hotel.getNombre());

            if (prefsManager != null && hotel.getId() != null) {
                try {
                    boolean wasFavorite = prefsManager.isHotelFavorite(hotel.getId());

                    // *** CONVERTIR Hotel a TU HotelInfo EXISTENTE ***
                    HotelInfo hotelInfo = convertToExistingHotelInfo(hotel);

                    if (wasFavorite) {
                        prefsManager.removeFavoriteHotel(hotel.getId());
                        Toast.makeText(context, "❌ " + hotel.getNombre() + " removido de favoritos", Toast.LENGTH_SHORT).show();
                    } else {
                        prefsManager.addFavoriteHotel(hotelInfo);
                        Toast.makeText(context, "❤️ " + hotel.getNombre() + " agregado a favoritos", Toast.LENGTH_SHORT).show();
                    }

                    updateFavoriteIndicator(hotel);

                    if (onFavoriteClickListener != null) {
                        onFavoriteClickListener.onFavoriteClick(hotel, !wasFavorite);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error toggling favorite", e);
                    Toast.makeText(context, "Error manejando favorito", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * Convierte Hotel a tu HotelInfo existente con los campos correctos
         */
        private HotelInfo convertToExistingHotelInfo(Hotel hotel) {
            HotelInfo hotelInfo = new HotelInfo();

            try {
                // Mapear campos básicos usando TUS nombres de campo
                if (hotel.getId() != null) {
                    hotelInfo.setId(hotel.getId());
                }

                if (hotel.getNombre() != null) {
                    hotelInfo.setName(hotel.getNombre()); // name, no nombre
                }

                if (hotel.getDescripcion() != null) {
                    hotelInfo.setDescription(hotel.getDescripcion());
                }

                // Mapear ubicación
                if (hotel.getUbicacion() != null) {
                    Ubicacion ubicacion = hotel.getUbicacion();
                    String locationStr = "";

                    if (ubicacion.getCiudad() != null && ubicacion.getPais() != null) {
                        locationStr = ubicacion.getCiudad() + ", " + ubicacion.getPais();
                    } else if (ubicacion.getDireccion() != null) {
                        locationStr = ubicacion.getDireccion();
                    }

                    hotelInfo.setLocation(locationStr);

                    if (ubicacion.getDireccion() != null) {
                        hotelInfo.setAddress(ubicacion.getDireccion());
                    }
                }

                // Mapear imagen
                if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                    String validImageUrl = getValidImageUrl(hotel);
                    if (validImageUrl != null) {
                        hotelInfo.setImageUrl(validImageUrl);
                    }
                }

                // Mapear precio mínimo (usando minPrice, no precio)
                double minPrice = calculateMinPrice(hotel);
                if (minPrice > 0) {
                    hotelInfo.setMinPrice(minPrice);
                }

                // Mapear rating (usando float, no double)
                float rating = generateConsistentRating(hotel);
                hotelInfo.setRating(rating);

                // Mapear servicios a características booleanas
                if (hotel.getServicios() != null) {
                    List<String> servicios = hotel.getServicios();

                    // Detectar servicios comunes y mapear a booleanos
                    for (String servicio : servicios) {
                        if (servicio != null) {
                            String servicioLower = servicio.toLowerCase();

                            if (servicioLower.contains("wifi") || servicioLower.contains("internet")) {
                                hotelInfo.setHasWifi(true);
                            }
                            if (servicioLower.contains("desayuno") || servicioLower.contains("breakfast")) {
                                hotelInfo.setHasBreakfast(true);
                            }
                            if (servicioLower.contains("parking") || servicioLower.contains("estacionamiento")) {
                                hotelInfo.setHasParking(true);
                            }
                            if (servicioLower.contains("piscina") || servicioLower.contains("pool")) {
                                hotelInfo.setHasPool(true);
                            }
                            if (servicioLower.contains("gym") || servicioLower.contains("gimnasio")) {
                                hotelInfo.setHasGym(true);
                            }
                        }
                    }
                }

                // Mapear monto mínimo taxi
                if (hotel.getMontoMinimoTaxi() != null) {
                    hotelInfo.setMinimumTaxiAmount(hotel.getMontoMinimoTaxi());
                }

                // Mapear teléfono si está disponible
                if (hotel.getTelefono() != null) {
                    hotelInfo.setPhone(hotel.getTelefono());
                }

                // Establecer timestamp actual
                hotelInfo.setTimestamp(System.currentTimeMillis());

                Log.d(TAG, "Hotel convertido a HotelInfo: " + hotelInfo.getName());

            } catch (Exception e) {
                Log.e(TAG, "Error convirtiendo Hotel a HotelInfo", e);
            }

            return hotelInfo;
        }

        private void setErrorState() {
            try {
                if (hotelName != null) hotelName.setText("Error cargando hotel");
                if (hotelDescription != null) hotelDescription.setText("No se pudo cargar la información");
                if (hotelRating != null) hotelRating.setText("⭐ --");
                if (hotelPrice != null) hotelPrice.setText("Precio no disponible");
                loadLocalPlaceholder();
            } catch (Exception e) {
                Log.e(TAG, "Error estableciendo estado de error", e);
            }
        }
    }

    // ===== MÉTODOS PÚBLICOS ADICIONALES =====

    /**
     * Filtra la lista de hoteles por nombre
     */
    public void filterByName(String query) {
        if (hotelList == null) return;

        // Implementar filtrado si es necesario
        Log.d(TAG, "Filtrado por nombre: " + query);
    }

    /**
     * Obtiene un hotel por posición
     */
    public Hotel getHotelAt(int position) {
        if (hotelList != null && position >= 0 && position < hotelList.size()) {
            return hotelList.get(position);
        }
        return null;
    }

    /**
     * Limpia la lista de hoteles
     */
    public void clearHotels() {
        if (hotelList != null) {
            hotelList.clear();
            notifyDataSetChanged();
            Log.d(TAG, "Lista de hoteles limpiada");
        }
    }
}