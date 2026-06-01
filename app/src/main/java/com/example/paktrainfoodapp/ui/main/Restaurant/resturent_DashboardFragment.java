package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paktrainfoodapp.R;

public class resturent_DashboardFragment extends Fragment {

    private LinearLayout btnProfile, btnMenu, btnDelivery;

    public resturent_DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resturent__dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnProfile = view.findViewById(R.id.btnProfile);
        btnMenu = view.findViewById(R.id.btnMenu);
        btnDelivery = view.findViewById(R.id.btnDelivery);

        // Click listeners calling the parent's navigation method
        btnMenu.setOnClickListener(v -> navigateTo("menu"));
        btnDelivery.setOnClickListener(v -> navigateTo("delivery"));
        btnProfile.setOnClickListener(v -> navigateTo("profile"));
    }

    private void navigateTo(String target) {
        if (getParentFragment() instanceof restaurant_LoadFragment) {
            ((restaurant_LoadFragment) getParentFragment()).navigateFromDashboard(target);
        }
    }
}



