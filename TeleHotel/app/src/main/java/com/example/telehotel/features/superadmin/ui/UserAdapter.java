package com.example.telehotel.features.superadmin.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Usuario> usuarioList;
    private OnUserActionListener listener;

    public UserAdapter(List<Usuario> usuarioList, OnUserActionListener listener) {
        this.usuarioList = usuarioList;
        this.listener = listener;
    }

    public interface OnUserActionListener {
        void onActivateClick(Usuario usuario);
        void onDeactivateClick(Usuario usuario);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtCorreo, txtEstado;
        Button btnActivar, btnDesactivar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtCorreo = itemView.findViewById(R.id.txtCorreo);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            btnActivar = itemView.findViewById(R.id.btnActivar);
            btnDesactivar = itemView.findViewById(R.id.btnDesactivar);
        }

        public void bind(Usuario usuario, OnUserActionListener listener) {
            txtNombre.setText(usuario.getName()); // usar getName() en lugar de getNombres()+getApellidos()
            txtCorreo.setText(usuario.getEmail()); // usar getEmail() en lugar de getCorreo()
            txtEstado.setText("Estado: " + usuario.getEstado());

            if ("Activo".equals(usuario.getEstado())) {
                btnActivar.setEnabled(false);
                btnActivar.setAlpha(0.5f);
                btnDesactivar.setEnabled(true);
                btnDesactivar.setAlpha(1f);
                btnDesactivar.setBackgroundColor(Color.RED);
            } else {
                btnActivar.setEnabled(true);
                btnActivar.setAlpha(1f);
                btnDesactivar.setEnabled(false);
                btnDesactivar.setAlpha(0.5f);
                btnActivar.setBackgroundColor(Color.GREEN);
            }

            btnActivar.setOnClickListener(v -> listener.onActivateClick(usuario));
            btnDesactivar.setOnClickListener(v -> listener.onDeactivateClick(usuario));
        }
    }
}