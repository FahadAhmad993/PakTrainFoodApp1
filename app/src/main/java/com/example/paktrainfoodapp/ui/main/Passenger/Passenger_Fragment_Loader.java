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
import com.example.paktrainfoodapp.ui.main.notification.NotificationFragment;
import com.example.paktrainfoodapp.ui.main.notification.NotificationRepository;

public class Passenger_Fragment_Loader extends Fragment {

    private LinearLayout btnNotification, btnOrder, btnDashboard, btnCart, btnProfile;

    // Main Bottom Navigation Fragments
    private final HomeFragment homeFragment = new HomeFragment();
//    private final MenuBrowseFragment menuFragment = new MenuBrowseFragment();
private final NotificationFragment notificationFragment = new NotificationFragment();
    private final OrderFragment orderFragment = new OrderFragment();
    private final CartFragment cartFragment = new CartFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private passenger_helpandsupport helpSupportFragment;
    private TextView txtNotificationBadge;

    private NotificationRepository notificationRepository;

    private Fragment activeFragment;

    // 🔥 Last opened dashboard screen save hoga
    // Example:
    // Restaurant List
    // Station Menu
    // Item Detail
    private Fragment lastDashboardFragment;
    private boolean openOrderAfterLoad = false;
    public void requestOpenOrders() {

        openOrderAfterLoad = true;

    }

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

        btnNotification = view.findViewById(R.id.btnNotification);
        txtNotificationBadge =  view.findViewById(R.id.txtNotificationBadge);

        notificationRepository = new NotificationRepository();
        btnOrder = view.findViewById(R.id.btn_order);
        btnDashboard = view.findViewById(R.id.btn_dashboard);
        btnCart = view.findViewById(R.id.btn_cart);
        btnProfile = view.findViewById(R.id.btn_profile);

        // ================= INITIAL FRAGMENTS =================

