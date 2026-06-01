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
import androidx.fragment.app.FragmentTransaction;

import com.example.paktrainfoodapp.R;

public class Passenger_Fragment_Loader extends Fragment {

    private LinearLayout btnMenu, btnOrder, btnDashboard, btnCart, btnProfile;

    // Main Bottom Navigation Fragments
    private final HomeFragment homeFragment = new HomeFragment();
    private final MenuBrowseFragment menuFragment = new MenuBrowseFragment();
    private final OrderFragment orderFragment = new OrderFragment();
    private final CartFragment cartFragment = new CartFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    private Fragment activeFragment;

    // 🔥 Last opened dashboard screen save hoga
    // Example:
    // Restaurant List
    // Station Menu
    // Item Detail
    private Fragment lastDashboardFragment;

    public Passenger_Fragment_Loader() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_passenger_load_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        btnMenu = view.findViewById(R.id.btn_menu);
        btnOrder = view.findViewById(R.id.btn_order);
        btnDashboard = view.findViewById(R.id.btn_dashboard);
        btnCart = view.findViewById(R.id.btn_cart);
        btnProfile = view.findViewById(R.id.btn_profile);

        // ================= INITIAL FRAGMENTS =================

        if (savedInstanceState == null) {

            FragmentTransaction ft =
                    getChildFragmentManager().beginTransaction();

            ft.add(R.id.fragment_holder, homeFragment, "HOME");

            ft.add(R.id.fragment_holder, menuFragment, "MENU")
                    .hide(menuFragment);

            ft.add(R.id.fragment_holder, orderFragment, "ORDER")
                    .hide(orderFragment);

            ft.add(R.id.fragment_holder, cartFragment, "CART")
                    .hide(cartFragment);

            ft.add(R.id.fragment_holder, profileFragment, "PROFILE")
                    .hide(profileFragment);

            ft.commit();

            activeFragment = homeFragment;

            // 🔥 Initial dashboard screen
            lastDashboardFragment = homeFragment;
        }

        // Default selected button
        selectNavButton(btnDashboard);

        // ================= DASHBOARD =================

        btnDashboard.setOnClickListener(v -> {

            selectNavButton(btnDashboard);

            // 🔥 Last dashboard screen open karo
            if (lastDashboardFragment != null) {
                showFragment(lastDashboardFragment);
            } else {
                showFragment(homeFragment);
            }
        });

        // ================= MENU =================

        btnMenu.setOnClickListener(v -> {

            selectNavButton(btnMenu);

            showFragment(menuFragment);
        });

        // ================= ORDER =================

        btnOrder.setOnClickListener(v -> {

            selectNavButton(btnOrder);

            showFragment(orderFragment);
        });

        // ================= CART =================

        btnCart.setOnClickListener(v -> {

            selectNavButton(btnCart);

            showFragment(cartFragment);
        });

        // ================= PROFILE =================

