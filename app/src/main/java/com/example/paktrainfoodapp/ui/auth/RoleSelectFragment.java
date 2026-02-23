package com.example.paktrainfoodapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class RoleSelectFragment extends Fragment {

    private CardView cardPassenger, cardRestaurant, cardDelivery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_role_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardPassenger = view.findViewById(R.id.card_passenger);
        cardRestaurant = view.findViewById(R.id.card_restaurant);
        cardDelivery = view.findViewById(R.id.card_delivery);

        // Click Listeners
        cardPassenger.setOnClickListener(v -> openRegister("PASSENGER"));
        cardRestaurant.setOnClickListener(v -> openRegister("RESTAURANT"));
        cardDelivery.setOnClickListener(v -> openRegister("DELIVERY"));
    }

    private void openRegister(String role) {
        Bundle bundle = new Bundle();
        bundle.putString(AuthActivity.USER_ROLE_KEY, role);

        Fragment fragment;

        switch (role) {
            case "PASSENGER":
                fragment = new LoginFragment();
                break;
            case "RESTAURANT":
                fragment = new LoginFragment();
                break;
            case "DELIVERY":
                fragment = new LoginFragment();
                break;
            default:
                fragment = new LoginFragment();
        }

        fragment.setArguments(bundle);
        ((AuthActivity) requireActivity()).loadFragment(fragment);
    }
}







