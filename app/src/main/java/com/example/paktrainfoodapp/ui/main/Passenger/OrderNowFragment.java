package com.example.paktrainfoodapp.ui.main.Passenger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class OrderNowFragment extends DialogFragment {

    private static final String ARG_NAME = "itemName";
    private static final String ARG_PRICE = "itemPrice";
    private static final String ARG_DESC = "itemDesc";
    private static final String ARG_REST = "itemRest";
    private static final String ARG_REST_UID = "restaurantUid";
    private static final String ARG_IMAGE = "itemImage";
    private static final String ARG_MEAL_STATION = "mealStation";

    private static final String ARG_TRAIN_ID = "trainId";
    private static final String ARG_ROUTE_ID = "routeId";
    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    private ImageView imgFood;
    private TextView txtName, txtPrice, txtDesc, txtRest;
    private EditText edtTicket, edtCoach, edtSeat, edtTrain, edtPhone;
    private Button btnOrderNow, btnCancel;

    private String passengerUid;
    private String orderId;
    private String mealStation = "";

    public static OrderNowFragment newInstance(
            String name,
            double price,
            String desc,
            String rest,
            String restUid,
            String image,
            String mealStation,
            String trainId,
            String routeId,
            String from,
            String to
    ) {

        OrderNowFragment f = new OrderNowFragment();
        Bundle args = new Bundle();

        args.putString(ARG_NAME, name);
        args.putDouble(ARG_PRICE, price);
        args.putString(ARG_DESC, desc);
        args.putString(ARG_REST, rest);
        args.putString(ARG_REST_UID, restUid);
        args.putString(ARG_IMAGE, image);
        args.putString(ARG_MEAL_STATION, mealStation);

        args.putString(ARG_TRAIN_ID, trainId);
        args.putString(ARG_ROUTE_ID, routeId);
        args.putString(ARG_FROM, from);
        args.putString(ARG_TO, to);

        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_order_now_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        passengerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Bundle args = getArguments();
        if (args == null) return;

        String name = args.getString(ARG_NAME);
        double price = args.getDouble(ARG_PRICE);
        String desc = args.getString(ARG_DESC);
        String rest = args.getString(ARG_REST);
        String restUid = args.getString(ARG_REST_UID);
        String image = args.getString(ARG_IMAGE);
        mealStation = args.getString(ARG_MEAL_STATION, "");

        String trainId = args.getString(ARG_TRAIN_ID, "");
        String routeId = args.getString(ARG_ROUTE_ID, "");
        String from = args.getString(ARG_FROM, "");
        String to = args.getString(ARG_TO, "");

        txtName.setText(name);
        txtPrice.setText("Rs. " + price);
        txtDesc.setText(desc);
        txtRest.setText(rest);

        setImage(imgFood, image);

        btnCancel.setOnClickListener(v -> dismiss());

        btnOrderNow.setOnClickListener(v -> {

            if (!isLocationEnabled()) {
                showLocationDialog();
                return;
            }

            placeOrder(name, price, desc, rest, restUid, image,
                    trainId, routeId, from, to);
        });
    }

    private void placeOrder(String name, double price, String desc,
                            String rest, String restUid, String image,
                            String trainId, String routeId,
                            String from, String to) {

        if (edtTicket.getText().toString().isEmpty() ||
                edtCoach.getText().toString().isEmpty() ||
                edtSeat.getText().toString().isEmpty() ||
                edtTrain.getText().toString().isEmpty() ||
                edtPhone.getText().toString().isEmpty()) {

            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        orderId = db.collection("Orders").document().getId();

        HashMap<String, Object> order = new HashMap<>();

        order.put("orderId", orderId);
        order.put("itemName", name);
        order.put("itemPrice", price);
        order.put("itemDesc", desc);
        order.put("itemImage", image);

        order.put("restaurantName", rest);
        order.put("restaurantUid", restUid);

        order.put("passengerUid", passengerUid);

        order.put("trainName", edtTrain.getText().toString());
        order.put("trainId", trainId);
        order.put("routeId", routeId);
        order.put("fromStation", from);
        order.put("toStation", to);

        order.put("mealStation", mealStation);

        order.put("ticketNumber", edtTicket.getText().toString());
        order.put("coachNumber", edtCoach.getText().toString());
        order.put("seatNumber", edtSeat.getText().toString());
        order.put("phone", edtPhone.getText().toString());

        order.put("orderStatus", "Active");
        order.put("timestamp", System.currentTimeMillis());

        // ================= MAIN SAVE =================
        db.collection("Orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {

                    // ================= SECOND SAVE (NEW PATH) =================
                    db.collection("Users")
                            .document("Passenger")
                            .collection("OrderNow")
                            .document(passengerUid)
                            .collection("Orders")
                            .document(orderId)
                            .set(order)
                            .addOnSuccessListener(aVoid2 -> {

                                startLocationService();

                                Toast.makeText(requireContext(),
                                        "Order placed successfully!",
                                        Toast.LENGTH_SHORT).show();

                                dismiss();
                            });
                });
    }

    private void setImage(ImageView img, String base64) {
        try {
            if (base64 != null && !base64.isEmpty()) {
                byte[] b = Base64.decode(base64, Base64.DEFAULT);
                img.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
            }
        } catch (Exception e) {
            img.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) requireActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        return lm != null &&
                (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void showLocationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Location")
                .setMessage("GPS required for tracking")
                .setPositiveButton("Open",
                        (d, w) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startLocationService() {
        Intent i = new Intent(requireContext(), LocationService.class);
        i.putExtra("orderId", orderId);
        i.putExtra("passengerUid", passengerUid);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(i);
        } else {
            requireContext().startService(i);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {

            DisplayMetrics dm = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            d.getWindow().setLayout(
                    (int) (dm.widthPixels * 0.9),
                    (int) (dm.heightPixels * 0.9)
            );
        }
    }
}



