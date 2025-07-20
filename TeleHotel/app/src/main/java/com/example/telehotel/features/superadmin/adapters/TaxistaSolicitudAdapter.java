package com.example.telehotel.features.superadmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Usuario;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaxistaSolicitudAdapter extends RecyclerView.Adapter<TaxistaSolicitudAdapter.TaxistaViewHolder> {

    private List<Usuario> taxistasList;
    private Context context;
    private OnTaxistaActionListener listener;

    // Interface para manejar los clicks
    public interface OnTaxistaActionListener {
        void onVerDetalles(Usuario taxista);
        void onAprobar(Usuario taxista);
        void onRechazar(Usuario taxista);
    }

    public TaxistaSolicitudAdapter(Context context, List<Usuario> taxistasList) {
        this.context = context;
        this.taxistasList = taxistasList;
    }

    public void setOnTaxistaActionListener(OnTaxistaActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaxistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_taxista_solicitud, parent, false);
        return new TaxistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxistaViewHolder holder, int position) {
        Usuario taxista = taxistasList.get(position);
        holder.bind(taxista);
    }

    @Override
    public int getItemCount() {
        return taxistasList != null ? taxistasList.size() : 0;
    }

    public void updateList(List<Usuario> newList) {
        this.taxistasList = newList;
        notifyDataSetChanged();
    }

    public class TaxistaViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFotoTaxista, ivFotoAuto;
        private TextView tvNombreCompleto, tvDni, tvTelefono, tvPlacaAuto,
                tvFechaRegistro, tvEstado;
        private MaterialButton btnVerDetalles, btnAprobar, btnRechazar;
        private LinearLayout actionButtonsLayout;
        private CardView statusCard;

        public TaxistaViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            ivFotoTaxista = itemView.findViewById(R.id.ivFotoTaxista);
            ivFotoAuto = itemView.findViewById(R.id.ivFotoAuto);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompleto);
            tvDni = itemView.findViewById(R.id.tvDni);
            tvTelefono = itemView.findViewById(R.id.tvTelefono);
            tvPlacaAuto = itemView.findViewById(R.id.tvPlacaAuto);
            tvFechaRegistro = itemView.findViewById(R.id.tvFechaRegistro);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAprobar = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            statusCard = itemView.findViewById(R.id.statusCard);
        }

        private void setupClickListeners() {
            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetalles(taxistasList.get(getAdapterPosition()));
                }
            });

            btnAprobar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAprobar(taxistasList.get(getAdapterPosition()));
                }
            });

            btnRechazar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRechazar(taxistasList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Usuario taxista) {
            // Nombre completo
            String nombreCompleto = taxista.getNombres() + " " + taxista.getApellidos();
            tvNombreCompleto.setText(nombreCompleto);

            // DNI
            tvDni.setText("DNI: " + taxista.getNumeroDocumento());

            // Teléfono
            tvTelefono.setText("📱 " + taxista.getTelefono());

            // Placa
            tvPlacaAuto.setText(taxista.getPlacaAuto());

            // Fecha de registro
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaTexto = "Registrado: Hoy"; // Por defecto

            // TODO: Aquí podrías usar una fecha real del objeto taxista si la tienes
            // if (taxista.getFechaRegistro() != null) {
            //     fechaTexto = "Registrado: " + sdf.format(taxista.getFechaRegistro());
            // }
            tvFechaRegistro.setText(fechaTexto);

            // Estado y configuración de UI según el estado
            configurarEstado(taxista.getEstado());

            // Cargar fotos (placeholder por ahora)
            // TODO: Implementar carga de imágenes con Glide o similar
            // Glide.with(context).load(taxista.getFotoUrl()).into(ivFotoTaxista);
            // Glide.with(context).load(taxista.getFotoAuto()).into(ivFotoAuto);
        }

        private void configurarEstado(String estado) {
            switch (estado.toLowerCase()) {
                case "pendiente":
                    tvEstado.setText("PENDIENTE");
                    statusCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange_light));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.orange_dark));
                    actionButtonsLayout.setVisibility(View.VISIBLE);
                    break;

                case "aprobado":
                case "activo":
                    tvEstado.setText("APROBADO");
                    statusCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_light));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.green_dark));
                    actionButtonsLayout.setVisibility(View.GONE);
                    break;

                case "rechazado":
                case "inactivo":
                    tvEstado.setText("RECHAZADO");
                    statusCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.red_dark));
                    actionButtonsLayout.setVisibility(View.GONE);
                    break;

                default:
                    tvEstado.setText("DESCONOCIDO");
                    statusCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_light));
                    tvEstado.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
                    actionButtonsLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }
}