const { onCall } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore"); // Sahi import
const admin = require("firebase-admin");
const stripe = require('stripe')('sk_test_51T2vhzDKgsKyivl6Oa9EOYPoVo92u0caUiGvJpwOwzgYMNhGBbrPOnmE9zMTcqQILGU3m6b0tWe3TgPSIB88rNR000UuZKzW8O');

admin.initializeApp();

// 1. Payment Intent
exports.createPaymentIntent = onCall(async (request) => {
    if (!request.auth) throw new Error("User not authenticated");
    const { amount } = request.data;
    const paymentIntent = await stripe.paymentIntents.create({
        amount: Math.round(amount * 100),
        currency: 'usd',
    });
    return { clientSecret: paymentIntent.client_secret };
});

// 2. Corrected Firestore Trigger
exports.onOrderPlaced = onDocumentCreated("Orders/{orderId}", async (event) => {
    const snap = event.data;
    if (!snap) return;
    
    const orderData = snap.data();
    const { restaurantId, subtotal, deliveryFee, adminFee } = orderData;

    // Restaurant ka wallet update
    if (restaurantId) {
        await admin.firestore().collection('Wallets').doc(restaurantId)
            .set({ balance: admin.firestore.FieldValue.increment(subtotal) }, { merge: true });
    }

    // Admin ka wallet update
    await admin.firestore().collection('Wallets').doc('admin_wallet')
        .set({ balance: admin.firestore.FieldValue.increment(adminFee) }, { merge: true });
        
    console.log("Wallets updated successfully for order: " + event.params.orderId);
});