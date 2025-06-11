import React from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import './Profile.css';

const Profile: React.FC = () => {
  const currentUser = AuthService.getCurrentUser();
  const navigate = useNavigate();

  const handleLogout = () => {
    AuthService.logout();
    navigate('/login');
    window.location.reload();
  };

  if (!currentUser) {
    navigate('/login');
    return null;
  }

  return (
    <div className="profile-container">
      <div className="profile-card">
        <div className="profile-header">
          <div className="profile-avatar">
            <span>{currentUser.username.charAt(0).toUpperCase()}</span>
          </div>
          <h2>Chào mừng, {currentUser.username}!</h2>
          <p>Bạn đã đăng nhập thành công vào hệ thống.</p>
        </div>

        <div className="profile-info">
          <h3>Thông tin tài khoản</h3>
          <div className="info-grid">
            <div className="info-item">
              <label>ID:</label>
              <span>{currentUser.id}</span>
            </div>
            <div className="info-item">
              <label>Tên đăng nhập:</label>
              <span>{currentUser.username}</span>
            </div>
            <div className="info-item">
              <label>Email:</label>
              <span>{currentUser.email}</span>
            </div>
            <div className="info-item">
              <label>Loại token:</label>
              <span>{currentUser.tokenType}</span>
            </div>
          </div>
        </div>

        <div className="profile-actions">
          <button onClick={handleLogout} className="logout-button">
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile; 