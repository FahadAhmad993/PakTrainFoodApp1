const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onOrderPickedUp = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (
            before.orderStatus === "pick_up" ||
            after.orderStatus !== "pick_up"
        ) {
            return;
        }

        const riderId = after.acceptedBy;
        const passengerUid = after.passengerUid;
        const restaurantId = after.restaurantId;
        const orderId = after.orderId;

        // =========================
        // Passenger Notification
        // =========================

        if (passengerUid) {

           await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "🛵 Order Picked Up",

    body: "Your order has been picked up and is on the way.",

    data: {

        orderId,

        status: ORDER_STATUS.PICK_UP

    }

});

        }

        // =========================
        // Restaurant Notification
        // =========================

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

        // =========================
        // Rider Notification
        // =========================

        if (riderId) {

           await sendNotification({

    uid: riderId,

    role: ROLES.DELIVERY,

    title: "🚚 Delivery Started",

    body: "You have picked up the order. Deliver it to the passenger.",

    data: {

        orderId,

        status: ORDER_STATUS.PICK_UP

    }

});

        }

        console.log("Pick Up Notifications Sent");

    }
);
















// const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

// const { sendNotification } = require("../../utils/sendNotification");

// exports.onOrderPickedUp = onDocumentUpdated(
//     "Orders/{orderId}",
//     async (event) => {

//         const before = event.data.before.data();
//         const after = event.data.after.data();

//         if (
//             before.orderStatus === "pick_up" ||
//             after.orderStatus !== "pick_up"
//         ) {
//             return;
//         }

//         const riderId = after.acceptedBy;
//         const passengerUid = after.passengerUid;
//         const restaurantId = after.restaurantId;
//         const orderId = after.orderId;

//         // =========================
//         // Passenger Notification
//         // =========================

//         if (passengerUid) {

//             await sendNotification(

//                 passengerUid,

//                 "🛵 Order Picked Up",

//                 "Your order has been picked up and is on the way.",

//                 {

//                     orderId,

//                     status: "pick_up"

//                 }

//             );

//         }

//         // =========================
//         // Restaurant Notification
//         // =========================

//         if (restaurantId) {

//             await sendNotification(

//                 restaurantId,

//                 "📦 Order Picked Up",

//                 "The rider has picked up the order and is delivering it.",

//                 {

//                     orderId,

//                     status: "pick_up"

//                 }

//             );

//         }

//         // =========================
//         // Rider Notification
//         // =========================

//         if (riderId) {

//             await sendNotification(

//                 riderId,

//                 "🚚 Delivery Started",

//                 "You have picked up the order. Deliver it to the passenger.",

//                 {

//                     orderId,

//                     status: "pick_up"

//                 }

//             );

//         }

//         console.log("Pick Up Notifications Sent");

//     }
// );