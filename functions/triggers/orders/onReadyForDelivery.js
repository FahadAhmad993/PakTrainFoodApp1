const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

const admin = require("../../config/firebase");
const { sendNotification } = require("../../utils/sendNotification");

const {
    ROLES,
    ORDER_STATUS
} = require("../../utils/constants");

exports.onReadyForDelivery = onDocumentUpdated(
    "Orders/{orderId}",
    async (event) => {

        const before = event.data.before.data();
        const after = event.data.after.data();

        if (

            before.orderStatus === "ready_for_delivery" ||

            after.orderStatus !== "ready_for_delivery"

        ) {

            return;

        }

        const passengerUid = after.passengerUid;
        const orderId = after.orderId;
        const mealStation = after.mealStation;

        // ===============================
        // Passenger Notification
        // ===============================

        if (passengerUid) {

         await sendNotification({

    uid: passengerUid,

    role: ROLES.PASSENGER,

    title: "📦 Order Ready",

    body: "Your order has been prepared and is ready for delivery.",

    data: {

        orderId,

        status: ORDER_STATUS.READY_FOR_DELIVERY

    }

});

        }

        // ===============================
        // Find Riders
        // ===============================

        const riders = await admin.firestore()

            .collection("Users")

            .doc("Delivery")

            .collection("VerifiedRegister")

            .where("city", "==", mealStation)

            .where("isVerified", "==", true)

            .get();

        // ===============================
        // Notification To Riders
        // ===============================

        for (const rider of riders.docs) {

            await sendNotification({

    uid: rider.id,

    role: ROLES.DELIVERY,

    title: "🛵 New Delivery Order",

    body: "A new delivery order is available near you.",

    data: {

        orderId,

        status: ORDER_STATUS.READY_FOR_DELIVERY

    }

});

        }

        console.log("Ready For Delivery Notifications Sent");

    }
);