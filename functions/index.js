const { onCall } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const stripe = require("stripe")("sk_test_51T2vhzDKgsKyivl6Oa9EOYPoVo92u0caUiGvJpwOwzgYMNhGBbrPOnmE9zMTcqQILGU3m6b0tWe3TgPSIB88rNR000UuZKzW8O");

admin.initializeApp();


// ========================================
// PAYMENT INTENT
// ========================================
exports.createPaymentIntent = onCall(async (request) => {

    if (!request.auth) {
        throw new Error("User not authenticated");
    }

    const { amount } = request.data;

    const paymentIntent = await stripe.paymentIntents.create({
        amount: Math.round(amount * 100),
        currency: "PKR",
    });

    return {
        clientSecret: paymentIntent.client_secret
    };
});


// ========================================
// ORDER CREATED
// ========================================
exports.onOrderPlaced = onDocumentCreated(
    "Orders/{orderId}",
    async (event) => {

        const data = event.data.data();

        const {
            orderId,
            restaurantId,
            subtotal,
            deliveryFee,
            adminFee,
            totalPrice
        } = data;

        // ADMIN MAIN WALLET
        await admin.firestore()
            .collection("Wallets")
            .doc("admin_wallet")
            .set({
                balance: admin.firestore.FieldValue.increment(totalPrice)
            }, { merge: true });

        // RESTAURANT PENDING
        if (restaurantId && subtotal) {

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .set({
                    pendingBalance:
                        admin.firestore.FieldValue.increment(subtotal),
                }, { merge: true });

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .collection("history")
                .add({
                    type: "Pending",
                    amount: subtotal,
                    orderId,
                    date: new Date().toISOString()
                });
        }

        // RIDER PENDING
        // if (deliveryFee) {

        //     await admin.firestore()
        //         .collection("Wallets")
        //         .doc(acceptedBy)
        //         .set({
        //             pendingBalance:
        //                 admin.firestore.FieldValue.increment(deliveryFee),
        //             availableBalance: 0
        //         }, { merge: true });
        // }

        // console.log("Order Created");
    }
);

exports.onRiderAccepted = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (
            before.orderStatus === "accepted_by_rider" ||
            after.orderStatus !== "accepted_by_rider"
        ) {
            return;
        }

        const riderId = after.acceptedBy;
        const deliveryFee = after.deliveryFee || 0;
        const orderId = after.orderId;

        if (!riderId || deliveryFee <= 0) {
            return;
        }

        await admin.firestore()
            .collection("Wallets")
            .doc(riderId)
            .set({
                pendingBalance:
                    admin.firestore.FieldValue.increment(deliveryFee),
            
            }, { merge: true });

        await admin.firestore()
            .collection("Wallets")
            .doc(riderId)
            .collection("history")
            .add({
                type: "Pending",
                amount: deliveryFee,
                orderId,
                date: new Date().toISOString()
            });

        console.log(
            "Rider pending wallet updated:",
            riderId
        );
    }
);
// ========================================
// ORDER COMPLETED
// ========================================
exports.onOrderCompleted = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        const riderId = after.acceptedBy;

        if (
            before.orderStatus === "completed" ||
            after.orderStatus !== "completed"
        ) {
            return;
        }

        const restaurantId = after.restaurantId;
        const subtotal = after.subtotal || 0;
        const deliveryFee = after.deliveryFee || 0;
        const orderId = after.orderId;

        // RESTAURANT
        if (restaurantId) {

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .set({
                    pendingBalance:
                        admin.firestore.FieldValue.increment(-subtotal),

                    availableBalance:
                        admin.firestore.FieldValue.increment(subtotal)

                }, { merge: true });

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .collection("history")
                .add({
                    type: "Available",
                    amount: subtotal,
                    orderId,
                    date: new Date().toISOString()
                });
        }

        // RIDER
      // RIDER
