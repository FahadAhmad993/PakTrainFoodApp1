package com.example.paktrainfoodapp.ui.main.Passenger;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.CartManager;
import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderNowFragment extends DialogFragment {

    private ImageView imgFood;
    private TextView txtName, txtPrice, txtDesc, txtRest;
    private EditText edtTicket, edtCoach, edtSeat, edtTrain, edtPhone;
    private Button btnOrderNow, btnCancel;

    private String passengerUid;
    private String mealStation;
    private PaymentSheet paymentSheet;
    private String clientSecret;
    private double totalAmount = 0;

    // ARG keys
    private static final String ARG_NAME = "itemName";
    private static final String ARG_PRICE = "itemPrice";
    private static final String ARG_DESC = "itemDesc";
    private static final String ARG_REST = "itemRest";
    private static final String ARG_IMAGE = "itemImage";
    private static final String ARG_MEAL_STATION = "mealStation";
    private static final String ARG_CART_ITEMS = "cartItems";

    public static OrderNowFragment newInstance(String name, double price, String desc, String rest, String restUid, String image, String mealStation, String trainId, String routeId, String from, String to, ArrayList<CartItem> cartItems) {
        OrderNowFragment fragment = new OrderNowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putDouble(ARG_PRICE, price);
        args.putString(ARG_DESC, desc);
        args.putString(ARG_REST, rest);
        args.putString(ARG_IMAGE, image);
        args.putString(ARG_MEAL_STATION, mealStation);
        args.putSerializable(ARG_CART_ITEMS, cartItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_order_now_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgFood = view.findViewById(R.id.imgFood);
        txtName = view.findViewById(R.id.txtName);
        txtPrice = view.findViewById(R.id.txtPrice);
        txtDesc = view.findViewById(R.id.txtDesc);
        txtRest = view.findViewById(R.id.txtRest);
        btnOrderNow = view.findViewById(R.id.btnOrderNow);
        btnCancel = view.findViewById(R.id.btnCancel);

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Login Required", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        passengerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Bundle args = getArguments();
        if (args != null) {
            txtName.setText(args.getString(ARG_NAME, ""));
            txtPrice.setText("Rs. " + args.getDouble(ARG_PRICE, 0));
            txtDesc.setText(args.getString(ARG_DESC, ""));
            txtRest.setText(args.getString(ARG_REST, ""));
            String image = args.getString(ARG_IMAGE, "");
            if (!TextUtils.isEmpty(image)) {
                Glide.with(this).load(image).placeholder(R.drawable.ic_food_placeholder).into(imgFood);
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());
        btnOrderNow.setOnClickListener(v -> {
            if (!hasLocationPermission() || !isLocationEnabled()) {
                Toast.makeText(getContext(), "Permission/Location Error", Toast.LENGTH_SHORT).show();
                return;
            }
            startPaymentFlow();
        });
    }

    private void startPaymentFlow() {
        totalAmount = CartManager.getTotalPrice();

        HashMap<String, Object> data = new HashMap<>();
        data.put("amount", totalAmount);
// startPaymentFlow() ke bilkul shuru mein
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User NULL hai - Please Login!", Toast.LENGTH_SHORT).show();
            return;
        }

// Token verify karne ke liye (Test karne ke liye ye add karein)
        auth.getCurrentUser().getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("AUTH_DEBUG", "Token: " + task.getResult().getToken());
            } else {
                Log.e("AUTH_DEBUG", "Token error: " + task.getException().getMessage());
            }
        });
        FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("createPaymentIntent")
                .call(data)
                .addOnSuccessListener(result -> {
                    Map<String, Object> response = (Map<String, Object>) result.getData();
                    clientSecret = (String) response.get("clientSecret"); // Ensure backend returns this

                    if (clientSecret != null) {
                        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Train Food App"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PAYMENT", "Error", e);
                    Toast.makeText(getContext(), "Payment Setup Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void onPaymentResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            saveOrderToFirestore();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Toast.makeText(getContext(), "Payment Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderToFirestore() {
        // Yahan aap apna Order Firestore mein save karein
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("uid", passengerUid);
        orderData.put("amount", totalAmount);
        orderData.put("status", "pending");

        db.collection("Orders").add(orderData)
                .addOnSuccessListener(documentReference -> {
                    CartManager.clear();
                    Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_LONG).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save order", Toast.LENGTH_SHORT).show());
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}











//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.LocationManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.DialogFragment;
//
//import com.bumptech.glide.Glide;
//import com.example.paktrainfoodapp.CartManager;
//import com.example.paktrainfoodapp.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.functions.FirebaseFunctions;
//import com.stripe.android.paymentsheet.PaymentSheet;
//import com.stripe.android.paymentsheet.PaymentSheetResult;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//
//public class OrderNowFragment extends DialogFragment {
//
//    private ImageView imgFood;
//    private TextView txtName, txtPrice, txtDesc, txtRest;
//
//    private EditText edtTicket, edtCoach, edtSeat, edtTrain, edtPhone;
//    private Button btnOrderNow, btnCancel;
//
//    private String passengerUid;
//    private String orderId;
//    private String mealStation;
//
//    private PaymentSheet paymentSheet;
//    private String clientSecret;
//
//    private double totalAmount = 0;
//
//    // ARG keys
//    private static final String ARG_NAME = "itemName";
//    private static final String ARG_PRICE = "itemPrice";
//    private static final String ARG_DESC = "itemDesc";
//    private static final String ARG_REST = "itemRest";
//    private static final String ARG_REST_UID = "restaurantUid";
//    private static final String ARG_IMAGE = "itemImage";
//    private static final String ARG_MEAL_STATION = "mealStation";
//    private static final String ARG_TRAIN_ID = "trainId";
//    private static final String ARG_ROUTE_ID = "routeId";
//    private static final String ARG_FROM = "from";
//    private static final String ARG_TO = "to";
//    private static final String ARG_CART_ITEMS = "cartItems";
//
//    public static OrderNowFragment newInstance(
//            String name,
//            double price,
//            String desc,
//            String rest,
//            String restUid,
//            String image,
//            String mealStation,
//            String trainId,
//            String routeId,
//            String from,
//            String to,
//            ArrayList<CartItem> cartItems
//    ) {
//
//        OrderNowFragment fragment = new OrderNowFragment();
//        Bundle args = new Bundle();
//
//        args.putString(ARG_NAME, name);
//        args.putDouble(ARG_PRICE, price);
//        args.putString(ARG_DESC, desc);
//        args.putString(ARG_REST, rest);
//        args.putString(ARG_REST_UID, restUid);
//        args.putString(ARG_IMAGE, image);
//        args.putString(ARG_MEAL_STATION, mealStation);
//        args.putString(ARG_TRAIN_ID, trainId);
//        args.putString(ARG_ROUTE_ID, routeId);
//        args.putString(ARG_FROM, from);
//        args.putString(ARG_TO, to);
//
//        args.putSerializable(ARG_CART_ITEMS, cartItems);
//
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_passanger_order_now_form, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        imgFood = view.findViewById(R.id.imgFood);
//        txtName = view.findViewById(R.id.txtName);
//        txtPrice = view.findViewById(R.id.txtPrice);
//        txtDesc = view.findViewById(R.id.txtDesc);
//        txtRest = view.findViewById(R.id.txtRest);
//
//        edtTicket = view.findViewById(R.id.edtTicket);
//        edtCoach = view.findViewById(R.id.edtCoach);
//        edtSeat = view.findViewById(R.id.edtSeat);
//        edtTrain = view.findViewById(R.id.edtTrain);
//        edtPhone = view.findViewById(R.id.edtPhone);
//
//        btnOrderNow = view.findViewById(R.id.btnOrderNow);
//        btnCancel = view.findViewById(R.id.btnCancel);
//
//        paymentSheet = new PaymentSheet(
//                this,
//                this::onPaymentResult
////                result -> {
////
////                    if (result instanceof PaymentSheetResult.Completed) {
////                        onPaymentSuccess();
////                    }
//       //         }
//        );
//
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            Toast.makeText(getContext(), "Login Required", Toast.LENGTH_SHORT).show();
//            dismiss();
//            return;
//        }
//
//        passengerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        Bundle args = getArguments();
//        if (args == null) return;
//
//        ArrayList<CartItem> cartItems =
//                (ArrayList<CartItem>) args.getSerializable(ARG_CART_ITEMS);
//
//        String name = args.getString(ARG_NAME, "");
//        double price = args.getDouble(ARG_PRICE, 0);
//        String desc = args.getString(ARG_DESC, "");
//        String rest = args.getString(ARG_REST, "");
//        String image = args.getString(ARG_IMAGE, "");
//
//        mealStation = args.getString(ARG_MEAL_STATION, "");
//
//        txtName.setText(name);
//        txtPrice.setText("Rs. " + price);
//        txtDesc.setText(desc);
//        txtRest.setText(rest);
//
//        if (!TextUtils.isEmpty(image)) {
//            Glide.with(this)
//                    .load(image)
//                    .placeholder(R.drawable.ic_food_placeholder)
//                    .into(imgFood);
//        }
//
//        btnCancel.setOnClickListener(v -> dismiss());
//
//        btnOrderNow.setOnClickListener(v -> {
//
//            Toast.makeText(getContext(), "CLICKED", Toast.LENGTH_SHORT).show();
//
//            if (!hasLocationPermission()) {
//                Toast.makeText(getContext(), "NO PERMISSION", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (!isLocationEnabled()) {
//                Toast.makeText(getContext(), "LOCATION OFF", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Toast.makeText(getContext(), "CALLING PAYMENT FLOW", Toast.LENGTH_SHORT).show();
//
//            startPaymentFlow();
//        });
//    }
//
//    // ================= PAYMENT FLOW =================
//
//    private void startPaymentFlow() {
//
//        totalAmount = CartManager.getTotalPrice();
//
//        Toast.makeText(getContext(),
//                "Starting Payment...",
//                Toast.LENGTH_SHORT).show();
//
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//
//            Toast.makeText(getContext(),
//                    "User NULL",
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        Toast.makeText(getContext(),
//                "UID: " + uid,
//                Toast.LENGTH_SHORT).show();
//
//        // 🔥 YAHAN SE DIRECT FUNCTION CALL HOGA
//        FirebaseAuth.getInstance().getCurrentUser()
//                .getIdToken(true)
//                .addOnSuccessListener(tokenResult -> {
//
//                    HashMap<String, Object> data = new HashMap<>();
//                    data.put("amount", totalAmount);
//
//                    FirebaseFunctions.getInstance("us-central1")
//                            .getHttpsCallable("createPaymentIntent")
//                            .call(data)
//                            .addOnSuccessListener(result -> {
//
//                                Toast.makeText(getContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
//
//                            })
//                            .addOnFailureListener(e -> {
//                                Log.e("FUNC", "ERROR", e);
//                            });
//
//                });
//    }
//
//    private void openPaymentSheet() {
//
//        paymentSheet.presentWithPaymentIntent(
//                clientSecret,
//                new PaymentSheet.Configuration("Train Food App")
//        );
//    }
//
//    private void onPaymentResult(PaymentSheetResult result) {
//
//        if (result instanceof PaymentSheetResult.Completed) {
//
//            saveOrderToFirestore();
//
//        } else {
//            Toast.makeText(getContext(),
//                    "Payment Failed / Cancelled",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // ================= ORDER SAVE =================
//
//    private void saveOrderToFirestore() {
//
//        FirebaseFunctions.getInstance("us-central1")
//                .getHttpsCallable("createPaymentIntent")
//                .call(Collections.singletonMap("amount", totalAmount))
//                .addOnSuccessListener(result -> {
//
//                    Toast.makeText(getContext(),
//                            "SUCCESS",
//                            Toast.LENGTH_LONG).show();
//
//                })
//                .addOnFailureListener(e -> {
//
//                    android.util.Log.e("PAYMENT_ERROR",
//                            "FULL ERROR",
//                            e);
//
//                    Toast.makeText(
//                            getContext(),
//                            e.toString(),
//                            Toast.LENGTH_LONG
//                    ).show();
//
//                });
//    }
//
//    private void startLocationService() {
//
//        Intent intent = new Intent(requireContext(), LocationService.class);
//        intent.putExtra("orderId", orderId);
//        intent.putExtra("passengerUid", passengerUid);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            requireContext().startForegroundService(intent);
//        } else {
//            requireContext().startService(intent);
//        }
//    }
//
//    // ================= LOCATION =================
//
//    private boolean hasLocationPermission() {
//        return ContextCompat.checkSelfPermission(requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private boolean isLocationEnabled() {
//        LocationManager lm = (LocationManager) requireActivity()
//                .getSystemService(Context.LOCATION_SERVICE);
//
//        return lm != null &&
//                (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                        || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
//    }
//
//    private void showLocationDialog() {
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Enable Location")
//                .setMessage("Required for delivery tracking")
//                .setPositiveButton("Open Settings",
//                        (d, w) -> startActivity(
//                                new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        Dialog dialog = getDialog();
//        if (dialog != null && dialog.getWindow() != null) {
//
//            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
//
//            dialog.getWindow().setLayout(
//                    width,
//                    WindowManager.LayoutParams.WRAP_CONTENT
//            );
//        }
//    }
//    private void onPaymentSuccess() {
//
//        // STEP 1: Save Order
//        saveOrderToFirestore();
//
//        // STEP 2: Clear cart
//        CartManager.clear();
//
//        Toast.makeText(getContext(),
//                "Payment Successful",
//                Toast.LENGTH_SHORT).show();
//    }
//}