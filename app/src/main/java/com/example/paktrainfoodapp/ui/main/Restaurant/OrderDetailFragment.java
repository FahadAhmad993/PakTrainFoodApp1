package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailFragment extends Fragment implements OnMapReadyCallback {

    // UI
    private TextView txtName, txtPrice, txtDesc, txtTrain, txtTicket, txtSeat, txtCoach, txtPhone, txtStation, txtEta;
    private ImageView imgFood;

    // IDs
    private String orderId, passengerUid;

    // Map
    private GoogleMap googleMap;
    private LatLng passengerLatLng, stationLatLng;
    private Marker passengerMarker;
    private Polyline routePolyline;

    // Firebase
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeRef;

    private Handler handler = new Handler();
    private Runnable updateRunnable;

    // ================= NEW INSTANCE =================
    public static OrderDetailFragment newInstance(String orderId, String passengerUid) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        args.putString("passengerUid", passengerUid);
        fragment.setArguments(args);
        return fragment;
    }

    // ================= ON CREATE VIEW =================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    // ================= ON VIEW CREATED =================
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // UI binding
        txtName = view.findViewById(R.id.txtName);
        txtPrice = view.findViewById(R.id.txtPrice);
        txtDesc = view.findViewById(R.id.txtDesc);
        txtTrain = view.findViewById(R.id.txtTrainName);
        txtTicket = view.findViewById(R.id.txtTicketNumber);
        txtSeat = view.findViewById(R.id.txtSeatNumber);
        txtCoach = view.findViewById(R.id.txtCoachNumber);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtStation = view.findViewById(R.id.txtMealStation);
        txtEta = view.findViewById(R.id.txtEta);
        imgFood = view.findViewById(R.id.imgFood);

        firestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            passengerUid = getArguments().getString("passengerUid");
        }

        // Map init
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadOrderDetail();
        startLiveLocationUpdates();
    }

    // ================= LOAD ORDER DETAIL =================
    private void loadOrderDetail() {
        firestore.collection("Users")
                .document("Passenger")
                .collection("OrderNow")
                .document(passengerUid)
                .collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    txtName.setText(doc.getString("itemName"));
                    txtPrice.setText("Rs. " + doc.getDouble("itemPrice"));
                    txtDesc.setText(doc.getString("itemDesc"));

                    txtTrain.setText("Train: " + doc.getString("trainName"));
                    txtTicket.setText("Ticket No: " + doc.getString("ticketNumber"));
                    txtSeat.setText("Seat No: " + doc.getString("seatNumber"));
                    txtCoach.setText("Coach No: " + doc.getString("coachNumber"));
                    txtPhone.setText("Phone: " + doc.getString("phone"));
                    txtStation.setText("Meal Station: " + doc.getString("mealStation"));

                    loadImage(doc.getString("itemImage"));
                    loadStationLocation(doc.getString("mealStation"));
                });
    }

    // ================= LOAD IMAGE =================
    private void loadImage(String base64) {
        if (base64 == null || base64.isEmpty()) {
            imgFood.setImageResource(R.drawable.ic_food_placeholder);
            return;
        }
        try {
            byte[] data = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            imgFood.setImageBitmap(bmp);
        } catch (Exception e) {
            imgFood.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    // ================= LOAD STATION LOCATION =================
    private void loadStationLocation(String stationName) {
        firestore.collection("Users")
                .document("StationLocation")
                .collection("Stations")
                .document(stationName)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat != null && lng != null) {
                        stationLatLng = new LatLng(lat, lng);
                        drawRoute(); // Draw initial route
                    }
                });
    }

    // ================= START LIVE LOCATION UPDATES =================
    private void startLiveLocationUpdates() {
        if (orderId == null || orderId.isEmpty()) return;

        realtimeRef = FirebaseDatabase.getInstance()
                .getReference("Orders")
                .child(orderId)
                .child("passengerLocation");

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                realtimeRef.get().addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);

                    if (lat != null && lng != null) {
                        passengerLatLng = new LatLng(lat, lng);
                        animatePassengerMarker(passengerLatLng);
                        updateEta();
                        drawRoute();
                    }
                });
                handler.postDelayed(this, 60 * 1000); // update every 1 min
            }
        };
        handler.post(updateRunnable);
    }

    // ================= MAP READY =================
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        drawRoute();
    }

    // ================= DRAW ROUTE =================
    private void drawRoute() {
        if (googleMap == null || passengerLatLng == null || stationLatLng == null) return;

        // Remove old polyline
        if (routePolyline != null) routePolyline.remove();

        // Simple straight line or list of stations in-between
        List<LatLng> points = new ArrayList<>();
        points.add(passengerLatLng);



        points.add(stationLatLng);

        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .color(0xFF2196F3)
                .width(8f)
                .geodesic(true);
        routePolyline = googleMap.addPolyline(options);

        // Add markers if not exist
        if (passengerMarker == null) {
            passengerMarker = googleMap.addMarker(new MarkerOptions()
                    .position(passengerLatLng)
                    .title("Passenger")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        googleMap.addMarker(new MarkerOptions()
                .position(stationLatLng)
                .title("Meal Station")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Move camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLatLng, 13f));
    }

    // ================= ANIMATE PASSENGER ================  
    private void animatePassengerMarker(LatLng newPos) {
        if (passengerMarker == null) return;

        final LatLng start = passengerMarker.getPosition();
        final LatLng end = newPos;
        final long duration = 2000; // 2 sec
        final long startTime = System.currentTimeMillis();
        final Handler h = new Handler();

        h.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1, (float) elapsed / duration);
                double lat = start.latitude + t * (end.latitude - start.latitude);
                double lng = start.longitude + t * (end.longitude - start.longitude);
                passengerMarker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) h.postDelayed(this, 16);
            }
        });
    }

    // ================= CALCULATE ETA =================
    private void updateEta() {
        if (passengerLatLng == null || stationLatLng == null) return;

        double earthRadius = 6371; // km
        double dLat = Math.toRadians(stationLatLng.latitude - passengerLatLng.latitude);
        double dLng = Math.toRadians(stationLatLng.longitude - passengerLatLng.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(passengerLatLng.latitude))
                * Math.cos(Math.toRadians(stationLatLng.latitude))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c; // km

        double speed = 60.0; // km/h average train
        int etaMinutes = (int) ((distance / speed) * 60);

        txtEta.setText("Estimated travel time by train: " + etaMinutes + " min (" + String.format("%.2f", distance) + " km)");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}





