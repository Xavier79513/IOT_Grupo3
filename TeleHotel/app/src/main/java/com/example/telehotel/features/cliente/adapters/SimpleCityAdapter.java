package com.example.telehotel.features.cliente.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.NominatimPlace;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SimpleCityAdapter extends RecyclerView.Adapter<SimpleCityAdapter.ViewHolder> {
    private static final String TAG = "SimpleCityAdapter";

    private List<NominatimPlace> places;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(NominatimPlace place);
    }

    public SimpleCityAdapter(List<NominatimPlace> places) {
        this.places = places != null ? places : new ArrayList<>();
        Log.d(TAG, "SimpleCityAdapter creado con " + this.places.size() + " lugares");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        Log.d(TAG, "OnItemClickListener establecido: " + (listener != null));
    }

    public void updateList(List<NominatimPlace> newPlaces) {
        Log.d(TAG, "updateList llamado - lugares anteriores: " + places.size() +
                ", nuevos lugares: " + (newPlaces != null ? newPlaces.size() : 0));

        if (newPlaces == null) {
            this.places = new ArrayList<>();
        } else {
            // Aplicar deduplicación y limpieza
            this.places = cleanAndDeduplicatePlaces(newPlaces);
        }

        Log.d(TAG, "Después de limpieza y deduplicación: " + places.size() + " lugares únicos");
        notifyDataSetChanged();
    }

    /**
     * Limpia y elimina duplicados de la lista de lugares
     */
    private List<NominatimPlace> cleanAndDeduplicatePlaces(List<NominatimPlace> originalPlaces) {
        Set<String> uniqueCityCountries = new LinkedHashSet<>();
        List<NominatimPlace> cleanedPlaces = new ArrayList<>();

        for (NominatimPlace place : originalPlaces) {
            if (place == null || place.getDisplayName() == null) {
                continue;
            }

            try {
                String displayName = place.getDisplayName();
                String[] parts = displayName.split(",");

                if (parts.length == 0) {
                    continue;
                }

                String city = parts[0].trim();
                String country = parts.length > 1 ? parts[parts.length - 1].trim() : "";

                // Crear clave única: "Ciudad, País"
                String uniqueKey = city.toLowerCase();
                if (!country.isEmpty() && !country.equalsIgnoreCase(city)) {
                    uniqueKey += ", " + country.toLowerCase();
                }

                // Solo agregar si no existe ya
                if (!uniqueCityCountries.contains(uniqueKey)) {
                    uniqueCityCountries.add(uniqueKey);
                    cleanedPlaces.add(place);

                    Log.d(TAG, "Lugar agregado: " + city +
                            (country.isEmpty() ? "" : ", " + country) +
                            " (clave: " + uniqueKey + ")");
                } else {
                    Log.d(TAG, "Lugar duplicado omitido: " + uniqueKey);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error procesando lugar: " + place.getDisplayName(), e);
            }
        }

        Log.d(TAG, "Deduplicación completada: " + originalPlaces.size() +
                " -> " + cleanedPlaces.size() + " lugares únicos");

        return cleanedPlaces;
    }

    /**
     * Formatea el nombre para mostrar: "Ciudad, País"
     */
    private String formatDisplayName(String originalDisplayName) {
        if (originalDisplayName == null || originalDisplayName.trim().isEmpty()) {
            return "Ciudad desconocida";
        }

        try {
            String[] parts = originalDisplayName.split(",");
            String city = parts.length > 0 ? parts[0].trim() : "";
            String country = parts.length > 1 ? parts[parts.length - 1].trim() : "";

            // Construir texto mostrando ciudad y país
            String textToShow = city;
            if (!country.isEmpty() && !country.equalsIgnoreCase(city)) {
                textToShow += ", " + country;
            }

            return textToShow;

        } catch (Exception e) {
            Log.e(TAG, "Error formateando display name: " + originalDisplayName, e);
            return originalDisplayName;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder llamado");

        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cliente_item_city_suggestion, parent, false);
            Log.d(TAG, "Layout cliente_item_city_suggestion inflado correctamente");
            return new ViewHolder(view);

        } catch (Exception e) {
            Log.e(TAG, "Error inflando layout personalizado, usando fallback", e);

            // Fallback: usar layout simple de Android
            try {
                View simpleView = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
                return new ViewHolder(simpleView, true);
            } catch (Exception e2) {
                Log.e(TAG, "Error en fallback también", e2);
                throw new RuntimeException("No se puede crear ViewHolder", e2);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder llamado para posición: " + position);

        try {
            if (places != null && position >= 0 && position < places.size()) {
                NominatimPlace place = places.get(position);
                if (place != null) {
                    Log.d(TAG, "Binding lugar en posición " + position + ": " + place.getDisplayName());
                    holder.bind(place, onItemClickListener);
                } else {
                    Log.e(TAG, "Place es null en posición: " + position);
                }
            } else {
                Log.e(TAG, "Posición inválida: " + position + ", total lugares: " +
                        (places != null ? places.size() : "null"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder para posición: " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = places != null ? places.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCityName;
        private TextView tvCityDetails;
        private ImageView ivLocationIcon;
        private boolean isFallback;

        public ViewHolder(@NonNull View itemView) {
            this(itemView, false);
        }

        public ViewHolder(@NonNull View itemView, boolean isFallback) {
            super(itemView);
            this.isFallback = isFallback;

            if (isFallback) {
                Log.d(TAG, "ViewHolder creado con fallback layout");
                tvCityName = itemView.findViewById(android.R.id.text1);
                tvCityDetails = itemView.findViewById(android.R.id.text2);
                ivLocationIcon = null;
            } else {
                Log.d(TAG, "ViewHolder creado con layout personalizado");
                tvCityName = itemView.findViewById(R.id.tvCityName);
                tvCityDetails = itemView.findViewById(R.id.tvCityDetails);
                ivLocationIcon = itemView.findViewById(R.id.ivLocationIcon);
            }

            Log.d(TAG, "ViewHolder inicializado - tvCityName: " + (tvCityName != null) +
                    ", tvCityDetails: " + (tvCityDetails != null) +
                    ", ivLocationIcon: " + (ivLocationIcon != null));
        }

        public void bind(NominatimPlace place, OnItemClickListener listener) {
            if (place == null) {
                Log.e(TAG, "Place es null, no se puede hacer bind");
                return;
            }

            try {
                String displayName = place.getDisplayName();
                Log.d(TAG, "Binding lugar: " + displayName);

                if (tvCityName != null) {
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        String[] parts = displayName.split(",");
                        String city = parts.length > 0 ? parts[0].trim() : "";
                        String country = parts.length > 1 ? parts[parts.length - 1].trim() : "";

                        // Mostrar ciudad como texto principal
                        tvCityName.setText(city);

                        // Mostrar país como detalles (si existe y es diferente)
                        if (tvCityDetails != null) {
                            if (!country.isEmpty() && !country.equalsIgnoreCase(city)) {
                                tvCityDetails.setText(country);
                                tvCityDetails.setVisibility(View.VISIBLE);
                            } else {
                                tvCityDetails.setVisibility(View.GONE);
                            }
                        }

                        Log.d(TAG, "Texto establecido - Ciudad: " + city +
                                ", País: " + (country.isEmpty() ? "ninguno" : country));

                    } else {
                        tvCityName.setText("Ciudad sin nombre");
                        if (tvCityDetails != null) {
                            tvCityDetails.setVisibility(View.GONE);
                        }
                        Log.w(TAG, "Display name es null o vacío");
                    }
                } else {
                    Log.e(TAG, "tvCityName es null");
                }

                // Configurar ícono si existe
                if (ivLocationIcon != null) {
                    ivLocationIcon.setVisibility(View.VISIBLE);
                }

                // Click listener
                itemView.setOnClickListener(v -> {
                    Log.d(TAG, "Click en lugar: " + place.getDisplayName());
                    if (listener != null) {
                        listener.onItemClick(place);
                    } else {
                        Log.w(TAG, "Listener es null");
                    }
                });

                Log.d(TAG, "Bind completado exitosamente para: " + place.getDisplayName());

            } catch (Exception e) {
                Log.e(TAG, "Error en bind para: " +
                        (place.getDisplayName() != null ? place.getDisplayName() : "lugar sin nombre"), e);

                // Fallback en caso de error
                if (tvCityName != null) {
                    tvCityName.setText("Error al cargar ciudad");
                }
            }
        }
    }

    /**
     * Limpia la lista de lugares
     */
    public void clear() {
        try {
            places.clear();
            notifyDataSetChanged();
            Log.d(TAG, "Lista de lugares limpiada");
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando lista", e);
        }
    }

    /**
     * Obtiene un lugar por posición
     */
    public NominatimPlace getItem(int position) {
        if (places != null && position >= 0 && position < places.size()) {
            return places.get(position);
        }
        return null;
    }
}