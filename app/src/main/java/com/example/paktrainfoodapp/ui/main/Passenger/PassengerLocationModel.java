package com.example.paktrainfoodapp.ui.main.Passenger;

public class PassengerLocationModel {

    private String orderId;
    private String passengerUid;
    private String station;
    private double lat;
    private double lng;
    private long timestamp;

    // Required empty constructor for Firebase Realtime/Firestore automatic object parsing
    public PassengerLocationModel() {}

    // Main Constructor
    public PassengerLocationModel(String orderId, String passengerUid, String station,
                                  double lat, double lng, long timestamp) {
        this.orderId = orderId;
        this.passengerUid = passengerUid;
        this.station = station;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    // ================= GETTERS & SETTERS =================

    public String getOrderId() {
        return orderId != null ? orderId : "";
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPassengerUid() {
        return passengerUid != null ? passengerUid : "";
    }

    public void setPassengerUid(String passengerUid) {
        this.passengerUid = passengerUid;
    }

    public String getStation() {
        return station != null ? station : "Unknown Station";
    }

    public void setStation(String station) {
        this.station = station;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ================= UTILITY HELPER METHODS (For Tracking Screen) =================

    /**
     * ⚡ FIX: Validation layer taake map loading ke waqt application loop coordinate zero crash se bache
     */
    public boolean isValidCoordinates() {
        return lat != 0.0 && lng != 0.0;
    }

    /**
     * Formatted string returns karega delivery map window popups ke liye
     */
    @Override
    public String toString() {
        return "PassengerLocation{" +
                "orderId='" + orderId + '\'' +
                ", station='" + station + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}




