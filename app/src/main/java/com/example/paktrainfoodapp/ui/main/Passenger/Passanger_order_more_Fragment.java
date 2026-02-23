package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class Passanger_order_more_Fragment extends DialogFragment {

    private ImageView imgFood;
    private TextView txtFoodName, txtPrice, txtDescription;
    private TextView btnMinus, btnPlus, txtQuantity, btnOrder;

    private String foodName, price, description, imageBase64;
    private int quantity = 1;
    private double unitPrice;

    public Passanger_order_more_Fragment() {}

    public static Passanger_order_more_Fragment newInstance(String name, String price,
                                                            String desc, String imageBase64,
                                                            String restaurantName, String restaurantId,
                                                            String city) {
        Passanger_order_more_Fragment fragment = new Passanger_order_more_Fragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("price", price);
        args.putString("desc", desc);
        args.putString("image", imageBase64);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_passanger_order_more_, container, false);

        // 🔹 Bind views
        imgFood = view.findViewById(R.id.imgFood);
        txtFoodName = view.findViewById(R.id.txtFoodName);
        txtPrice = view.findViewById(R.id.txtPrice);
        txtDescription = view.findViewById(R.id.txtDescription);
        btnMinus = view.findViewById(R.id.btnMinus);
        btnPlus = view.findViewById(R.id.btnPlus);
        txtQuantity = view.findViewById(R.id.txtQuantity);
        btnOrder = view.findViewById(R.id.btnOrder);

        if (getArguments() != null) {
            foodName = getArguments().getString("name");
            price = getArguments().getString("price");
            description = getArguments().getString("desc");
            imageBase64 = getArguments().getString("image");
        }

        txtFoodName.setText(foodName);
        txtDescription.setText(description);

        try {
            unitPrice = Double.parseDouble(price);
        } catch (Exception e) {
            unitPrice = 0;
        }

        txtPrice.setText("Rs. " + unitPrice);

        // 🔹 Load Base64 image
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgFood.setImageBitmap(bitmap);
        }

        // 🔹 Quantity buttons
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
                updatePrice();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            txtQuantity.setText(String.valueOf(quantity));
            updatePrice();
        });

        // 🔹 Order button
        btnOrder.setOnClickListener(v -> {
            // 1️⃣ Show toast
            Toast.makeText(getContext(),
                    quantity + " x " + foodName + " ordered for Rs. " + (unitPrice * quantity),
                    Toast.LENGTH_SHORT).show();

            // 2️⃣ Open Station_menu_Fragment
            Fragment stationMenuFragment = new Station_Menu_Fragment();

            // Agar tum Fragment inside Fragment use kar rahe ho (childFragmentManager)
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, stationMenuFragment) // fragment_container = tumhara FrameLayout ID
                    .addToBackStack(null) // back button kaam kare
                    .commit();

            // 3️⃣ Dismiss current dialog / bottom sheet
            dismiss();
        });

        return view;
    }

    private void updatePrice() {
        double total = unitPrice * quantity;
        txtPrice.setText("Rs. " + total);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.75); // 75% of screen
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setGravity(Gravity.BOTTOM); // bottom se appear hoga

            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;
        }
    }

}
