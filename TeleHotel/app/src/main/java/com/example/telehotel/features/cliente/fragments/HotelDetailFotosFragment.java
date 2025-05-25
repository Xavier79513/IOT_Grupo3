package com.example.telehotel.features.cliente.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.adapters.PhotoAdapter;

import java.util.Arrays;
import java.util.List;

public class HotelDetailFotosFragment extends Fragment {

    public HotelDetailFotosFragment() {
        // Constructor vacío requerido
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

        RecyclerView recyclerView = view.findViewById(R.id.photoGalleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        List<Integer> photos = Arrays.asList(
                R.drawable.foto1, R.drawable.foto2, R.drawable.foto3,
                R.drawable.foto4, R.drawable.foto5, R.drawable.foto6,
                R.drawable.foto7, R.drawable.foto8, R.drawable.foto9,
                R.drawable.foto10, R.drawable.foto11, R.drawable.foto12
        );

        PhotoAdapter adapter = new PhotoAdapter(requireContext(), photos);
        recyclerView.setAdapter(adapter);
    }
}
