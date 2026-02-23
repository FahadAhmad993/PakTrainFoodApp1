package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.paktrainfoodapp.R;
import com.google.android.material.tabs.TabLayout;

public class OrderFragment extends Fragment {

    private TabLayout tabsOrders;
    private ImageView headerImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passanger_order, container, false);

        tabsOrders = view.findViewById(R.id.tabsOrders);
        headerImage = view.findViewById(R.id.headerImage);

        // Tabs
        tabsOrders.addTab(tabsOrders.newTab().setText("Active"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Accepted"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Delivered"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Completed"));

        // Default: Active
        replaceChildFragment(new ActiveOrdersFragment());

        tabsOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected = null;
                switch (tab.getPosition()) {
                    case 0:
                        selected = new ActiveOrdersFragment();
                        break;
                    case 1:
                        selected = new AcceptedOrdersFragment();
                        break;
                    case 2:
                        selected = new DeliveredOrdersFragment();
                        break;
                    case 3:
                        selected = new CompletedOrdersFragment();
                        break;
                }
                replaceChildFragment(selected);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void replaceChildFragment(Fragment fragment) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.orders_tab_container, fragment);
        ft.commit();
    }
}




//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.example.paktrainfoodapp.R;
//import com.google.android.material.tabs.TabLayout;
//
//public class OrderFragment extends Fragment {
//
//    private TabLayout tabsOrders;
//    private ImageView headerImage;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        // ✅ Updated layout file name
//        View view = inflater.inflate(R.layout.fragment_passanger_order, container, false);
//
//        // ✅ Bind views
//        tabsOrders = view.findViewById(R.id.tabsOrders);
//        headerImage = view.findViewById(R.id.headerImage);
//
//        // ✅ Add tabs manually (if not added in XML)
//        tabsOrders.addTab(tabsOrders.newTab().setText("Active"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Accepted"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Delivered"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Completed"));
//
//        // ✅ Show first tab (Active Orders) by default
//        replaceChildFragment(new ActiveOrdersFragment());
//
//        // ✅ Tab selection listener
//        tabsOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                switch (tab.getPosition()) {
//                    case 0:
//                        replaceChildFragment(new ActiveOrdersFragment());
//                        break;
//                    case 1:
//                        replaceChildFragment(new AcceptedOrdersFragment());
//                        break;
//                    case 2:
//                        replaceChildFragment(new DeliveredOrdersFragment());
//                        break;
//                    case 3:
//                        replaceChildFragment(new CompletedOrdersFragment());
//                        break;
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) { }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) { }
//        });
//
//        return view;
//    }
//
//    // ✅ Helper method to replace child fragment
//    private void replaceChildFragment(Fragment fragment) {
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.orders_tab_container, fragment);
//        transaction.commit();
//    }
//}
