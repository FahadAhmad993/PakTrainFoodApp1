const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { sendNotification } = require("../../utils/sendNotification");
const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onRiderArrived = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (
            before.orderStatus === "arrive_rider_at_resturent" ||
            after.orderStatus !== "arrive_rider_at_resturent"
        ) {
            return;
        }

        const passengerUid = after.passengerUid;
        const restaurantId = after.restaurantId;
        const riderId = after.acceptedBy;
        const orderId = after.orderId;

        // =========================
        // Passenger
        // =========================

        if (passengerUid) {

            await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "📍 Rider Arrived",

    body: "The rider has arrived at the restaurant to collect your order.",

    data: {

        orderId,

        status: ORDER_STATUS.ARRIVE_RIDER_AT_RESTAURANT

    }

});

        }

        // =========================
        // Restaurant
        // =========================

        if (restaurantId) {

          await sendNotification({

    uid: restaurantId,

    role: ROLES.RESTAURANT,

    title: "📍 Rider Arrived",

    body: "The rider has arrived. Please hand over the order.",

    data: {

        orderId,

        status: ORDER_STATUS.ARRIVE_RIDER_AT_RESTAURANT

    }

});

        }

        // =========================
        // Rider
        // =========================

        if (riderId) {

           await sendNotification({

    uid: riderId,

    role: ROLES.DELIVERY,

    title: "📍 Arrival Confirmed",

    body: "You have arrived at the restaurant. Wait for the restaurant to hand over the order.",

    data: {

        orderId,

        status: ORDER_STATUS.ARRIVE_RIDER_AT_RESTAURANT

    }

});d

        }

        console.log("Rider Arrived Notifications Sent");

    }
);