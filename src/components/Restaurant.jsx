import { useEffect, useRef, useState } from 'react';
import {
  collection,
  onSnapshot,
  orderBy,
  query,
  addDoc,
  doc,
  updateDoc,
  serverTimestamp
} from 'firebase/firestore';
import { db } from '../firebase/config';
import { formatDate, getPendingCount, getStatusClass, getStatusBadgeLabel } from '../utils/verificationHelpers';
import './Restaurant.css';

const DEFAULT_LOGO = 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=600&q=80';
const DEFAULT_DOC = 'https://images.unsplash.com/photo-1521791136064-7986c2920216?auto=format&fit=crop&w=600&q=80';
const RESTAURANT_VERIFICATION_LOGO = 'https://images.unsplash.com/photo-1517242023851-130b586a2e8a?auto=format&fit=crop&w=200&q=80';

const Restaurant = () => {
  const [activeTab, setActiveTab] = useState('Active Partners');
  const [requests, setRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const seededRef = useRef(false);

  useEffect(() => {
    const verificationsRef = collection(db, 'restaurant_verifications');
    const q = query(verificationsRef, orderBy('createdAt', 'desc'));

    const unsubscribe = onSnapshot(
      q,
      async (snapshot) => {
        const items = snapshot.docs.map((docSnap) => ({ id: docSnap.id, ...docSnap.data() }));

        if (items.length === 0 && !seededRef.current) {
          seededRef.current = true;
          try {
            await seedRestaurantVerifications();
          } catch (seedError) {
            setError(seedError.message || 'Could not seed restaurant verification data.');
            setIsLoading(false);
          }
          return;
        }

        setRequests(items);
        setIsLoading(false);
      },
      (listenError) => {
        setError(listenError.message || 'Failed to load restaurant verifications.');
        setIsLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const pendingCount = getPendingCount(requests);

  const handleApprove = async (requestId) => {
    try {
      await updateDoc(doc(db, 'restaurant_verifications', requestId), {
        status: 'Approved',
        verified: true
      });
      setSelectedRequest(null);
    } catch (approveError) {
      setError(approveError.message || 'Could not approve request.');
    }
  };

  const handleReject = async (requestId) => {
    try {
      await updateDoc(doc(db, 'restaurant_verifications', requestId), {
        status: 'Rejected',
        verified: false
      });
      setSelectedRequest(null);
    } catch (rejectError) {
      setError(rejectError.message || 'Could not reject request.');
    }
  };

  const renderRequestStatus = (status) => {
    const badgeClass = getStatusClass(status);
    return (
      <span className={`status-badge ${badgeClass}`}>
        <span className="dot"></span>
        {getStatusBadgeLabel(status)}
      </span>
    );
  };

  const getVerificationSummary = (status) => {
    switch (status) {
      case 'Approved':
        return 'This restaurant is verified and ready to onboard.';
      case 'Rejected':
        return 'This restaurant is not verified and requires reevaluation.';
      default:
        return 'This restaurant verification request is pending review.';
    }
  };

  const renderVerificationTab = () => (
    <div className="verification-panel">
      <div className="verification-header">
        <div className="verification-brand">
          <img src={RESTAURANT_VERIFICATION_LOGO} alt="Restaurant verification logo" className="verification-logo-image" />
          <div>
            <h2>Restaurant Verification Requests</h2>
            <p>Review submitted restaurant verification flows and approve or reject them in real time.</p>
          </div>
        </div>
        <div className="verification-actions">
          <div className="pending-badge">Pending: {pendingCount}</div>
          <button className="btn-primary" onClick={() => setActiveTab('Active Partners')}>
            Back to Active Partners
          </button>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {isLoading ? (
        <div className="status-message">Loading verification requests...</div>
      ) : (
        <div className="table-card verification-table-card">
          <div className="table-responsive">
            <table className="rest-table">
              <thead>
                <tr>
                  <th>RESTAURANT</th>
                  <th>OWNER</th>
                  <th>CUISINE</th>
                  <th>STATUS</th>
                  <th>SUBMITTED</th>
                  <th className="text-right">ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request) => (
                  <tr key={request.id}>
                    <td>
                      <div className="flex-cell">
                        <div className="verification-logo" style={{ backgroundImage: `url(${request.logoImage || DEFAULT_LOGO})` }}></div>
                        <div>
                          <p className="primary-text">{request.restaurantName}</p>
                          <p className="secondary-text">{request.address}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <p className="primary-text">{request.ownerName}</p>
                      <p className="secondary-text">{request.email}</p>
                    </td>
                    <td>{request.cuisine}</td>
                    <td>{renderRequestStatus(request.status)}</td>
                    <td>{formatDate(request.createdAt)}</td>
                    <td className="text-right">
                      <div className="action-buttons">
                        <button
                          className="btn-reject-small"
                          onClick={() => handleReject(request.id)}
                          disabled={request.status && request.status !== 'Pending'}
                          title={request.status && request.status !== 'Pending' ? 'Action disabled for non-pending' : 'Reject'}
                        >
                          Reject
                        </button>
                        <button
                          className="btn-approve-small"
                          onClick={() => handleApprove(request.id)}
                          disabled={request.status && request.status !== 'Pending'}
                          title={request.status && request.status !== 'Pending' ? 'Action disabled for non-pending' : 'Approve'}
                        >
                          Approve
                        </button>
                        <button className="btn-view-details" onClick={() => setSelectedRequest(request)}>
                          View Details
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedRequest && (
        <div className="modal-overlay" onClick={() => setSelectedRequest(null)}>
          <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3>Verification Details</h3>
                <p>Review this restaurant verification submission before approving or rejecting.</p>
              </div>
              <button className="close-btn" onClick={() => setSelectedRequest(null)}>
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="entity-summary">
              <div className="entity-logo" style={{ backgroundImage: `url(${selectedRequest.logoImage || DEFAULT_LOGO})` }} />
              <div>
                <p className="entity-label">Restaurant Verification</p>
                <h4>{selectedRequest.restaurantName}</h4>
                <p className="entity-note">{selectedRequest.address}</p>
                <div className="status-summary">
                  {renderRequestStatus(selectedRequest.status)}
                  <p>{getVerificationSummary(selectedRequest.status)}</p>
                </div>
              </div>
            </div>

            <div className="modal-body modal-grid two-column">
              <div className="detail-card">
                <h4>Restaurant Information</h4>
                <div className="detail-item">
                  <span>Restaurant Name</span>
                  <p>{selectedRequest.restaurantName}</p>
                </div>
                <div className="detail-item">
                  <span>Address</span>
                  <p>{selectedRequest.address}</p>
                </div>
                <div className="detail-item">
                  <span>Cuisine</span>
                  <p>{selectedRequest.cuisine}</p>
                </div>
                <div className="detail-item">
                  <span>Operating Hours</span>
                  <p>{selectedRequest.operatingHours}</p>
                </div>
                <div className="detail-item">
                  <span>Registration</span>
                  <p>NTN: {selectedRequest.NTN}</p>
                  <p>CNIC: {selectedRequest.CNIC}</p>
                </div>
              </div>

              <div className="detail-card">
                <h4>Owner & Business Details</h4>
                <div className="detail-item">
                  <span>Owner Name</span>
                  <p>{selectedRequest.ownerName}</p>
                </div>
                <div className="detail-item">
                  <span>Phone</span>
                  <p>{selectedRequest.phone}</p>
                </div>
                <div className="detail-item">
                  <span>Email</span>
                  <p>{selectedRequest.email}</p>
                </div>
                <div className="detail-item">
                  <span>Status</span>
                  {renderRequestStatus(selectedRequest.status)}
                </div>
              </div>

              <div className="detail-card">
                <h4>Financial Settlement</h4>
                <div className="detail-item">
                  <span>Bank Name</span>
                  <p>{selectedRequest.bankName}</p>
                </div>
                <div className="detail-item">
                  <span>Account Title</span>
                  <p>{selectedRequest.accountTitle}</p>
                </div>
                <div className="detail-item">
                  <span>IBAN</span>
                  <p>{selectedRequest.IBAN}</p>
                </div>
              </div>

              <div className="detail-card">
                <h4>Verification Documents</h4>
                <div className="document-grid">
                  <div className="document-card">
                    <span>Restaurant Logo</span>
                    <img src={selectedRequest.logoImage || DEFAULT_LOGO} alt="Logo" />
                  </div>
                  <div className="document-card">
                    <span>CNIC Image</span>
                    <img src={selectedRequest.CNICImage || DEFAULT_DOC} alt="CNIC" />
                  </div>
                  <div className="document-card">
                    <span>Trade License</span>
                    <img src={selectedRequest.tradeLicenseImage || DEFAULT_DOC} alt="Trade License" />
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-reject btn-secondary" onClick={() => handleReject(selectedRequest.id)}>
                Mark as Not Verified
              </button>
              <button className="btn-approve" onClick={() => handleApprove(selectedRequest.id)}>
                Mark as Verified
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <div className="restaurant-container animate-fade-in">
      <div className="section-tabs">
        <button
          className={`tab-button ${activeTab === 'Active Partners' ? 'active' : ''}`}
          onClick={() => setActiveTab('Active Partners')}
        >
          <span className="tab-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 7h18M12 3v18" />
              <path d="M6 11a4 4 0 108 0" />
            </svg>
          </span>
          <span>Active Partners</span>
        </button>
        <button
          className={`tab-button ${activeTab === 'Verification Requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('Verification Requests')}
        >
          <span className="tab-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 12l2 2 4-4" />
              <path d="M21 12v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6" />
              <path d="M7 6V4a2 2 0 012-2h6a2 2 0 012 2v2" />
            </svg>
          </span>
          <span>Verification Requests</span>
          <span className="pending-tab-badge">{pendingCount}</span>
        </button>
      </div>

      {activeTab === 'Active Partners' ? (
        <>
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
              value={pendingCount.toString()}
              trend="Requires attention"
              trendClass="text-red-600"
              icon="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
              iconColor="#dc2626"
            />
          </div>

          <div className="restaurant-table-card">
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
                    avatar="https://images.unsplash.com/photo-1541542684-6f0fd3f2a4d9?auto=format&fit=crop&w=80&q=80"
                    avatarBg="#eff6ff"
                    avatarColor="#1d4ed8"
                    name="Bistro Gardenia"
                    id="ID: PK-RES-9041"
                    location="Lahore, Gulberg III"
                    subLocation="Sector 12, Main Blvd"
                    status="Active"
                    statusClass="status-active"
                    rating="4.9"
                    ratingCount="(1.2k)"
                  />
                  <TableRow
                    avatar="https://images.unsplash.com/photo-1542744173-8e7e53415bb0?auto=format&fit=crop&w=80&q=80"
                    avatarBg="#fff7ed"
                    avatarColor="#c2410c"
                    name="Spicy Hub"
                    id="ID: PK-RES-3321"
                    location="Karachi, DHA Phase 6"
                    subLocation="Street 15, Khayaban-e-Ittehad"
                    status="Inactive"
                    statusClass="status-inactive"
                    rating="4.2"
                    ratingCount="(450)"
                  />
                  <TableRow
                    avatar="https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=80&q=80"
                    avatarBg="#eff6ff"
                    avatarColor="#1d4ed8"
                    name="Urban Munchies"
                    id="ID: PK-RES-1182"
                    location="Islamabad, F-7 Markaz"
                    subLocation="Shop 44, Blue Area"
                    status="Active"
                    statusClass="status-active"
                    rating="4.7"
                    ratingCount="(892)"
                  />
                  <TableRow
                    avatar="https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=80&q=80"
                    avatarBg="#faf5ff"
                    avatarColor="#7e22ce"
                    name="The Local Hub"
                    id="ID: PK-RES-0043"
                    location="Multan, Gulgasht"
                    subLocation="Block A, Commercial Area"
                    status="Pending"
                    statusClass="status-pending"
                    rating="0.0"
                    ratingCount="(0)"
                  />
                </tbody>
              </table>
            </div>

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

          <div className="bottom-banners">
            <div className="expansion-banner">
              <div className="banner-content">
                <h3>Expansion Opportunity</h3>
                <p>Our analytics show a high demand for Italian cuisine in the South District. Consider onboarding more partners in that region.</p>
                <button className="btn-success">View Heatmap</button>
              </div>
            </div>

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
        </>
      ) : (
        renderVerificationTab()
      )}
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

const TableRow = ({ avatar, avatarBg, avatarColor, name, id, location, subLocation, status, statusClass, rating, ratingCount }) => {
  const isImage = typeof avatar === 'string' && (avatar.startsWith('http') || avatar.startsWith('data:'));
  return (
  <tr>
    <td>
      <div className="flex-cell">
        {isImage ? (
          <img src={avatar} alt={name} style={{ width: 40, height: 40, borderRadius: '50%', objectFit: 'cover' }} />
        ) : (
          <div className="avatar" style={{ backgroundColor: avatarBg, color: avatarColor }}>{avatar}</div>
        )}
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
};

const seedRestaurantVerifications = async () => {
  const sample = [
    {
      authUid: 'restaurant-uid-001',
      restaurantName: 'Karachi Spice House',
      ownerName: 'Aamir Khan',
      phone: '+92 300 1234567',
      email: 'aamir@karachispice.com',
      address: 'Shop 12, Bukhari Commercial, Karachi',
      cuisine: 'Pakistani',
      NTN: '3740987-8',
      CNIC: '42301-1234567-1',
      operatingHours: '10:00 AM - 11:00 PM',
      bankName: 'Habib Bank Limited',
      accountTitle: 'Karachi Spice House',
      IBAN: 'PK36HABB0000123456789012',
      logoImage: 'https://images.unsplash.com/photo-1529692236671-f1aa348e3a75?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1609942816567-5a91f5c9c48e?auto=format&fit=crop&w=600&q=80',
      tradeLicenseImage: 'https://images.unsplash.com/photo-1554774853-aae7a5c1e254?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    },
    {
      authUid: 'restaurant-uid-002',
      restaurantName: 'Lahore Grill Lounge',
      ownerName: 'Sana Ahmed',
      phone: '+92 321 7654321',
      email: 'sana@lahoregrill.com',
      address: 'Main Blvd, Gulberg III, Lahore',
      cuisine: 'BBQ',
      NTN: '4723098-3',
      CNIC: '37405-9876543-2',
      operatingHours: '11:00 AM - 12:00 AM',
      bankName: 'MCB Bank',
      accountTitle: 'Lahore Grill Lounge',
      IBAN: 'PK12MUCB0001234567890123',
      logoImage: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80',
      tradeLicenseImage: 'https://images.unsplash.com/photo-1515165562835-c69315b7a081?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    },
    {
      authUid: 'restaurant-uid-003',
      restaurantName: 'Punjab Tandoor Express',
      ownerName: 'Bilal Raza',
      phone: '+92 333 2221100',
      email: 'bilal@punjabtandoor.com',
      address: 'F-10 Markaz, Islamabad',
      cuisine: 'North Indian',
      NTN: '5823091-7',
      CNIC: '61101-2233445-7',
      operatingHours: '09:00 AM - 10:00 PM',
      bankName: 'Allied Bank',
      accountTitle: 'Punjab Tandoor Express',
      IBAN: 'PK84ABPA0001234567890123',
      logoImage: 'https://images.unsplash.com/photo-1541544741938-0af808871cc0?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=600&q=80',
      tradeLicenseImage: 'https://images.unsplash.com/photo-1581091870628-1adac7c1b25e?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    }
  ];

  const promises = sample.map((item) => addDoc(collection(db, 'restaurant_verifications'), item));
  await Promise.all(promises);
};

export default Restaurant;
