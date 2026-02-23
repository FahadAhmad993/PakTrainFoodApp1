package com.example.paktrainfoodapp.ui.main.Passenger;

public class PassengerLocationModel {

    private String passengerUid;
    private double passengerLat;
    private double passengerLng;
    private long timestamp;

    public PassengerLocationModel() {
        // Firestore ke liye empty constructor zaruri hai
    }

    public PassengerLocationModel(String passengerUid, double passengerLat, double passengerLng, long timestamp) {
        this.passengerUid = passengerUid;
        this.passengerLat = passengerLat;
        this.passengerLng = passengerLng;
        this.timestamp = timestamp;
    }

    public String getPassengerUid() {
        return passengerUid;
    }

    public void setPassengerUid(String passengerUid) {
        this.passengerUid = passengerUid;
    }

    public double getPassengerLat() {
        return passengerLat;
    }

    public void setPassengerLat(double passengerLat) {
        this.passengerLat = passengerLat;
    }

    public double getPassengerLng() {
        return passengerLng;
    }

    public void setPassengerLng(double passengerLng) {
        this.passengerLng = passengerLng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PassengerLocationModel{" +
                "passengerUid='" + passengerUid + '\'' +
                ", passengerLat=" + passengerLat +
                ", passengerLng=" + passengerLng +
                ", timestamp=" + timestamp +
                '}';
    }
}
