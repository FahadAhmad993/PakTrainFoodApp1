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
                "pk_test_51T2vhzDKgsKyivl6XMUfyaPEUTHOo5Nbbzh8myFJzg4CsHLpwrmwCPCHfXJS3TMF2ZTxvjgx4SO32tJ7oWSe6djY00M3AcQsWg" // Yahan apni Publishable Key dalein
        );
    }
}