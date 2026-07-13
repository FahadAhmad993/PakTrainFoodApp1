import { useEffect, useState, useCallback } from "react";
import "./Payments.css";

import {
  collection,
  doc,
  getDoc,
  getDocs,
  updateDoc,
  addDoc,
  serverTimestamp,
} from "firebase/firestore";

import { db } from "../firebase/config";

// Currency formatting utility shifted to PKR
const formatCurrency = (amount, currency = "PKR") => {
  return new Intl.NumberFormat("en-PK", {
    style: "currency",
    currency,
    minimumFractionDigits: 0
  }).format(Number(amount || 0));
};

const Payments = () => {
  const [loading, setLoading] = useState(true);

  // Admin Wallet State
  const [adminWallet, setAdminWallet] = useState({
    available: 0,
    pending: 0,
    currency: "PKR"
  });

  // Wallets + History Arrays
  const [wallets, setWallets] = useState([]);
  const [history, setHistory] = useState([]);

  // Metrics Display States
  const [restaurantPending, setRestaurantPending] = useState(0);
  const [riderPending, setRiderPending] = useState(0);
  const [totalPaid, setTotalPaid] = useState(0);

  // PAYOUT SHEET MODAL STATES
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedWallet, setSelectedWallet] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [payoutError, setPayoutError] = useState("");

  // ----------------------------------------------------
  // REAL TIME ADMIN STRIPE WALLET FETCH
  // ----------------------------------------------------
  const loadStripeBalance = useCallback(async (isMounted = { current: true }) => {
    try {
      const response = await fetch(
        "https://us-central1-paktrainfoodservice.cloudfunctions.net/getAdminBalance",
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json"
          }
        }
      );

      const data = await response.json();

      if (!data || data.success !== true) return;

      const availableItems = data.available || [];
      const pendingItems = data.pending || [];

      const availableAmt = availableItems.length > 0 ? (availableItems[0].amount || 0) / 100 : 0;
      const pendingAmt = pendingItems.length > 0 ? (pendingItems[0].amount || 0) / 100 : 0;
      const currencyType = availableItems.length > 0 ? (availableItems[0].currency || "PKR").toUpperCase() : "PKR";

      if (isMounted.current) {
        setAdminWallet({
          available: Number(availableAmt),
          pending: Number(pendingAmt),
          currency: currencyType
        });
      }
    } catch (err) {
      console.error("Network or JSON Serialization Error inside Admin Wallet:", err);
    }
  }, []);

  // ----------------------------------------------------
  // FETCH FIRESTORE WALLETS DATA WITH EXTENDED FIELDS
  // ----------------------------------------------------
  const fetchWallets = useCallback(async (isMounted = { current: true }) => {
    try {
      if (isMounted.current) setLoading(true);

      const walletSnapshot = await getDocs(collection(db, "Wallets"));
      const walletList = [];
      const historyList = [];

      let restaurantTotal = 0;
      let riderTotal = 0;
      let paidTotal = 0;

      for (const walletDoc of walletSnapshot.docs) {
        if (walletDoc.id === "admin_wallet") continue;

        const walletData = walletDoc.data();
        const available = Number(walletData.availableBalance || 0);
        const pending = Number(walletData.pendingBalance || 0);

        let user = null;
        let role = "";
        let name = "";
        let phone = "";
        let email = "";
        let city = "";

        let bankName = "Not Set";
        let accountTitle = "Not Set";
        let accountNumber = "Not Available";
        let iban = "Not Available";

        // FIX: pehle default "No Stripe Account Linked" rakhte hain,
        // baad mein user document se asli value nikalenge (walletData se nahi)
        let stripeAccountId = "No Stripe Account Linked";
        let stripeOnboardingComplete = false;

        // Restaurant Registration Context Document Reference
        const restaurantRef = doc(
          db,
          "Users",
          "Restaurant",
          "VerifiedRegister",
          walletDoc.id
        );
        const restaurantSnap = await getDoc(restaurantRef);

        if (restaurantSnap.exists()) {
          user = restaurantSnap.data();
          role = "Restaurant";
          name = user.restaurantName || user.ownerName || "Restaurant";
          phone = user.phone || "";
          email = user.email || "";
          city = user.city || "";
          bankName = user.bankName || "HBL Sandbox";
          accountTitle = user.ownerName || name;
          accountNumber = user.phone || "Not Available";
          iban = user.iban || user.IBAN || "Not Available";

          // FIX: asli Stripe Account ID yahan se milegi
          if (user.stripeAccountId) {
            stripeAccountId = user.stripeAccountId;
          }
          stripeOnboardingComplete = user.stripeOnboardingComplete || false;

          restaurantTotal += available;
        }

        // Rider Registration Document Reference
        if (!user) {
          const riderRef = doc(
            db,
            "Users",
            "Delivery",
            "VerifiedRegister",
            walletDoc.id
          );
          const riderSnap = await getDoc(riderRef);

          if (riderSnap.exists()) {
            user = riderSnap.data();
            role = "Delivery";
            name = user.name || "Delivery Boy";
            phone = user.phone || "";
            email = user.email || "";
            city = user.city || "";
            bankName = user.bankName || "EasyPaisa Sandbox";
            accountTitle = user.name || "Rider Account";
            accountNumber = user.phone || "Not Available";
            iban = user.iban || user.IBAN || "Not Available";

            // FIX: asli Stripe Account ID yahan se milegi
            if (user.stripeAccountId) {
              stripeAccountId = user.stripeAccountId;
            }
            stripeOnboardingComplete = user.stripeOnboardingComplete || false;

            riderTotal += available;
          }
        }

        walletList.push({
          id: walletDoc.id,
          role,
          name,
          phone,
          email,
          city,

          available,
          pending,

          bankName,
          accountTitle,
          accountNumber,
          iban,

          stripeAccountId,
          stripeOnboardingComplete
        });

        // Subcollection Sub-History Queries
        const historySnapshot = await getDocs(
          collection(db, "Wallets", walletDoc.id, "history")
        );

        historySnapshot.forEach((item) => {
          const h = item.data();
          historyList.push({
            walletId: walletDoc.id,
            role,
            name,
            phone,
            amount: h.amount || 0,
            orderId: h.orderId || "-",
            type: h.type || "",
            date: h.date || "",
            transferId: h.transferId || "-",
            method: h.method || "manual"
          });

          if (h.type === "Paid by Admin") {
            paidTotal += Number(h.amount || 0);
          }
        });
      }

      if (isMounted.current) {
        setWallets(walletList);
        setHistory(historyList);
        setRestaurantPending(restaurantTotal);
        setRiderPending(riderTotal);
        setTotalPaid(paidTotal);
        setLoading(false);
      }
    } catch (error) {
      console.error("Error reading Firestore collection nodes:", error);
      if (isMounted.current) setLoading(false);
    }
  }, []);

  // ----------------------------------------------------
  // INITIAL LOAD AND SCHEDULER SUBSCRIPTION
  // ----------------------------------------------------
  useEffect(() => {
    const isMounted = { current: true };

    const init = async () => {
      await loadStripeBalance(isMounted);
      await fetchWallets(isMounted);
    };

    init();

    const interval = setInterval(() => {
      loadStripeBalance(isMounted);
    }, 10000);

    return () => {
      isMounted.current = false;
      clearInterval(interval);
    };
  }, [loadStripeBalance, fetchWallets]);

  // ----------------------------------------------------
  // OPEN PAYOUT SHEET TRIGGER
  // ----------------------------------------------------
  const handleOpenPayoutSheet = (wallet) => {
    setPayoutError("");
    setSelectedWallet(wallet);
    setIsModalOpen(true);
  };

  // ----------------------------------------------------
  // EXECUTE REAL TRANSFER/MUTATION LAYER
  // ----------------------------------------------------
  const handleConfirmPayout = async () => {
    if (!selectedWallet) return;

    setPayoutError("");

    const hasValidStripeAccount =
      selectedWallet.stripeAccountId &&
      selectedWallet.stripeAccountId.startsWith("acct_");

    try {
      setIsProcessing(true);

      let transferId = null;
      let method = "manual";

      // ============================================
      // AGAR REAL STRIPE CONNECTED ACCOUNT HAI,
      // TOU ASAL TRANSFER CALL KARO (Admin Stripe
      // balance real mein kam hoga)
      // ============================================
      if (hasValidStripeAccount) {
        const response = await fetch(
          "https://us-central1-paktrainfoodservice.cloudfunctions.net/payoutToPartner",
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              walletId: selectedWallet.id,
              amount: selectedWallet.available,
              stripeAccountId: selectedWallet.stripeAccountId,
              receiverType: selectedWallet.role,
              name: selectedWallet.name
            })
          }
        );

        const result = await response.json();

        if (!result.success) {
          throw new Error(result.error || "Stripe transfer failed");
        }

        transferId = result.transferId;
        method = "stripe";
      }
      // Agar valid connected account nahi hai, tou purana
      // manual flow chalta rahega (sirf Firestore record) -
      // isliye demo/testing rukta nahi.

      const walletRef = doc(db, "Wallets", selectedWallet.id);

      // Reset internal database entries
      await updateDoc(walletRef, {
        availableBalance: 0,
        pendingBalance: 0
      });

      // Insert ledger notification document
      await addDoc(
        collection(db, "Wallets", selectedWallet.id, "history"),
        {
          amount: selectedWallet.available,
          orderId: "ADMIN_STRIPE_PAYOUT",
          type: "Paid by Admin",
          role: selectedWallet.role,
          receiver: selectedWallet.name,
          phone: selectedWallet.phone,
          email: selectedWallet.email,
          city: selectedWallet.city,
          bankName: selectedWallet.bankName,
          accountTitle: selectedWallet.accountTitle,
          method,
          transferId: transferId || "N/A",
          date: new Date().toISOString(),
          timestamp: serverTimestamp()
        }
      );

      setIsModalOpen(false);
      setSelectedWallet(null);

      if (method === "stripe") {
        alert("Real Stripe sandbox transfer successful! Admin balance updated.");
      } else {
        alert("Payout recorded (manual/no Stripe account linked yet).");
      }

      // Admin ka real balance turant refresh karo (Stripe se live fetch)
      await loadStripeBalance();
      await fetchWallets();
    } catch (error) {
      console.error("Disbursement operation mutation crash:", error);
      setPayoutError(error.message || "Payment processing failure.");
      alert("Payment processing failure: " + (error.message || ""));
    } finally {
      setIsProcessing(false);
    }
  };

  // ----------------------------------------------------
  // DERIVED METRICS
  // ----------------------------------------------------
  const totalWalletBalance = wallets.reduce((sum, w) => sum + Number(w.available || 0), 0);
  const totalRestaurants = wallets.filter((w) => w.role === "Restaurant").length;
  const totalRiders = wallets.filter((w) => w.role === "Delivery").length;

  const sortedHistory = [...history].sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0));
  const sortedWallets = [...wallets].sort((a, b) => b.available - a.available);

  return (
    <div className="payments-container animate-fade-in">
      {/* MAIN VIEW HEADER */}
      <div className="payments-header">
        <div>
          <h2>Payment Management</h2>
          <p>Manage Restaurant & Rider Payments</p>
        </div>
      </div>

      {/* DASHBOARD CARD ROW COMPONENTS */}
      <div className="payments-metrics-grid">
        <div className="payment-card">
          <h3>ADMIN STRIPE WALLET</h3>
          <h2>{formatCurrency(adminWallet.available, adminWallet.currency)}</h2>
          <p style={{ marginTop: "10px" }}>
            Pending : {formatCurrency(adminWallet.pending, adminWallet.currency)}
          </p>
        </div>

        <div className="payment-card">
          <h3>RESTAURANT PENDING</h3>
          <h2>{formatCurrency(restaurantPending, "PKR")}</h2>
        </div>

        <div className="payment-card">
          <h3>RIDER PENDING</h3>
          <h2>{formatCurrency(riderPending, "PKR")}</h2>
        </div>

        <div className="payment-card">
          <h3>TOTAL PAID</h3>
          <h2>{formatCurrency(totalPaid, "PKR")}</h2>
        </div>
      </div>

      {/* ACCOUNT BALANCES DATA GRID */}
      <div className="payments-table-card">
        <h3 style={{ padding: "20px" }}>Wallets</h3>

        <table className="payments-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th>Phone</th>
              <th>Stripe Status</th>
              <th>Available Balance</th>
              <th>Pending Balance</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7">Loading...</td>
              </tr>
            ) : sortedWallets.length === 0 ? (
              <tr>
                <td colSpan="7">No Wallet Found</td>
              </tr>
            ) : (
              sortedWallets.map((wallet) => (
                <tr key={wallet.id}>
                  <td>{wallet.name}</td>
                  <td>
                    <span
                      className={
                        wallet.role === "Restaurant"
                          ? "role-badge restaurant"
                          : "role-badge rider"
                      }
                    >
                      {wallet.role}
                    </span>
                  </td>
                  <td>{wallet.phone}</td>
                  <td>
                    {wallet.stripeAccountId?.startsWith("acct_") ? (
                      <span style={{ color: "green", fontWeight: "bold" }}>
                        {wallet.stripeOnboardingComplete ? "Linked ✓" : "Pending Onboarding"}
                      </span>
                    ) : (
                      <span style={{ color: "#999" }}>Not Linked</span>
                    )}
                  </td>
                  <td className="available-balance">
                    {formatCurrency(wallet.available, "PKR")}
                  </td>
                  <td className="pending-balance">
                    {formatCurrency(wallet.pending, "PKR")}
                  </td>
                  <td>
                    <button
                      className="pay-now-btn"
                      disabled={wallet.available <= 0}
                      onClick={() => handleOpenPayoutSheet(wallet)}
                    >
                      Pay Now
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* HISTORICAL LEDGER ENTRIES */}
      <div className="payments-table-card">
        <h3 style={{ padding: "20px" }}>Payment History</h3>

        <table className="payments-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th>Amount</th>
              <th>Type</th>
              <th>Method</th>
              <th>Order</th>
              <th>Date</th>
            </tr>
          </thead>

          <tbody>
            {sortedHistory.length === 0 ? (
              <tr>
                <td colSpan="7">No History Found</td>
              </tr>
            ) : (
              sortedHistory.map((item, index) => (
                <tr key={index}>
                  <td>{item.name}</td>
                  <td>{item.role}</td>
                  <td>{formatCurrency(item.amount, "PKR")}</td>
                  <td>{item.type}</td>
                  <td>{item.method === "stripe" ? "Stripe (Real)" : "Manual"}</td>
                  <td>{item.orderId}</td>
                  <td>{item.date ? item.date.slice(0, 10) : "-"}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* DYNAMIC PAYOUT SHEET (MODAL POPUP VIEW) */}
      {isModalOpen && selectedWallet && (
        <div className="payout-modal-overlay">
          <div className="payout-modal-sheet">
            <div className="payout-modal-header">
              <h3>Execute Payout Gateway Transfer</h3>
              <button className="close-btn" onClick={() => setIsModalOpen(false)}>×</button>
            </div>

            <div className="payout-modal-body">

              <div className="modal-user-card">
                <div className="avatar">
                  {selectedWallet.name?.charAt(0)}
                </div>

                <div>
                  <h2>{selectedWallet.name}</h2>
                  <span className="role-tag">{selectedWallet.role}</span>
                </div>
              </div>

              <div className="info-grid">
                <div><b>Phone:</b> : {selectedWallet.phone}</div>
                <div><b>Email:</b> : {selectedWallet.email || "N/A"}</div>
                <div><b>City:</b> : {selectedWallet.city || "N/A"}</div>
                <div><b>Bank:</b> : {selectedWallet.bankName}</div>
                <div><b>Account Title:</b> : {selectedWallet.accountTitle}</div>
                <div><b>Account No:</b> : {selectedWallet.phone}</div>
              </div>

              {selectedWallet.stripeAccountId?.startsWith("acct_") ? (
                <div style={{ margin: "10px 0", color: "green", fontWeight: "bold" }}>
                  ✓ Stripe Connected Account Linked — Real sandbox transfer hogi
                </div>
              ) : (
                <div style={{ margin: "10px 0", color: "#a15c00", fontWeight: "bold" }}>
                  ⚠ Koi Stripe account linked nahi — sirf manual record hoga
                </div>
              )}

              <div className="balance-box">
                <span>AVAILABLE BALANCE</span>
                <h1>{formatCurrency(selectedWallet.available, "PKR")}</h1>
              </div>

              {payoutError && (
                <div style={{ color: "red", marginTop: "10px" }}>{payoutError}</div>
              )}

            </div>

            <div className="payout-modal-footer">
              <button className="cancel-action-btn" onClick={() => setIsModalOpen(false)} disabled={isProcessing}>
                Cancel
              </button>
              <button className="confirm-payout-btn" onClick={handleConfirmPayout} disabled={isProcessing}>
                {isProcessing ? "Processing Transfer..." : "Confirm Payment"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* FOOTER METRICS SUMMARY */}
      <div style={{ marginTop: "20px", fontWeight: "bold", textAlign: "right" }}>
        Total Wallets : {wallets.length}
        <br />
        Restaurants : {totalRestaurants}
        <br />
        Riders : {totalRiders}
        <br />
        Total Balance : {formatCurrency(totalWalletBalance, "PKR")}
      </div>
    </div>
  );
};

export default Payments;
