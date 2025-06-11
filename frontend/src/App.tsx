import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Profile from './components/Profile';
import AuthService from './services/auth.service';
import './App.css';

function App() {
  const currentUser = AuthService.getCurrentUser();

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route 
            path="/login" 
            element={!currentUser ? <Login /> : <Navigate to="/profile" />} 
          />
          <Route 
            path="/register" 
            element={!currentUser ? <Register /> : <Navigate to="/profile" />} 
          />
          <Route 
            path="/profile" 
            element={currentUser ? <Profile /> : <Navigate to="/login" />} 
          />
          <Route 
            path="/" 
            element={<Navigate to={currentUser ? "/profile" : "/login"} />} 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
