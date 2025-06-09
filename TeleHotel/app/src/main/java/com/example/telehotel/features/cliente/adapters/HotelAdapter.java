package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.core.storage.PrefsManager;
import com.example.telehotel.data.model.HotelInfo;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.cliente.HotelDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private static final String TAG = "HotelAdapter";

    private List<Hotel> hotelList;
    private Context context;
    private PrefsManager prefsManager;
    private OnItemClickListener onItemClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;

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
                if (hotelName != null) {
                    String nombre = hotel.getNombre() != null ? hotel.getNombre() : "Hotel sin nombre";
                    hotelName.setText(nombre);
                    Log.d(TAG, "Nombre establecido: " + nombre);
                }

                // Descripción
                if (hotelDescription != null) {
                    String descripcion = hotel.getDescripcion() != null ? hotel.getDescripcion() : "Sin descripción";
                    hotelDescription.setText(descripcion);
                    Log.d(TAG, "Descripción establecida");
                }

                // Rating (simulado)
                setRatingDisplay(hotel);

                // Precio
                setPriceDisplay(hotel);

                // Imagen
                loadHotelImage(hotel);

                // Indicador de favorito
                updateFavoriteIndicator(hotel);

                Log.d(TAG, "Hotel bindeado exitosamente: " + hotel.getNombre());

            } catch (Exception e) {
                Log.e(TAG, "Error haciendo bind del hotel: " + hotel.getNombre(), e);
            }
        }

        private void setRatingDisplay(Hotel hotel) {
            if (hotelRating != null) {
                try {
                    double rating = 3.9 + (Math.random() * 1.1);
                    int reviewCount = (int)(Math.random() * 400) + 50;

                    String ratingText = String.format(Locale.getDefault(),
                            "⭐ %.1f  Reseñas (%d)", rating, reviewCount);
                    hotelRating.setText(ratingText);

                    Log.d(TAG, "Rating establecido: " + ratingText);
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo rating", e);
                }
            }
        }

        private void setPriceDisplay(Hotel hotel) {
            if (hotelPrice != null) {
                try {
                    double price = 0.0;

                    // Obtener precio
                    if (hotel.getHabitaciones() != null && !hotel.getHabitaciones().isEmpty()) {
                        price = hotel.getHabitaciones().stream()
                                .mapToDouble(h -> h.getPrecio())
                                .min()
                                .orElse(0.0);
                        Log.d(TAG, "Precio desde habitaciones: " + price);
                    } else if (hotel.getMontoMinimoTaxi() != null) {
                        price = hotel.getMontoMinimoTaxi();
                        Log.d(TAG, "Precio desde monto mínimo taxi: " + price);
                    }

                    if (price > 0) {
                        String priceText = String.format(Locale.getDefault(), "Desde S/.%.1f", price);
                        hotelPrice.setText(priceText);
                        Log.d(TAG, "Precio establecido: " + priceText);
                    } else {
                        hotelPrice.setText("Precio a consultar");
                        Log.d(TAG, "Precio no disponible");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error estableciendo precio", e);
                    hotelPrice.setText("Error en precio");
                }
            }
        }

        private void loadHotelImage(Hotel hotel) {
            if (hotelImage != null) {
                try {
                    if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                        String imageUrl = hotel.getImagenes().get(0);
                        Log.d(TAG, "Cargando imagen: " + imageUrl);

                        Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.sample_hotel)
                                .error(R.drawable.sample_hotel)
                                .centerCrop()
                                .into(hotelImage);
                    } else {
                        Log.d(TAG, "No hay imágenes, usando placeholder");
                        hotelImage.setImageResource(R.drawable.sample_hotel);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error cargando imagen", e);
                    hotelImage.setImageResource(R.drawable.sample_hotel);
                }
            }
        }

        private void updateFavoriteIndicator(Hotel hotel) {
            if (hotelName != null && prefsManager != null && hotel.getId() != null) {
                try {
                    boolean isFavorite = prefsManager.isHotelFavorite(hotel.getId());
                    Log.d(TAG, "Hotel " + hotel.getNombre() + " es favorito: " + isFavorite);

                    if (isFavorite) {
                        hotelName.setTextColor(context.getResources().getColor(R.color.primaryColor));
                        hotelName.setText("❤️ " + hotel.getNombre());
                    } else {
                        hotelName.setTextColor(context.getResources().getColor(android.R.color.black));
                        String name = hotel.getNombre();
                        if (name != null && name.startsWith("❤️ ")) {
                            hotelName.setText(name.substring(3));
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
                    boolean isFavorite = prefsManager.isHotelFavorite(hotel.getId());

                    // Aquí necesitarías convertir Hotel a HotelInfo
                    // Por ahora solo simulamos el toggle

                    if (isFavorite) {
                        // prefsManager.removeFavoriteHotel(hotel.getId());
                        Toast.makeText(context, "❌ Removido de favoritos", Toast.LENGTH_SHORT).show();
                    } else {
                        // prefsManager.addFavoriteHotel(hotelInfo);
                        Toast.makeText(context, "❤️ Agregado a favoritos", Toast.LENGTH_SHORT).show();
                    }

                    updateFavoriteIndicator(hotel);

                    if (onFavoriteClickListener != null) {
                        onFavoriteClickListener.onFavoriteClick(hotel, !isFavorite);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error toggling favorite", e);
                }
            }
        }
    }
}