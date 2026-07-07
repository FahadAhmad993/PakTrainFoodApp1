package com.example.paktrainfoodapp.ui.main.Restaurant.order;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Passenger.MenuitemModel;
import com.example.paktrainfoodapp.ui.main.Passenger.OrderItemsAdapter;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class OrderDetailFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    private ValueEventListener locationListener;
    private static final String DB_URL = "https://paktrainfoodservice-default-rtdb.firebaseio.com/";

    private String orderId;
    private final List<String> stations = new ArrayList<>();
    private final List<LatLng> routePoints = new ArrayList<>();

    private Marker trainMarker;
    private Marker mealMarker;
    private Polyline polyline;
    private boolean mapReady = false;
    private LatLng currentPos;
    private String mealStationName;
    private long lastSavedMinutes = -1;

    private TextView txtEta, txtTrain,txtTotalPrice,
            txtSeat, txtCoach, txtTicket, txtPhone, txtMealStation, txtCurrentStation;

    private static final float SPEED = 800; // meters per minute

    private RecyclerView recyclerItems;
    private OrderItemsAdapter adapter;
    private final List<MenuitemModel> itemList =
            new ArrayList<>();
    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment f = new OrderDetailFragment();
        Bundle b = new Bundle();
        b.putString("orderId", orderId);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_order_detail, container, false);

        txtEta = v.findViewById(R.id.txtEta);

        txtTrain = v.findViewById(R.id.txtTrainName);
        txtSeat = v.findViewById(R.id.txtSeatNumber);
        txtCoach = v.findViewById(R.id.txtCoachNumber);
        txtTicket = v.findViewById(R.id.txtTicketNumber);
        txtPhone = v.findViewById(R.id.txtPhone);
        txtMealStation = v.findViewById(R.id.txtMealStation);
        txtCurrentStation = v.findViewById(R.id.txtCurrentStation);
        recyclerItems =
                v.findViewById(R.id.recyclerItems);

        txtTotalPrice = v.findViewById(R.id.txtTotalPrice);

        recyclerItems.setLayoutManager(
                new LinearLayoutManager(getContext())
        );
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance(DB_URL).getReference();

        if (getArguments() != null) orderId = getArguments().getString("orderId");
        SupportMapFragment map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (map != null) map.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        mMap.setPadding(0, 50, 0, 50); // Padding for better camera view
        if (currentPos != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 14f));
        }
        loadOrder();
    }

    private void loadOrder() {
        if (orderId == null) return;
        firestore.collection("Orders").document(orderId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            // =============================
// LOAD ORDER ITEMS
// =============================

            List<Map<String, Object>> cartItems =
                    (List<Map<String, Object>>) doc.get("cartItems");

            itemList.clear();

            double total = 0;

            if (cartItems != null) {

                for (Map<String, Object> m : cartItems) {

                    MenuitemModel item =
                            new MenuitemModel();

                    item.setName(
                            String.valueOf(m.get("name"))
                    );

                    item.setDescription(
                            String.valueOf(m.get("description"))
                    );

                    item.setRestaurantName(
                            String.valueOf(m.get("restaurantName"))
                    );

                    item.setImageUrl(
                            String.valueOf(m.get("imageUrl"))
                    );

                    Object priceObj = m.get("price");

                    double price = 0;

                    if (priceObj != null) {

                        price =
                                Double.parseDouble(
                                        priceObj.toString()
                                );

                        item.setPrice(price);
                    }

                    Object quantityObj =
                            m.get("quantity");

                    int quantity = 1;

                    if (quantityObj != null) {

                        quantity =
                                Integer.parseInt(
                                        quantityObj.toString()
                                );

                        item.setQuantity(quantity);
                    }

                    total += price * quantity;

                    itemList.add(item);
                }
            }

            adapter = new OrderItemsAdapter(itemList);

            recyclerItems.setAdapter(adapter);

            txtTotalPrice.setText(
                    "Total: Rs " + total
            );
            txtTrain.setText("Train: " + doc.getString("trainName"));
            txtSeat.setText("Seat: " + doc.getString("seatNumber"));
            txtCoach.setText("Coach: " + doc.getString("coachNumber"));
            txtTicket.setText("Ticket: " + doc.getString("ticketNumber"));
            txtPhone.setText("Phone: " + doc.getString("phone"));
            mealStationName = doc.getString("mealStation");
            txtMealStation.setText("Meal Station: " + mealStationName);

            loadRoute(doc.getString("routeId"), mealStationName);
            listenLocation();
        });
    }

    private void loadRoute(String routeId, String mealStation) {
        firestore.collection("RailwaySystem").document("main").collection("Routes").document(routeId).get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) doc.get("stations");
                    if (list == null) return;
                    stations.clear();
                    for (Map<String, Object> m : list) {
                        String name = String.valueOf(m.get("name"));
                        stations.add(name);
                        if (name.equalsIgnoreCase(mealStation)) break;
                    }
                    loadCoords(list);
                });
    }

    private void loadCoords(List<Map<String, Object>> allStations) {
        Map<String, LatLng> coordMap = new HashMap<>();
        for (Map<String, Object> m : allStations) {
            String name = String.valueOf(m.get("name"));
            firestore.collection("RailwaySystem").document("main").collection("Stations").document(name).get()
                    .addOnSuccessListener(doc -> {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat != null && lng != null) {
                            coordMap.put(name, new LatLng(lat, lng));
                        }
                        if (coordMap.size() >= stations.size()) {
                            routePoints.clear();
                            for (String s : stations) {
                                if (coordMap.containsKey(s)) routePoints.add(coordMap.get(s));
                            }
                            drawRoute();
                            if (mealStationName != null && coordMap.containsKey(mealStationName)) {
                                if (mealMarker != null) mealMarker.remove();
                                mealMarker = mMap.addMarker(new MarkerOptions()
                                        .position(coordMap.get(mealStationName))
                                        .title("Meal Station: " + mealStationName)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            }
                            if (currentPos != null) { updateCurrentStation(); updateETA(); }
                        }
                    });
        }
    }

    private void drawRoute() {
        if (!mapReady || routePoints.isEmpty()) return;
        if (polyline != null) polyline.remove();
        polyline = mMap.addPolyline(new PolylineOptions().addAll(routePoints).width(12).color(Color.BLUE));
    }

    private void listenLocation() {
        if (locationListener != null) realtimeDb.child("OrderLocations").child(orderId).child("latest").removeEventListener(locationListener);
        locationListener = realtimeDb.child("OrderLocations").child(orderId).child("latest").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                Double lat = snap.child("lat").getValue(Double.class);
                Double lng = snap.child("lng").getValue(Double.class);
                if (lat == null || lng == null) return;

                currentPos = new LatLng(lat, lng);
                if (trainMarker != null) trainMarker.remove();
                trainMarker = mMap.addMarker(new MarkerOptions().position(currentPos).title("Train"));

                // Zoom fix
                if (mapReady) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 14f));
                }

                updateCurrentStation();
                updateETA();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateCurrentStation() {
        if (currentPos == null || routePoints.isEmpty()) return;
        String current = "In Transit";
        float min = Float.MAX_VALUE;
        for (int i = 0; i < routePoints.size(); i++) {
            float[] d = new float[1];
            Location.distanceBetween(currentPos.latitude, currentPos.longitude, routePoints.get(i).latitude, routePoints.get(i).longitude, d);
            if (d[0] < min) { min = d[0]; current = stations.get(i); }
        }
        txtCurrentStation.setText("Current: " + current);
    }

    private void updateETA() {

        if (currentPos == null || routePoints.isEmpty()) return;

        float[] d = new float[1];

        Location.distanceBetween(
                currentPos.latitude,
                currentPos.longitude,
                routePoints.get(routePoints.size() - 1).latitude,
                routePoints.get(routePoints.size() - 1).longitude,
                d
        );

        int travelMins = (int) (d[0] / SPEED);

        int totalDelay = stations.size() * 10;

        int totalMins = travelMins + totalDelay;

        // =========================
        // CREATE END TIME
        // =========================

        long etaEndTime =
                System.currentTimeMillis()
                        + (totalMins * 60 * 1000L);

        // =========================
        // SHOW ETA LOCALLY
        // =========================

        int hours = totalMins / 60;

        int minutes = totalMins % 60;

        txtEta.setText(
                "ETA: ~" + hours + " hr " + minutes + " min"
        );

        // =========================
        // SAVE ONLY IF ETA CHANGED
        // =========================

        if (Math.abs(totalMins - lastSavedMinutes) >= 1) {

            lastSavedMinutes = totalMins;

            firestore.collection("Orders")
                    .document(orderId)
                    .update("etaEndTime", etaEndTime);
        }
    }

    @Override public void onDestroyView() {
        if (locationListener != null) realtimeDb.child("OrderLocations").child(orderId).child("latest").removeEventListener(locationListener);
        super.onDestroyView();
    }
}



//





























