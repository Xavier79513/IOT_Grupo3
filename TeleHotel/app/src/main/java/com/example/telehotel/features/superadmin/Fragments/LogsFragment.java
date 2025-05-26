package com.example.telehotel.features.superadmin.Fragments;

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
import com.example.telehotel.data.model.LogItem;
import com.example.telehotel.features.superadmin.ui.LogsAdapter;

import java.util.ArrayList;
import java.util.List;

public class LogsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        recyclerView = view.findViewById(R.id.logsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<LogItem> logItems = new ArrayList<>();
        logItems.add(new LogItem("2025-05-25 10:45 AM", "El superadmin eliminó al usuario Juan Perez."));
        logItems.add(new LogItem("2025-05-25 09:30 AM", "Se creó un nuevo administrador para la zona norte."));
        logItems.add(new LogItem("2025-05-24 06:00 PM", "El taxista Miguel Rojas cerró su sesión."));
        logItems.add(new LogItem("2025-05-24 03:15 PM", "Se actualizó la contraseña del usuario María L."));
        logItems.add(new LogItem("2025-05-24 12:00 PM", "El sistema realizó un backup automático."));

        adapter = new LogsAdapter(logItems);
        recyclerView.setAdapter(adapter);

        return view;
    }
}