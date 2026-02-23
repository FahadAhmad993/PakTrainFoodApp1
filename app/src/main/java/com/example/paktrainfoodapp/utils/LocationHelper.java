package com.example.paktrainfoodapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LocationHelper {

    @SuppressLint("MissingPermission")
    public static void savePassengerLocation(Context context, String passengerUid) {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                HashMap<String, Object> loc = new HashMap<>();
                loc.put("latitude", latitude);
                loc.put("longitude", longitude);
                loc.put("timestamp", System.currentTimeMillis());

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document("Passenger")
                        .collection("Locations")
                        .document(passengerUid)
                        .set(loc)
                        .addOnSuccessListener(a -> Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to save location", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
