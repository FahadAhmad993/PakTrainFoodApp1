const { onDocumentCreated } = require("firebase-functions/v2/firestore");

const admin = require("../../config/firebase");

const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onOrderPlaced = onDocumentCreated(
    "Orders/{orderId}",
    async (event) => {

        const data = event.data.data();

        const {
            orderId,
            passengerUid,
            restaurantId,
            subtotal,
            deliveryFee,
            adminFee,
            totalPrice
        } = data;

        // ===============================
        // ADMIN MAIN WALLET
        // ===============================

        await admin.firestore()
            .collection("Wallets")
            .doc("admin_wallet")
            .set({

                balance:
                    admin.firestore.FieldValue.increment(totalPrice)

            }, { merge: true });

        // ===============================
        // RESTAURANT PENDING WALLET
        // ===============================

        if (restaurantId && subtotal) {

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .set({

                    pendingBalance:
                        admin.firestore.FieldValue.increment(subtotal)

                }, { merge: true });

            await admin.firestore()
                .collection("Wallets")
                .doc(restaurantId)
                .collection("history")
                .add({

                    type: "Pending",

                    amount: subtotal,

                    orderId: orderId,

                    date: new Date().toISOString()

                });

        }

        // ===============================
        // NOTIFICATION TO RESTAURANT
        // ===============================

        if (restaurantId) {

            await sendNotification({

    uid: restaurantId,

    role: ROLES.RESTAURANT,

    title: "🍽️ New Order",

    body: "You have received a new order.",

    data: {

        orderId,

        status: ORDER_STATUS.ACTIVE

    }

});

        }

        // ===============================
        // NOTIFICATION TO PASSENGER
        // ===============================

        if (passengerUid) {

           await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "✅ Order Placed",

    body: "Your order has been placed successfully.",

    data: {

        orderId,

        status: ORDER_STATUS.ACTIVE

    }

});

        }

        console.log("Order Placed Trigger Completed");

    }
);





















// const { onDocumentCreated } = require("firebase-functions/v2/firestore");

// const admin = require("../../config/firebase");

// const { sendNotification } = require("../../utils/sendNotification");

// exports.onOrderPlaced = onDocumentCreated(
//     "Orders/{orderId}",
//     async (event) => {

//         const data = event.data.data();

//         const {
//             orderId,
//             passengerUid,
//             restaurantId,
//             subtotal,
//             deliveryFee,
//             adminFee,
//             totalPrice
//         } = data;

//         // ===============================
//         // ADMIN MAIN WALLET
//         // ===============================

//         await admin.firestore()
//             .collection("Wallets")
//             .doc("admin_wallet")
//             .set({

//                 balance:
//                     admin.firestore.FieldValue.increment(totalPrice)

//             }, { merge: true });

//         // ===============================
//         // RESTAURANT PENDING WALLET
//         // ===============================

//         if (restaurantId && subtotal) {

//             await admin.firestore()
//                 .collection("Wallets")
//                 .doc(restaurantId)
//                 .set({

//                     pendingBalance:
//                         admin.firestore.FieldValue.increment(subtotal)

//                 }, { merge: true });

//             await admin.firestore()
//                 .collection("Wallets")
//                 .doc(restaurantId)
//                 .collection("history")
//                 .add({

//                     type: "Pending",

//                     amount: subtotal,

//                     orderId: orderId,

//                     date: new Date().toISOString()

//                 });

//         }

//         // ===============================
//         // NOTIFICATION TO RESTAURANT
//         // ===============================

//         if (restaurantId) {

//             await sendNotification(

//                 restaurantId,

//                 "🍽️ New Order",

//                 "You have received a new order.",

//                 {

//                     orderId: orderId,

//                     status: "Active"

//                 }

//             );

//         }

//         // ===============================
//         // NOTIFICATION TO PASSENGER
//         // ===============================

//         if (passengerUid) {

//             await sendNotification(

//                 passengerUid,

//                 "✅ Order Placed",

//                 "Your order has been placed successfully.",

//                 {

//                     orderId: orderId,

//                     status: "Active"

//                 }

//             );

//         }

//         console.log("Order Placed Trigger Completed");

//     }
// );