//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.paktrainfoodapp.R;
//import com.google.android.gms.maps.*;
//import com.google.android.gms.maps.model.*;
//import com.google.firebase.database.*;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class OrderDetailFragment extends Fragment implements OnMapReadyCallback {
//
//    // UI
//    private TextView txtName, txtPrice, txtDesc;
//    private TextView txtTrain, txtTicket, txtSeat, txtCoach, txtPhone, txtStation;
//    private ImageView imgFood;
//
//    // IDs
//    private String orderId, passengerUid;
//
//    // Map
//    private GoogleMap googleMap;
//    private LatLng passengerLatLng, stationLatLng;
//
//    // Firebase
//    private FirebaseFirestore firestore;
//    private DatabaseReference realtimeRef;
//
//    // ================= NEW INSTANCE =================
//
//    public static OrderDetailFragment newInstance(String orderId, String passengerUid) {
//        OrderDetailFragment fragment = new OrderDetailFragment();
//        Bundle args = new Bundle();
//        args.putString("orderId", orderId);
//        args.putString("passengerUid", passengerUid);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    // ================= ON CREATE VIEW =================
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState
//    ) {
//        return inflater.inflate(R.layout.fragment_order_detail, container, false);
//    }
//
//    // ================= ON VIEW CREATED =================
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        // UI binding
//        txtName = view.findViewById(R.id.txtName);
//        txtPrice = view.findViewById(R.id.txtPrice);
//        txtDesc = view.findViewById(R.id.txtDesc);
//
//        txtTrain = view.findViewById(R.id.txtTrainName);
//        txtTicket = view.findViewById(R.id.txtTicketNumber);
//        txtSeat = view.findViewById(R.id.txtSeatNumber);
//        txtCoach = view.findViewById(R.id.txtCoachNumber);
//        txtPhone = view.findViewById(R.id.txtPhone);
//        txtStation = view.findViewById(R.id.txtMealStation);
//
//        imgFood = view.findViewById(R.id.imgFood);
//
//        firestore = FirebaseFirestore.getInstance();
//
//        if (getArguments() != null) {
//            orderId = getArguments().getString("orderId");
//            passengerUid = getArguments().getString("passengerUid");
//        }
//
//        // Map init
//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//
//        loadOrderDetail();
//        loadPassengerLiveLocation();
//    }
//
//    // ================= LOAD ORDER DETAIL (FIRESTORE) =================
//
//    private void loadOrderDetail() {
//
//        firestore.collection("Users")
//                .document("Passenger")
//                .collection("OrderNow")
//                .document(passengerUid)
//                .collection("Orders")
//                .document(orderId)
//                .get()
//                .addOnSuccessListener(doc -> {
//
//                    if (!doc.exists()) return;
//
//                    txtName.setText(doc.getString("itemName"));
//                    txtPrice.setText("Rs. " + doc.getDouble("itemPrice"));
//                    txtDesc.setText(doc.getString("itemDesc"));
//
//                    txtTrain.setText(doc.getString("trainName"));
//                    txtTicket.setText(doc.getString("ticketNumber"));
//                    txtSeat.setText(doc.getString("seatNumber"));
//                    txtCoach.setText(doc.getString("coachNumber"));
//                    txtPhone.setText(doc.getString("phone"));
//                    txtStation.setText(doc.getString("mealStation"));
//
//                    loadImage(doc.getString("itemImage"));
//
//                    // Station location
//                    loadStationLocation(doc.getString("mealStation"));
//                });
//    }
//
//    // ================= IMAGE =================
//
//    private void loadImage(String base64) {
//        if (base64 == null || base64.isEmpty()) {
//            imgFood.setImageResource(R.drawable.ic_food_placeholder);
//            return;
//        }
//
//        try {
//            byte[] data = Base64.decode(base64, Base64.DEFAULT);
//            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//            imgFood.setImageBitmap(bmp);
//        } catch (Exception e) {
//            imgFood.setImageResource(R.drawable.ic_food_placeholder);
//        }
//    }
//
//    // ================= STATION LOCATION (FIRESTORE) =================
//
//    private void loadStationLocation(String stationName) {
//
//        firestore.collection("Users")
//                .document("StationLocation")
//                .collection("Stations")
//                .document(stationName)
//                .get()
//                .addOnSuccessListener(doc -> {
//
//                    if (!doc.exists()) return;
//
//                    Double lat = doc.getDouble("lat");
//                    Double lng = doc.getDouble("lng");
//
//                    if (lat != null && lng != null) {
//                        stationLatLng = new LatLng(lat, lng);
//                        updateMap();
//                    }
//                });
//    }
//
//    // ================= PASSENGER LIVE LOCATION (REALTIME DB) =================
//
//    private void loadPassengerLiveLocation() {
//
//        if (orderId == null || orderId.isEmpty()) return;
//
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference()
//                .child("Orders")
//                .child(orderId)
//                .child("passengerLocation");
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (!snapshot.exists()) return;
//
//                Double lat = snapshot.child("lat").getValue(Double.class);
//                Double lng = snapshot.child("lng").getValue(Double.class);
//
//                if (lat != null && lng != null) {
//                    passengerLatLng = new LatLng(lat, lng);
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // optional
//            }
//        });
//    }
//
//
//    // ================= MAP READY =================
//
//    @Override
//    public void onMapReady(@NonNull GoogleMap map) {
//        googleMap = map;
//        updateMap();
//    }
//
//    // ================= UPDATE MAP =================
//
//    private void updateMap() {
//
//        if (googleMap == null ||
//                passengerLatLng == null ||
//                stationLatLng == null) return;
//
//        googleMap.clear();
//
//        googleMap.addMarker(new MarkerOptions()
//                .position(passengerLatLng)
//                .title("Passenger"));
//
//        googleMap.addMarker(new MarkerOptions()
//                .position(stationLatLng)
//                .title("Meal Station"));
//
//        googleMap.moveCamera(
//                CameraUpdateFactory.newLatLngZoom(passengerLatLng, 14f)
//        );
//    }
//}







