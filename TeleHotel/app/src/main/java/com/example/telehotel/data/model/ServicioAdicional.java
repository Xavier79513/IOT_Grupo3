package com.example.telehotel.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ServicioAdicional implements Parcelable {
    public String servicioId;
    public Integer cantidad;
    public Double precio;

    public ServicioAdicional() {}

    // Constructor para Parcelable
    protected ServicioAdicional(Parcel in) {
        servicioId = in.readString();
        if (in.readByte() == 0) {
            cantidad = null;
        } else {
            cantidad = in.readInt();
        }
        if (in.readByte() == 0) {
            precio = null;
        } else {
            precio = in.readDouble();
        }
    }

    public static final Creator<ServicioAdicional> CREATOR = new Creator<ServicioAdicional>() {
        @Override
        public ServicioAdicional createFromParcel(Parcel in) {
            return new ServicioAdicional(in);
        }

        @Override
        public ServicioAdicional[] newArray(int size) {
            return new ServicioAdicional[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(servicioId);
        if (cantidad == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cantidad);
        }
        if (precio == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(precio);
        }
    }

    // Getters y Setters
    public String getServicioId() {
        return servicioId;
    }

    public void setServicioId(String servicioId) {
        this.servicioId = servicioId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "ServicioAdicional{" +
                "servicioId='" + servicioId + '\'' +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                '}';
    }
}