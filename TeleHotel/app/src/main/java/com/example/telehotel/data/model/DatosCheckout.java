package com.example.telehotel.data.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatosCheckout {
    public LocalDateTime fecha;
    public Integer valoracion;
    public String comentarios;
    public List<CobroAdicional> cobrosAdicionales = new ArrayList<>();

    public DatosCheckout() {}
}
