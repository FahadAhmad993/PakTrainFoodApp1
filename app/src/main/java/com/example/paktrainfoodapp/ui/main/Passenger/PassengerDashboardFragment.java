package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class PassengerDashboardFragment extends Fragment {

    private LinearLayout btnMenu, btnOrder, btnDashboard, btnCart, btnProfile;

    public PassengerDashboardFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnMenu = view.findViewById(R.id.btn_menu);
        btnOrder = view.findViewById(R.id.btn_order);
        btnDashboard = view.findViewById(R.id.btn_dashboard);
        btnCart = view.findViewById(R.id.btn_cart);
        btnProfile = view.findViewById(R.id.btn_profile);

        // Default fragment: Dashboard
        loadFragment(new HomeFragment(), "DASHBOARD_FRAGMENT");
        selectNavButton(btnDashboard);

        // Button click listeners
        btnMenu.setOnClickListener(v -> {
            selectNavButton(btnMenu);
            loadFragment(new MenuBrowseFragment(), "MENU_FRAGMENT");
        });

        btnOrder.setOnClickListener(v -> {
            selectNavButton(btnOrder);
            loadFragment(new OrderFragment(), "ORDER_FRAGMENT");
        });

        btnDashboard.setOnClickListener(v -> {
            selectNavButton(btnDashboard);
            loadFragment(new HomeFragment(), "DASHBOARD_FRAGMENT");
        });

        btnCart.setOnClickListener(v -> {
            selectNavButton(btnCart);
            loadFragment(new CartFragment(), "CART_FRAGMENT");
        });

        btnProfile.setOnClickListener(v -> {
            selectNavButton(btnProfile);

            // Check if ProfileFragment already loaded
            Fragment currentFragment = getChildFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
            if (currentFragment != null && currentFragment.isVisible()) return;

            ProfileFragment profileFragment = new ProfileFragment();
            getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .replace(R.id.fragment_holder, profileFragment, "PROFILE_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_holder);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return; // Already loaded
        }
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, fragment, tag)
                .commit();
    }

    private void selectNavButton(LinearLayout selectedBtn) {
        LinearLayout[] buttons = {btnMenu, btnOrder, btnDashboard, btnCart, btnProfile};

        for (LinearLayout btn : buttons) {
            ImageView icon = (ImageView) btn.getChildAt(0);
            TextView text = (TextView) btn.getChildAt(1);

            icon.setScaleX(1f);
            icon.setScaleY(1f);
            icon.setColorFilter(getResources().getColor(R.color.gray));
            text.setTextColor(getResources().getColor(R.color.gray));
            text.setTypeface(null, Typeface.NORMAL);
            btn.setBackground(null);
        }

        ImageView selIcon = (ImageView) selectedBtn.getChildAt(0);
        TextView selText = (TextView) selectedBtn.getChildAt(1);

        selIcon.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start();
        selIcon.setColorFilter(getResources().getColor(R.color.green));
        selText.setTextColor(getResources().getColor(R.color.green));
        selText.setTypeface(null, Typeface.BOLD);
//        selectedBtn.setBackgroundResource(R.drawable.selected_circle_bg);
    }
}



