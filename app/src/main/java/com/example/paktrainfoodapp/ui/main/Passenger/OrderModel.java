package com.example.paktrainfoodapp.ui.main.Passenger;

public class OrderModel {
    private String orderId;
    private double totalPrice;
    private String status;
    public OrderModel() {}

    public OrderModel(String orderId, double totalPrice) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
    }
    public OrderModel(String orderId,
                      double totalPrice,
                      String status) {

        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
}//