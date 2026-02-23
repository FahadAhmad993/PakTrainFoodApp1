package com.example.paktrainfoodapp.ui.main.Delivery;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LiveTrackingFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Marker passengerMarker, stationMarker;
    private Polyline bikeRoutePolyline, walkRoutePolyline;

    private TextView tvDistance, tvETA_Bike, tvETA_Walk;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration locationListener;

    private final LatLng malakwalStation = new LatLng(32.5528, 73.2165);
    private final String passengerUid = "tZtn3qs6NXd7hrnmmGcWxlGnZJ72";

    private final String DIRECTIONS_API_KEY = "AIzaSyCmRd6o2p8AOTeoKJHb5DqHm5ih8fzWKRg";

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          @Nullable android.view.ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {

        android.view.View view = inflater.inflate(R.layout.fragment_delivery_live_tracking, container, false);

        mapView = view.findViewById(R.id.mapView);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvETA_Bike = view.findViewById(R.id.tvETA_Bike);
        tvETA_Walk = view.findViewById(R.id.tvETA_Walk);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        stationMarker = googleMap.addMarker(new MarkerOptions()
                .position(malakwalStation)
                .title("Malakwal Station")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malakwalStation, 12f));

        startListeningPassengerLocation(passengerUid);
    }

    private void startListeningPassengerLocation(String passengerUid) {
        DocumentReference locRef = db.collection("Users")
                .document("Passenger")
                .collection("Locations")
                .document(passengerUid);

        locationListener = locRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error fetching location", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Double lat = snapshot.getDouble("latitude");
                Double lng = snapshot.getDouble("longitude");

                if (lat != null && lng != null) {
                    LatLng passengerLatLng = new LatLng(lat, lng);

                    if (passengerMarker != null) {
                        passengerMarker.setPosition(passengerLatLng);
                    } else {
                        passengerMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(passengerLatLng)
                                        .title("Passenger")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        );
                    }

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLatLng, 15f));

                    // Fetch routes for both driving and walking
                    fetchRoute(passengerLatLng, malakwalStation, "driving");  // bike
                    fetchRoute(passengerLatLng, malakwalStation, "walking");  // walk
                }
            }
        });
    }

    private void fetchRoute(LatLng origin, LatLng dest, String mode) {
        new Thread(() -> {
            try {
                String urlStr = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + origin.latitude + "," + origin.longitude +
                        "&destination=" + dest.latitude + "," + dest.longitude +
                        "&mode=" + mode +
                        "&key=" + DIRECTIONS_API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() == 0) return;

                JSONObject route = routes.getJSONObject(0);
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                String distanceText = leg.getJSONObject("distance").getString("text");
                String durationText = leg.getJSONObject("duration").getString("text");
                String polylinePoints = route.getJSONObject("overview_polyline").getString("points");

                List<LatLng> path = decodePoly(polylinePoints);

                requireActivity().runOnUiThread(() -> {
                    if (mode.equals("driving")) {
                        if (bikeRoutePolyline != null) bikeRoutePolyline.remove();
                        bikeRoutePolyline = googleMap.addPolyline(new PolylineOptions()
                                .addAll(path)
                                .width(10)
                                .color(0xFF2196F3)
                                .geodesic(true));

                        tvDistance.setText("Distance: " + distanceText);
                        tvETA_Bike.setText("ETA (Bike): " + durationText);
                    } else if (mode.equals("walking")) {
                        if (walkRoutePolyline != null) walkRoutePolyline.remove();
                        walkRoutePolyline = googleMap.addPolyline(new PolylineOptions()
                                .addAll(path)
                                .width(10)
                                .color(0xFF4CAF50) // green
                                .geodesic(true));

                        tvETA_Walk.setText("ETA (Walk): " + durationText);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng((lat / 1E5), (lng / 1E5)));
        }
        return poly;
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onStop()  { super.onStop(); mapView.onStop(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationListener != null) locationListener.remove();
    }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}





//package com.example.paktrainfoodapp.ui.main.Delivery;
//
//import android.os.Bundle;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.paktrainfoodapp.R;
//import com.google.android.gms.maps.*;
//import com.google.android.gms.maps.model.*;
//import com.google.firebase.firestore.*;
//
//import com.google.maps.android.SphericalUtil;
//
//public class LiveTrackingFragment extends Fragment implements OnMapReadyCallback {
//
//    private MapView mapView;
//    private GoogleMap googleMap;
//    private Marker passengerMarker, stationMarker;
//    private Polyline routePolyline;
//
//    private TextView tvDistance;  // Distance TextView
//
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private ListenerRegistration locationListener;
//
//    // Malakwal Station coordinates
//    private final LatLng malakwalStation = new LatLng(32.5528, 73.2165);
//
//    // Specific passenger UID
//    private final String passengerUid = "tZtn3qs6NXd7hrnmmGcWxlGnZJ72";
//
//    @Nullable
//    @Override
//    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
//                                          @Nullable android.view.ViewGroup container,
//                                          @Nullable Bundle savedInstanceState) {
//
//        android.view.View view = inflater.inflate(R.layout.fragment_delivery_live_tracking, container, false);
//
//        mapView = view.findViewById(R.id.mapView);
//        tvDistance = view.findViewById(R.id.tvDistance);  // Initialize distance TextView
//
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);
//
//        return view;
//    }
//
//    @Override
//    public void onMapReady(@NonNull GoogleMap map) {
//        googleMap = map;
//
//        // Add Malakwal Station marker
//        stationMarker = googleMap.addMarker(new MarkerOptions()
//                .position(malakwalStation)
//                .title("Malakwal Station")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//
//        // Zoom initially to Malakwal
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malakwalStation, 12f));
//
//        // Start listening passenger location
//        startListeningPassengerLocation(passengerUid);
//    }
//
//    private void startListeningPassengerLocation(String passengerUid) {
//        DocumentReference locRef = db.collection("Users")
//                .document("Passenger")
//                .collection("Locations")
//                .document(passengerUid);
//
//        locationListener = locRef.addSnapshotListener((snapshot, error) -> {
//            if (error != null) {
//                System.out.println("Error fetching location: " + error.getMessage());
//                Toast.makeText(getContext(), "Error fetching location", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (snapshot != null && snapshot.exists()) {
//                Double lat = snapshot.getDouble("latitude");
//                Double lng = snapshot.getDouble("longitude");
//
//                if (lat != null && lng != null) {
//                    LatLng passengerLatLng = new LatLng(lat, lng);
//
//                    // Update or create passenger marker
//                    if (passengerMarker != null) {
//                        passengerMarker.setPosition(passengerLatLng);
//                    } else {
//                        passengerMarker = googleMap.addMarker(
//                                new MarkerOptions()
//                                        .position(passengerLatLng)
//                                        .title("Passenger")
//                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//                        );
//                    }
//
//                    // Zoom to passenger
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLatLng, 15f));
//
//                    // Draw route from passenger to Malakwal
//                    drawRoute(passengerLatLng, malakwalStation);
//
//                    // Calculate distance in km
//                    double distance = SphericalUtil.computeDistanceBetween(passengerLatLng, malakwalStation) / 1000;
//
//                    // Show distance in TextView
//                    tvDistance.setText("Distance: " + String.format("%.2f km", distance));
//                }
//            }
//        });
//    }
//
//    private void drawRoute(LatLng from, LatLng to) {
//        if (routePolyline != null) routePolyline.remove();
//
//        routePolyline = googleMap.addPolyline(new PolylineOptions()
//                .add(from, to)
//                .width(8)
//                .color(0xFF2196F3)
//                .geodesic(true));
//    }
//
//    @Override public void onResume() { super.onResume(); mapView.onResume(); }
//    @Override public void onStart() { super.onStart(); mapView.onStart(); }
//    @Override public void onStop()  { super.onStop(); mapView.onStop(); }
//    @Override public void onPause() { super.onPause(); mapView.onPause(); }
//    @Override public void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//        if (locationListener != null) locationListener.remove();
//    }
//    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
//}






