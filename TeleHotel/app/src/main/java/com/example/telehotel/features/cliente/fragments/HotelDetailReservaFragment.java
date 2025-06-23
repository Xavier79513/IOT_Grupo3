package com.example.telehotel.features.cliente.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.telehotel.R;
import com.example.telehotel.core.utils.PrefsManager;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Servicio;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.features.cliente.HotelHabitacionActivity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HotelDetailReservaFragment extends Fragment {

    private static final String TAG = "HotelDetailReserva";

    private String hotelId;
    private Hotel hotelActual;
    private List<Servicio> serviciosCompletos = new ArrayList<>();

    // Views principales (manteniendo IDs existentes)
    private TextView hotelDescription, hotelPhone, hotelLocation;
    private TextView txtCheckIn, txtCheckOut, txtGuests;

    // Views del layout actual
    private LinearLayout btnCheckIn, btnCheckOut, btnGuests;
    private TextView btnCall, btnMap;
    private TextView btnCheckAvailability;
    private LinearLayout pricePreview;
    private TextView txtEstimatedPrice;
    private TextView viewRooms;
    private LinearLayout facilitiesGrid; // Este será el contenedor dinámico

    private PrefsManager prefsManager;

    private final String CHANNEL_ID = "hotel_booking_channel";
    private final int NOTIFICATION_ID = 101;

    public HotelDetailReservaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cliente_fragment_hotel_detail_reserva, container, false);

        // Inicializar PrefsManager
        prefsManager = new PrefsManager(requireContext());

        // Inicializar vistas
        inicializarVistas(view);

        // Obtener hotelId de la actividad padre
        obtenerHotelId();

        // Configurar listeners
        configurarListeners(view);

        // Crear canal de notificación
        createNotificationChannel();

        // Cargar datos del hotel
        if (hotelId != null && !hotelId.isEmpty()) {
            cargarDatosDelHotel();
        } else {
            Log.e(TAG, "No se recibió hotelId");
            mostrarErrorCarga();
        }

        // Cargar datos guardados de fechas y huéspedes
        cargarDatosReservaGuardados();

        return view;
    }

    private void inicializarVistas(View view) {
        // Views principales (manteniendo compatibilidad)
        hotelDescription = view.findViewById(R.id.hotelDescription);
        hotelPhone = view.findViewById(R.id.hotelPhone);
        hotelLocation = view.findViewById(R.id.hotelLocation);

        // Views de reserva (textos)
        txtCheckIn = view.findViewById(R.id.txtCheckIn);
        txtCheckOut = view.findViewById(R.id.txtCheckOut);
        txtGuests = view.findViewById(R.id.txtGuests);

        // Views de reserva (botones/contenedores)
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        btnGuests = view.findViewById(R.id.btnGuests);

        // Views de contacto
        btnCall = view.findViewById(R.id.btnCall);
        btnMap = view.findViewById(R.id.btnMap);

        // Views adicionales del nuevo diseño
        btnCheckAvailability = view.findViewById(R.id.btnCheckAvailability);
        pricePreview = view.findViewById(R.id.pricePreview);
        txtEstimatedPrice = view.findViewById(R.id.txtEstimatedPrice);
        viewRooms = view.findViewById(R.id.viewRooms);

        // Grid de servicios dinámico
        facilitiesGrid = view.findViewById(R.id.facilitiesGrid);

        Log.d(TAG, "Vistas inicializadas correctamente");
    }

    private void obtenerHotelId() {
        // Intentar obtener hotelId desde la actividad padre
        if (getActivity() != null && getActivity().getIntent() != null) {
            hotelId = getActivity().getIntent().getStringExtra("hotelId");
            Log.d(TAG, "HotelId obtenido: " + hotelId);
        }

        // Si no se encuentra, intentar desde argumentos del fragment
        if ((hotelId == null || hotelId.isEmpty()) && getArguments() != null) {
            hotelId = getArguments().getString("hotelId");
            Log.d(TAG, "HotelId desde argumentos: " + hotelId);
        }
    }

    private void configurarListeners(View view) {
        // Botón "Ver habitaciones"
        if (viewRooms != null) {
            viewRooms.setOnClickListener(v -> navegarAHabitaciones());
        }

        // Botón "Verificar disponibilidad"
        if (btnCheckAvailability != null) {
            btnCheckAvailability.setOnClickListener(v -> verificarDisponibilidad());
        }

        // Selectores de fecha
        if (btnCheckIn != null) {
            btnCheckIn.setOnClickListener(v -> mostrarSelectorFechas());
        }
        if (btnCheckOut != null) {
            btnCheckOut.setOnClickListener(v -> mostrarSelectorFechas());
        }

        // Selector de huéspedes
        if (btnGuests != null) {
            btnGuests.setOnClickListener(v -> mostrarSelectorHuespedes());
        }

        // Botones de contacto
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> realizarLlamada());
        }
        if (btnMap != null) {
            btnMap.setOnClickListener(v -> abrirMapa());
        }
    }

    private void navegarAHabitaciones() {
        Intent intent = new Intent(requireContext(), HotelHabitacionActivity.class);
        intent.putExtra("hotelId", hotelId);

        // Agregar parámetros de búsqueda si están disponibles
        if (prefsManager != null) {
            intent.putExtra("startDate", prefsManager.getStartDate());
            intent.putExtra("endDate", prefsManager.getEndDate());
            intent.putExtra("peopleString", prefsManager.getPeopleString());
        }

        startActivity(intent);
        Log.d(TAG, "Navegando a habitaciones del hotel: " + hotelId);
    }

    private void verificarDisponibilidad() {
        if (hotelActual == null) {
            Toast.makeText(getContext(), "Cargando información del hotel...", Toast.LENGTH_SHORT).show();
            return;
        }

        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();

        if (startDate == 0 || endDate == 0) {
            Toast.makeText(getContext(), "Por favor selecciona las fechas de estadía", Toast.LENGTH_SHORT).show();
            mostrarSelectorFechas();
            return;
        }

        // Simular verificación de disponibilidad
        Toast.makeText(getContext(), "✅ Hotel disponible para las fechas seleccionadas", Toast.LENGTH_LONG).show();
        mostrarPrecioEstimado();
    }

    private void mostrarSelectorFechas() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Seleccionar fechas de estadía")
                        .build();

        dateRangePicker.show(getParentFragmentManager(), "DATE_PICKER");

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDateUTC = selection.first;
            Long endDateUTC = selection.second;

            if (startDateUTC != null && endDateUTC != null) {
                // Convertir fechas UTC a zona horaria local correctamente
                long startDateLocal = convertirUTCaLocal(startDateUTC);
                long endDateLocal = convertirUTCaLocal(endDateUTC);

                // Log para debugging
                Log.d(TAG, "Fecha inicio UTC: " + new Date(startDateUTC));
                Log.d(TAG, "Fecha inicio Local: " + new Date(startDateLocal));
                Log.d(TAG, "Fecha fin UTC: " + new Date(endDateUTC));
                Log.d(TAG, "Fecha fin Local: " + new Date(endDateLocal));

                prefsManager.saveDateRange(startDateLocal, endDateLocal);
                actualizarFechasEnUI(startDateLocal, endDateLocal);

                // Calcular días de estadía de manera correcta
                int days = calcularDiasEstadia(startDateLocal, endDateLocal);
                Toast.makeText(getContext(), "Estadía de " + days + " días seleccionada", Toast.LENGTH_SHORT).show();

                // Mostrar precio estimado
                mostrarPrecioEstimado();
            }
        });
    }

    private void mostrarSelectorHuespedes() {
        // Por ahora, mostrar un mensaje simple
        Toast.makeText(getContext(), "Selector de huéspedes - Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();

        // Ejemplo de actualización manual
        String guestInfo = "2 Adultos • 0 Niños • 1 Habitación";
        if (txtGuests != null) {
            txtGuests.setText(guestInfo);
        }
        prefsManager.savePeopleString(guestInfo);
    }

    private void realizarLlamada() {
        if (hotelActual != null && hotelActual.getTelefono() != null) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + hotelActual.getTelefono()));
                startActivity(callIntent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error realizando llamada", e);
            }
        } else {
            Toast.makeText(getContext(), "Número de teléfono no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirMapa() {
        if (hotelActual != null && hotelActual.getUbicacion() != null) {
            Double lat = hotelActual.getUbicacion().getLatitud();
            Double lng = hotelActual.getUbicacion().getLongitud();

            if (lat != null && lng != null) {
                try {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                            lat, lng, lat, lng, hotelActual.getNombre());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch (Exception e) {
                    // Fallback a navegador web
                    String webUri = String.format(Locale.ENGLISH,
                            "https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lng);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                    startActivity(webIntent);
                }
            } else {
                Toast.makeText(getContext(), "Coordenadas no disponibles", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarPrecioEstimado() {
        if (pricePreview != null) {
            pricePreview.setVisibility(View.VISIBLE);

            if (txtEstimatedPrice != null) {
                txtEstimatedPrice.setText("Desde S/ 250/noche");
            }
        }
    }

    private void actualizarFechasEnUI(long startDate, long endDate) {
        // Usar Calendar para formateo seguro en zona horaria local
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endDate);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM", Locale.getDefault());

        if (txtCheckIn != null) {
            String formattedStart = formatter.format(startCal.getTime());
            txtCheckIn.setText(formattedStart);
            Log.d(TAG, "Check-in mostrado: " + formattedStart + " (día " + startCal.get(Calendar.DAY_OF_MONTH) + ")");
        }

        if (txtCheckOut != null) {
            String formattedEnd = formatter.format(endCal.getTime());
            txtCheckOut.setText(formattedEnd);
            Log.d(TAG, "Check-out mostrado: " + formattedEnd + " (día " + endCal.get(Calendar.DAY_OF_MONTH) + ")");
        }
    }

    /**
     * Convierte fecha UTC (del MaterialDatePicker) a zona horaria local
     */
    private long convertirUTCaLocal(long utcTimestamp) {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(utcTimestamp);

        // Crear calendar en zona horaria local con la misma fecha
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
        localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
        localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));
        localCalendar.set(Calendar.HOUR_OF_DAY, 12); // Usar mediodía para evitar problemas de DST
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        return localCalendar.getTimeInMillis();
    }

    /**
     * Calcula correctamente los días de estadía
     */
    private int calcularDiasEstadia(long startDate, long endDate) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDate);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endDate);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long diffInMillis = end.getTimeInMillis() - start.getTimeInMillis();
        int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));

        Log.d(TAG, "Cálculo días: inicio=" + start.get(Calendar.DAY_OF_MONTH) +
                ", fin=" + end.get(Calendar.DAY_OF_MONTH) +
                ", días=" + days);

        return Math.max(1, days); // Mínimo 1 día
    }

    private void cargarDatosDelHotel() {
        Log.d(TAG, "Cargando datos del hotel: " + hotelId);

        HotelRepository.getHotelById(hotelId,
                hotel -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                hotelActual = hotel;
                                actualizarDatosBasicos(hotel);
                                cargarServiciosCompletos(); // MÉTODO NUEVO
                                Log.d(TAG, "Datos básicos del hotel cargados: " + hotel.getNombre());
                            } catch (Exception e) {
                                Log.e(TAG, "Error actualizando interfaz", e);
                                mostrarErrorCarga();
                            }
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Error cargando hotel: " + error.getMessage(), error);
                            mostrarErrorCarga();
                        });
                    }
                }
        );
    }

    // ========== NUEVOS MÉTODOS PARA SERVICIOS DINÁMICOS ==========

    /**
     * Carga servicios como objetos completos en lugar de strings
     */
    private void cargarServiciosCompletos() {
        Log.d(TAG, "Cargando servicios completos para hotel: " + hotelId);

        // NOTA: Necesitas implementar este método en HotelRepository
        HotelRepository.getServiciosObjetosByHotelId(hotelId,
                servicios -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Servicios completos cargados: " + servicios.size());
                            serviciosCompletos = servicios;
                            crearServiciosDinamicamente(servicios);
                        });
                    }
                },
                error -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Error cargando servicios completos: " + error.getMessage());
                            mostrarMensajeSinServicios();
                        });
                    }
                }
        );
    }

    /**
     * Crea las vistas de servicios dinámicamente
     */
    private void crearServiciosDinamicamente(List<Servicio> servicios) {
        if (facilitiesGrid == null) {
            Log.e(TAG, "facilitiesGrid es null");
            return;
        }

        // Limpiar vistas existentes
        facilitiesGrid.removeAllViews();

        if (servicios == null || servicios.isEmpty()) {
            Log.d(TAG, "Lista de servicios vacía o null");
            mostrarMensajeSinServicios();
            return;
        }

        Log.d(TAG, "Creando " + servicios.size() + " servicios dinámicamente");

        // Configurar orientación vertical para el contenedor principal
        facilitiesGrid.setOrientation(LinearLayout.VERTICAL);

        // Crear filas de 2 servicios cada una
        LinearLayout filaActual = null;
        for (int i = 0; i < servicios.size(); i++) {
            Servicio servicio = servicios.get(i);

            // Validar que el servicio no sea null
            if (servicio == null) {
                Log.w(TAG, "Servicio null en índice " + i + ", saltando...");
                continue;
            }

            Log.d(TAG, "Procesando servicio " + (i + 1) + ": " + servicio.getNombre());

            // Crear nueva fila cada 2 servicios
            if (i % 2 == 0) {
                filaActual = new LinearLayout(requireContext());
                filaActual.setOrientation(LinearLayout.HORIZONTAL);
                filaActual.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Agregar margen inferior entre filas
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) filaActual.getLayoutParams();
                params.bottomMargin = dpToPx(12);
                filaActual.setLayoutParams(params);

                facilitiesGrid.addView(filaActual);
            }

            // Crear vista del servicio
            try {
                View vistaServicio = crearVistaServicio(servicio, i % 2 == 0);
                if (filaActual != null && vistaServicio != null) {
                    filaActual.addView(vistaServicio);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creando vista para servicio: " + servicio.getNombre(), e);
            }
        }

        Log.d(TAG, "Servicios dinámicos creados exitosamente");
    }

    /**
     * Crea la vista individual para cada servicio
     */
    private View crearVistaServicio(Servicio servicio, boolean esIzquierda) {
        if (servicio == null) {
            Log.e(TAG, "No se puede crear vista para servicio null");
            return null;
        }

        String nombreServicio = servicio.getNombre() != null ? servicio.getNombre() : "Servicio";
        Log.d(TAG, "Creando vista para servicio: " + nombreServicio);

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(android.view.Gravity.CENTER);

        // Usar color de fondo más seguro
        try {
            contenedor.setBackgroundColor(getResources().getColor(R.color.light_gray, null));
        } catch (Exception e) {
            contenedor.setBackgroundColor(0xFFF5F7FA); // Color hardcodeado como fallback
        }

        contenedor.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Configurar parámetros de layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );

        // Agregar margen entre servicios
        if (esIzquierda) {
            params.rightMargin = dpToPx(6);
        } else {
            params.leftMargin = dpToPx(6);
        }
        contenedor.setLayoutParams(params);

        // Crear ImageView para el icono/imagen
        ImageView iconoServicio = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(36), dpToPx(36)
        );
        iconoServicio.setLayoutParams(iconParams);
        iconoServicio.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Cargar imagen del servicio de manera segura
        try {
            cargarImagenServicio(iconoServicio, servicio);
        } catch (Exception e) {
            Log.e(TAG, "Error cargando imagen para servicio: " + nombreServicio, e);
            iconoServicio.setImageResource(R.drawable.ic_service_default);
        }

        // Crear TextView para el nombre
        TextView nombreText = new TextView(requireContext());
        nombreText.setText(nombreServicio);

        try {
            nombreText.setTextColor(getResources().getColor(R.color.dark_text, null));
        } catch (Exception e) {
            nombreText.setTextColor(0xFF333333); // Color hardcodeado como fallback
        }

        nombreText.setTextSize(13);
        nombreText.setTypeface(null, android.graphics.Typeface.BOLD);
        nombreText.setGravity(android.view.Gravity.CENTER);
        nombreText.setPadding(0, dpToPx(8), 0, 0);

        // Agregar vistas al contenedor
        contenedor.addView(iconoServicio);
        contenedor.addView(nombreText);

        // Configurar click listener
        contenedor.setOnClickListener(v -> mostrarDetallesServicio(servicio));

        // Agregar efecto de presión
        contenedor.setClickable(true);
        contenedor.setFocusable(true);

        Log.d(TAG, "Vista creada exitosamente para: " + nombreServicio);
        return contenedor;
    }

    /**
     * Carga la imagen del servicio usando Glide
     */
    private void cargarImagenServicio(ImageView imageView, Servicio servicio) {
        // Validación de seguridad
        if (servicio == null) {
            Log.e(TAG, "Servicio es null en cargarImagenServicio");
            imageView.setImageResource(R.drawable.ic_service_default);
            return;
        }

        String nombreServicio = servicio.getNombre() != null ? servicio.getNombre() : "servicio";
        String urlImagen = null;

        // Validar si el servicio tiene imágenes
        try {
            // Opción 1: Si tu modelo usa getImagenes() que devuelve una lista
            List<String> imagenes = servicio.getImagenes();
            if (imagenes != null && !imagenes.isEmpty()) {
                urlImagen = imagenes.get(0);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo lista de imágenes para: " + nombreServicio, e);
        }

        // Opción 2: Si no hay lista, intentar con imagenUrl directamente
        if (urlImagen == null || urlImagen.isEmpty()) {
            try {
                urlImagen = servicio.getImagenes().get(0);
            } catch (Exception e) {
                Log.w(TAG, "Error obteniendo imagenUrl para: " + nombreServicio, e);
            }
        }

        Log.d(TAG, "Cargando imagen para servicio: " + nombreServicio + ", URL: " + urlImagen);

        // Verificar si tenemos una URL válida
        if (urlImagen != null && !urlImagen.isEmpty() && !urlImagen.equals("null")) {
            // Cargar imagen real con Glide
            try {
                Glide.with(this)
                        .load(urlImagen)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(getIconoServicioPorDefecto(nombreServicio))
                        .error(getIconoServicioPorDefecto(nombreServicio))
                        .into(imageView);

                Log.d(TAG, "Imagen cargada exitosamente para: " + nombreServicio);
            } catch (Exception e) {
                Log.e(TAG, "Error cargando imagen con Glide para " + nombreServicio, e);
                imageView.setImageResource(getIconoServicioPorDefecto(nombreServicio));
                imageView.setColorFilter(getColorServicio(nombreServicio));
            }
        } else {
            // Usar icono por defecto
            Log.d(TAG, "Usando icono por defecto para: " + nombreServicio + " (sin imagen)");
            imageView.setImageResource(getIconoServicioPorDefecto(nombreServicio));
            imageView.setColorFilter(getColorServicio(nombreServicio));
        }
    }
    /**
     * Obtiene el icono por defecto basado en el nombre del servicio
     */
    private int getIconoServicioPorDefecto(String nombreServicio) {
        if (nombreServicio == null) {
            return R.drawable.ic_service_default;
        }

        String nombre = nombreServicio.toLowerCase().trim();

        if (servicioContiene(nombre, "wifi", "internet", "wi-fi")) {
            return R.drawable.wifi;
        } else if (servicioContiene(nombre, "piscina", "pool", "natacion")) {
            return R.drawable.piscina;
        } else if (servicioContiene(nombre, "desayuno", "breakfast", "comida", "restaurante")) {
            return R.drawable.breakfast;
        } else if (servicioContiene(nombre, "gimnasio", "gym", "fitness", "ejercicio")) {
            return R.drawable.gym;
        } else if (servicioContiene(nombre, "spa", "masaje", "relajacion")) {
            // Usar un icono existente como fallback si no tienes ic_spa
            return R.drawable.ic_service_default;
        } else if (servicioContiene(nombre, "parking", "estacionamiento", "garage")) {
            // Usar un icono existente como fallback si no tienes ic_parking
            return R.drawable.ic_service_default;
        } else if (servicioContiene(nombre, "aire", "acondicionado", "climatizacion")) {
            // Usar un icono existente como fallback si no tienes ic_ac
            return R.drawable.ic_service_default;
        } else {
            return R.drawable.ic_service_default; // Icono genérico
        }
    }

    /**
     * Obtiene el color del servicio basado en su tipo
     */
    private int getColorServicio(String nombreServicio) {
        if (nombreServicio == null) {
            return 0xFF757575; // Gris por defecto
        }

        String nombre = nombreServicio.toLowerCase().trim();

        try {
            if (servicioContiene(nombre, "wifi", "internet")) {
                return getResources().getColor(R.color.green, null); // Verde
            } else if (servicioContiene(nombre, "piscina", "pool")) {
                return getResources().getColor(R.color.blue, null); // Azul
            } else if (servicioContiene(nombre, "desayuno", "breakfast", "comida")) {
                return getResources().getColor(R.color.orange, null); // Naranja
            } else if (servicioContiene(nombre, "gimnasio", "gym", "fitness")) {
                return getResources().getColor(R.color.pink, null); // Rosa
            } else if (servicioContiene(nombre, "spa", "masaje")) {
                return getResources().getColor(R.color.purple, null); // Morado
            } else {
                return getResources().getColor(R.color.gray, null); // Gris por defecto
            }
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo color para servicio, usando por defecto", e);
            // Colores hardcodeados como fallback
            if (servicioContiene(nombre, "wifi", "internet")) {
                return 0xFF4CAF50; // Verde
            } else if (servicioContiene(nombre, "piscina", "pool")) {
                return 0xFF2196F3; // Azul
            } else if (servicioContiene(nombre, "desayuno", "breakfast", "comida")) {
                return 0xFFFF9800; // Naranja
            } else if (servicioContiene(nombre, "gimnasio", "gym", "fitness")) {
                return 0xFFE91E63; // Rosa
            } else if (servicioContiene(nombre, "spa", "masaje")) {
                return 0xFF9C27B0; // Morado
            } else {
                return 0xFF757575; // Gris por defecto
            }
        }
    }

    /**
     * Muestra detalles del servicio al hacer click
     */
    private void mostrarDetallesServicio(Servicio servicio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(servicio.getNombre());

        String mensaje = "";
        if (servicio.getDescripcion() != null && !servicio.getDescripcion().isEmpty()) {
            mensaje += servicio.getDescripcion() + "\n\n";
        }

        if (servicio.getPrecio() != null && servicio.getPrecio() > 0) {
            mensaje += "Precio: S/ " + String.format("%.2f", servicio.getPrecio());
        } else {
            mensaje += "Servicio incluido";
        }

        builder.setMessage(mensaje);
        builder.setPositiveButton("Cerrar", null);
        builder.show();

        Log.d(TAG, "Mostrando detalles del servicio: " + servicio.getNombre());
    }

    /**
     * Muestra mensaje cuando no hay servicios
     */
    private void mostrarMensajeSinServicios() {
        if (facilitiesGrid == null) return;

        facilitiesGrid.removeAllViews();

        TextView mensajeVacio = new TextView(requireContext());
        mensajeVacio.setText("No hay servicios disponibles para mostrar");
        mensajeVacio.setTextColor(getResources().getColor(R.color.gray_text, null));
        mensajeVacio.setTextSize(14);
        mensajeVacio.setGravity(android.view.Gravity.CENTER);
        mensajeVacio.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

        facilitiesGrid.addView(mensajeVacio);

        Log.d(TAG, "Mostrando mensaje de servicios vacío");
    }

    /**
     * Verifica si el servicio contiene alguna palabra clave
     */
    private boolean servicioContiene(String servicio, String... palabrasClave) {
        for (String palabra : palabrasClave) {
            if (servicio.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte dp a pixeles
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics()
        );
    }

    // ========== MÉTODOS EXISTENTES (SIN CAMBIOS) ==========

    private void actualizarDatosBasicos(Hotel hotel) {
        // Actualizar descripción
        if (hotel.getDescripcion() != null && !hotel.getDescripcion().isEmpty()) {
            hotelDescription.setText(hotel.getDescripcion());
        } else {
            hotelDescription.setText("Un excelente hotel con todas las comodidades para una estadía perfecta.");
        }

        // Actualizar teléfono
        if (hotel.getTelefono() != null && !hotel.getTelefono().isEmpty()) {
            hotelPhone.setText(hotel.getTelefono());
        } else {
            hotelPhone.setText("Información no disponible");
        }

        // Actualizar ubicación
        if (hotel.getUbicacion() != null) {
            String ubicacionTexto = construirTextoUbicacion(hotel.getUbicacion());
            hotelLocation.setText(ubicacionTexto);
        } else {
            hotelLocation.setText("Ubicación no disponible");
        }
    }

    private String construirTextoUbicacion(com.example.telehotel.data.model.Ubicacion ubicacion) {
        if (ubicacion.getDireccion() != null && !ubicacion.getDireccion().isEmpty()) {
            return ubicacion.getDireccion();
        } else if (ubicacion.getCiudad() != null && ubicacion.getPais() != null) {
            return ubicacion.getCiudad() + ", " + ubicacion.getPais();
        } else {
            return "Ubicación no especificada";
        }
    }

    private void mostrarErrorCarga() {
        if (hotelDescription != null) {
            hotelDescription.setText("Error al cargar la información del hotel");
        }
        if (hotelPhone != null) {
            hotelPhone.setText("Información no disponible");
        }
        if (hotelLocation != null) {
            hotelLocation.setText("Ubicación no disponible");
        }

        Toast.makeText(getContext(), "Error cargando detalles del hotel", Toast.LENGTH_SHORT).show();
    }

    private void cargarDatosReservaGuardados() {
        long startDate = prefsManager.getStartDate();
        long endDate = prefsManager.getEndDate();
        String peopleString = prefsManager.getPeopleString();

        if (startDate != 0 && endDate != 0) {
            actualizarFechasEnUI(startDate, endDate);
        }

        if (peopleString != null && !peopleString.isEmpty() && txtGuests != null) {
            txtGuests.setText(peopleString);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking Channel";
            String description = "Canal para notificaciones de reserva";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void mostrarNotificacionReservaExitosa() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Reserva Confirmada")
                .setContentText("¡Reserva realizada con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}