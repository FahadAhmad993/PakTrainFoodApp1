package com.example.paktrainfoodapp.ui.main.Passenger;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.CartManager;
import com.example.paktrainfoodapp.R;

import java.util.ArrayList;

public class CartUIHelper {

    public static void setupCartBar(View view, Fragment fragment) {

        TextView tvCartTotal =
                view.findViewById(R.id.tvCartTotal);

        Button btnOrderNowCart =
                view.findViewById(R.id.btnOrderNowCart);

        updateTotal(tvCartTotal);

        CartManager.addListener(() -> {

            if (tvCartTotal != null) {

                tvCartTotal.post(() -> {

                    updateTotal(tvCartTotal);
                });
            }
        });

        btnOrderNowCart.setOnClickListener(v -> {

            ArrayList<CartItem> items =
                    new ArrayList<>(CartManager.getCartItems());

            if (items.isEmpty()) {
                Toast.makeText(fragment.getContext(), "Cart Empty", Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem first = items.get(0);

            OrderNowFragment dialog =
                    OrderNowFragment.newInstance(
                            first.getName(),
                            CartManager.getTotalPrice(),
                            "Multiple Items",
                            first.getRestaurantName(),
                            first.getRestaurantId(),
                            first.getImageUrl(),
                            first.getMealStation(),
                            first.getTrainId(),
                            first.getRouteId(),
                            first.getFromStation(),
                            first.getToStation(),
                            items
                    );

            dialog.show(fragment.getParentFragmentManager(), "OrderNow");
        });
    }

    private static void updateTotal(TextView tvCartTotal) {

        if (tvCartTotal == null) return;

        double total =
                CartManager.getTotalPrice();

        tvCartTotal.setText(
                "Total: Rs. " + (int) total
        );
    }
}











//


