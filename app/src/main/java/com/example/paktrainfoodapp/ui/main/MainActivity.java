package com.example.paktrainfoodapp.ui.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryDashboardFragment;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryRegisterFragment;
import com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader;
import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_LoadFragment;
import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_registers;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    public static final String USER_ROLE_KEY = "USER_ROLE_KEY";
    private PrefManager prefManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userRole = getIntent().getStringExtra(USER_ROLE_KEY);
        if (userRole == null || userRole.isEmpty()) {
            userRole = prefManager.getUserRole();
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Roles handle karna
        switch (userRole) {
            case "RESTAURANT":
                handleRestaurantRole(uid);
                break;

            case "PASSENGER":
                loadFragment(new Passenger_Fragment_Loader());
                break;

            case "DELIVERY":
                handleDeliveryRole(uid);
                break;

            default:
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // 🔹 Restaurant verification logic
    private void handleRestaurantRole(String uid) {
        // Agar locally already verified → dashboard
        if (prefManager.isRegistered() && prefManager.isRestaurantVerified()) {
            loadFragment(new restaurant_LoadFragment());
            return;
        }

        // Firestore se verify karo (correct path)
        db.collection("Users")                  // Users collection
                .document("Restaurant")        // Restaurant document
                .collection("VerifiedRegister") // VerifiedRegister subcollection
                .document(uid)                 // Restaurant UID
                .get()
                .addOnSuccessListener(this::handleRestaurantDocument)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadFragment(new restaurant_registers());
                });
    }

    // 🔹 Firestore se document handle
    private void handleRestaurantDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            prefManager.setRegistered(false, auth.getCurrentUser().getEmail());
            loadFragment(new restaurant_registers());
            return;
        }

        Boolean isVerified = doc.getBoolean("isVerified");
        String email = doc.getString("email");
        String city = doc.getString("city");

        if (isVerified != null && isVerified) {
            prefManager.setRegistered(true, email);
            prefManager.setIsRestaurantVerified(true);
            prefManager.setUserCity(city);
            loadFragment(new restaurant_LoadFragment());
        } else {
            prefManager.setRegistered(false, email);
            loadFragment(new restaurant_registers());
        }
    }





    // 🔹 Delivery Boy verification logic
    private void handleDeliveryRole(String uid) {
        // Agar locally already verified → dashboard
        if (prefManager.isRegistered() && prefManager.isDeliveryVerified()) {
            loadFragment(new DeliveryDashboardFragment());
            return;
        }

        // Firestore se verify karo (correct path)
        db.collection("Users")                  // Users collection
                .document("Delivery")        // Deliveryboy document
                .collection("VerifiedRegister") // VerifiedRegister subcollection
                .document(uid)                 // Deliver UID
                .get()
                .addOnSuccessListener(this::handleDeliveryDocument)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadFragment(new DeliveryRegisterFragment());
                });
    }

    // 🔹 Firestore se document handle
    private void handleDeliveryDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            prefManager.setRegistered(false, auth.getCurrentUser().getEmail());
            loadFragment(new DeliveryRegisterFragment());
            return;
        }

        Boolean isVerified = doc.getBoolean("isVerified");
        String email = doc.getString("email");
        String city = doc.getString("city");

        if (isVerified != null && isVerified) {
            prefManager.setRegistered(true, email);
            prefManager.setIsDeliveryVerified(true);
            prefManager.setUserCity(city);
            loadFragment(new DeliveryDashboardFragment());
        } else {
            prefManager.setRegistered(false, email);
            loadFragment(new DeliveryRegisterFragment());
        }
    }




    // 🔹 Fragment load karna
    private void loadFragment(Fragment fragment) {
        if (isFinishing()) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commitAllowingStateLoss();
    }
}

