const { setGlobalOptions } = require("firebase-functions");
const { onCall } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

// Order status update
exports.updateOrderStatus = onCall((data, context) => {
    const { orderId, status } = data;
    console.log(`Order ${orderId} updated to ${status}`);
    // Yahan aap Firestore ya Realtime DB update kar sakte ho
    return { message: `Order ${orderId} is now ${status}` };
});

// Add wallet balance
exports.addWalletBalance = onCall((data, context) => {
    const { userId, amount } = data;
    console.log(`Added ${amount} to ${userId}'s wallet`);
    // Yahan aap DB me wallet update kar sakte ho
    return { message: `Wallet updated with ${amount}` };
});
