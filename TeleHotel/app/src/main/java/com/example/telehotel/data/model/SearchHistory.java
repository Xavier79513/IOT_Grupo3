package com.example.telehotel.data.model;
// SearchHistory.java - Modelo de historial de búsquedas

import java.io.Serializable;

public class SearchHistory implements Serializable {
    private String location;
    private long startDate;
    private long endDate;
    private String peopleString;
    private long timestamp;
    private int adultsCount;
    private int childrenCount;
    private int roomsCount;

    public SearchHistory() {
        this.timestamp = System.currentTimeMillis();
    }

    public SearchHistory(String location, long startDate, long endDate, String peopleString) {
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleString = peopleString;
        this.timestamp = System.currentTimeMillis();
    }

    public SearchHistory(String location, long startDate, long endDate, int adultsCount, int childrenCount, int roomsCount) {
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adultsCount = adultsCount;
        this.childrenCount = childrenCount;
        this.roomsCount = roomsCount;
        this.peopleString = buildPeopleString();
        this.timestamp = System.currentTimeMillis();
    }

    private String buildPeopleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(adultsCount).append(" adulto");
        if (adultsCount > 1) sb.append("s");

        if (childrenCount > 0) {
            sb.append(", ").append(childrenCount).append(" niño");
            if (childrenCount > 1) sb.append("s");
        }

        sb.append(", ").append(roomsCount).append(" habitación");
        if (roomsCount > 1) sb.append("es");

        return sb.toString();
    }

    // Getters y Setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getPeopleString() {
        return peopleString;
    }

    public void setPeopleString(String peopleString) {
        this.peopleString = peopleString;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    // Método utilitario para obtener la duración de la estadía
    public int getStayDuration() {
        return (int) ((endDate - startDate) / (1000 * 60 * 60 * 24));
    }

    // Método para verificar si es una búsqueda reciente (últimas 24 horas)
    public boolean isRecent() {
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - timestamp) < oneDayInMillis;
    }

    @Override
    public String toString() {
        return "SearchHistory{" +
                "location='" + location + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", peopleString='" + peopleString + '\'' +
                ", adultsCount=" + adultsCount +
                ", childrenCount=" + childrenCount +
                ", roomsCount=" + roomsCount +
                '}';
    }
}