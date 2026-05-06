package com.example.paktrainfoodapp.ui.main.Delivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Passenger.DeliveryOrderFragment;

public class DeliveryDashboardFragment extends Fragment {

    private LinearLayout btn_delivery_menu, btn_delivery_order, btn_deliver_home,  btn_delivery_profile;
    private ImageView icon_delivery_menu, icon_delivery_order, icon_deliver_home, icon_delivery_profile;
    private TextView text_delivery_menu, text_delivery_order, text_deliver_home, text_delivery_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        btn_delivery_menu = view.findViewById(R.id.btn_delivery_menu);
        btn_delivery_order = view.findViewById(R.id.btn_delivery_order);
        btn_deliver_home = view.findViewById(R.id.btn_deliver_home);
        btn_delivery_profile = view.findViewById(R.id.btn_delivery_profile);

        icon_delivery_menu = view.findViewById(R.id.icon_delivery_menu);
        icon_delivery_order = view.findViewById(R.id.icon_delivery_order);
        icon_deliver_home = view.findViewById(R.id.icon_deliver_home);
        icon_delivery_profile = view.findViewById(R.id.icon_delivery_profile);

        text_delivery_menu = view.findViewById(R.id.text_delivery_menu);
        text_delivery_order = view.findViewById(R.id.text_delivery_order);
        text_deliver_home = view.findViewById(R.id.text_deliver_home);
        text_delivery_profile = view.findViewById(R.id.text_delivery_profile);

        Switch statusSwitch = view.findViewById(R.id.status_switch);
        TextView statusText = view.findViewById(R.id.status_text);
        View statusDot = view.findViewById(R.id.status_dot);

// Default Online
        statusSwitch.setChecked(true);
        statusText.setText("Online");
        statusDot.setBackgroundResource(R.drawable.status_dot_green);

// Handle switch change
        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                statusText.setText("Online");
                statusDot.setBackgroundResource(R.drawable.status_dot_green);
            } else {
                statusText.setText("Offline");
                statusDot.setBackgroundResource(R.drawable.status_dot_red);
            }
        });


        // Default fragment open (Dashboard)
        openFragment(new DeliveryHomeFragment());
        highlightButton(btn_deliver_home, icon_deliver_home, text_deliver_home);

        // Click listeners
        btn_delivery_menu.setOnClickListener(v -> {
            openFragment(new DeliveryNotificationFragment());
            highlightButton(btn_delivery_menu, icon_delivery_menu, text_delivery_menu);
        });

        btn_delivery_order.setOnClickListener(v -> {
            openFragment(new DeliveryOrderFragment());
            highlightButton(btn_delivery_order, icon_delivery_order, text_delivery_order);
        });

        btn_deliver_home.setOnClickListener(v -> {
            openFragment(new DeliveryHomeFragment());
            highlightButton(btn_deliver_home, icon_deliver_home, text_deliver_home);
        });


        btn_delivery_profile.setOnClickListener(v -> {
            openFragment(new DeliveryProfileFragment());
            highlightButton(btn_delivery_profile, icon_delivery_profile, text_delivery_profile);
        });
    }

    private void openFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_loader, fragment)
                .commit();
    }

    private void highlightButton(LinearLayout selectedLayout, ImageView selectedIcon, TextView selectedText) {
        // Reset all
        resetButtons();

        // Highlight selected
        selectedLayout.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
//        selectedLayout.setBackgroundResource(R.drawable.selected_circle_bg);
        selectedIcon.setColorFilter(getResources().getColor(R.color.green));
        selectedText.setTextColor(getResources().getColor(R.color.green));
        selectedText.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetButtons() {
        LinearLayout[] layouts = {btn_delivery_menu, btn_delivery_order, btn_deliver_home, btn_delivery_profile};
        ImageView[] icons = {icon_delivery_menu, icon_delivery_order, icon_deliver_home, icon_delivery_profile};
        TextView[] texts = {text_delivery_menu, text_delivery_order, text_deliver_home, text_delivery_profile};

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
}
