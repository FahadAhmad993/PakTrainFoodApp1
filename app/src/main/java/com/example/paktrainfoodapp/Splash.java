package com.example.paktrainfoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.paktrainfoodapp.ui.FirebaseSeeder;
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



        // 🔥 Firestore Cache ENABLE (ONE TIME) that mean without internet last data access
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

