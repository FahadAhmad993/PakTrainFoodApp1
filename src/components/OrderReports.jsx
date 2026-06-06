import { useState } from 'react';
import './OrderReports.css';

const orders = [
  { id: '#ORD-5041', customer: 'Ahmed Hassan', customerAvatar: 'AH', avatarBg: '#e0e7ff', avatarColor: '#4f46e5', restaurant: 'Bistro Gardenia', rider: 'Marcus Holloway', amount: '$45.50', status: 'Delivered', statusClass: 'status-delivered', date: 'Oct 24, 2023 14:32' },
  { id: '#ORD-5042', customer: 'Sara Khan', customerAvatar: 'SK', avatarBg: '#fce7f3', avatarColor: '#db2777', restaurant: 'Spicy Hub', rider: 'Sarah Chen', amount: '$28.00', status: 'In Transit', statusClass: 'status-transit', date: 'Oct 24, 2023 13:15' },
  { id: '#ORD-5043', customer: 'M. Zaid', customerAvatar: 'MZ', avatarBg: '#d1fae5', avatarColor: '#059669', restaurant: 'Urban Munchies', rider: 'James Wilson', amount: '$62.10', status: 'Pending', statusClass: 'status-pending', date: 'Oct 24, 2023 12:45' },
  { id: '#ORD-5044', customer: 'Fatima Ali', customerAvatar: 'FA', avatarBg: '#fef3c7', avatarColor: '#d97706', restaurant: 'The Local Hub', rider: 'Elena Rodriguez', amount: '$112.00', status: 'Cancelled', statusClass: 'status-cancelled', date: 'Oct 24, 2023 11:20' },
  { id: '#ORD-5045', customer: 'Usman Raza', customerAvatar: 'UR', avatarBg: '#e0e7ff', avatarColor: '#4f46e5', restaurant: 'Bistro Gardenia', rider: 'David Kim', amount: '$35.75', status: 'Delivered', statusClass: 'status-delivered', date: 'Oct 24, 2023 10:55' },
  { id: '#ORD-5046', customer: 'Hina Malik', customerAvatar: 'HM', avatarBg: '#fce7f3', avatarColor: '#db2777', restaurant: 'Spicy Hub', rider: 'Marcus Holloway', amount: '$58.20', status: 'Delivered', statusClass: 'status-delivered', date: 'Oct 24, 2023 10:10' },
  { id: '#ORD-5047', customer: 'Bilal Ahmed', customerAvatar: 'BA', avatarBg: '#d1fae5', avatarColor: '#059669', restaurant: 'Urban Munchies', rider: 'Sarah Chen', amount: '$19.99', status: 'In Transit', statusClass: 'status-transit', date: 'Oct 24, 2023 09:45' },
];

const OrderReports = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');

  const filtered = orders.filter(o => {
    const matchSearch = o.customer.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.restaurant.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'All' || o.status === statusFilter;
    return matchSearch && matchStatus;
  });

  return (
    <div className="reports-container animate-fade-in">
      {/* Header */}
      <div className="reports-header">
        <div>
          <h2>Order Reports</h2>
          <p>Full breakdown of orders, deliveries, and transaction records.</p>
        </div>
        <div className="header-actions">
          <button className="btn-secondary">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
            </svg>
            Filter
          </button>
          <button className="btn-primary">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            Export Report
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="report-metrics-grid">
        <SummaryCard title="Total Orders" value="5,284" icon="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" iconColor="#4f46e5" bg="#e0e7ff" trend="+8.2% this month" trendPos={true} />
        <SummaryCard title="Delivered" value="4,198" icon="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" iconColor="#059669" bg="#d1fae5" trend="+5.1% this month" trendPos={true} />
        <SummaryCard title="Pending / In Transit" value="842" icon="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" iconColor="#d97706" bg="#fef3c7" trend="Real-time status" trendPos={null} />
        <SummaryCard title="Cancelled" value="244" icon="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" iconColor="#dc2626" bg="#fee2e2" trend="-2.3% improvement" trendPos={true} />
      </div>

      {/* Orders Table */}
      <div className="reports-table-card">
        {/* Toolbar */}
        <div className="table-toolbar">
          <div className="search-bar">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search by order ID, customer, or restaurant..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="status-tabs">
            {['All', 'Delivered', 'In Transit', 'Pending', 'Cancelled'].map(s => (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                className={`tab-btn ${statusFilter === s ? 'active' : ''}`}
              >
                {s}
              </button>
            ))}
          </div>
        </div>

        {/* Table */}
        <div className="table-responsive">
          <table className="orders-report-table">
            <thead>
              <tr>
                <th>ORDER ID</th>
                <th>CUSTOMER</th>
                <th>RESTAURANT</th>
                <th>RIDER</th>
                <th>AMOUNT</th>
                <th>STATUS</th>
                <th>DATE</th>
                <th className="text-right">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((order) => (
                <tr key={order.id}>
                  <td className="order-id">{order.id}</td>
                  <td>
                    <div className="flex-cell">
                      <div className="customer-avatar" style={{ backgroundColor: order.avatarBg, color: order.avatarColor }}>
                        {order.customerAvatar}
                      </div>
                      <span className="primary-text">{order.customer}</span>
                    </div>
                  </td>
                  <td className="secondary-text">{order.restaurant}</td>
                  <td className="secondary-text">{order.rider}</td>
                  <td className="font-semibold">{order.amount}</td>
                  <td>
                    <span className={`order-badge ${order.statusClass}`}>
                      <span className="dot"></span>
                      {order.status}
                    </span>
                  </td>
                  <td className="secondary-text">{order.date}</td>
                  <td className="text-right">
                    <button className="btn-view">View</button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={8} className="empty-row">No orders match your filter.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Footer */}
        <div className="table-footer-pagination">
          <span className="showing-text">Showing {filtered.length} of {orders.length} orders</span>
          <div className="pagination-controls">
            <button className="page-btn text-btn">Previous</button>
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <span className="page-dots">...</span>
            <button className="page-btn">24</button>
            <button className="page-btn text-btn">Next</button>
          </div>
        </div>
      </div>
    </div>
  );
};

const SummaryCard = ({ title, value, icon, iconColor, bg, trend, trendPos }) => (
  <div className="summary-card">
    <div className="summary-info">
      <p className="summary-title">{title}</p>
      <h4 className="summary-value">{value}</h4>
      <p className={`summary-trend ${trendPos === true ? 'text-green' : trendPos === false ? 'text-red' : 'text-gray'}`}>
        {trend}
      </p>
    </div>
    <div className="summary-icon" style={{ backgroundColor: bg }}>
      <svg viewBox="0 0 24 24" fill="none" stroke={iconColor}>
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={icon} />
      </svg>
    </div>
  </div>
);

export default OrderReports;
