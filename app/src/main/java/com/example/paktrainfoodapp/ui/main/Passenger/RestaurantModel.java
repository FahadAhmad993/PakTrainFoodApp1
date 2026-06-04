package com.example.paktrainfoodapp.ui.main.Passenger;

public class RestaurantModel {

    // ⚡ FIXED: Added imageUrl along with legacy variables to support smooth transition
    private String uid;
    private String restaurantName;
    private String city;
    private String imageBase64;
    private String imageUrl;

    // Required empty constructor for Firebase Firestore parsing layers
    public RestaurantModel() { }

    // Legacy Constructor (Agar kahin purana code call kar raha ho to crash nahi hoga)
    public RestaurantModel(String uid, String restaurantName, String city, String imageBase64) {
        this.uid = uid;
        this.restaurantName = restaurantName;
        this.city = city;
        this.imageBase64 = imageBase64;
        this.imageUrl = imageBase64; // Fallback configuration logic
    }

    // ================= GETTERS & SETTERS =================

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    // ⚡ THE METHOD: This fixes the compilation crash inside the fragment layer
    public String getImageUrl() {
        // Safe validation: agar imageUrl empty ho to fallback to base64
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageBase64;
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

//

