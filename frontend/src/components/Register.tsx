import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import './Register.css';

const Register: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [successful, setSuccessful] = useState(false);
  
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage('');
    setSuccessful(false);
    setLoading(true);

    if (password !== confirmPassword) {
      setMessage('Mật khẩu xác nhận không khớp!');
      setLoading(false);
      return;
    }

    try {
      const response = await AuthService.register(username, email, password);
      setMessage(response.data);
      setSuccessful(true);
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (error: any) {
      const resMessage =
        (error.response &&
          error.response.data &&
          error.response.data.message) ||
        error.message ||
        error.toString();
      setMessage(resMessage);
      setSuccessful(false);
    }
    setLoading(false);
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <h2>Đăng Ký</h2>
          <p>Tạo tài khoản mới để bắt đầu sử dụng dịch vụ của chúng tôi.</p>
        </div>
        
        <form onSubmit={handleRegister} className="register-form">
          <div className="form-group">
            <label htmlFor="username">Tên đăng nhập</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              minLength={3}
              maxLength={20}
              className="form-input"
              placeholder="Nhập tên đăng nhập (3-20 ký tự)"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="form-input"
              placeholder="Nhập địa chỉ email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mật khẩu</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              maxLength={40}
              className="form-input"
              placeholder="Nhập mật khẩu (ít nhất 6 ký tự)"
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              className="form-input"
              placeholder="Nhập lại mật khẩu"
            />
          </div>

          {message && (
            <div className={`alert ${successful ? 'alert-success' : 'alert-error'}`}>
              {message}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="register-button"
          >
            {loading ? (
              <span className="loading-spinner"></span>
            ) : (
              'Đăng Ký'
            )}
          </button>
        </form>

        <div className="register-footer">
          <p>
            Đã có tài khoản? {' '}
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="link-button"
            >
              Đăng nhập ngay
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register; 