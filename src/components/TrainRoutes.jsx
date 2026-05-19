import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './TrainRoutes.css';

const TrainRoutes = () => {
  const navigate = useNavigate();
  
  const [trains, setTrains] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [newTrain, setNewTrain] = useState({ name: '', departure: '', destination: '', status: 'Active' });

  // Load from local storage on mount
  useEffect(() => {
    const storedTrains = JSON.parse(localStorage.getItem('pakTrain_trains') || '[]');
    if (storedTrains.length === 0) {
      // Default dummy data
      const defaultTrains = [
        { id: 'TRN-101', name: 'Green Line', departure: 'Karachi', destination: 'Islamabad', status: 'Active' },
        { id: 'TRN-102', name: 'Karakoram Express', departure: 'Lahore', destination: 'Karachi', status: 'Active' }
      ];
      setTrains(defaultTrains);
      localStorage.setItem('pakTrain_trains', JSON.stringify(defaultTrains));
    } else {
      setTrains(storedTrains);
    }
  }, []);

  const saveTrains = (updatedTrains) => {
    setTrains(updatedTrains);
    localStorage.setItem('pakTrain_trains', JSON.stringify(updatedTrains));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewTrain({ ...newTrain, [name]: value });
  };

  const handleAddTrain = (e) => {
    e.preventDefault();
    if (newTrain.name && newTrain.departure && newTrain.destination) {
      const trainObj = {
        id: `TRN-${Math.floor(100 + Math.random() * 900)}`,
        ...newTrain
      };
      const updated = [...trains, trainObj];
      saveTrains(updated);
      setNewTrain({ name: '', departure: '', destination: '', status: 'Active' });
      setShowModal(false);
    }
  };

  const handleDeleteTrain = (id) => {
    if (window.confirm('Are you sure you want to delete this train?')) {
      const updated = trains.filter(t => t.id !== id);
      saveTrains(updated);
      // Also remove its stations from local storage
      localStorage.removeItem(`pakTrain_stations_${id}`);
    }
  };

  const getStatusClass = (status) => {
    return status.toLowerCase();
  };

  return (
    <div className="train-routes-wrapper">
      <header className="train-routes-header">
        <button onClick={() => navigate('/dashboard')} className="back-btn">
          &larr; Back to Dashboard
        </button>
        <div className="header-actions">
          <h1>Trains Management</h1>
          <button className="btn-primary" onClick={() => setShowModal(true)}>
            + Add New Train
          </button>
        </div>
      </header>
      
      <main className="train-routes-content">
        <div className="card">
          <h2>Registered Trains</h2>
          <p>List of all trains operating in the network. Click "Manage Stations" to add its route timeline.</p>
          <div className="table-responsive">
            <table className="routes-table">
              <thead>
                <tr>
                  <th>Train ID</th>
                  <th>Train Name</th>
                  <th>Origin &rarr; Destination</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {trains.map((train) => (
                  <tr key={train.id}>
                    <td className="font-medium text-dark">{train.id}</td>
                    <td className="font-medium">{train.name}</td>
                    <td>{train.departure} &rarr; {train.destination}</td>
                    <td><span className={`status ${getStatusClass(train.status)}`}>{train.status}</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button 
                          className="btn-secondary" 
                          style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                          onClick={() => navigate(`/train-details/${train.id}`, { state: { train } })}
                        >
                          Manage Stations
                        </button>
                        <button 
                          className="btn-icon-delete"
                          onClick={() => handleDeleteTrain(train.id)}
                          style={{ border: 'none', background: 'transparent', color: '#ff4d4d', cursor: 'pointer' }}
                          title="Delete Train"
                        >
                          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="18" height="18">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {trains.length === 0 && (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '2rem' }}>No trains added yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>

      {/* Add Train Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Add New Train</h2>
            <form onSubmit={handleAddTrain}>
              <div className="form-group">
                <label>Train Name</label>
                <input 
                  type="text" 
                  name="name" 
                  placeholder="e.g., Green Line" 
                  value={newTrain.name} 
                  onChange={handleInputChange}
                  required 
                />
              </div>
              <div className="form-row" style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>From (Origin)</label>
                  <input 
                    type="text" 
                    name="departure" 
                    placeholder="e.g., Karachi" 
                    value={newTrain.departure} 
                    onChange={handleInputChange}
                    required 
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>To (Destination)</label>
                  <input 
                    type="text" 
                    name="destination" 
                    placeholder="e.g., Rawalpindi" 
                    value={newTrain.destination} 
                    onChange={handleInputChange}
                    required 
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Status</label>
                <select name="status" value={newTrain.status} onChange={handleInputChange}>
                  <option value="Active">Active</option>
                  <option value="Pending">Pending</option>
                  <option value="Delayed">Delayed</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Save Train</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrainRoutes;
