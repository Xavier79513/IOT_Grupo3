package com.example.telehotel.features.superadmin.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.telehotel.R;
import com.example.telehotel.data.model.LogSistema;
import com.example.telehotel.features.superadmin.ui.LogsAdapter;
import com.example.telehotel.core.utils.LogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView logsCount;
    private View emptyStateLayout;
    private MaterialButton clearLogsButton;
    private List<LogSistema> logsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.superadmin_fragment_logs, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadLogs();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.logsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        logsCount = view.findViewById(R.id.logsCount);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        clearLogsButton = view.findViewById(R.id.clearLogsButton);
        logsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LogsAdapter(logsList);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadLogs);

        clearLogsButton.setOnClickListener(v -> showClearLogsDialog());
    }

    private void loadLogs() {
        swipeRefreshLayout.setRefreshing(true);

        LogUtils.obtenerLogs(100, new LogUtils.LogsCallback() {
            @Override
            public void onSuccess(com.google.firebase.firestore.QuerySnapshot logs) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    logsList.clear();

                    for (DocumentSnapshot document : logs.getDocuments()) {
                        LogSistema log = document.toObject(LogSistema.class);
                        if (log != null) {
                            log.setId(document.getId());
                            logsList.add(log);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateUI();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(),
                            "Error al cargar logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI();
                });
            }
        });
    }

    private void updateUI() {
        int count = logsList.size();
        logsCount.setText(count + (count == 1 ? " registro" : " registros"));

        if (count == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        clearLogsButton.setEnabled(count > 0);
    }

    private void showClearLogsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Limpiar Registros")
                .setMessage("¿Estás seguro de que deseas eliminar todos los registros? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> clearLogs())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearLogs() {
        LogUtils.limpiarLogs(new LogUtils.LogCallback() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    loadLogs(); // Recargar la lista

                    // Registrar la acción de limpieza
                    LogUtils.logSistema("Se eliminaron todos los registros del sistema");
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Error al limpiar logs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogs(); // Recargar cuando se vuelve al fragment
    }
}