if (riderId && deliveryFee) {

    await admin.firestore()
        .collection("Wallets")
        .doc(riderId)
        .set({
            pendingBalance:
                admin.firestore.FieldValue.increment(-deliveryFee),

            availableBalance:
                admin.firestore.FieldValue.increment(deliveryFee)

        }, { merge: true });

    await admin.firestore()
        .collection("Wallets")
        .doc(riderId)
        .collection("history")
        .add({
            type: "Available",
            amount: deliveryFee,
            orderId,
            date: new Date().toISOString()
        });
}

        console.log("Order Completed");
    }
);




















// const { onCall } = require("firebase-functions/v2/https");
// const { onDocumentCreated } = require("firebase-functions/v2/firestore");
// const admin = require("firebase-admin");
// const stripe = require("stripe")("sk_test_51T2vhzDKgsKyivl6Oa9EOYPoVo92u0caUiGvJpwOwzgYMNhGBbrPOnmE9zMTcqQILGU3m6b0tWe3TgPSIB88rNR000UuZKzW8O");

// admin.initializeApp();


// // ===============================
// // 1. STRIPE PAYMENT INTENT
// // ===============================
// exports.createPaymentIntent = onCall(async (request) => {

//     if (!request.auth) {
//         throw new Error("User not authenticated");
//     }

//     const { amount } = request.data;

//     const paymentIntent = await stripe.paymentIntents.create({
//         amount: Math.round(amount * 100),
//         currency: "usd",
//     });

//     return {
//         clientSecret: paymentIntent.client_secret
//     };
// });


// // ===============================
// // 2. AUTO WALLET SYSTEM (MAIN)
// // ===============================
// exports.onOrderPlaced = onDocumentCreated("Orders/{orderId}", async (event) => {

//     const snap = event.data;
//     if (!snap) return;

//     const orderData = snap.data();

//     const {
//         orderId,
//         restaurantId,
//         subtotal,
//         deliveryFee,
//         adminFee
//     } = orderData;

//     // ===============================
//     // RESTAURANT WALLET UPDATE
//     // ===============================
//    if (restaurantId && subtotal !== undefined){
//       try {

//     await admin.firestore()
//         .collection("Wallets")
//         .doc(restaurantId)
//         .set({
//             balance: admin.firestore.FieldValue.increment(subtotal)
//         }, { merge: true });

//     console.log("Restaurant wallet updated");

//     await admin.firestore()
//         .collection("Wallets")
//         .doc(restaurantId)
//         .collection("history")
//         .add({
//             type: "Deposit",
//             amount: Number(subtotal),
//             orderId: orderId || "N/A",
//             role: "Restaurant",
//             date: new Date().toISOString()
//         });

//     console.log("History created");

// } catch (e) {
//     console.error("History Error:", e);
// }
//     }

//     // ===============================
//     // ADMIN WALLET UPDATE
//     // ===============================
//     if (adminFee) {

//         await admin.firestore()
//             .collection("Wallets")
//             .doc("admin_wallet")
//             .set({
//                 balance: admin.firestore.FieldValue.increment(adminFee)
//             }, { merge: true });

//         await admin.firestore()
//             .collection("Wallets")
//             .doc("admin_wallet")
//             .collection("history")
//             .add({
//                 type: "Commission",
//                 amount: adminFee,
//                 orderId: orderId || "N/A",
//                 role: "admin",
//                 date: new Date().toDateString()
//             });
//     }

//     // ===============================
//     // RIDER WALLET UPDATE (NEW)
//     // ===============================
//     if (deliveryFee) {

//         await admin.firestore()
//             .collection("RiderWallets")
//             .doc("auto_rider") // later real rider id replace hoga
//             .set({
//                 balance: admin.firestore.FieldValue.increment(deliveryFee)
//             }, { merge: true });

//         await admin.firestore()
//             .collection("RiderWallets")
//             .doc("auto_rider")
//             .collection("history")
//             .add({
//                 type: "Delivery Earning",
//                 amount: deliveryFee,
//                 orderId: orderId || "N/A",
//                 role: "rider",
//                 date: new Date().toDateString()
//             });
//     }

//     console.log("🔥 Wallets updated successfully for order:", orderId);
// });











