package com.example.paktrainfoodapp;

import android.app.Application;
import com.stripe.android.PaymentConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Stripe yahan initialize hoga
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_YOUR_PUBLISHABLE_KEY_HERE" // Yahan apni Publishable Key dalein
        );
    }
}