package com.example.telehotel.features.cliente.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.Hotel;
import com.example.telehotel.features.cliente.HotelDetailActivity;
import com.example.telehotel.features.cliente.adapters.HotelAdapter;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    public RoomsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHotelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Hotel> hoteles = new ArrayList<>();
        hoteles.add(new Hotel("Hotel Libertador", "⭐ 4.5 Reviews (320)", "En el corazón de Lima", "10% OFF", "$180",R.drawable.hotel1));
        hoteles.add(new Hotel("Hotel London Brigde", "⭐ 3.9 Reviews (150)", "A 2 cuadras de la plaza", "25% OFF", "$90",R.drawable.hotel2));
        hoteles.add(new Hotel("Hotel Grantley Hall ", "⭐ 3.9 Reviews (150)", "A 2 cuadras de la plaza", "25% OFF", "$90",R.drawable.hotel3));
        hoteles.add(new Hotel("Hotel London Hilon", "⭐ 3.9 Reviews (150)", "A 2 cuadras de la plaza", "25% OFF", "$90",R.drawable.hotel4));
        hoteles.add(new Hotel("Hotel Cliveden", "⭐ 3.9 Reviews (150)", "A 2 cuadras de la plaza", "25% OFF", "$90",R.drawable.hotel4));


        recyclerView.setAdapter(new HotelAdapter(hoteles));
    }
}
