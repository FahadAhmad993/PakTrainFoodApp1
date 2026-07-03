const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onOrderDropped = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (
            before.orderStatus === "dropped" ||
            after.orderStatus !== "dropped"
        ) {
            return;
        }

        const riderId = after.acceptedBy;
        const passengerUid = after.passengerUid;
        const restaurantId = after.restaurantId;
        const orderId = after.orderId;

        // =========================
        // Rider Notification
        // =========================

        if (riderId) {

           await sendNotification({

    uid: riderId,

    role: ROLES.DELIVERY,

    title: "📦 Order Ready for Pickup",

    body: "The restaurant has handed over the order. Please pick it up.",

    data: {

        orderId,

        status: ORDER_STATUS.DROPPED

    }

});

        }

        // =========================
        // Passenger Notification
        // =========================

        if (passengerUid) {

           await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "🍽️ Order Handover",

    body: "The restaurant has handed your order to the rider.",

    data: {

        orderId,

        status: ORDER_STATUS.DROPPED

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

    title: "✅ Order Handed Over",

    body: "You have successfully handed over the order to the rider.",

    data: {

        orderId,

        status: ORDER_STATUS.DROPPED

    }

});

        }

        console.log("Dropped Notifications Sent");

    }
);