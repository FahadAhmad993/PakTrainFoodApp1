package com.example.paktrainfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.paktrainfoodapp.ui.auth.AuthActivity;
import com.example.paktrainfoodapp.ui.main.MainActivity;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 Firestore Cache ENABLE (ONE TIME)
        FirebaseFirestoreSettings settings =
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build();

        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SaveStationLocations.saveAllStations();

        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PrefManager pref = new PrefManager(this);

            if (FirebaseAuth.getInstance().getCurrentUser() != null && pref.isLoggedIn()) {
                // User already logged in and role saved
                Intent intent = new Intent(Splash.this, MainActivity.class);
                intent.putExtra("USER_ROLE_KEY", pref.getUserRole());
                startActivity(intent);
                finish();
            } else {
                // Not logged in
                startActivity(new Intent(Splash.this, AuthActivity.class));
                finish();
            }
        }, SPLASH_DELAY_MS);
    }
}





//package com.example.paktrainfoodapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.WindowManager;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.paktrainfoodapp.ui.auth.AuthActivity;
//import com.example.paktrainfoodapp.ui.main.MainActivity;
//import com.example.paktrainfoodapp.utils.PrefManager;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class Splash extends AppCompatActivity {
//
//    private static final int SPLASH_DELAY_MS = 2000;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setContentView(R.layout.activity_splash);
//
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            PrefManager pref = new PrefManager(this);
//
//            if (FirebaseAuth.getInstance().getCurrentUser() != null && pref.isLoggedIn()) {
//                // User already logged in and role saved
//                Intent intent = new Intent(Splash.this, MainActivity.class);
//                intent.putExtra("USER_ROLE_KEY", pref.getUserRole());
//                startActivity(intent);
//                finish();
//            } else {
//                // Not logged in
//                startActivity(new Intent(Splash.this, AuthActivity.class));
//                finish();
//            }
//        }, SPLASH_DELAY_MS);
//    }
//}





//package com.example.paktrainfoodapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.WindowManager;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.paktrainfoodapp.ui.auth.AuthActivity;
//import com.example.paktrainfoodapp.ui.main.MainActivity;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class Splash extends AppCompatActivity {
//
//    private static final int SPLASH_DELAY_MS = 2000; // 2 sec delay
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setContentView(R.layout.activity_splash);
//
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//                // already logged in -> get role from Firestore
//                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                FirebaseFirestore.getInstance().collection("Users").document(uid)
//                        .get()
//                        .addOnSuccessListener(document -> {
//                            if (document.exists() && document.getString("role") != null) {
//                                String role = document.getString("role");
//                                Intent intent = new Intent(Splash.this, MainActivity.class);
//                                intent.putExtra("USER_ROLE_KEY", role);
//                                startActivity(intent);
//                                finish();
//                            } else {
//                                // role missing -> force login
//                                startActivity(new Intent(Splash.this, AuthActivity.class));
//                                finish();
//                            }
//                        })
//                        .addOnFailureListener(e -> {
//                            startActivity(new Intent(Splash.this, AuthActivity.class));
//                            finish();
//                        });
//
//            } else {
//                // not logged in
//                startActivity(new Intent(Splash.this, AuthActivity.class));
//                finish();
//            }
//        }, SPLASH_DELAY_MS);
//    }
//}
