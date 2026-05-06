package com.example.paktrainfoodapp.ui.main.Delivery;

public class DeliveryBoyModel {

    private String uid;
    private String name;
    private String phone;
    private String email;
    private String city;
    private String imageBase64;

    private String docPath;

    private String passengerUid;

    // Getters and Setters
    public String getPassengerUid() {
        return passengerUid;
    }
    public void setPassengerUid(String passengerUid) {
        this.passengerUid = passengerUid;
    }

    // 🔹 Empty constructor (required for firestore deserialization)
    public DeliveryBoyModel() {
    }

    // 🔹 Full constructor
    public DeliveryBoyModel(String uid, String name, String phone, String email, String city, String imageBase64) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.imageBase64 = imageBase64;
    }

    // 🔹 Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // 🔹 toString() for easy debugging
    @Override
    public String toString() {
        return "DeliveryBoyModel{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", imageBase64=" + (imageBase64 != null ? "exists" : "null") +
                '}';
    }
}









//package com.example.paktrainfoodapp.ui.main.Delivery;
//
//public class DeliveryBoyModel {
//
//    private String uid;          // Firebase document id
//    private String name;         // Delivery boy name
//    private String phone;        // Phone number
//    private String email;        // Email
//    private String city;         // City
//    private String imageBase64;  // Image in Base64 string format
//
//    // --- Empty constructor required by Firebase ---
//    public DeliveryBoyModel() {
//    }
//
//    // --- Full constructor ---
//    public DeliveryBoyModel(String uid, String name, String phone, String email, String city, String imageBase64) {
//        this.uid = uid;
//        this.name = name;
//        this.phone = phone;
//        this.email = email;
//        this.city = city;
//        this.imageBase64 = imageBase64;
//    }
//
//    // --- Getters & Setters ---
//
//    public String getUid() {
//        return uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }
//
//    public String getImageBase64() {
//        return imageBase64;
//    }
//
//    public void setImageBase64(String imageBase64) {
//        this.imageBase64 = imageBase64;
//    }
//
//    // --- Optional: toString() for debugging ---
//    @Override
//    public String toString() {
//        return "DeliveryBoyModel{" +
//                "uid='" + uid + '\'' +
//                ", name='" + name + '\'' +
//                ", phone='" + phone + '\'' +
//                ", email='" + email + '\'' +
//                ", city='" + city + '\'' +
//                ", imageBase64=" + (imageBase64 != null ? "exists" : "null") +
//                '}';
//    }
//}
//
//
//
//