        if (savedInstanceState == null) {

            FragmentTransaction ft =
                    getChildFragmentManager().beginTransaction();

            ft.add(R.id.fragment_holder, homeFragment, "HOME");

            ft.add(R.id.fragment_holder, notificationFragment, "NOTIFICATION")
                    .hide(notificationFragment);

            ft.add(R.id.fragment_holder, orderFragment, "ORDER")
                    .hide(orderFragment);

            ft.add(R.id.fragment_holder, cartFragment, "CART")
                    .hide(cartFragment);

            ft.add(R.id.fragment_holder, profileFragment, "PROFILE")
                    .hide(profileFragment);
            helpSupportFragment = new passenger_helpandsupport();

            ft.add(R.id.fragment_holder,
                            helpSupportFragment,
                            "HELP_SUPPORT")
                    .hide(helpSupportFragment);

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

        // ================= Notification =================

        btnNotification.setOnClickListener(v -> {

            selectNavButton(btnNotification);

            showFragment(notificationFragment);

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

        if (openOrderAfterLoad) {

            openOrderAfterLoad = false;

            view.post(() -> {

                navigateToOrders();

            });

        }
        startBadgeListener();
        startNotificationBadge();

    }
    public void navigateToOrders() {

        if (!isAdded()) {

            openOrderAfterLoad = true;
            return;

        }

        showFragment(orderFragment);

        selectNavButton(btnOrder);

    }
    public void navigateToOrders(int tabIndex) {

        orderFragment.setSelectedTab(tabIndex);

        navigateToOrders();

    }
    private void startBadgeListener() {

        notificationRepository.listenUnreadCount(
                "PASSENGER",
                new NotificationRepository.BadgeCallback() {

                    @Override
                    public void onCountChanged(int count) {

                        if (getActivity() == null)
                            return;

                        getActivity().runOnUiThread(() -> {

                            if (count <= 0) {

                                txtNotificationBadge.setVisibility(View.GONE);

                            } else {

                                txtNotificationBadge.setVisibility(View.VISIBLE);

                                if (count > 99) {

                                    txtNotificationBadge.setText("99+");

                                } else {

                                    txtNotificationBadge.setText(
                                            String.valueOf(count));

                                }

                            }

                        });

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }

                });

    }
    private void startNotificationBadge() {

        notificationRepository.listenUnreadCount(

                NotificationRepository.ROLE_PASSENGER,

                new NotificationRepository.BadgeCallback() {

                    @Override
                    public void onCountChanged(int count) {

                        if (!isAdded())
                            return;

                        requireActivity().runOnUiThread(() -> {

                            if (count <= 0) {

                                txtNotificationBadge.setVisibility(View.GONE);

                            } else {

                                txtNotificationBadge.setVisibility(View.VISIBLE);

                                if (count > 99) {

                                    txtNotificationBadge.setText("99+");

                                } else {

                                    txtNotificationBadge.setText(
                                            String.valueOf(count));

                                }

                            }

                        });

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }

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

        } else if (fragment instanceof NotificationFragment) {

            selectNavButton(btnNotification);


        } else if (fragment instanceof OrderFragment) {

            selectNavButton(btnOrder);

        } else if (fragment instanceof CartFragment) {

            selectNavButton(btnCart);

        } else if (fragment instanceof ProfileFragment
                || fragment instanceof passenger_helpandsupport
                || fragment instanceof CommonIssues
                || fragment instanceof StaticPage
                || fragment instanceof LiveChatFragment) {

            selectNavButton(btnProfile);
        }
    }
    // =========================================================
    // BOTTOM NAVIGATION COLORS
    // =========================================================

    private void selectNavButton(LinearLayout selectedBtn) {

        LinearLayout[] buttons = {
                btnNotification,
                btnOrder,
                btnDashboard,
                btnCart,
                btnProfile
        };

        // Reset all buttons
        for (LinearLayout btn : buttons) {

            if (btn == null) continue;

            ImageView icon = null;
            TextView text = null;

            if (btn.getId() == R.id.btnNotification) {

                icon = btn.findViewById(R.id.imgNotification);
                text = btn.findViewById(R.id.text_notification);

            } else if (btn.getId() == R.id.btn_order) {

                icon = btn.findViewById(R.id.icon_order);
                text = btn.findViewById(R.id.text_order);

            } else if (btn.getId() == R.id.btn_dashboard) {

                icon = btn.findViewById(R.id.icon_dashboard);
                text = btn.findViewById(R.id.text_dashboard);

            } else if (btn.getId() == R.id.btn_cart) {

                icon = btn.findViewById(R.id.icon_cart);
                text = btn.findViewById(R.id.text_cart);

            } else if (btn.getId() == R.id.btn_profile) {

                icon = btn.findViewById(R.id.icon_profile);
                text = btn.findViewById(R.id.text_profile);
            }

            if (icon != null) {
                icon.setColorFilter(getResources().getColor(R.color.gray));
            }

            if (text != null) {
                text.setTextColor(getResources().getColor(R.color.gray));
                text.setTypeface(null, Typeface.NORMAL);
            }
        }

        if (selectedBtn == null) return;

        ImageView selectedIcon = null;
        TextView selectedText = null;

        if (selectedBtn.getId() == R.id.btnNotification) {

            selectedIcon = selectedBtn.findViewById(R.id.imgNotification);
            selectedText = selectedBtn.findViewById(R.id.text_notification);

        } else if (selectedBtn.getId() == R.id.btn_order) {

            selectedIcon = selectedBtn.findViewById(R.id.icon_order);
            selectedText = selectedBtn.findViewById(R.id.text_order);

        } else if (selectedBtn.getId() == R.id.btn_dashboard) {

            selectedIcon = selectedBtn.findViewById(R.id.icon_dashboard);
            selectedText = selectedBtn.findViewById(R.id.text_dashboard);

        } else if (selectedBtn.getId() == R.id.btn_cart) {

            selectedIcon = selectedBtn.findViewById(R.id.icon_cart);
            selectedText = selectedBtn.findViewById(R.id.text_cart);

        } else if (selectedBtn.getId() == R.id.btn_profile) {

            selectedIcon = selectedBtn.findViewById(R.id.icon_profile);
            selectedText = selectedBtn.findViewById(R.id.text_profile);
        }

        if (selectedIcon != null) {
            selectedIcon.setColorFilter(getResources().getColor(R.color.green));
        }

        if (selectedText != null) {
            selectedText.setTextColor(getResources().getColor(R.color.green));
            selectedText.setTypeface(null, Typeface.BOLD);
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
    public void openHelpSupport() {

        if (helpSupportFragment == null) {
            helpSupportFragment = new passenger_helpandsupport();
        }

        showFragment(helpSupportFragment);
    }
    public void openCommonFragment(Fragment fragment) {

        FragmentTransaction transaction =
                getChildFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_holder, fragment);

        transaction.addToBackStack(null);

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (notificationRepository != null) {

            notificationRepository.removeListener();

        }

    }
}

        transaction.commit();
    }

}



//















