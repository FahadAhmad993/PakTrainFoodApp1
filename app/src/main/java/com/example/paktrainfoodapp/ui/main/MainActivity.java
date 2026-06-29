package com.example.paktrainfoodapp.ui.main;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryDashboardFragment;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryRegisterFragment;
import com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader;
import com.example.paktrainfoodapp.ui.main.Restaurant.RestaurantPendingFragment;
import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_LoadFragment;
import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_registers;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String USER_ROLE_KEY = "USER_ROLE_KEY";
    private PrefManager prefManager;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
// Android 13+ Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                );


        prefManager = new PrefManager(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 100% FIXED BACK PRESS HANDLING FOR INNER FRAGMENTS
        // 🔹 100% PERFECT UNIVERSAL BACK PRESS HANDLING
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Main Container se Passenger Loader nikalein
                Fragment currentLoader = getSupportFragmentManager().findFragmentById(R.id.main_container);

                if (currentLoader instanceof Passenger_Fragment_Loader) {
                    Passenger_Fragment_Loader loader = (Passenger_Fragment_Loader) currentLoader;

                    // 1. Pehle check karo kya Restaurant List ya Menu ka internal backstack maujood hai?
                    boolean handledInternally = loader.getChildFragmentManager().popBackStackImmediate();
                    Fragment current =
                            loader.getChildFragmentManager()
                                    .findFragmentById(R.id.fragment_holder);

                    if (current != null) {
                        loader.showTempFragment(current);
                        loader.updateBottomNav(current);
                    }

                    if (handledInternally) {
                        // Agar peeche koi fragment tha (jaise menu se list ya list se home), to wo chala gaya.
                        return;
                    }

                    // 2. Agar koi internal backstack nahi hai, to check karo user kis tab par khara hai?
                    // Agar user Profile, Order, Cart ya Menu Browse par hai, to back dabane par usay wapas Dashboard/Home tab par aana chahiye!
                    if (loader.getActiveFragment() != null && loader.getActiveFragment() != loader.getHomeFragment()) {
                        // Wapas Dashboard wale button ko trigger karo taaki user Home par aa jaye
                        // 🌟 FIX: loader.getView() lagane se fragment ka layout view mil jata hai
                        if (loader.getView() != null) {
                            loader.getView().findViewById(R.id.btn_dashboard).performClick();
                        }
                    } else {
                        // 3. Agar user pehle se hi bilkul main Home/Dashboard fragment par hai, ab app close hona chahiye.
                        setEnabled(false); // Callback temporary off
                        MainActivity.this.getOnBackPressedDispatcher().onBackPressed(); // Default close behavior
                    }
                } else {
                    // Delivery ya Restaurant role ke liye default exit
                    setEnabled(false);
                    MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

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

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e("FCM", "Token generate nahi hua");
                        return;
                    }

                    String token = task.getResult();

                    Log.d("FCM_TOKEN", token);

                    Map<String, Object> map = new HashMap<>();
                    map.put("fcmToken", token);

                    db.collection("Users")
                            .document("Notification")
                            .collection("FCMTokens")
                            .document(uid)
                            .set(map)
                            .addOnSuccessListener(unused ->
                                    Log.d("FCM", "Token Save Successfully"))
                            .addOnFailureListener(e ->
                                    Log.e("FCM", e.getMessage()));
                });


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

    private void handleRestaurantRole(String uid) {
        if (prefManager.isRegistered() && prefManager.isRestaurantVerified()) {
            loadFragment(new restaurant_LoadFragment());
            return;
        }

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(uid)
                .get()
                .addOnSuccessListener(this::handleRestaurantDocument)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadFragment(new restaurant_registers());
                });
    }

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
//            prefManager.setRegistered(false, email);
//            loadFragment(new restaurant_registers());
            prefManager.setRegistered(true, email);

            loadFragment(new RestaurantPendingFragment());
        }
    }

    private void handleDeliveryRole(String uid) {
        if (prefManager.isRegistered() && prefManager.isDeliveryVerified()) {
            loadFragment(new DeliveryDashboardFragment());
            return;
        }

        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(uid)
                .get()
                .addOnSuccessListener(this::handleDeliveryDocument)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadFragment(new DeliveryRegisterFragment());
                });
    }

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

    private void loadFragment(Fragment fragment) {
        if (isFinishing()) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commitAllowingStateLoss();
    }
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "General Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

}







//




