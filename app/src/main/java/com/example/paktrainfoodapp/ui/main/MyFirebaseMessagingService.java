package com.example.paktrainfoodapp.ui.main;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "Message received: " + remoteMessage.getData());
        // Yahan notification show kar sakte ho
    }

    @Override
    public void onNewToken(String token) {
        Log.d("FCM", "New token: " + token);
        // Backend API call karke token save karo
        sendTokenToBackend(token);
    }

    private void sendTokenToBackend(String token) {
        // Example using Retrofit or OkHttp
        Log.d("FCM", "Send this token to backend: " + token);
    }
}
//