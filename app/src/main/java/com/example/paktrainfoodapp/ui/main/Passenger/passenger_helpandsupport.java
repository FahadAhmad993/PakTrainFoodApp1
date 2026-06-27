package com.example.paktrainfoodapp.ui.main.Passenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class passenger_helpandsupport extends Fragment {

    private LinearLayout layoutFaq;
    private LinearLayout layoutLiveChat;
    private LinearLayout layoutCallSupport;
    private LinearLayout layoutEmailSupport;
    private LinearLayout layoutReportProblem;
    private LinearLayout layoutOrderIssue;
    private LinearLayout layoutPaymentIssue;
    private LinearLayout layoutRefundRequest;
    private LinearLayout layoutDeliveryIssue;
    private LinearLayout layoutRateApp;
    private LinearLayout layoutTerms;
    private LinearLayout layoutAbout;
    private LinearLayout layoutVersion;

    public passenger_helpandsupport() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passenger_helpandsupport, container, false);

        // Initialize Views
        layoutFaq = view.findViewById(R.id.layoutFaq);
        layoutLiveChat = view.findViewById(R.id.layoutLiveChat);
        layoutCallSupport = view.findViewById(R.id.layoutCallSupport);
        layoutEmailSupport = view.findViewById(R.id.layoutEmailSupport);
        layoutReportProblem = view.findViewById(R.id.layoutReportProblem);
        layoutOrderIssue = view.findViewById(R.id.layoutOrderIssue);
        layoutPaymentIssue = view.findViewById(R.id.layoutPaymentIssue);
        layoutRefundRequest = view.findViewById(R.id.layoutRefundRequest);
        layoutDeliveryIssue = view.findViewById(R.id.layoutDeliveryIssue);
        layoutRateApp = view.findViewById(R.id.layoutRateApp);
        layoutTerms = view.findViewById(R.id.layoutTerms);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutVersion = view.findViewById(R.id.layoutVersion);

        // ==========================
        // FAQs
        // ==========================
        layoutFaq.setOnClickListener(v ->
                openCommonIssue(
                        "FAQs",
                        "• How to place an order?\n\n" +
                                "• How to track your order?\n\n" +
                                "• How to cancel an order?\n\n" +
                                "• How to contact support?"
                ));

        // ==========================
        // Live Chat
        // ==========================
        layoutLiveChat.setOnClickListener(v -> {

            LiveChatFragment fragment = new LiveChatFragment();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit();

        });

        // ==========================
        // Call Support
        // ==========================
        layoutCallSupport.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+923001234567"));
            startActivity(intent);

        });

        // ==========================
        // Email Support
        // ==========================
        layoutEmailSupport.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@paktrainfood.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Pak Train Food Support");
            startActivity(intent);

        });
        // ==========================
        // Report Problem
        // ==========================
        layoutReportProblem.setOnClickListener(v ->
                openCommonIssue(
                        "Report a Problem",
                        "Describe your problem clearly.\n\n" +
                                "Include your Order ID, Train Name and Seat Number so our support team can help you quickly."
                ));

        // ==========================
        // Order Issue
        // ==========================
        layoutOrderIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Order Issue",
                        "If your order is delayed, incorrect or missing, please provide your Order ID and complete details."
                ));

        // ==========================
        // Payment Issue
        // ==========================
        layoutPaymentIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Payment Issue",
                        "If your payment has been deducted but the order is not confirmed, please contact support with your payment details."
                ));

        // ==========================
        // Refund Request
        // ==========================
        layoutRefundRequest.setOnClickListener(v ->
                openCommonIssue(
                        "Refund Request",
                        "Refunds are processed after verification.\n\nPlease provide your Order ID and payment information."
                ));

        // ==========================
        // Delivery Issue
        // ==========================
        layoutDeliveryIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Delivery Issue",
                        "If your food was delivered late or not delivered, please contact support immediately with your Order ID."
                ));

        // ==========================
        // Rate App
        // ==========================
        layoutRateApp.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store"));
            startActivity(intent);

        });
        // ==========================
        // Terms & Conditions
        // ==========================
        layoutTerms.setOnClickListener(v ->
                openStaticPage(
                        "Terms & Conditions",
                        "• Orders cannot be cancelled after preparation.\n\n"
                                + "• Refunds are processed after verification.\n\n"
                                + "• Passengers must provide correct train and seat details."
                ));

        // ==========================
        // About App
        // ==========================
        layoutAbout.setOnClickListener(v ->
                openStaticPage(
                        "About App",
                        "Pak Train Food App allows passengers to order food while travelling by train.\n\n"
                                + "Passengers can browse restaurants, place orders, make secure payments and track deliveries in real time."
                ));

        // ==========================
        // App Version
        // ==========================
        layoutVersion.setOnClickListener(v ->
                openStaticPage(
                        "App Version",
                        "Pak Train Food\n\nVersion 1.0"
                ));

        return view;
    }

    // ==========================
    // Open Common Issue Fragment
    // ==========================
    private void openCommonIssue(String title, String description) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("description", description);

        CommonIssues fragment = new CommonIssues();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ==========================
    // Open Static Page Fragment
    // ==========================
    private void openStaticPage(String title, String description) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("description", description);

        StaticPage fragment = new StaticPage();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();
    }

}

        // ==========================
        // Terms & Conditions
        //