//package com.example.paktrainfoodapp.ui.main.Delivery;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.Fragment;
//
//import com.example.paktrainfoodapp.R;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.MapsInitializer;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.IOException;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class LiveTrackingFragment extends Fragment {
//
//    private MapView mapView;
//    private GoogleMap googleMap;
//
//    private TextView tvDistance, tvFrom, tvTo, tvETA_Bike, tvETA_Walk;
//
//    private FirebaseFirestore db;
//
//    private double deliveryLat = 0, deliveryLng = 0;
//    private double passengerLat = 0, passengerLng = 0;
//
//    private String deliveryUid;
//    private String orderId;
//
//    private OkHttpClient client = new OkHttpClient();
//    private String directionsApiKey = "AIzaSyCmRd6o2p8AOTeoKJHb5DqHm5ih8fzWKRg";
//
//    private Handler handler = new Handler();
//    private Runnable updateRunnable;
//
//    // Use this method to create fragment dynamically
//    public static LiveTrackingFragment newInstance(String orderId, String deliveryUid) {
//        LiveTrackingFragment fragment = new LiveTrackingFragment();
//        Bundle args = new Bundle();
//        args.putString("orderId", orderId);
//        args.putString("deliveryUid", deliveryUid);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            orderId = getArguments().getString("orderId");
//            deliveryUid = getArguments().getString("deliveryUid");
//        }
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        mapView = view.findViewById(R.id.mapView);
//        tvDistance = view.findViewById(R.id.tvDistance);
//        tvFrom = view.findViewById(R.id.tvFrom);
//        tvTo = view.findViewById(R.id.tvTo);
//        tvETA_Bike = view.findViewById(R.id.tvETA_Bike);
//        tvETA_Walk = view.findViewById(R.id.tvETA_Walk);
//
//        db = FirebaseFirestore.getInstance();
//
//        mapView.onCreate(savedInstanceState);
//        mapView.onResume();
//        MapsInitializer.initialize(requireContext());
//
//        mapView.getMapAsync(map -> {
//            googleMap = map;
//            googleMap.getUiSettings().setZoomControlsEnabled(true);
//            startTracking();
//        });
//    }
//
//    private void startTracking() {
//        updateRunnable = new Runnable() {
//            @Override
//            public void run() {
//                fetchLocations();
//                handler.postDelayed(this, 5000); // update every 5 seconds
//            }
//        };
//        handler.post(updateRunnable);
//    }
//
//    private void fetchLocations() {
//        if (orderId == null || deliveryUid == null) return;
//
//        // Delivery boy location
//        DocumentReference deliveryRef = db.collection("Users")
//                .document("Delivery")
//                .collection("VerifiedRegister")
//                .document(deliveryUid);
//
//        deliveryRef.get().addOnSuccessListener(snapshot -> {
//            if (snapshot.exists()) {
//                Double lat = snapshot.getDouble("latitude");
//                Double lng = snapshot.getDouble("longitude");
//                if (lat != null && lng != null) {
//                    deliveryLat = lat;
//                    deliveryLng = lng;
//                    updateMap();
//                }
//            }
//        });
//
//        // Passenger location
//        DocumentReference passengerRef = db.collection("Orders")
//                .document(orderId);
//
//        passengerRef.get().addOnSuccessListener(snapshot -> {
//            if (snapshot.exists()) {
//                Double lat = snapshot.getDouble("passengerLat");
//                Double lng = snapshot.getDouble("passengerLng");
//                if (lat != null && lng != null) {
//                    passengerLat = lat;
//                    passengerLng = lng;
//                    updateMap();
//                }
//            }
//        });
//    }
//
//    private void updateMap() {
//        if (googleMap == null || deliveryLat == 0 || passengerLat == 0) return;
//
//        googleMap.clear();
//
//        LatLng deliveryLatLng = new LatLng(deliveryLat, deliveryLng);
//        LatLng passengerLatLng = new LatLng(passengerLat, passengerLng);
//
//        googleMap.addMarker(new MarkerOptions().position(deliveryLatLng).title("Delivery Boy"));
//        googleMap.addMarker(new MarkerOptions().position(passengerLatLng).title("Passenger"));
//
//        PolylineOptions line = new PolylineOptions()
//                .add(deliveryLatLng)
//                .add(passengerLatLng)
//                .width(6)
//                .color(Color.BLUE);
//        googleMap.addPolyline(line);
//
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(deliveryLatLng)
//                .include(passengerLatLng)
//                .build();
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//
//        tvFrom.setText("From: Delivery Boy");
//        tvTo.setText("To: Passenger");
//
//        getDirections(deliveryLatLng, passengerLatLng, "driving");  // Bike
//        getDirections(deliveryLatLng, passengerLatLng, "walking");  // Walk
//    }
//
//    private void getDirections(LatLng origin, LatLng dest, String mode) {
//        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
//                + origin.latitude + "," + origin.longitude
//                + "&destination=" + dest.latitude + "," + dest.longitude
//                + "&mode=" + mode
//                + "&key=" + directionsApiKey;
//
//        Request request = new Request.Builder().url(url).build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.e("DirectionsAPI", "Failed: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (!response.isSuccessful()) return;
//
//                String resp = response.body().string();
//                try {
//                    JSONObject json = new JSONObject(resp);
//                    JSONArray routes = json.getJSONArray("routes");
//                    if (routes.length() > 0) {
//                        JSONObject leg = routes.getJSONObject(0)
//                                .getJSONArray("legs").getJSONObject(0);
//
//                        double distanceKm = leg.getJSONObject("distance").getDouble("value") / 1000.0;
//                        double durationMin = leg.getJSONObject("duration").getDouble("value") / 60.0;
//
//                        String finalText = String.format("%.2f km, %.0f min", distanceKm, durationMin);
//
//                        requireActivity().runOnUiThread(() -> {
//                            if (mode.equals("driving")) tvETA_Bike.setText("ETA (Bike): " + finalText);
//                            else tvETA_Walk.setText("ETA (Walk): " + finalText);
//
//                            if (mode.equals("driving")) tvDistance.setText("Distance: " + String.format("%.2f km", distanceKm));
//                        });
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    // MapView lifecycle
//    @Override
//    public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
//    @Override
//    public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mapView != null) mapView.onDestroy();
//        handler.removeCallbacks(updateRunnable);
//    }
//    @Override
//    public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
//}



