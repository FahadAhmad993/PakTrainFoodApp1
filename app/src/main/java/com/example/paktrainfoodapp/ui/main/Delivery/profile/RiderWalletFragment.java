package com.example.paktrainfoodapp.ui.main.Delivery.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Restaurant.profile.WalletHistory;
import com.example.paktrainfoodapp.ui.main.Restaurant.profile.WalletHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class RiderWalletFragment extends Fragment {

    private TextView txtAvailableBalance;
    private TextView txtPendingBalance;

    private RecyclerView recyclerView;

    private ArrayList<WalletHistory> list;
    private WalletHistoryAdapter adapter;

    private String riderId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_rider_wallet,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        txtAvailableBalance =
                view.findViewById(R.id.txtAvailableBalance);

        txtPendingBalance =
                view.findViewById(R.id.txtPendingBalance);

        recyclerView =
                view.findViewById(R.id.recyclerHistory);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new WalletHistoryAdapter(getContext(), list);

        recyclerView.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            riderId =
                    FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid();
        }

        loadWalletBalance();
        loadWalletHistory();
    }

    // =========================
    // WALLET BALANCE
    // =========================

    private void loadWalletBalance() {

        FirebaseFirestore.getInstance()
                .collection("Wallets")
                .document(riderId)
                .addSnapshotListener((value, error) -> {

                    if (error != null ||
                            value == null ||
                            !value.exists()) {

                        txtAvailableBalance.setText("Rs 0");
                        txtPendingBalance.setText("Rs 0");
                        return;
                    }

                    Double available =
                            value.getDouble("availableBalance");

                    Double pending =
                            value.getDouble("pendingBalance");

                    if (available == null)
                        available = 0.0;

                    if (pending == null)
                        pending = 0.0;

                    txtAvailableBalance.setText(
                            "Rs " + available);

                    txtPendingBalance.setText(
                            "Rs " + pending);
                });
    }

    // =========================
    // WALLET HISTORY
    // =========================

    private void loadWalletHistory() {

        FirebaseFirestore.getInstance()
                .collection("Wallets")
                .document(riderId)
                .collection("history")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null)
                        return;

                    list.clear();

                    for (DocumentSnapshot doc :
                            value.getDocuments()) {

                        String type =
                                doc.getString("type");

                        String orderId =
                                doc.getString("orderId");

                        Double amount =
                                doc.getDouble("amount");

                        String date =
                                doc.getString("date");

                        if (amount == null)
                            amount = 0.0;

                        list.add(
                                new WalletHistory(
                                        type,
                                        String.valueOf(amount),
                                        date,
                                        orderId
                                )
                        );
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}