/*package com.example.paktrainfoodapp.ui.main.Passenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class passenger_helpandsupport extends Fragment {

    LinearLayout layoutFaq,
            layoutLiveChat,
            layoutCallSupport,
            layoutEmailSupport,
            layoutReportProblem,
            layoutOrderIssue,
            layoutPaymentIssue,
            layoutRefundRequest,
            layoutDeliveryIssue,
            layoutRateApp,
            layoutTerms,
            layoutAbout,
            layoutVersion;
    private Fragment fragment;

    public passenger_helpandsupport() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passenger_helpandsupport, container, false);

        layoutFaq = view.findViewById(R.id.layoutFaq);
        layoutLiveChat = view.findViewById(R.id.layoutLiveChat);
        layoutCallSupport = view.findViewById(R.id.layoutCallSupport);
        layoutEmailSupport = view.findViewById(R.id.layoutEmailSupport);
        layoutReportProblem = view.findViewById(R.id.layoutReportProblem);
        layoutOrderIssue = view.findViewById(R.id.layoutOrderIssue);
        layoutPaymentIssue = view.findViewById(R.id.layoutPaymentIssue);
        layoutRefundRequest = view.findViewById(R.id.layoutRefundRequest);
        layoutDeliveryIssue = view.findViewById(R.id.layoutDeliveryIssue);
        layoutRateApp = view.findViewById(R.id.layoutRateApp);
        layoutTerms = view.findViewById(R.id.layoutTerms);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutVersion = view.findViewById(R.id.layoutVersion);

        // FAQs
        layoutFaq.setOnClickListener(v ->
                openCommonIssue(
                        "FAQs",
                        "• How to place an order?\n\n" +
                                "• How to track an order?\n\n" +
                                "• How to cancel an order?"
                ));

        // Report Problem
        layoutReportProblem.setOnClickListener(v ->
                openCommonIssue(
                        "Report a Problem",
                        "Describe your issue clearly so our support team can help you."
                ));

        // Order Issue
        layoutOrderIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Order Issue",
                        "If your order is delayed, missing or incorrect, please provide your Order ID."
                ));

        // Payment Issue
        layoutPaymentIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Payment Issue",
                        "If payment is deducted but order is not confirmed, contact support with payment details."
                ));

        // Refund Request
        layoutRefundRequest.setOnClickListener(v ->
                openCommonIssue(
                        "Refund Request",
                        "Refunds are processed after verification. Please provide your Order ID."
                ));

        // Delivery Issue
        layoutDeliveryIssue.setOnClickListener(v ->
                openCommonIssue(
                        "Delivery Issue",
                        "If your food was late or not delivered, contact support immediately."
                ));

        // Terms
        layoutTerms.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", "Terms & Conditions");
                    bundle.putString("description",
                            "• Orders cannot be cancelled after preparation.\n\n"
                                    + "• Refunds are processed after verification.\n\n"
                                    + "• Passengers must provide correct train and seat details.");

                    StaticPage fragment = new StaticPage();
                    fragment.setArguments(bundle);

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, fragment)
                            .addToBackStack(null)
                            .commit();

                }
                );

        // About
        layoutAbout.setOnClickListener(v ->{
                    Bundle bundle = new Bundle();
                    bundle.putString("title", "About App");
                    bundle.putString("description",
                            "Pak Train Food App allows passengers to order food while travelling by train. "
                                    + "Passengers can browse restaurants, place orders, make payments and track deliveries.");

                    StaticPage fragment = new StaticPage();
                    fragment.setArguments(bundle);

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                 );

        // Version
        layoutVersion.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", "App Version");
                    bundle.putString("description",
                            "Pak Train Food\n\nVersion 1.0");

                    StaticPage fragment = new StaticPage();
                    fragment.setArguments(bundle);

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                );

        // Call Support
        layoutCallSupport.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+923001234567"));
            startActivity(intent);

        });

        // Email Support
        layoutEmailSupport.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@paktrainfood.com"));
            startActivity(intent);

        });

        // Rate App
        layoutRateApp.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store"));
            startActivity(intent);

        });

        // Live Chat
        layoutLiveChat.setOnClickListener(v -> {

         //   LiveChatFragment fragment = new LiveChatFragment();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit();

        });

        return view;
    }

    private void openCommonIssue(String title, String description) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("description", description);

        CommonIssues fragment = new CommonIssues();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openStaticPage(String title, String description) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("description", description);

        StaticPage fragment = new StaticPage();
        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();
    }
}*/