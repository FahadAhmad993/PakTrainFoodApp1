package com.example.paktrainfoodapp.ui.main.Passenger;

public class PassengerLocationModel {

    private String orderId;
    private String passengerUid;
    private String station;
    private double lat;
    private double lng;
    private long timestamp;

    public PassengerLocationModel() {}

    public PassengerLocationModel(String orderId, String passengerUid, String station,
                                  double lat, double lng, long timestamp) {
        this.orderId = orderId;
        this.passengerUid = passengerUid;
        this.station = station;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public String getPassengerUid() { return passengerUid; }
    public String getStation() { return station; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public long getTimestamp() { return timestamp; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPassengerUid(String passengerUid) { this.passengerUid = passengerUid; }
    public void setStation(String station) { this.station = station; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}






