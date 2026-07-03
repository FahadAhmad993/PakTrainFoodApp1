const admin = require("../config/firebase");

async function addPendingBalance(uid, amount) {

    await admin.firestore()
        .collection("Wallets")
        .doc(uid)
        .set({

            pendingBalance:
                admin.firestore.FieldValue.increment(amount)

        }, { merge: true });

}

async function movePendingToAvailable(uid, amount) {

    await admin.firestore()
        .collection("Wallets")
        .doc(uid)
        .set({

            pendingBalance:
                admin.firestore.FieldValue.increment(-amount),

            availableBalance:
                admin.firestore.FieldValue.increment(amount)

        }, { merge: true });

}

async function addAdminBalance(amount) {

    await admin.firestore()
        .collection("Wallets")
        .doc("admin_wallet")
        .set({

            balance:
                admin.firestore.FieldValue.increment(amount)

        }, { merge: true });

}

module.exports = {

    addPendingBalance,

    movePendingToAvailable,

    addAdminBalance

};