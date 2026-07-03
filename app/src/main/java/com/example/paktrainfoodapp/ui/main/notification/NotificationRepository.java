package com.example.paktrainfoodapp.ui.main.notification;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;

import com.example.paktrainfoodapp.ui.main.notification.NotificationModel;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private ListenerRegistration notificationListener;
    private ListenerRegistration badgeListener;
    public static final String ROLE_PASSENGER = "PASSENGER";
    public static final String ROLE_RESTAURANT = "RESTAURANT";
    public static final String ROLE_DELIVERY = "DELIVERY";
    public NotificationRepository() {

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

    }

    private String getUid() {

        if (auth.getCurrentUser() == null)
            return null;

        return auth.getCurrentUser().getUid();

    }

    private CollectionReference getNotificationCollection(String role) {

        String uid = getUid();

        if (uid == null)
            return null;

        switch (role) {

            case "PASSENGER":

                return db.collection("Users")
                        .document("Passenger")
                        .collection("Register")
                        .document(uid)
                        .collection("Notifications");

            case "RESTAURANT":

                return db.collection("Users")
                        .document("Restaurant")
                        .collection("VerifiedRegister")
                        .document(uid)
                        .collection("Notifications");

            case "DELIVERY":

                return db.collection("Users")
                        .document("Delivery")
                        .collection("VerifiedRegister")
                        .document(uid)
                        .collection("Notifications");

            default:
                return null;

        }

    }
    public interface NotificationCallback {

        void onSuccess(List<NotificationModel> list);

        void onFailure(Exception e);

    }
    public interface NotificationRealtimeCallback {

        void onChanged(List<NotificationModel> list);

        void onFailure(Exception e);

    }
    public interface BadgeCallback {

        void onCountChanged(int count);

        void onFailure(Exception e);

    }
    public void loadNotifications(String role,
                                  @NonNull NotificationCallback callback) {

        CollectionReference ref = getNotificationCollection(role);

        if (ref == null) {

            callback.onFailure(new Exception("Notification path not found"));
            return;

        }

        ref.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<NotificationModel> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        NotificationModel model =
                                doc.toObject(NotificationModel.class);

                        list.add(model);

                    }

                    callback.onSuccess(list);

                })
                .addOnFailureListener(callback::onFailure);

    }
    public void listenNotifications(
            String role,
            @NonNull NotificationRealtimeCallback callback) {

        CollectionReference ref = getNotificationCollection(role);

        if (ref == null) {

            callback.onFailure(
                    new Exception("Notification path not found"));

            return;
        }

        if (notificationListener != null) {

            notificationListener.remove();

        }

        

        notificationListener = ref
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {

                        callback.onFailure(error);

                        return;

                    }

                    List<NotificationModel> list =
                            new ArrayList<>();

                    if (value != null) {

                        for (QueryDocumentSnapshot doc :
                                value) {

                            NotificationModel model =
                                    doc.toObject(NotificationModel.class);

                            list.add(model);

                        }

                    }

                    callback.onChanged(list);

                });

    }
    public void removeListener() {

        if (notificationListener != null) {

            notificationListener.remove();
            notificationListener = null;

        }

        if (badgeListener != null) {

            badgeListener.remove();
            badgeListener = null;

        }

    }
    public void listenUnreadCount(
            String role,
            @NonNull BadgeCallback callback) {

        CollectionReference ref = getNotificationCollection(role);

        if (ref == null) {

            callback.onFailure(new Exception("Notification path not found"));
            return;

        }

        if (badgeListener != null) {

            badgeListener.remove();

        }

        badgeListener = ref
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {

                        callback.onFailure(error);
                        return;

                    }

                    int count = 0;

                    if (value != null) {

                        count = value.size();

                    }

                    callback.onCountChanged(count);

                });

    }
}