package com.example.telehotel.core.storage;
// ReservationInfo.java - Modelo de información de reservas

import java.io.Serializable;
import java.util.List;

public class ReservationInfo implements Serializable {
    private String reservationId;
    private String hotelId;
    private String hotelName;
    private String hotelLocation;
    private String hotelImageUrl;
    private String roomType;
    private String roomDescription;
    private String roomNumber;
    private long startDate;
    private long endDate;
    private double roomPrice;
    private double taxes;
    private double additionalCharges;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED, IN_PROGRESS
    private String paymentInfo;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD
    private String cardLastFourDigits;
    private boolean taxiRequested;
    private long createdAt;
    private long updatedAt;
    private int adultsCount;
    private int childrenCount;
    private int roomsCount;
    private String specialRequests;
    private double rating;
    private String review;
    private List<String> selectedServices;

    // Estados posibles de reserva
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    public ReservationInfo() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = STATUS_PENDING;
        this.taxiRequested = false;
        this.rating = 0.0;
    }

    public ReservationInfo(String reservationId, String hotelId, String hotelName, String roomType,
                           long startDate, long endDate, double totalPrice, String status) {
        this.reservationId = reservationId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomType = roomType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.taxiRequested = false;
        this.rating = 0.0;
    }

    // Getters y Setters
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

    public String getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(String hotelLocation) {
        this.hotelLocation = hotelLocation;
    }

    public String getHotelImageUrl() {
        return hotelImageUrl;
    }

    public void setHotelImageUrl(String hotelImageUrl) {
        this.hotelImageUrl = hotelImageUrl;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public double getTaxes() {
        return taxes;
    }

    public void setTaxes(double taxes) {
        this.taxes = taxes;
    }

    public double getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(double additionalCharges) {
        this.additionalCharges = additionalCharges;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardLastFourDigits() {
        return cardLastFourDigits;
    }

    public void setCardLastFourDigits(String cardLastFourDigits) {
        this.cardLastFourDigits = cardLastFourDigits;
    }

    public boolean isTaxiRequested() {
        return taxiRequested;
    }

    public void setTaxiRequested(boolean taxiRequested) {
        this.taxiRequested = taxiRequested;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(int adultsCount) {
        this.adultsCount = adultsCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getRoomsCount() {
        return roomsCount;
    }

    public void setRoomsCount(int roomsCount) {
        this.roomsCount = roomsCount;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
        this.updatedAt = System.currentTimeMillis();
    }

    public List<String> getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(List<String> selectedServices) {
        this.selectedServices = selectedServices;
    }

    // Métodos utilitarios
    public int getStayDuration() {
        return (int) ((endDate - startDate) / (1000 * 60 * 60 * 24));
    }

    public boolean isActive() {
        return STATUS_CONFIRMED.equals(status) || STATUS_IN_PROGRESS.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean canCancel() {
        return STATUS_PENDING.equals(status) || STATUS_CONFIRMED.equals(status);
    }

    public boolean canRate() {
        return STATUS_COMPLETED.equals(status) && rating == 0.0;
    }

    public boolean hasRating() {
        return rating > 0.0;
    }

    @Override
    public String toString() {
        return "ReservationInfo{" +
                "reservationId='" + reservationId + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", roomType='" + roomType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", taxiRequested=" + taxiRequested +
                '}';
    }
}