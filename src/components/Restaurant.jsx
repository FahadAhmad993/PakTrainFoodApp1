import React from 'react';
import './Restaurant.css';

const Restaurant = () => {
  return (
    <div className="restaurant-container animate-fade-in">
      {/* Header */}
      <div className="restaurant-header">
        <div>
          <h2>Restaurant Management</h2>
          <p>Manage and monitor partner restaurants across the network.</p>
        </div>
        <button className="btn-primary">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add New Restaurant
        </button>
      </div>

      {/* Metric Cards */}
      <div className="metrics-grid">
        <MetricCard 
          title="Total Partners" 
          value="1,284" 
          trend="↑ 12% from last month" 
          trendClass="text-green-600" 
          icon="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" 
          iconColor="#1e3a8a" 
        />
        <MetricCard 
          title="Active Now" 
          value="842" 
          trend="Real-time status" 
          trendClass="text-gray-500" 
          icon="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" 
          iconColor="#16a34a" 
        />
        <MetricCard 
          title="Avg. Rating" 
          value="4.8" 
          trend="Across all locations" 
          trendClass="text-gray-500" 
          icon="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" 
          iconColor="#ea580c" 
        />
        <MetricCard 
          title="Pending Review" 
          value="18" 
          trend="Requires attention" 
          trendClass="text-red-600" 
          icon="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" 
          iconColor="#dc2626" 
        />
      </div>

      {/* Table Section */}
      <div className="restaurant-table-card">
        {/* Table Toolbar */}
        <div className="table-toolbar">
          <div className="search-bar">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input type="text" placeholder="Search by name, ID or location..." />
          </div>
          <div className="filter-actions">
            <button className="filter-btn">All Status <svg viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" /></svg></button>
            <button className="filter-btn">Sort by Rating <svg viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" /></svg></button>
            <button className="icon-filter-btn">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
              </svg>
            </button>
          </div>
        </div>

        {/* Table */}
        <div className="table-responsive">
          <table className="rest-table">
            <thead>
              <tr>
                <th>RESTAURANT NAME</th>
                <th>LOCATION</th>
                <th>STATUS</th>
                <th>RATING</th>
                <th className="text-right">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              <TableRow 
                avatar="BG" avatarBg="#eff6ff" avatarColor="#1d4ed8"
                name="Bistro Gardenia" id="ID: PK-RES-9041"
                location="Lahore, Gulberg III" subLocation="Sector 12, Main Blvd"
                status="Active" statusClass="status-active"
                rating="4.9" ratingCount="(1.2k)"
              />
              <TableRow 
                avatar="SH" avatarBg="#fff7ed" avatarColor="#c2410c"
                name="Spicy Hub" id="ID: PK-RES-3321"
                location="Karachi, DHA Phase 6" subLocation="Street 15, Khayaban-e-Ittehad"
                status="Inactive" statusClass="status-inactive"
                rating="4.2" ratingCount="(450)"
              />
              <TableRow 
                avatar="UM" avatarBg="#eff6ff" avatarColor="#1d4ed8"
                name="Urban Munchies" id="ID: PK-RES-1182"
                location="Islamabad, F-7 Markaz" subLocation="Shop 44, Blue Area"
                status="Active" statusClass="status-active"
                rating="4.7" ratingCount="(892)"
              />
              <TableRow 
                avatar="TL" avatarBg="#faf5ff" avatarColor="#7e22ce"
                name="The Local Hub" id="ID: PK-RES-0043"
                location="Multan, Gulgasht" subLocation="Block A, Commercial Area"
                status="Pending" statusClass="status-pending"
                rating="0.0" ratingCount="(0)"
              />
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="table-footer-pagination">
          <span className="showing-text">Showing 1 to 10 of 1,284 restaurants</span>
          <div className="pagination-controls">
            <button className="page-btn text-btn">Previous</button>
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <span className="page-dots">...</span>
            <button className="page-btn">129</button>
            <button className="page-btn text-btn">Next</button>
          </div>
        </div>
      </div>

      {/* Bottom Banners */}
      <div className="bottom-banners">
        {/* Expansion Banner */}
        <div className="expansion-banner">
          <div className="banner-content">
            <h3>Expansion Opportunity</h3>
            <p>Our analytics show a high demand for Italian cuisine in the South District. Consider onboarding more partners in that region.</p>
            <button className="btn-success">View Heatmap</button>
          </div>
        </div>

        {/* Performance Card */}
        <div className="performance-card">
          <div className="perf-icon">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
          <h3>Weekly Performance</h3>
          <p>Overall network efficiency is up by 4.2% this week compared to the previous cycle.</p>
          <div className="progress-bar-container">
            <div className="progress-track">
              <div className="progress-fill" style={{ width: '85%' }}></div>
            </div>
            <span className="progress-text">GROWTH TARGET: 85%</span>
          </div>
        </div>
      </div>
    </div>
  );
};

const MetricCard = ({ title, value, trend, trendClass, icon, iconColor }) => (
  <div className="rest-metric-card">
    <div className="metric-info">
      <p className="metric-title">{title}</p>
      <h4 className="metric-value">{value}</h4>
      <p className={`metric-trend ${trendClass}`}>{trend}</p>
    </div>
    <svg className="metric-icon" viewBox="0 0 24 24" fill="none" stroke={iconColor}>
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={icon} />
    </svg>
  </div>
);

const TableRow = ({ avatar, avatarBg, avatarColor, name, id, location, subLocation, status, statusClass, rating, ratingCount }) => (
  <tr>
    <td>
      <div className="flex-cell">
        <div className="avatar" style={{ backgroundColor: avatarBg, color: avatarColor }}>{avatar}</div>
        <div>
          <p className="primary-text">{name}</p>
          <p className="secondary-text">{id}</p>
        </div>
      </div>
    </td>
    <td>
      <p className="primary-text">{location}</p>
      <p className="secondary-text">{subLocation}</p>
    </td>
    <td>
      <span className={`status-badge ${statusClass}`}>
        <span className="dot"></span>
        {status}
      </span>
    </td>
    <td>
      <div className="rating-cell">
        <span className="rating-score">{rating}</span>
        <svg viewBox="0 0 20 20" fill="currentColor" className={rating > 0 ? 'star-filled' : 'star-empty'}>
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
        <span className="rating-count">{ratingCount}</span>
      </div>
    </td>
    <td className="text-right">
      <div className="action-buttons">
        <button className="btn-view">View</button>
        <button className="btn-delete">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </td>
  </tr>
);

export default Restaurant;
