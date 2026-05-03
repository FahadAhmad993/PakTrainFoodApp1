import React, { useState } from 'react';
import './Riders.css';

const Riders = () => {
  // State for riders data so we can update statuses
  const [riders, setRiders] = useState([
    {
      id: "PT-90210",
      name: "Marcus Holloway",
      avatar: "https://randomuser.me/api/portraits/men/32.jpg",
      phone: "+1 (555) 012-3456",
      vehicle: "Electric Bike",
      vehicleIcon: "M11.5 12.5l2 2m0 0l2-2m-2 2V9.5m-5 3v-3M3 10.5A2.5 2.5 0 105.5 8 2.5 2.5 0 003 10.5z",
      status: "Pending", // Changed to Pending so we can approve/reject
      statusClass: "status-pending"
    },
    {
      id: "PT-88129",
      name: "Sarah Chen",
      avatar: "https://randomuser.me/api/portraits/women/44.jpg",
      phone: "+1 (555) 432-1098",
      vehicle: "Compact Van",
      vehicleIcon: "M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4",
      status: "On-Delivery",
      statusClass: "status-delivery"
    },
    {
      id: "PT-11204",
      name: "James Wilson",
      avatar: "https://randomuser.me/api/portraits/men/85.jpg",
      phone: "+1 (555) 987-6543",
      vehicle: "Scooter",
      vehicleIcon: "M13 10V3L4 14h7v7l9-11h-7z",
      status: "Pending",
      statusClass: "status-pending"
    },
    {
      id: "PT-44501",
      name: "Elena Rodriguez",
      avatar: "https://randomuser.me/api/portraits/women/68.jpg",
      phone: "+1 (555) 777-8888",
      vehicle: "Electric Bike",
      vehicleIcon: "M11.5 12.5l2 2m0 0l2-2m-2 2V9.5m-5 3v-3M3 10.5A2.5 2.5 0 105.5 8 2.5 2.5 0 003 10.5z",
      status: "Online",
      statusClass: "status-online"
    },
    {
      id: "PT-22345",
      name: "David Kim",
      avatar: "https://randomuser.me/api/portraits/men/22.jpg",
      phone: "+1 (555) 222-3333",
      vehicle: "Hatchback",
      vehicleIcon: "M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm14 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1z",
      status: "On-Delivery",
      statusClass: "status-delivery"
    }
  ]);

  const [selectedRider, setSelectedRider] = useState(null);

  const handleApprove = (id) => {
    setRiders(riders.map(r => r.id === id ? { ...r, status: "Approved", statusClass: "status-online" } : r));
    setSelectedRider(null);
  };

  const handleReject = (id) => {
    setRiders(riders.map(r => r.id === id ? { ...r, status: "Rejected", statusClass: "status-offline" } : r));
    setSelectedRider(null);
  };
  return (
    <div className="riders-container animate-fade-in">
      {/* Header */}
      <div className="riders-header">
        <div>
          <h2>Delivery Riders</h2>
          <p>Manage your logistics fleet and monitor real-time rider status.</p>
        </div>
        <button className="btn-primary">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add New Rider
        </button>
      </div>

      {/* Metric Cards */}
      <div className="metrics-grid">
        <MetricCard 
          title="Total Riders" 
          value="1,284" 
          trend="↑ 12% from last month" 
          trendClass="text-green-600" 
          icon="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" 
          iconColor="#1e3a8a" 
        />
        <MetricCard 
          title="Online Now" 
          value="452" 
          trend="Currently active on the road" 
          trendClass="text-gray-500" 
          icon="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" 
          iconColor="#16a34a" 
        />
        <MetricCard 
          title="Avg. Delivery Time" 
          value="24.5 min" 
          trend="↘ -3% improvement" 
          trendClass="text-green-600" 
          icon="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" 
          iconColor="#ea580c" 
        />
        <MetricCard 
          title="Active Deliveries" 
          value="318" 
          trend="Packages currently in transit" 
          trendClass="text-gray-500" 
          icon="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" 
          iconColor="#2563eb" 
        />
      </div>

      {/* Directory Table */}
      <div className="riders-table-card">
        {/* Table Toolbar */}
        <div className="table-toolbar">
          <h3>Personnel Directory</h3>
          <div className="filter-actions">
            <button className="filter-btn">
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4"><path fillRule="evenodd" d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0 018 17v-5.586L3.293 6.707A1 1 0 013 6V3z" clipRule="evenodd" /></svg>
              Filter
            </button>
            <button className="filter-btn">
              <svg viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4"><path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" /></svg>
              Export
            </button>
          </div>
        </div>

        {/* Table */}
        <div className="table-responsive">
          <table className="riders-table">
            <thead>
              <tr>
                <th>RIDER NAME</th>
                <th>PHONE</th>
                <th>VEHICLE TYPE</th>
                <th>CURRENT STATUS</th>
                <th className="text-right">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {riders.map((rider) => (
                <TableRow 
                  key={rider.id}
                  avatar={rider.avatar}
                  name={rider.name} 
                  id={`ID: ${rider.id}`}
                  phone={rider.phone}
                  vehicle={rider.vehicle} 
                  vehicleIcon={rider.vehicleIcon}
                  status={rider.status} 
                  statusClass={rider.statusClass}
                  onView={() => setSelectedRider(rider)}
                />
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="table-footer-pagination">
          <span className="showing-text">Showing 1 to 5 of 1,284 riders</span>
          <div className="pagination-controls">
            <button className="page-btn-icon"><svg viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" /></svg></button>
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <span className="page-dots">...</span>
            <button className="page-btn-icon"><svg viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" /></svg></button>
          </div>
        </div>
      </div>

      {/* Bottom Banners */}
      <div className="bottom-banners">
        {/* Map Card */}
        <div className="riders-map-card">
          <div className="map-card-content">
            <h3>Real-time Map</h3>
            <p>Monitor all active riders in the metro area live.</p>
            <button className="btn-white">Open Satellite View</button>
          </div>
          <div className="map-bg-pattern">
             <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
               <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={0.5} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
             </svg>
          </div>
        </div>
      </div>

      {/* Rider Details Modal */}
      {selectedRider && (
        <div className="modal-overlay" onClick={() => setSelectedRider(null)}>
          <div className="modal-content animate-fade-in" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Rider Details</h3>
              <button className="close-btn" onClick={() => setSelectedRider(null)}>
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            
            <div className="modal-body">
              <div className="modal-profile">
                <img src={selectedRider.avatar} alt={selectedRider.name} className="modal-avatar" />
                <div>
                  <h2>{selectedRider.name}</h2>
                  <p>ID: {selectedRider.id}</p>
                </div>
              </div>
              
              <div className="modal-details-grid">
                <div className="detail-item">
                  <span>Phone Number</span>
                  <p>{selectedRider.phone}</p>
                </div>
                <div className="detail-item">
                  <span>Vehicle Type</span>
                  <p>{selectedRider.vehicle}</p>
                </div>
                <div className="detail-item">
                  <span>Current Status</span>
                  <span className={`status-badge ${selectedRider.statusClass}`} style={{ marginTop: '4px' }}>
                    <span className="dot"></span>
                    {selectedRider.status}
                  </span>
                </div>
                <div className="detail-item">
                  <span>Join Date</span>
                  <p>Oct 15, 2023</p>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button 
                className="btn-reject" 
                onClick={() => handleReject(selectedRider.id)}
              >
                Reject Rider
              </button>
              <button 
                className="btn-approve" 
                onClick={() => handleApprove(selectedRider.id)}
              >
                Approve Rider
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const MetricCard = ({ title, value, trend, trendClass, icon, iconColor }) => (
  <div className="riders-metric-card">
    <div className="metric-info">
      <p className="metric-title">{title}</p>
      <h4 className="metric-value">{value}</h4>
      <p className={`metric-trend ${trendClass}`}>{trend}</p>
    </div>
    <div className="metric-icon-circle">
      <svg className="metric-icon" viewBox="0 0 24 24" fill="none" stroke={iconColor}>
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={icon} />
      </svg>
    </div>
  </div>
);

const TableRow = ({ avatar, name, id, phone, vehicle, vehicleIcon, status, statusClass, onView }) => (
  <tr>
    <td>
      <div className="flex-cell">
        <img src={avatar} alt={name} className="rider-avatar" />
        <div>
          <p className="primary-text">{name}</p>
          <p className="secondary-text">{id}</p>
        </div>
      </div>
    </td>
    <td>
      <p className="primary-text">{phone}</p>
    </td>
    <td>
      <div className="vehicle-cell">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
           <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={vehicleIcon} />
        </svg>
        <span className="primary-text">{vehicle}</span>
      </div>
    </td>
    <td>
      <span className={`status-badge ${statusClass}`}>
        <span className="dot"></span>
        {status}
      </span>
    </td>
    <td className="text-right">
      <button className="btn-view-details" onClick={onView}>
        View Details
      </button>
    </td>
  </tr>
);

export default Riders;
