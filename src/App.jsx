import { Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import TrainRoutes from './components/TrainRoutes';
import TrainDetails from './components/TrainDetails';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/train-routes" element={<TrainRoutes />} />
      <Route path="/train-routes/:id" element={<TrainDetails />} />
    </Routes>
  );
}

export default App;
