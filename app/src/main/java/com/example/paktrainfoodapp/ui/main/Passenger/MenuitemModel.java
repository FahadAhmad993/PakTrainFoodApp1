package com.example.paktrainfoodapp.ui.main.Passenger;

public class MenuitemModel {

    private String id, name, description, imageUrl, time, category;
    private double price;
    private String restaurantId, restaurantName, restaurantCity, restaurantUid;
    private String orderStatus = "active"; // default

    public MenuitemModel() {} // required for Firestore

    public MenuitemModel(String id, String name, String description, String imageUrl,
                         double price, String time, String category,
                         String restaurantId, String restaurantName, String restaurantCity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.time = time;
        this.category = category;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantCity = restaurantCity;
        this.orderStatus = "active";
    }

    // 🔹 Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantCity() { return restaurantCity; }
    public void setRestaurantCity(String restaurantCity) { this.restaurantCity = restaurantCity; }

    public String getRestaurantUid() { return restaurantUid; }
    public void setRestaurantUid(String restaurantUid) { this.restaurantUid = restaurantUid; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}




