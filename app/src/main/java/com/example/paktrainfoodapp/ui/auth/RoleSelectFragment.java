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
        cardPassenger.setOnClickListener(v -> openLoginWithRole("PASSENGER"));
        cardRestaurant.setOnClickListener(v -> openLoginWithRole("RESTAURANT"));
        cardDelivery.setOnClickListener(v -> openLoginWithRole("DELIVERY"));
    }

    private void openLoginWithRole(String role) {
        Bundle bundle = new Bundle();
        bundle.putString(AuthActivity.USER_ROLE_KEY, role);

        // Kyunki teeno cases mein LoginFragment hi khulna hai, switch ki zaroorat nahi
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setArguments(bundle);

        // Naye loadFragment method ke mutabiq 'true' pass kiya taake ye backstack mein jaye
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity()).loadFragment(loginFragment, true);
        }
    }
}




