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

        adapter = new NotificationAdapter(requireContext(), notificationList);

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