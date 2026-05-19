import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './TrainRoutes.css';

const defaultTrains = [
  { id: 'TRN-001', name: 'Green Line', departure: 'Karachi', destination: 'Islamabad', status: 'Active' },
  { id: 'TRN-002', name: 'Khyber Mail', departure: 'Karachi', destination: 'Peshawar', status: 'Active' },
];

const TrainRoutes = () => {
  const navigate = useNavigate();
  
  const [trains, setTrains] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [newTrain, setNewTrain] = useState({ id: '', name: '', departure: '', destination: '', status: 'Active' });

  // Load trains from localStorage on mount
  useEffect(() => {
    const storedTrains = localStorage.getItem('pakTrain_trains');
    if (storedTrains) {
      setTrains(JSON.parse(storedTrains));
    } else {
      setTrains(defaultTrains);
      localStorage.setItem('pakTrain_trains', JSON.stringify(defaultTrains));
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
    if (newTrain.id && newTrain.name && newTrain.departure && newTrain.destination) {
      const updated = [...trains, newTrain];
      saveTrains(updated);
      setNewTrain({ id: '', name: '', departure: '', destination: '', status: 'Active' });
      setShowModal(false);
    }
  };

  const handleDeleteTrain = (id) => {
    if (window.confirm('Are you sure you want to delete this train?')) {
      const updated = trains.filter(t => t.id !== id);
      saveTrains(updated);
      
      // Also delete its stations
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
          <h1>Train Fleet Management</h1>
          <button className="btn-primary" onClick={() => setShowModal(true)}>
            + Add New Train
          </button>
        </div>
      </header>
      
      <main className="train-routes-content">
        <div className="card">
          <h2>Active Trains</h2>
          <p>Manage the train fleet and their specific route stations.</p>
          <div className="table-responsive">
            <table className="routes-table">
              <thead>
                <tr>
                  <th>Train ID</th>
                  <th>Train Name</th>
                  <th>Route</th>
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
                      <div className="action-buttons">
                        <button 
                          className="btn-manage"
                          onClick={() => navigate(`/train-routes/${train.id}`, { state: { train } })}
                        >
                          Manage Stations
                        </button>
                        <button 
                          className="btn-delete"
                          onClick={() => handleDeleteTrain(train.id)}
                        >
                          Delete
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
                <label>Train ID</label>
                <input 
                  type="text" 
                  name="id" 
                  placeholder="e.g., TRN-004" 
                  value={newTrain.id} 
                  onChange={handleInputChange}
                  required 
                />
              </div>
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
              <div className="form-group">
                <label>Origin City</label>
                <input 
                  type="text" 
                  name="departure" 
                  placeholder="e.g., Karachi" 
                  value={newTrain.departure} 
                  onChange={handleInputChange}
                  required 
                />
              </div>
              <div className="form-group">
                <label>Destination City</label>
                <input 
                  type="text" 
                  name="destination" 
                  placeholder="e.g., Islamabad" 
                  value={newTrain.destination} 
                  onChange={handleInputChange}
                  required 
                />
              </div>
              <div className="form-group">
                <label>Status</label>
                <select name="status" value={newTrain.status} onChange={handleInputChange}>
                  <option value="Active">Active</option>
                  <option value="Pending">Pending</option>
                  <option value="Delayed">Delayed</option>
                  <option value="Maintenance">Maintenance</option>
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
