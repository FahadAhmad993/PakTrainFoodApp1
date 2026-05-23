package com.example.paktrainfoodapp.ui.main.Passenger;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.*;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private String orderId;
    private String passengerUid;
    private String station;

    private Location lastSavedLocation = null;
    private long lastHistoryTime = 0;

    private static final String CHANNEL_ID = "LocationChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        Log.d("SERVICE_DEBUG", "Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            orderId = intent.getStringExtra("orderId");
            passengerUid = intent.getStringExtra("passengerUid");
            station = intent.getStringExtra("station");
        }

        Log.d("SERVICE_DEBUG", "OrderId: " + orderId);
        Log.d("SERVICE_DEBUG", "PassengerUid: " + passengerUid);

        startForegroundService();
        startLocationUpdates();

        return START_STICKY;
    }

    private void startForegroundService() {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Live Tracking Active")
                .setContentText("Location is being tracked")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
    }

    private void startLocationUpdates() {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000
        ).setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                if (result == null || result.getLastLocation() == null) return;

                Location location = result.getLastLocation();

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                Log.d("LOCATION_DEBUG", "Callback fired");
                Log.d("LOCATION_DEBUG", "Lat: " + lat + " Lng: " + lng);

                PassengerLocationModel model = new PassengerLocationModel(
                        orderId,
                        passengerUid,
                        station,
                        lat,
                        lng,
                        System.currentTimeMillis()
                );

                // 🔵 ALWAYS UPDATE LATEST
                FirebaseDatabase.getInstance()
                        .getReference("OrderLocations")
                        .child(orderId)
                        .child("latest")
                        .setValue(model);

                // 🟢 HISTORY (SMART SAVE)
                long currentTime = System.currentTimeMillis();

                boolean movedEnough = false;
                if (lastSavedLocation != null) {
                    float distance = location.distanceTo(lastSavedLocation);
                    if (distance > 30) movedEnough = true;
                } else {
                    movedEnough = true;
                }

                boolean timePassed = (currentTime - lastHistoryTime) > 60000; // 60 sec

                if (movedEnough && timePassed) {

                    FirebaseDatabase.getInstance()
                            .getReference("OrderLocations")
                            .child(orderId)
                            .child("history")
                            .push()
                            .setValue(model);

                    lastHistoryTime = currentTime;
                    lastSavedLocation = location;

                    Log.d("FIREBASE_DEBUG", "History saved");
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LOCATION_ERROR", "Permission not granted");
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        Log.d("SERVICE_DEBUG", "Service Stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}



