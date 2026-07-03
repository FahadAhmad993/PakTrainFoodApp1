package com.example.paktrainfoodapp.ui.main.notification;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String documentId;

    private String title;

    private String body;

    private String orderId;

    private String status;

    private boolean isRead;

    private Timestamp createdAt;

    public NotificationModel() {
        // Firestore Required
    }

    public NotificationModel(String title,
                             String body,
                             String orderId,
                             String status,
                             boolean isRead,
                             Timestamp createdAt) {

        this.title = title;
        this.body = body;
        this.orderId = orderId;
        this.status = status;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRead() {
        return isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}