package com.example.telehotel.core.storage;

import java.io.Serializable;

public class TaxiRequest implements Serializable {
    private String requestId;
    private String reservationId;
    private String hotelId;
    private String hotelName;
    private String taxiDriverId;
    private String taxiDriverName;
    private String taxiDriverPhone;
    private String taxiPlate;
    private String taxiModel;
    private String taxiColor;
    private String airport;
    private String airportCode;
    private String qrCode;
    private String status; // REQUESTED, ASSIGNED, ON_WAY, ARRIVED, IN_TRANSIT, COMPLETED, CANCELLED
    private double driverLat;
    private double driverLng;
    private double hotelLat;
    private double hotelLng;
    private long requestTime;
    private long assignedTime;
    private long arrivedTime;
    private long completedTime;
    private String estimatedArrival;
    private double estimatedDistance;
    private String customerNotes;
    private String driverNotes;

    // Estados posibles de solicitud de taxi
    public static final String STATUS_REQUESTED = "REQUESTED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_ON_WAY = "ON_WAY";
    public static final String STATUS_ARRIVED = "ARRIVED";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public TaxiRequest() {
        this.requestTime = System.currentTimeMillis();
        this.status = STATUS_REQUESTED;
    }

    public TaxiRequest(String requestId, String reservationId, String hotelId, String airport) {
        this.requestId = requestId;
        this.reservationId = reservationId;
        this.hotelId = hotelId;
        this.airport = airport;
        this.status = STATUS_REQUESTED;
        this.requestTime = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getTaxiDriverId() {
        return taxiDriverId;
    }

    public void setTaxiDriverId(String taxiDriverId) {
        this.taxiDriverId = taxiDriverId;
    }

    public String getTaxiDriverName() {
        return taxiDriverName;
    }

    public void setTaxiDriverName(String taxiDriverName) {
        this.taxiDriverName = taxiDriverName;
    }

    public String getTaxiDriverPhone() {
        return taxiDriverPhone;
    }

    public void setTaxiDriverPhone(String taxiDriverPhone) {
        this.taxiDriverPhone = taxiDriverPhone;
    }

    public String getTaxiPlate() {
        return taxiPlate;
    }

    public void setTaxiPlate(String taxiPlate) {
        this.taxiPlate = taxiPlate;
    }

    public String getTaxiModel() {
        return taxiModel;
    }

    public void setTaxiModel(String taxiModel) {
        this.taxiModel = taxiModel;
    }

    public String getTaxiColor() {
        return taxiColor;
    }

    public void setTaxiColor(String taxiColor) {
        this.taxiColor = taxiColor;
    }

    public String getAirport() {
        return airport;
    }

    public void setAirport(String airport) {
        this.airport = airport;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        // Actualizar timestamps según el estado
        switch (status) {
            case STATUS_ASSIGNED:
                this.assignedTime = System.currentTimeMillis();
                break;
            case STATUS_ARRIVED:
                this.arrivedTime = System.currentTimeMillis();
                break;
            case STATUS_COMPLETED:
                this.completedTime = System.currentTimeMillis();
                break;
        }
    }

    public double getDriverLat() {
        return driverLat;
    }

    public void setDriverLat(double driverLat) {
        this.driverLat = driverLat;
    }

    public double getDriverLng() {
        return driverLng;
    }

    public void setDriverLng(double driverLng) {
        this.driverLng = driverLng;
    }

    public double getHotelLat() {
        return hotelLat;
    }

    public void setHotelLat(double hotelLat) {
        this.hotelLat = hotelLat;
    }

    public double getHotelLng() {
        return hotelLng;
    }

    public void setHotelLng(double hotelLng) {
        this.hotelLng = hotelLng;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(long assignedTime) {
        this.assignedTime = assignedTime;
    }

    public long getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(long arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(long completedTime) {
        this.completedTime = completedTime;
    }

    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(String estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public String getDriverNotes() {
        return driverNotes;
    }

    public void setDriverNotes(String driverNotes) {
        this.driverNotes = driverNotes;
    }

    // Métodos utilitarios
    public boolean isActive() {
        return STATUS_REQUESTED.equals(status) || STATUS_ASSIGNED.equals(status) ||
                STATUS_ON_WAY.equals(status) || STATUS_ARRIVED.equals(status) ||
                STATUS_IN_TRANSIT.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean canCancel() {
        return STATUS_REQUESTED.equals(status) || STATUS_ASSIGNED.equals(status);
    }

    public boolean hasDriver() {
        return taxiDriverId != null && !taxiDriverId.isEmpty();
    }

    public boolean isDriverOnWay() {
        return STATUS_ON_WAY.equals(status) || STATUS_ARRIVED.equals(status);
    }

    public String getStatusDisplayText() {
        switch (status) {
            case STATUS_REQUESTED:
                return "Buscando taxi disponible";
            case STATUS_ASSIGNED:
                return "Taxi asignado";
            case STATUS_ON_WAY:
                return "Taxi en camino";
            case STATUS_ARRIVED:
                return "Taxi ha llegado";
            case STATUS_IN_TRANSIT:
                return "En camino al aeropuerto";
            case STATUS_COMPLETED:
                return "Viaje completado";
            case STATUS_CANCELLED:
                return "Solicitud cancelada";
            default:
                return "Estado desconocido";
        }
    }

    // Método para actualizar la posición del conductor
    public void updateDriverLocation(double lat, double lng) {
        this.driverLat = lat;
        this.driverLng = lng;
    }

    @Override
    public String toString() {
        return "TaxiRequest{" +
                "requestId='" + requestId + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", airport='" + airport + '\'' +
                ", status='" + status + '\'' +
                ", taxiDriverName='" + taxiDriverName + '\'' +
                ", taxiPlate='" + taxiPlate + '\'' +
                '}';
    }
}