const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

const admin = require("../../config/firebase");
const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

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
        const passengerUid = after.passengerUid;
        const restaurantId = after.restaurantId;

        const deliveryFee = after.deliveryFee || 0;
        const orderId = after.orderId;

        if (!riderId || deliveryFee <= 0) {
            return;
        }

        // =========================
        // Rider Wallet
        // =========================

        await admin.firestore()
            .collection("Wallets")
            .doc(riderId)
            .set({

                pendingBalance:
                    admin.firestore.FieldValue.increment(deliveryFee)

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

        console.log("Rider Pending Wallet Updated");

        // =========================
        // Passenger Notification
        // =========================

        if (passengerUid) {

            await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "🛵 Rider Accepted",

    body: "A rider has accepted your order and is on the way to the restaurant.",

    data: {

        orderId,

        status: ORDER_STATUS.ACCEPTED_BY_RIDER

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

    title: "🛵 Rider Assigned",

    body: "A rider has accepted the order and is coming to collect it.",

    data: {

        orderId,

        status: ORDER_STATUS.ACCEPTED_BY_RIDER

    }

});

        }

        // =========================
        // Rider Notification
        // =========================

       await sendNotification({

    uid: riderId,

    role: ROLES.DELIVERY,

    title: "✅ Order Accepted",

    body: "You accepted the order. Please reach the restaurant.",

    data: {

        orderId,

        status: ORDER_STATUS.ACCEPTED_BY_RIDER

    }

});

    }
);