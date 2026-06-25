package com.example.paktrainfoodapp.ui.main.Restaurant;

public class WalletHistory {

    private String type;
    private String amount;
    private String date;
    private String orderId;

    public WalletHistory() {}

    public WalletHistory(String type, String amount, String date, String orderId) {
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getOrderId() {
        return orderId;
    }
}