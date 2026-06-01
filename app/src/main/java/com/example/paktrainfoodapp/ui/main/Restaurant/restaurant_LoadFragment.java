package com.example.paktrainfoodapp.ui.main.Restaurant;

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

public class restaurant_LoadFragment extends Fragment {

    private LinearLayout btnMenu, btnOrder, btnDashboard, btnDelivery, btnProfile;
    private ImageView iconMenu, iconOrder, iconDashboard, iconDelivery, iconProfile;
    private TextView textMenu, textOrder, textDashboard, textDelivery, textProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant__load, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        btnMenu = view.findViewById(R.id.btn_menu);
        btnOrder = view.findViewById(R.id.btn_order);
        btnDashboard = view.findViewById(R.id.btn_dashboard);
        btnDelivery = view.findViewById(R.id.btn_delivery);
        btnProfile = view.findViewById(R.id.btn_profile);

        iconMenu = view.findViewById(R.id.icon_menu);
        iconOrder = view.findViewById(R.id.icon_order);
        iconDashboard = view.findViewById(R.id.icon_dashboard);
        iconDelivery = view.findViewById(R.id.icon_delivery);
        iconProfile = view.findViewById(R.id.icon_profile);

        textMenu = view.findViewById(R.id.text_menu);
        textOrder = view.findViewById(R.id.text_order);
        textDashboard = view.findViewById(R.id.text_dashboard);
        textDelivery = view.findViewById(R.id.text_delivery);
        textProfile = view.findViewById(R.id.text_profile);

        // Default fragment open (Dashboard)
        openFragment(new resturent_DashboardFragment());
        highlightButton(btnDashboard, iconDashboard, textDashboard);

        // Click listeners
        btnMenu.setOnClickListener(v -> {
            openFragment(new resturent_MenuFragment());
            highlightButton(btnMenu, iconMenu, textMenu);
        });

        btnOrder.setOnClickListener(v -> {
            openFragment(new returent_OrdersFragment());
            highlightButton(btnOrder, iconOrder, textOrder);
        });

        btnDashboard.setOnClickListener(v -> {
            openFragment(new resturent_DashboardFragment());
            highlightButton(btnDashboard, iconDashboard, textDashboard);
        });

        btnDelivery.setOnClickListener(v -> {
            openFragment(new resturent_DeliveryFragment());
            highlightButton(btnDelivery, iconDelivery, textDelivery);
        });

        btnProfile.setOnClickListener(v -> {
            openFragment(new resturent_ProfileFragment());
            highlightButton(btnProfile, iconProfile, textProfile);
        });
    }

    private void openFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_holder, fragment)
                .commit();
    }

    private void highlightButton(LinearLayout selectedLayout, ImageView selectedIcon, TextView selectedText) {
        // Reset all
        resetButtons();

        // Highlight selected
        selectedLayout.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
        selectedLayout.setBackgroundResource(R.drawable.selected_circle_bg);
        selectedIcon.setColorFilter(getResources().getColor(R.color.green));
        selectedText.setTextColor(getResources().getColor(R.color.green));
        selectedText.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetButtons() {
        LinearLayout[] layouts = {btnMenu, btnOrder, btnDashboard, btnDelivery, btnProfile};
        ImageView[] icons = {iconMenu, iconOrder, iconDashboard, iconDelivery, iconProfile};
        TextView[] texts = {textMenu, textOrder, textDashboard, textDelivery, textProfile};

        for (LinearLayout layout : layouts) {
            layout.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            layout.setBackgroundResource(android.R.color.transparent);
        }

        for (ImageView icon : icons) {
            icon.setColorFilter(getResources().getColor(R.color.gray));
        }

        for (TextView text : texts) {
            text.setTextColor(getResources().getColor(R.color.gray));
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    // Eh method restaurant_LoadFragment class de vich rakho
    public void navigateFromDashboard(String target) {
        switch (target) {
            case "menu":
                btnMenu.performClick(); // btnMenu da click trigger karo
                break;
            case "delivery":
                btnDelivery.performClick();
                break;
            case "profile":
                btnProfile.performClick();
                break;
        }
    }
}
