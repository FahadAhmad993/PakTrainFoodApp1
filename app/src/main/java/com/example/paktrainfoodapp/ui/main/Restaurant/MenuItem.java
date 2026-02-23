package com.example.paktrainfoodapp.ui.main.Restaurant;

import java.util.Map;

public class MenuItem {
    private String id;
    private String restaurantId;
    private String restaurantName;
    private String restaurantCity;
    private String imageUrl;
    private String category;
    private String name;
    private String description;
    private String time;
    private double price;

    private String passengerUid;
    private String orderStatus; // 🔹 Added field



    // inside MenuItem.java (add these fields + methods)
    private Map<String, Object> meta;
    private String docPath;

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public String getDocPath() { return docPath; }
    public void setDocPath(String docPath) { this.docPath = docPath; }


    // 👇 Default constructor (Firestore ke liye required)
    public MenuItem() {}

    // For new menu items (without status)
    public MenuItem(String name, String desc, String imageUrl, double price, String time,
                    String category, String restId, String restaurantName, String restaurantCity) {
        this(name, desc, imageUrl, price, time, category, restId, restaurantName, restaurantCity, "Pending");
    }

    // 👇 Proper constructor with assignments
    public MenuItem(String name, String desc, String imageUrl, double price, String time,
                    String category, String restId, String restaurantName, String restaurantCity, String orderStatus) {
        this.name = name;
        this.description = desc;
        this.imageUrl = imageUrl;
        this.price = price;
        this.time = time;
        this.category = category;
        this.restaurantId = restId;
        this.restaurantName = restaurantName;
        this.restaurantCity = restaurantCity;
        this.orderStatus = orderStatus;
    }

    // 👇 Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantCity() { return restaurantCity; }
    public void setRestaurantCity(String restaurantCity) { this.restaurantCity = restaurantCity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getPassengerUid() {
        return passengerUid;
    }

    public void setPassengerUid(String passengerUid) {
        this.passengerUid = passengerUid;
    }
}


