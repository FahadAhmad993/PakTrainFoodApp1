const { onCall } = require("firebase-functions/v2/https");

const stripe = require("../config/stripe");

exports.createPaymentIntent = onCall(async (request) => {

    if (!request.auth) {

        throw new Error("User not authenticated");

    }

    const { amount } = request.data;

    const paymentIntent = await stripe.paymentIntents.create({

        amount: Math.round(amount * 100),

        currency: "PKR"

    });

    return {

        clientSecret: paymentIntent.client_secret

    };

});