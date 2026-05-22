import React, { useEffect, useRef, useState } from 'react';
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
import './Riders.css';

const DEFAULT_RIDER_IMAGE = 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80';
const DEFAULT_DOC = 'https://images.unsplash.com/photo-1521791136064-7986c2920216?auto=format&fit=crop&w=600&q=80';
const RIDER_VERIFICATION_LOGO = 'https://images.unsplash.com/photo-1517242023851-130b586a2e8a?auto=format&fit=crop&w=200&q=80';

const Riders = () => {
  const [activeTab, setActiveTab] = useState('Active Riders');
  const [requests, setRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const seededRef = useRef(false);

  useEffect(() => {
    const verificationsRef = collection(db, 'rider_verifications');
    const q = query(verificationsRef, orderBy('createdAt', 'desc'));

    const unsubscribe = onSnapshot(
      q,
      async (snapshot) => {
        const items = snapshot.docs.map((docSnap) => ({ id: docSnap.id, ...docSnap.data() }));

        if (items.length === 0 && !seededRef.current) {
          seededRef.current = true;
          try {
            await seedRiderVerifications();
          } catch (seedError) {
            setError(seedError.message || 'Could not seed rider verification data.');
            setIsLoading(false);
          }
          return;
        }

        setRequests(items);
        setIsLoading(false);
      },
      (listenError) => {
        setError(listenError.message || 'Failed to load rider verifications.');
        setIsLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const pendingCount = getPendingCount(requests);

  const handleApprove = async (requestId) => {
    try {
      await updateDoc(doc(db, 'rider_verifications', requestId), {
        status: 'Approved',
        verified: true
      });
      setSelectedRequest(null);
    } catch (approveError) {
      setError(approveError.message || 'Could not approve rider.');
    }
  };

  const handleReject = async (requestId) => {
    try {
      await updateDoc(doc(db, 'rider_verifications', requestId), {
        status: 'Rejected',
        verified: false
      });
      setSelectedRequest(null);
    } catch (rejectError) {
      setError(rejectError.message || 'Could not reject rider.');
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
        return 'This rider is verified and cleared for delivery.';
      case 'Rejected':
        return 'This rider is not verified and needs further review.';
      default:
        return 'This rider verification request is pending review.';
    }
  };

  const renderVerificationTab = () => (
    <div className="verification-panel">
      <div className="verification-header">
        <div className="verification-brand">
          <img src={RIDER_VERIFICATION_LOGO} alt="Rider verification logo" className="verification-logo-image" />
          <div>
            <h2>Rider Verification Requests</h2>
            <p>Approve or reject rider submissions with full document previews.</p>
          </div>
        </div>
        <div className="verification-actions">
          <div className="pending-badge">Pending: {pendingCount}</div>
          <button className="btn-primary" onClick={() => setActiveTab('Active Riders')}>
            Back to Active Riders
          </button>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {isLoading ? (
        <div className="status-message">Loading verification requests...</div>
      ) : (
        <div className="table-card verification-table-card">
          <div className="table-responsive">
            <table className="riders-table">
              <thead>
                <tr>
                  <th>RIDER</th>
                  <th>VEHICLE</th>
                  <th>PAYMENT</th>
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
                        <img className="rider-avatar" src={request.riderImage || DEFAULT_RIDER_IMAGE} alt={request.riderName} />
                        <div>
                          <p className="primary-text">{request.riderName}</p>
                          <p className="secondary-text">{request.email}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <p className="primary-text">{request.vehicleType}</p>
                      <p className="secondary-text">{request.vehicleNumber}</p>
                    </td>
                    <td>
                      <p className="primary-text">{request.paymentMethod}</p>
                      <p className="secondary-text">{request.paymentAccount}</p>
                    </td>
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
                <h3>Rider Verification Detail</h3>
                <p>Review rider information, vehicle details, and supporting documents.</p>
              </div>
              <button className="close-btn" onClick={() => setSelectedRequest(null)}>
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="entity-summary">
              <div className="entity-logo" style={{ backgroundImage: `url(${selectedRequest.riderImage || DEFAULT_RIDER_IMAGE})` }} />
              <div>
                <p className="entity-label">Rider Verification</p>
                <h4>{selectedRequest.riderName}</h4>
                <p className="entity-note">{selectedRequest.vehicleType}</p>
                <div className="status-summary">
                  {renderRequestStatus(selectedRequest.status)}
                  <p>{getVerificationSummary(selectedRequest.status)}</p>
                </div>
              </div>
            </div>

            <div className="modal-body modal-grid two-column">
              <div className="detail-card">
                <h4>Personal Details</h4>
                <div className="detail-item">
                  <span>Name</span>
                  <p>{selectedRequest.riderName}</p>
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
                  <span>Home Address</span>
                  <p>{selectedRequest.homeAddress}</p>
                </div>
                <div className="detail-item">
                  <span>CNIC</span>
                  <p>{selectedRequest.CNIC}</p>
                </div>
              </div>

              <div className="detail-card">
                <h4>Vehicle Information</h4>
                <div className="detail-item">
                  <span>Vehicle Type</span>
                  <p>{selectedRequest.vehicleType}</p>
                </div>
                <div className="detail-item">
                  <span>Vehicle Number</span>
                  <p>{selectedRequest.vehicleNumber}</p>
                </div>
                <div className="detail-item">
                  <span>Driving License</span>
                  <p>{selectedRequest.drivingLicenseNumber}</p>
                </div>
                <div className="detail-item">
                  <span>Payment</span>
                  <p>{selectedRequest.paymentMethod}</p>
                  <p>{selectedRequest.paymentAccount}</p>
                </div>
              </div>

              <div className="detail-card">
                <h4>Verification Summary</h4>
                <div className="detail-item">
                  <span>Status</span>
                  {renderRequestStatus(selectedRequest.status)}
                </div>
                <div className="detail-item">
                  <span>Submitted</span>
                  <p>{formatDate(selectedRequest.createdAt)}</p>
                </div>
                <div className="detail-item">
                  <span>UID</span>
                  <p>{selectedRequest.authUid}</p>
                </div>
              </div>

              <div className="detail-card">
                <h4>Document Previews</h4>
                <div className="document-grid">
                  <div className="document-card">
                    <span>Rider Photo</span>
                    <img src={selectedRequest.riderImage || DEFAULT_RIDER_IMAGE} alt="Rider" />
                  </div>
                  <div className="document-card">
                    <span>CNIC Image</span>
                    <img src={selectedRequest.CNICImage || DEFAULT_DOC} alt="CNIC" />
                  </div>
                  <div className="document-card">
                    <span>License Image</span>
                    <img src={selectedRequest.licenseImage || DEFAULT_DOC} alt="License" />
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
    <div className="riders-container animate-fade-in">
      <div className="section-tabs">
        <button
          className={`tab-button ${activeTab === 'Active Riders' ? 'active' : ''}`}
          onClick={() => setActiveTab('Active Riders')}
        >
          <span className="tab-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 12h18M12 3v18" />
              <path d="M6 16a4 4 0 008 0" />
            </svg>
          </span>
          <span>Active Riders</span>
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

      {activeTab === 'Active Riders' ? (
        <>
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

          <div className="riders-table-card">
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
                  <TableRow
                    avatar="https://randomuser.me/api/portraits/men/32.jpg"
                    name="Marcus Holloway"
                    id="PT-90210"
                    phone="+1 (555) 012-3456"
                    vehicle="Electric Bike"
                    vehicleIcon="M11.5 12.5l2 2m0 0l2-2m-2 2V9.5m-5 3v-3M3 10.5A2.5 2.5 0 105.5 8 2.5 2.5 0 003 10.5z"
                    status="Pending"
                    statusClass="status-pending"
                    onView={() => null}
                  />
                  <TableRow
                    avatar="https://randomuser.me/api/portraits/women/44.jpg"
                    name="Sarah Chen"
                    id="PT-88129"
                    phone="+1 (555) 432-1098"
                    vehicle="Compact Van"
                    vehicleIcon="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"
                    status="On-Delivery"
                    statusClass="status-delivery"
                    onView={() => null}
                  />
                  <TableRow
                    avatar="https://randomuser.me/api/portraits/men/85.jpg"
                    name="James Wilson"
                    id="PT-11204"
                    phone="+1 (555) 987-6543"
                    vehicle="Scooter"
                    vehicleIcon="M13 10V3L4 14h7v7l9-11h-7z"
                    status="Pending"
                    statusClass="status-pending"
                    onView={() => null}
                  />
                  <TableRow
                    avatar="https://randomuser.me/api/portraits/women/68.jpg"
                    name="Elena Rodriguez"
                    id="PT-44501"
                    phone="+1 (555) 777-8888"
                    vehicle="Electric Bike"
                    vehicleIcon="M11.5 12.5l2 2m0 0l2-2m-2 2V9.5m-5 3v-3M3 10.5A2.5 2.5 0 105.5 8 2.5 2.5 0 003 10.5z"
                    status="Online"
                    statusClass="status-online"
                    onView={() => null}
                  />
                  <TableRow
                    avatar="https://randomuser.me/api/portraits/men/22.jpg"
                    name="David Kim"
                    id="PT-22345"
                    phone="+1 (555) 222-3333"
                    vehicle="Hatchback"
                    vehicleIcon="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm14 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1z"
                    status="On-Delivery"
                    statusClass="status-delivery"
                    onView={() => null}
                  />
                </tbody>
              </table>
            </div>

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

          <div className="bottom-banners">
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
        </>
      ) : (
        renderVerificationTab()
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
        <img src={avatar || DEFAULT_RIDER_IMAGE} alt={name} className="rider-avatar" />
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

const seedRiderVerifications = async () => {
  const sample = [
    {
      authUid: 'rider-uid-001',
      riderName: 'Mohammad Usman',
      phone: '+92 311 9988776',
      email: 'usman.rider@paktrain.com',
      homeAddress: 'House 18, Street 3, Johar Town, Lahore',
      CNIC: '42101-9876543-2',
      vehicleType: 'Motorcycle',
      vehicleNumber: 'LEA-1234',
      drivingLicenseNumber: 'DL-987654321',
      paymentMethod: 'JazzCash',
      paymentAccount: '+92 311 9988776',
      riderImage: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80',
      licenseImage: 'https://images.unsplash.com/photo-1515165562835-c69315b7a081?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    },
    {
      authUid: 'rider-uid-002',
      riderName: 'Ayesha Khan',
      phone: '+92 333 6622445',
      email: 'ayesha.khan@paktrain.com',
      homeAddress: 'House 7, Block E, PECHS, Karachi',
      CNIC: '42201-5566778-3',
      vehicleType: 'Car',
      vehicleNumber: 'KAR-9987',
      drivingLicenseNumber: 'DL-123456789',
      paymentMethod: 'Bank Transfer',
      paymentAccount: 'UBA1234567890',
      riderImage: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80',
      licenseImage: 'https://images.unsplash.com/photo-1515165562835-c69315b7a081?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    },
    {
      authUid: 'rider-uid-003',
      riderName: 'Imran Shah',
      phone: '+92 321 5566778',
      email: 'imran.shah@paktrain.com',
      homeAddress: 'Plot 34, F-11/2, Islamabad',
      CNIC: '61101-1122334-5',
      vehicleType: 'Scooter',
      vehicleNumber: 'ISB-7801',
      drivingLicenseNumber: 'DL-564738291',
      paymentMethod: 'Easypaisa',
      paymentAccount: '+92 321 5566778',
      riderImage: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=600&q=80',
      CNICImage: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=600&q=80',
      licenseImage: 'https://images.unsplash.com/photo-1515165562835-c69315b7a081?auto=format&fit=crop&w=600&q=80',
      status: 'Pending',
      createdAt: serverTimestamp()
    }
  ];

  const promises = sample.map((item) => addDoc(collection(db, 'rider_verifications'), item));
  await Promise.all(promises);
};

export default Riders;
