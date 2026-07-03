const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onRestaurantAccepted = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (
            before.orderStatus === "Accepted" ||
            after.orderStatus !== "Accepted"
        ) {
            return;
        }

        const passengerUid = after.passengerUid;
        const orderId = after.orderId;

        // ===============================
        // Passenger Notification
        // ===============================

        if (passengerUid) {

          await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "🍽️ Order Accepted",

    body: "Your order has been accepted by the restaurant and is being prepared.",

    data: {

        orderId,

        status: ORDER_STATUS.ACCEPTED

    }

});

        }

        console.log("Restaurant Accepted Notification Sent");

    }
);