        btnProfile.setOnClickListener(v -> {

            selectNavButton(btnProfile);

            showFragment(profileFragment);
        });
    }

    // =========================================================
    // SHOW FRAGMENT
    // =========================================================

    public void showFragment(Fragment fragment) {

        FragmentTransaction transaction =
                getChildFragmentManager().beginTransaction();

        // Hide all visible fragments
        for (Fragment frag : getChildFragmentManager().getFragments()) {

            if (frag != null &&
                    frag.isAdded() &&
                    frag.isVisible()) {

                transaction.hide(frag);
            }
        }

        // If not added before
        if (!fragment.isAdded()) {

            transaction.add(R.id.fragment_holder, fragment);

        } else {

            transaction.show(fragment);
        }

        transaction.commit();

        activeFragment = fragment;
        updateBottomNav(fragment);

        // 🔥 Dashboard flow save
        if (fragment instanceof HomeFragment ||
                fragment instanceof Passanger_Resturent_list_Fragment ||
                fragment instanceof Station_Menu_Fragment ||
                fragment instanceof Passanger_ItemDetailsFragment) {

            lastDashboardFragment = fragment;
        }
    }

    // =========================================================
    // RESTAURANT LIST
    // =========================================================

    public void openRestaurantList(Fragment fragment) {

        FragmentTransaction transaction =
                getChildFragmentManager().beginTransaction();

        // Hide all
        for (Fragment frag : getChildFragmentManager().getFragments()) {

            if (frag != null &&
                    frag.isAdded() &&
                    frag.isVisible()) {

                transaction.hide(frag);
            }
        }

        transaction.add(R.id.fragment_holder, fragment);

        transaction.addToBackStack(null);

        transaction.commit();

        activeFragment = fragment;

        // 🔥 Save dashboard state
        lastDashboardFragment = fragment;
    }

    // =========================================================
    // RESTAURANT MENU / ITEM DETAILS
    // =========================================================

    public void openRestaurantMenu(Fragment fragment) {

        FragmentTransaction transaction =
                getChildFragmentManager().beginTransaction();

        // Hide all visible fragments
        for (Fragment frag : getChildFragmentManager().getFragments()) {

            if (frag != null &&
                    frag.isAdded() &&
                    frag.isVisible()) {

                transaction.hide(frag);
            }
        }

        transaction.add(R.id.fragment_holder, fragment);

        transaction.addToBackStack(null);

        transaction.commit();

        activeFragment = fragment;

        // 🔥 Save dashboard state
        lastDashboardFragment = fragment;
    }

    // =========================================================
    // TEMP FRAGMENT
    // =========================================================

    public void showTempFragment(Fragment fragment) {

        activeFragment = fragment;

        lastDashboardFragment = fragment;
        // 🔥 Bottom nav sync
        updateBottomNav(fragment);
    }
// =========================================================
// UPDATE BOTTOM NAV BY CURRENT FRAGMENT
// =========================================================

    public void updateBottomNav(Fragment fragment) {

        if (fragment instanceof HomeFragment ||
                fragment instanceof Passanger_Resturent_list_Fragment ||
                fragment instanceof Station_Menu_Fragment ||
                fragment instanceof Passanger_ItemDetailsFragment) {

            selectNavButton(btnDashboard);

        } else if (fragment instanceof MenuBrowseFragment) {

            selectNavButton(btnMenu);

        } else if (fragment instanceof OrderFragment) {

            selectNavButton(btnOrder);

        } else if (fragment instanceof CartFragment) {

            selectNavButton(btnCart);

        } else if (fragment instanceof ProfileFragment) {

            selectNavButton(btnProfile);
        }
    }
    // =========================================================
    // BOTTOM NAVIGATION COLORS
    // =========================================================

    private void selectNavButton(LinearLayout selectedBtn) {

        LinearLayout[] buttons = {
                btnMenu,
                btnOrder,
                btnDashboard,
                btnCart,
                btnProfile
        };

        for (LinearLayout btn : buttons) {

            if (btn == null) continue;

            ImageView icon = (ImageView) btn.getChildAt(0);
            TextView text = (TextView) btn.getChildAt(1);

            // Default Gray
            icon.setColorFilter(
                    getResources().getColor(R.color.gray)
            );

            text.setTextColor(
                    getResources().getColor(R.color.gray)
            );

            text.setTypeface(null, Typeface.NORMAL);
        }

        // Selected Green
        if (selectedBtn != null) {

            ImageView selIcon =
                    (ImageView) selectedBtn.getChildAt(0);

            TextView selText =
                    (TextView) selectedBtn.getChildAt(1);

            selIcon.setColorFilter(
                    getResources().getColor(R.color.green)
            );

            selText.setTextColor(
                    getResources().getColor(R.color.green)
            );

            selText.setTypeface(null, Typeface.BOLD);
        }
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Fragment getActiveFragment() {
        return activeFragment;
    }

    public Fragment getHomeFragment() {
        return homeFragment;
    }
}




















