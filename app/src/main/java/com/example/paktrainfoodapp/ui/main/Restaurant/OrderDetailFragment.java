package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class OrderDetailFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    private ValueEventListener trainLocationListener;

    private String orderId;
    private List<String> stations = new ArrayList<>();
    private List<LatLng> routePoints = new ArrayList<>();

    private TextView txtEta, txtName, txtPrice, txtDesc;
    private TextView txtTrain, txtSeat, txtCoach, txtTicket, txtPhone, txtMealStation, txtCurrentStation;

    private LatLng trainPos;
    private Marker trainMarker;
    private Polyline routePolyline;
    private Marker mealMarker;

    private boolean mapReady = false;
    private static final float AVG_SPEED_M_PER_MIN = 800;
    private static final int STOP_TIME_PER_STATION = 10;

    public static OrderDetailFragment newInstance(String orderId, String passengerUid) {
        OrderDetailFragment f = new OrderDetailFragment();
        Bundle b = new Bundle();
        b.putString("orderId", orderId);
        b.putString("passengerUid", passengerUid);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_order_detail, container, false);

        txtEta = v.findViewById(R.id.txtEta);
        txtName = v.findViewById(R.id.txtName);
        txtPrice = v.findViewById(R.id.txtPrice);
        txtDesc = v.findViewById(R.id.txtDesc);
        txtTrain = v.findViewById(R.id.txtTrainName);
        txtSeat = v.findViewById(R.id.txtSeatNumber);
        txtCoach = v.findViewById(R.id.txtCoachNumber);
        txtTicket = v.findViewById(R.id.txtTicketNumber);
        txtPhone = v.findViewById(R.id.txtPhone);
        txtMealStation = v.findViewById(R.id.txtMealStation);
        txtCurrentStation = v.findViewById(R.id.txtCurrentStation);

        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        mMap.clear();
        routePoints.clear();
        stations.clear();
        loadOrder();
    }

    private void loadOrder() {
        if (orderId == null) return;

        firestore.collection("Orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || getContext() == null) return;

                    // 1. Text with Labels (Prefixes)
                    txtName.setText(doc.getString("itemName"));
                    txtDesc.setText(doc.getString("itemDesc"));

                    // Price handling (Checking for String or Long/Double)
                    Object priceObj = doc.get("itemPrice");
                    txtPrice.setText("Price: Rs. " + (priceObj != null ? priceObj.toString() : "0"));

                    txtTrain.setText("Train: " + doc.getString("trainName"));
                    txtSeat.setText("Seat: " + doc.getString("seatNumber"));
                    txtCoach.setText("Coach: " + doc.getString("coachNumber"));
                    txtTicket.setText("Ticket: " + doc.getString("ticketNumber"));
                    txtPhone.setText("Phone: " + doc.getString("phone"));

                    String meal = doc.getString("mealStation");
                    txtMealStation.setText("Meal Station: " + meal);

                    String routeId = doc.getString("routeId");
                    if (routeId != null && meal != null) {
                        loadRoute(routeId, meal);
                    }

                    listenTrainLocation();
                })
                .addOnFailureListener(e -> showToast("Connection Error"));
    }

    private void loadRoute(String routeId, String mealStation) {
        firestore.collection("RailwaySystem").document("main")
                .collection("Routes").document(routeId).get()
                .addOnSuccessListener(doc -> {
                    List<Object> raw = (List<Object>) doc.get("stations");
                    if (raw == null) return;

                    stations.clear();
                    for (Object o : raw) {
                        if (o instanceof String) stations.add((String) o);
                        else if (o instanceof Map) {
                            Object name = ((Map) o).get("name");
                            if (name != null) stations.add(name.toString());
                        }
                    }
                    buildOrderedRoute(mealStation);
                });
    }

    private void buildOrderedRoute(String mealStation) {
        List<String> filtered = new ArrayList<>();
        boolean startTracking = false;
        for (String s : stations) {
            if (!startTracking) startTracking = true;
            if (startTracking) filtered.add(s);
            if (s.equalsIgnoreCase(mealStation)) break;
        }
        stations.clear();
        stations.addAll(filtered);
        loadCoords();
    }

    private void loadCoords() {
        routePoints.clear();
        Map<String, LatLng> tempMap = new HashMap<>();
        final int totalStations = stations.size();

        for (String st : stations) {
            firestore.collection("RailwaySystem").document("main")
                    .collection("Stations").document(st).get()
                    .addOnSuccessListener(doc -> {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            tempMap.put(st, new LatLng(lat, lng));
                            if (tempMap.size() == totalStations) {
                                for (String s : stations) {
                                    routePoints.add(tempMap.get(s));
                                }
                                drawRoute();
                                updateCurrentStation();
                                updateETA();
                            }
                        }
                    });
        }
    }

    private void drawRoute() {
        if (!mapReady || routePoints.isEmpty()) return;

        if (routePolyline != null) routePolyline.remove();
        routePolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(12)
                .color(Color.BLUE));

        if (mealMarker != null) mealMarker.remove();
        mealMarker = mMap.addMarker(new MarkerOptions()
                .position(routePoints.get(routePoints.size() - 1))
                .title("Delivery Point"));
    }

    private void listenTrainLocation() {
        if (orderId == null) return;

        if (trainLocationListener != null) {
            realtimeDb.child("OrderLocations").child(orderId).child("latest").removeEventListener(trainLocationListener);
        }

        trainLocationListener = realtimeDb.child("OrderLocations").child(orderId).child("latest")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double lat = snapshot.child("lat").getValue(Double.class);
                        Double lng = snapshot.child("lng").getValue(Double.class);

                        if (lat == null || lng == null || !mapReady) return;

                        trainPos = new LatLng(lat, lng);

                        if (trainMarker != null) trainMarker.remove();

                        trainMarker = mMap.addMarker(new MarkerOptions()
                                .position(trainPos)
                                .title("Live Train")
                                .anchor(0.5f, 0.5f)
                                .icon(getResizedIcon(R.drawable.logo5, 80, 80))); // Adjusted size

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trainPos, 12));

                        updateCurrentStation();
                        updateETA();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private BitmapDescriptor getResizedIcon(int resourceId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void updateCurrentStation() {
        if (trainPos == null || routePoints.isEmpty() || txtCurrentStation == null) return;

        float minDistance = Float.MAX_VALUE;
        String current = "In Transit";

        for (int i = 0; i < routePoints.size(); i++) {
            float[] results = new float[1];
            Location.distanceBetween(trainPos.latitude, trainPos.longitude,
                    routePoints.get(i).latitude, routePoints.get(i).longitude, results);

            if (results[0] < minDistance) {
                minDistance = results[0];
                current = stations.get(i);
            }
        }
        txtCurrentStation.setText("Current: " + current);
    }

    private void updateETA() {
        if (trainPos == null || routePoints.isEmpty() || txtEta == null) return;

        LatLng mealPos = routePoints.get(routePoints.size() - 1);
        float[] results = new float[1];
        Location.distanceBetween(trainPos.latitude, trainPos.longitude,
                mealPos.latitude, mealPos.longitude, results);

        int distance = (int) results[0];
        int travelTimeMinutes = (int) (distance / AVG_SPEED_M_PER_MIN);
        int totalMinutes = Math.max(2, travelTimeMinutes + (stations.size() * STOP_TIME_PER_STATION));

        // 2. New Time Format: Hours and Minutes
        String timeText;
        if (totalMinutes >= 60) {
            int hours = totalMinutes / 60;
            int mins = totalMinutes % 60;
            timeText = hours + " hour " + mins + " min";
        } else {
            timeText = totalMinutes + " min";
        }

        txtEta.setText("ETA: ~" + timeText);
    }

    private void showToast(String msg) {
        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realtimeDb != null && trainLocationListener != null) {
            realtimeDb.child("OrderLocations").child(orderId).child("latest").removeEventListener(trainLocationListener);
        }
    }
}







