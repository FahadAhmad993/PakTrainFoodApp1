import { useState } from 'react';
import './Payments.css';

const Payments = () => {
  const [transactions] = useState([
    {
      id: "TX-402",
      date: "Oct 24, 2023 14:32",
      entity: "Burger King #402",
      entityType: "Restaurant",
      avatar: "BK",
      avatarBg: "#e0e7ff",
      avatarColor: "#4f46e5",
      isAvatarImage: false,
      amount: "$1,240.00",
      type: "Payout",
      status: "COMPLETED",
      statusClass: "status-completed"
    },
    {
      id: "TX-403",
      date: "Oct 24, 2023 13:15",
      entity: "Michael Scott",
      entityType: "Delivery Rider",
      avatar: "https://randomuser.me/api/portraits/men/32.jpg",
      isAvatarImage: true,
      amount: "$45.50",
      type: "Commission",
      status: "PENDING",
      statusClass: "status-pending"
    },
    {
      id: "TX-404",
      date: "Oct 24, 2023 12:45",
      entity: "Papa Pancho's",
      entityType: "Restaurant",
      avatar: "PP",
      avatarBg: "#e0e7ff",
      avatarColor: "#4f46e5",
      isAvatarImage: false,
      amount: "$892.20",
      type: "Payout",
      status: "IN TRANSIT",
      statusClass: "status-transit"
    },
    {
      id: "TX-405",
      date: "Oct 24, 2023 11:20",
      entity: "James Wilson",
      entityType: "Delivery Rider",
      avatar: "https://randomuser.me/api/portraits/men/85.jpg",
      isAvatarImage: true,
      amount: "$120.00",
      type: "Commission",
      status: "DELAYED",
      statusClass: "status-delayed"
    }
  ]);

  return (
    <div className="payments-container animate-fade-in">
      {/* Header */}
      <div className="payments-header">
        <div>
          <h2>Payment Management</h2>
          <p>Monitor cash flows, settlements, and payout efficiency.</p>
        </div>
        <button className="btn-primary">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-5 h-5 mr-2">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Export Ledger
        </button>
      </div>

      {/* Metric Cards */}
      <div className="payments-metrics-grid">
        {/* Ecosystem Volume */}
        <div className="payment-card volume-card">
          <div className="card-header">
            <div className="header-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <span>TOTAL ECOSYSTEM VOLUME</span>
            </div>
          </div>
          <div className="card-body">
            <h3>$1,284,590.00</h3>
            <span className="trend-text">
              <svg viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z" clipRule="evenodd" /></svg>
              +12.4% from last month
            </span>
          </div>
          <div className="volume-sub-metrics">
            <div className="sub-metric">
              <span>Commissions</span>
              <h4>$192,688</h4>
            </div>
            <div className="sub-metric">
              <span>Logistics Fees</span>
              <h4>$84,120</h4>
            </div>
          </div>
        </div>

        {/* Pending Payouts */}
        <div className="payment-card pending-card">
          <div className="card-header">
            <div className="header-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>PENDING PAYOUTS</span>
            </div>
            <span className="badge-urgent">URGENT</span>
          </div>
          <div className="card-body split-body">
            <h3>$42,350.50</h3>
            <span className="entities-text">128 Entities</span>
          </div>
          <div className="progress-container">
            <div className="progress-bar red-progress" style={{width: '75%'}}></div>
          </div>
          <p className="card-description">
            These funds are scheduled for disbursement in the next 24-hour cycle. 12 payouts require manual verification.
          </p>
          <button className="btn-outline-full">Verify All Pending</button>
        </div>

        {/* Success Rate */}
        <div className="payment-card success-card">
          <div className="card-header">
            <div className="header-title text-white">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>SUCCESS RATE</span>
            </div>
          </div>
          <div className="card-body">
            <h3 className="text-white">99.8%</h3>
            <span className="success-desc">Payment processing reliability</span>
          </div>
          <div className="mini-bar-chart">
            <div className="chart-bar" style={{height: '30%', opacity: 0.4}}></div>
            <div className="chart-bar" style={{height: '40%', opacity: 0.5}}></div>
            <div className="chart-bar" style={{height: '60%', opacity: 0.6}}></div>
            <div className="chart-bar" style={{height: '50%', opacity: 0.7}}></div>
            <div className="chart-bar" style={{height: '80%', opacity: 0.8}}></div>
            <div className="chart-bar" style={{height: '100%', opacity: 1}}></div>
          </div>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="payments-table-card">
        {/* Toolbar */}
        <div className="table-toolbar">
          <div className="filter-group">
            <button className="filter-dropdown">
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 mr-2"><path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" /></svg>
              Last 30 Days
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 ml-1"><path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" /></svg>
            </button>
            <button className="filter-dropdown">
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 mr-2"><path fillRule="evenodd" d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0 018 17v-5.586L3.293 6.707A1 1 0 013 6V3z" clipRule="evenodd" /></svg>
              Status: All
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 ml-1"><path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" /></svg>
            </button>
            <button className="filter-dropdown">
              Type: All
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 ml-1"><path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" /></svg>
            </button>
          </div>
          <span className="showing-text">Showing 432 transactions</span>
        </div>

        {/* Table */}
        <div className="table-responsive">
          <table className="payments-table">
            <thead>
              <tr>
                <th>DATE</th>
                <th>ENTITY</th>
                <th>AMOUNT</th>
                <th>TYPE</th>
                <th>STATUS</th>
                <th className="text-right">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.id}>
                  <td className="primary-text">{tx.date}</td>
                  <td>
                    <div className="flex-cell">
                      {tx.isAvatarImage ? (
                        <img src={tx.avatar} alt={tx.entity} className="entity-avatar" />
                      ) : (
                        <div className="entity-initials" style={{backgroundColor: tx.avatarBg, color: tx.avatarColor}}>{tx.avatar}</div>
                      )}
                      <div>
                        <p className="primary-text">{tx.entity}</p>
                        <p className="secondary-text">{tx.entityType}</p>
                      </div>
                    </div>
                  </td>
                  <td className="font-semibold text-dark">{tx.amount}</td>
                  <td>
                    <div className="type-cell">
                      {tx.type === 'Payout' ? (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                      )}
                      {tx.type}
                    </div>
                  </td>
                  <td>
                    <span className={`payment-badge ${tx.statusClass}`}>
                      {tx.status}
                    </span>
                  </td>
                  <td className="text-right">
                    <button className="btn-icon-only">
                      <svg viewBox="0 0 20 20" fill="currentColor">
                        <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                      </svg>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="table-footer-pagination">
          <button className="page-btn text-btn">Previous</button>
          <div className="pagination-controls">
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <span className="page-dots">...</span>
            <button className="page-btn">12</button>
          </div>
          <button className="page-btn text-btn">Next</button>
        </div>
      </div>
    </div>
  );
};

export default Payments;
