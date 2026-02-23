package com.example.paktrainfoodapp.ui.main.Passenger;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.example.paktrainfoodapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class OrderNowFragment extends DialogFragment {

    private static final String ARG_NAME = "itemName";
    private static final String ARG_PRICE = "itemPrice";
    private static final String ARG_DESC = "itemDesc";
    private static final String ARG_REST = "itemRest";
    private static final String ARG_IMAGE = "itemImage";
    private static final String ARG_REST_UID = "restaurantUid";

    private static final String ARG_MEAL_STATION = "MealStation";


    private ImageView imgFood;
    private TextView txtName, txtPrice, txtDesc, txtRest;
    private EditText edtTicket, edtCoach, edtSeat, edtTrain, edtPhone;
    private Button btnOrderNow, btnCancel;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler locationHandler = new Handler(Looper.getMainLooper());
    private Runnable locationRunnable;

    private String passengerUid;
    private String orderId;
    private String MealStation = "";

    //    public static OrderNowFragment newInstance(String name, double price,
//                                               String desc, String rest,
//                                               String restUid, String image) {
//        OrderNowFragment f = new OrderNowFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_NAME, name);
//        args.putDouble(ARG_PRICE, price);
//        args.putString(ARG_DESC, desc);
//        args.putString(ARG_REST, rest);
//        args.putString(ARG_REST_UID, restUid);
//        args.putString(ARG_IMAGE, image);
//        f.setArguments(args);
//        return f;
//    }
public static OrderNowFragment newInstance(String name, double price,
                                           String desc, String rest,
                                           String restUid, String image,
                                           String MealStation) {

    OrderNowFragment f = new OrderNowFragment();
    Bundle args = new Bundle();
    args.putString(ARG_NAME, name);
    args.putDouble(ARG_PRICE, price);
    args.putString(ARG_DESC, desc);
    args.putString(ARG_REST, rest);
    args.putString(ARG_REST_UID, restUid);
    args.putString(ARG_IMAGE, image);
    args.putString(ARG_MEAL_STATION, MealStation);
    f.setArguments(args);
    return f;
}


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_order_now_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        imgFood = view.findViewById(R.id.imgFood);
        txtName = view.findViewById(R.id.txtName);
        txtPrice = view.findViewById(R.id.txtPrice);
        txtDesc = view.findViewById(R.id.txtDesc);
        txtRest = view.findViewById(R.id.txtRest);
        edtTicket = view.findViewById(R.id.edtTicket);
        edtCoach = view.findViewById(R.id.edtCoach);
        edtSeat = view.findViewById(R.id.edtSeat);
        edtTrain = view.findViewById(R.id.edtTrain);
        edtPhone = view.findViewById(R.id.edtPhone);

        btnOrderNow = view.findViewById(R.id.btnOrderNow);
        btnCancel = view.findViewById(R.id.btnCancel);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        passengerUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "Guest_" + System.currentTimeMillis();

        Bundle args = getArguments();
        if (args == null) return;

        String name = args.getString(ARG_NAME);
        double price = args.getDouble(ARG_PRICE);
        String desc = args.getString(ARG_DESC);
        String rest = args.getString(ARG_REST);
        String restUid = args.getString(ARG_REST_UID);
        String imageBase64 = args.getString(ARG_IMAGE);
        this.MealStation = args.getString(ARG_MEAL_STATION, "");



        txtName.setText(name);
        txtPrice.setText("Rs. " + price);
        txtDesc.setText(desc);
        txtRest.setText("By " + rest);

        setImageFromBase64(imgFood, imageBase64);

        btnOrderNow.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                showEnableLocationDialog();
            } else {
                placeOrder(name, price, desc, rest, restUid, imageBase64);
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setImageFromBase64(ImageView imageView, String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_food_placeholder);
            return;
        }
        try {
            byte[] decoded = Base64.decode(base64String, Base64.DEFAULT);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Location Required")
                .setMessage("Please turn on your location to place the order.")
                .setCancelable(false)
                .setPositiveButton("Turn On", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void placeOrder(String name, double price, String desc,
                            String rest, String restUid, String image) {

        if (edtTicket.getText().toString().trim().isEmpty() ||
                edtCoach.getText().toString().trim().isEmpty() ||
                edtSeat.getText().toString().trim().isEmpty() ||
                edtTrain.getText().toString().trim().isEmpty() ||
                edtPhone.getText().toString().trim().isEmpty()) {

            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        orderId = firestore.collection("Orders").document().getId();

        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("ticketNumber", edtTicket.getText().toString());
        orderData.put("coachNumber", edtCoach.getText().toString());
        orderData.put("seatNumber", edtSeat.getText().toString());
        orderData.put("trainName", edtTrain.getText().toString());
        orderData.put("phone", edtPhone.getText().toString());
        orderData.put("itemName", name);
        orderData.put("itemPrice", price);
        orderData.put("itemDesc", desc);
        orderData.put("restaurantName", rest);
        orderData.put("restaurantUid", restUid);
        orderData.put("passengerUid", passengerUid);
        orderData.put("itemImage", image);
        orderData.put("orderStatus", "Active");
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("mealStation", MealStation); // <-- ADD THIS

        // Save order details only in Firestore
        firestore.collection("Users")
                .document("Passenger")
                .collection("OrderNow")
                .document(passengerUid)
                .collection("Orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    startAutoLocationUpdates();  // Start location updates in Realtime DB only
                    dismiss();
                })
        .addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void startAutoLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001
            );
            return;
        }

        locationRunnable = new Runnable() {
            @Override
            public void run() {
                // Permission check inside Runnable
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    // Request permissions
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            1001
                    );
                    return;
                }

                fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_HIGH_ACCURACY,
                        null
                ).addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        HashMap<String, Object> locData = new HashMap<>();
                        locData.put("lat", lat);
                        locData.put("lng", lng);
                        locData.put("timestamp", System.currentTimeMillis());

                        // Save location only in Realtime Database
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                        db.child("Orders").child(orderId).child("passengerLocation").setValue(locData);
                    }
                });

                locationHandler.postDelayed(this, 300000); // Update every 5 minutes
            }
        };

        locationHandler.post(locationRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationRunnable != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.90);
            int height = (int) (dm.heightPixels * 0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}






