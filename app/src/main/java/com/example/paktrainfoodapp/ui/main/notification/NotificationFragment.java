package com.example.paktrainfoodapp.ui.main.notification;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerNotifications;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private Toolbar toolbar;

    private NotificationAdapter adapter;
    private final List<NotificationModel> notificationList = new ArrayList<>();


    private NotificationRepository repository;

    public NotificationFragment() {
        super(R.layout.fragment_notification);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        toolbar = view.findViewById(R.id.toolbarNotification);

        adapter = new NotificationAdapter(

                requireContext(),
                notificationList,

                new NotificationAdapter.NotificationClickListener() {

                    @Override
                    public void onOrderClick(NotificationModel model) {

                        openOrder(model);

                    }

//            @Override
//            public void onWalletClick(NotificationModel model) {
//
//            }
//
//            @Override
//            public void onProfileClick(NotificationModel model) {
//
//            }
//
//            @Override
//            public void onRestaurantClick(NotificationModel model) {
//
//            }
//
//            @Override
//            public void onOfferClick(NotificationModel model) {
//
//            }

                }

        );
        recyclerNotifications.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        recyclerNotifications.setAdapter(adapter);

        repository = new NotificationRepository();



        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_clear_all) {

                // Future
                // Firestore se delete hoga

                return true;
            }

            return false;
        });
        startRealtimeNotifications();

    }

    private void openOrder(NotificationModel model) {

        if (!(getParentFragment() instanceof com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader))
            return;

        com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader loader =
                (com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader) getParentFragment();

        String status = model.getStatus();

        int tab = 0;

        if ("Active".equalsIgnoreCase(status)) {

            tab = 0;

        } else if ("Accepted".equalsIgnoreCase(status)) {

            tab = 1;

        } else if ("Delivered".equalsIgnoreCase(status)) {

            tab = 2;

        } else if ("Completed".equalsIgnoreCase(status)) {

            tab = 3;

        }

        loader.navigateToOrders(tab);

        loader.openOrderDetail(model.getOrderId());

    }

    private void startRealtimeNotifications() {

        progressBar.setVisibility(View.VISIBLE);

        repository.listenNotifications(
                NotificationRepository.ROLE_PASSENGER,
                new NotificationRepository.NotificationRealtimeCallback() {

                    @Override
                    public void onChanged(List<NotificationModel> list) {

                        notificationList.clear();

                        notificationList.addAll(list);

                        adapter.notifyDataSetChanged();

                        updateUI();

                    }

                    @Override
                    public void onFailure(Exception e) {

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                requireContext(),
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

    }
    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (repository != null) {
            repository.removeListener();
        }
    }


    private void updateUI() {

        if (notificationList.isEmpty()) {

            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);

        } else {

            layoutEmpty.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);

        }

        progressBar.setVisibility(View.GONE);
    }

}