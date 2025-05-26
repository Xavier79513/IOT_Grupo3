package com.example.telehotel.features.cliente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.data.model.NominatimPlace;

import java.util.List;

public class CitySuggestionAdapter extends RecyclerView.Adapter<CitySuggestionAdapter.ViewHolder> {

    private List<NominatimPlace> places;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NominatimPlace place);
    }

    public CitySuggestionAdapter(List<NominatimPlace> places) {
        this.places = places;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<NominatimPlace> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NominatimPlace place = places.get(position);
        String displayName = place.getDisplayName();

        // Separar display_name por comas
        String[] parts = displayName.split(",");
        String city = parts.length > 0 ? parts[0].trim() : "";
        String country = parts.length > 1 ? parts[parts.length - 1].trim() : "";

        // Construir texto mostrando ciudad y país
        String textToShow = city;
        if (!country.isEmpty() && !country.equalsIgnoreCase(city)) {
            textToShow += ", " + country;
        }

        holder.textView.setText(textToShow);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places == null ? 0 : places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