//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.Manifest;
//import android.app.Dialog;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Base64;
//import android.util.DisplayMetrics;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.DialogFragment;
//
//import com.example.paktrainfoodapp.R;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//
//public class OrderNowFragment extends DialogFragment {
//
//    private static final String ARG_NAME = "itemName";
//    private static final String ARG_PRICE = "itemPrice";
//    private static final String ARG_DESC = "itemDesc";
//    private static final String ARG_REST = "itemRest";
//    private static final String ARG_IMAGE = "itemImage";
//    private static final String ARG_REST_UID = "restaurantUid";
//
//    private ImageView imgFood;
//    private TextView txtName, txtPrice, txtDesc, txtRest;
//    private EditText edtTicket, edtCoach, edtSeat, edtTrain, edtPhone;
//    private Button btnOrderNow, btnCancel;
//
//    private FusedLocationProviderClient fusedLocationClient;
//    private Handler locationHandler = new Handler(Looper.getMainLooper());
//    private Runnable locationRunnable;
//
//    private String passengerUid;
//    private String orderId;
//
//    public static OrderNowFragment newInstance(String name, double price,
//                                               String desc, String rest,
//                                               String restUid, String image) {
//
//        OrderNowFragment f = new OrderNowFragment();
//        Bundle args = new Bundle();
//
//        args.putString(ARG_NAME, name);
//        args.putDouble(ARG_PRICE, price);
//        args.putString(ARG_DESC, desc);
//        args.putString(ARG_REST, rest);
//        args.putString(ARG_REST_UID, restUid);
//        args.putString(ARG_IMAGE, image);
//
//        f.setArguments(args);
//        return f;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_passanger_order_now_form, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view,
//                              @Nullable Bundle savedInstanceState) {
//
//        imgFood = view.findViewById(R.id.imgFood);
//        txtName = view.findViewById(R.id.txtName);
//        txtPrice = view.findViewById(R.id.txtPrice);
//        txtDesc = view.findViewById(R.id.txtDesc);
//        txtRest = view.findViewById(R.id.txtRest);
//        edtTicket = view.findViewById(R.id.edtTicket);
//        edtCoach = view.findViewById(R.id.edtCoach);
//        edtSeat = view.findViewById(R.id.edtSeat);
//        edtTrain = view.findViewById(R.id.edtTrain);
//        edtPhone = view.findViewById(R.id.edtPhone);
//
//        btnOrderNow = view.findViewById(R.id.btnOrderNow);
//        btnCancel = view.findViewById(R.id.btnCancel);
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
//
//        // Passenger UID
//        passengerUid = FirebaseAuth.getInstance().getCurrentUser() != null
//                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
//                : "Guest_" + System.currentTimeMillis();
//
//        // Get arguments
//        Bundle args = getArguments();
//        if (args == null) return;
//
//        String name = args.getString(ARG_NAME);
//        double price = args.getDouble(ARG_PRICE);
//        String desc = args.getString(ARG_DESC);
//        String rest = args.getString(ARG_REST);
//        String restUid = args.getString(ARG_REST_UID);
//        String imageBase64 = args.getString(ARG_IMAGE);
//
//        txtName.setText(name);
//        txtPrice.setText("Rs. " + price);
//        txtDesc.setText(desc);
//        txtRest.setText("By " + rest);
//
//        setImageFromBase64(imgFood, imageBase64);
//
//        btnOrderNow.setOnClickListener(v ->
//                placeOrder(name, price, desc, rest, restUid, imageBase64)
//        );
//
//        btnCancel.setOnClickListener(v -> dismiss());
//    }
//
//    private void setImageFromBase64(ImageView imageView, String base64String) {
//        if (base64String == null || base64String.isEmpty()) {
//            imageView.setImageResource(R.drawable.ic_food_placeholder);
//            return;
//        }
//        try {
//            byte[] decoded = Base64.decode(base64String, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
//            imageView.setImageBitmap(bitmap != null ? bitmap : null);
//        } catch (Exception e) {
//            imageView.setImageResource(R.drawable.ic_food_placeholder);
//        }
//    }
//
//    private void placeOrder(String name, double price, String desc,
//                            String rest, String restUid, String image) {
//
//        // Required fields check
//        if (edtTicket.getText().toString().trim().isEmpty() ||
//                edtCoach.getText().toString().trim().isEmpty() ||
//                edtSeat.getText().toString().trim().isEmpty() ||
//                edtTrain.getText().toString().trim().isEmpty() ||
//                edtPhone.getText().toString().trim().isEmpty()) {
//
//            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Create Order ID
//        orderId = db.collection("Orders").document().getId();
//
//        HashMap<String, Object> orderData = new HashMap<>();
//        orderData.put("orderId", orderId);
//        orderData.put("ticketNumber", edtTicket.getText().toString());
//        orderData.put("coachNumber", edtCoach.getText().toString());
//        orderData.put("seatNumber", edtSeat.getText().toString());
//        orderData.put("trainName", edtTrain.getText().toString());
//        orderData.put("phone", edtPhone.getText().toString());
//        orderData.put("itemName", name);
//        orderData.put("itemPrice", price);
//        orderData.put("itemDesc", desc);
//        orderData.put("restaurantName", rest);
//        orderData.put("restaurantUid", restUid);
//        orderData.put("passengerUid", passengerUid);
//        orderData.put("itemImage", image);
//        orderData.put("orderStatus", "Active");
//        orderData.put("timestamp", System.currentTimeMillis());
//
//        saveOrderData(orderData);
//        startAutoLocationUpdates();
//    }
//
//    private void saveOrderData(HashMap<String, Object> data) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("Users")
//                .document("Passenger")
//                .collection("OrderNow")
//                .document(passengerUid)
//                .collection("Orders")
//                .document(orderId)
//                .set(data);
//
//        db.collection("Orders")
//                .document(orderId)
//                .set(data)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
//                    dismiss();
//                });
//    }
//
//    // 🔵 Auto Location Update every 5 seconds
//    private void startAutoLocationUpdates() {
//
//        if (ActivityCompat.checkSelfPermission(requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        locationRunnable = new Runnable() {
//            @Override
//            public void run() {
//
//                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                    // Request permission if not granted
//                    requestPermissions(
//                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                            1001
//                    );
//
//                    return;
//                }
//
//                fusedLocationClient.getLastLocation()
//                        .addOnSuccessListener(location -> {
//                            if (location == null) return;
//
//                            double lat = location.getLatitude();
//                            double lng = location.getLongitude();
//
//                            HashMap<String, Object> locData = new HashMap<>();
//                            locData.put("passengerLat", lat);
//                            locData.put("passengerLng", lng);
//                            locData.put("timestamp", System.currentTimeMillis());
//
//                            FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//                            db.collection("Orders")
//                                    .document(orderId)
//                                    .update(locData);
//
//                            db.collection("Users")
//                                    .document("Passenger")
//                                    .collection("OrderNow")
//                                    .document(passengerUid)
//                                    .collection("Orders")
//                                    .document(orderId)
//                                    .update(locData);
//                        });
//
//                locationHandler.postDelayed(this, 500000000);
//            }
//        };
//
//        locationHandler.post(locationRunnable);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (locationRunnable != null) {
//            locationHandler.removeCallbacks(locationRunnable);
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        Dialog dialog = getDialog();
//        if (dialog != null && dialog.getWindow() != null) {
//
//            DisplayMetrics dm = new DisplayMetrics();
//            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//            int width = (int) (dm.widthPixels * 0.90);
//            int height = (int) (dm.heightPixels * 0.90);
//
//            dialog.getWindow().setLayout(width, height);
//            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        }
//    }
//}
//
//
//
