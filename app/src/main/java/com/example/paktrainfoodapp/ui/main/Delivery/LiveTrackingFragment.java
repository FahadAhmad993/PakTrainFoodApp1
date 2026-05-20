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






