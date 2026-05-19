import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import './TrainDetails.css';

const TrainDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  
  // We can get train info from router state, or fallback to local storage
  const [train, setTrain] = useState(location.state?.train || null);
  const [stations, setStations] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [newStation, setNewStation] = useState({ name: '', arrivalTime: '', departureTime: '' });

  useEffect(() => {
    // If no train in state, try to find it from localStorage
    if (!train) {
      const storedTrains = JSON.parse(localStorage.getItem('pakTrain_trains') || '[]');
      const foundTrain = storedTrains.find(t => t.id === id);
      if (foundTrain) {
        setTrain(foundTrain);
      } else {
        // If still not found, go back
        navigate('/train-routes');
        return;
      }
    }

    // Load stations for this specific train
    const storedStations = localStorage.getItem(`pakTrain_stations_${id}`);
    if (storedStations) {
      setStations(JSON.parse(storedStations));
    } else {
      // Create some default dummy stations if first time
      const defaultStations = [
        { id: Date.now().toString(), name: train?.departure || 'Origin', arrivalTime: '08:00 AM', departureTime: '08:30 AM' },
        { id: (Date.now() + 1).toString(), name: train?.destination || 'Destination', arrivalTime: '02:00 PM', departureTime: '02:30 PM' }
      ];
      setStations(defaultStations);
      localStorage.setItem(`pakTrain_stations_${id}`, JSON.stringify(defaultStations));
    }
  }, [id, train, navigate]);

  const saveStations = (updatedStations) => {
    setStations(updatedStations);
    localStorage.setItem(`pakTrain_stations_${id}`, JSON.stringify(updatedStations));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewStation({ ...newStation, [name]: value });
  };

  const handleAddStation = (e) => {
    e.preventDefault();
    if (newStation.name) {
      const newStationObj = {
        id: Date.now().toString(),
        name: newStation.name,
        arrivalTime: newStation.arrivalTime || '--:--',
        departureTime: newStation.departureTime || '--:--'
      };
      
      // In a real app we'd probably sort by time or let user reorder.
      // Here we just append to the end.
      const updated = [...stations, newStationObj];
      saveStations(updated);
      setNewStation({ name: '', arrivalTime: '', departureTime: '' });
      setShowModal(false);
    }
  };

  const handleDeleteStation = (stationId) => {
    if (window.confirm('Remove this station from the route?')) {
      const updated = stations.filter(s => s.id !== stationId);
      saveStations(updated);
    }
  };

  if (!train) return <div className="loading">Loading...</div>;

  return (
    <div className="train-details-wrapper">
      <header className="train-details-header">
        <button onClick={() => navigate('/train-routes')} className="back-btn">
          &larr; Back to Trains
        </button>
        <div className="header-info">
          <div>
            <h1>{train.name} <span>({train.id})</span></h1>
            <p className="route-subtitle">{train.departure} &rarr; {train.destination}</p>
          </div>
          <button className="btn-primary" onClick={() => setShowModal(true)}>
            + Add Station
          </button>
        </div>
      </header>

      <main className="train-details-content">
        <div className="timeline-card">
          <h2>Route Stations</h2>
          <p>The sequence of stops for this train route.</p>

          <div className="stations-timeline">
            {stations.length === 0 ? (
              <p className="no-stations">No stations added yet. Add a station to begin.</p>
            ) : (
              stations.map((station, index) => (
                <div key={station.id} className="timeline-item">
                  <div className="timeline-marker">
                    <div className="marker-dot"></div>
                    {index < stations.length - 1 && <div className="marker-line"></div>}
                  </div>
                  <div className="timeline-content">
                    <div className="station-info">
                      <h3>{station.name}</h3>
                      <div className="station-times">
                        <span className="time-badge arr">ARR: {station.arrivalTime}</span>
                        <span className="time-badge dep">DEP: {station.departureTime}</span>
                      </div>
                    </div>
                    <button 
                      className="btn-icon-delete"
                      onClick={() => handleDeleteStation(station.id)}
                      title="Remove Station"
                    >
                      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </main>

      {/* Add Station Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Add Station to {train.name}</h2>
            <form onSubmit={handleAddStation}>
              <div className="form-group">
                <label>Station Name</label>
                <input 
                  type="text" 
                  name="name" 
                  placeholder="e.g., Lahore Junction" 
                  value={newStation.name} 
                  onChange={handleInputChange}
                  required 
                />
              </div>
              <div className="form-row">
                <div className="form-group half">
                  <label>Arrival Time</label>
                  <input 
                    type="time" 
                    name="arrivalTime" 
                    value={newStation.arrivalTime} 
                    onChange={handleInputChange}
                  />
                </div>
                <div className="form-group half">
                  <label>Departure Time</label>
                  <input 
                    type="time" 
                    name="departureTime" 
                    value={newStation.departureTime} 
                    onChange={handleInputChange}
                  />
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Add Station</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrainDetails;
