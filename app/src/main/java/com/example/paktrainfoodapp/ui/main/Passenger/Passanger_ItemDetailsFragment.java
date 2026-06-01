package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.CartManager;
import com.example.paktrainfoodapp.R;

import java.util.Map;

public class Passanger_ItemDetailsFragment extends Fragment {

    private MenuitemModel item;

    private String restaurantId, restaurantName, mealStation, trainId, routeId, fromStation, toStation, trainName;

    private TextView tvPrice, tvName, tvDesc, tvQuantity;
    private LinearLayout layoutSizeButtons, layoutReviews;
    private ImageView imgItem;
    private Button btnAddToCart;
    private TextView btnMinus, btnPlus;

    private int quantity = 1;
    private double basePrice = 0;
    private String selectedSize = "";

    public static Passanger_ItemDetailsFragment newInstance(
            MenuitemModel item, String restaurantId, String restaurantName,
            String mealStation, String trainId, String routeId,
            String from, String to, String trainName) {

        Passanger_ItemDetailsFragment fragment = new Passanger_ItemDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("item_data", item);
        args.putString("restaurantId", restaurantId);
        args.putString("restaurantName", restaurantName);
        args.putString("mealStation", mealStation);
        args.putString("trainId", trainId);
        args.putString("routeId", routeId);
        args.putString("from", from);
        args.putString("to", to);
        args.putString("trainName", trainName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            item = (MenuitemModel) getArguments().getSerializable("item_data");
            restaurantId = getArguments().getString("restaurantId");
            restaurantName = getArguments().getString("restaurantName");
            mealStation = getArguments().getString("mealStation");
            trainId = getArguments().getString("trainId");
            routeId = getArguments().getString("routeId");
            fromStation = getArguments().getString("from");
            toStation = getArguments().getString("to");
            trainName = getArguments().getString("trainName");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_passanger__item_details, container, false);

        imgItem = v.findViewById(R.id.imgItem);
        tvName = v.findViewById(R.id.tvName);
        tvDesc = v.findViewById(R.id.tvDesc);
        tvPrice = v.findViewById(R.id.tvPrice);
        layoutSizeButtons = v.findViewById(R.id.layoutSizeButtons);
        layoutReviews = v.findViewById(R.id.layoutReviews);
        btnAddToCart = v.findViewById(R.id.btnAddToCart);
        tvQuantity = v.findViewById(R.id.tvQuantity);
        btnMinus = v.findViewById(R.id.btnMinus);
        btnPlus = v.findViewById(R.id.btnPlus);

        if (item != null) {

            tvName.setText(item.getName());
            tvDesc.setText(item.getDescription());

            Glide.with(this)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_food_placeholder)
                    .into(imgItem);

            if (item.getVariations() != null && !item.getVariations().isEmpty()) {

                Map.Entry<String, Double> first =
                        item.getVariations().entrySet().iterator().next();

                basePrice = first.getValue();
                selectedSize = first.getKey();

                tvPrice.setText("Rs. " + (int) basePrice);

                boolean firstBtn = true;

                for (Map.Entry<String, Double> entry : item.getVariations().entrySet()) {

                    Button btn = (Button) inflater.inflate(
                            R.layout.passanger_item_size_button,
                            layoutSizeButtons,
                            false
                    );

                    btn.setText(entry.getKey());

                    if (firstBtn) {
                        btn.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.green)
                        );
                        btn.setTextColor(Color.WHITE);
                        firstBtn = false;
                    }

                    btn.setOnClickListener(view -> {

                        basePrice = entry.getValue();
                        selectedSize = entry.getKey();
                        quantity = 1;
                        tvQuantity.setText("1");

                        updatePrice();

                        for (int i = 0; i < layoutSizeButtons.getChildCount(); i++) {

                            Button b = (Button) layoutSizeButtons.getChildAt(i);

                            b.setBackgroundTintList(
                                    ContextCompat.getColorStateList(requireContext(), R.color.gray)
                            );

                            b.setTextColor(Color.BLACK);
                        }

                        btn.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.green)
                        );

                        btn.setTextColor(Color.WHITE);
                    });

                    layoutSizeButtons.addView(btn);
                }
            }
        }

        btnPlus.setOnClickListener(v1 -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updatePrice();
        });

        btnMinus.setOnClickListener(v1 -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updatePrice();
            }
        });

        btnAddToCart.setOnClickListener(view -> {

            if (item == null) return;

            CartItem cartItem = new CartItem(
                    item.getId(),
                    restaurantId,
                    restaurantName,
                    item.getName(),
                    basePrice,
                    quantity,
                    selectedSize,
                    item.getImageUrl(),
                    item.getDescription(),
                    mealStation,
                    trainId,
                    routeId,
                    fromStation,
                    toStation,
                    trainName
            );

            CartManager.addOrUpdate(cartItem);

            Toast.makeText(getContext(),
                    "Added to Cart",
                    Toast.LENGTH_SHORT).show();

            getParentFragmentManager().popBackStack();
        });

        return v;
    }

    private void updatePrice() {

        double finalPrice = basePrice * quantity;

        tvPrice.setText("Rs. " + (int) finalPrice);
    }
}