//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.os.Bundle;
//import android.view.*;
//import android.widget.*;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Base64;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import com.example.paktrainfoodapp.R;
//
//public class OrderDetailFragment extends Fragment {
//
//    private TextView txtName, txtPrice, txtDesc, txtPassengerUid;
//    private ImageView imgFood;
//
//    public static OrderDetailFragment newInstance(String name, double price,
//                                                  String desc, String image,
//                                                  String passengerUid) {
//        OrderDetailFragment fragment = new OrderDetailFragment();
//        Bundle args = new Bundle();
//        args.putString("itemName", name);
//        args.putDouble("itemPrice", price);
//        args.putString("itemDesc", desc);
//        args.putString("itemImage", image);
//        args.putString("passengerUid", passengerUid);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_order_detail, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        txtName = view.findViewById(R.id.txtName);
//        txtPrice = view.findViewById(R.id.txtPrice);
//        txtDesc = view.findViewById(R.id.txtDesc);
//        txtPassengerUid = view.findViewById(R.id.passengerUid);
//        imgFood = view.findViewById(R.id.imgFood);
//
//        if (getArguments() != null) {
//            String itemName = getArguments().getString("itemName");
//            double itemPrice = getArguments().getDouble("itemPrice");
//            String itemDesc = getArguments().getString("itemDesc");
//            String itemImage = getArguments().getString("itemImage");
//            String passengerUid = getArguments().getString("passengerUid");
//
//            txtName.setText(itemName);
//            txtPrice.setText("Rs. " + itemPrice);
//            txtDesc.setText(itemDesc);
//            txtPassengerUid.setText("Passenger UID: " + passengerUid);
//
//            if (itemImage != null && !itemImage.isEmpty()) {
//                try {
//                    byte[] dec = Base64.decode(itemImage, Base64.DEFAULT);
//                    Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
//                    imgFood.setImageBitmap(bmp);
//                } catch (Exception e) {
//                    imgFood.setImageResource(R.drawable.ic_food_placeholder);
//                }
//            } else {
//                imgFood.setImageResource(R.drawable.ic_food_placeholder);
//            }
//        }
//    }
//}
