package com.example.paktrainfoodapp.ui.main.Passenger;

public class RestaurantModel {

    String uid, restaurantName, city, imageBase64;

    public RestaurantModel() { }

    public RestaurantModel(String uid, String restaurantName, String city, String imageBase64) {
        this.uid = uid;
        this.restaurantName = restaurantName;
        this.city = city;
        this.imageBase64 = imageBase64;
    }

    public String getUid() {
        return uid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getCity() {
        return city;
    }

    public String getImageBase64() {
        return imageBase64;
    }
}
