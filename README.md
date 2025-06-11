# 🔐 Simple Login - Secure Authentication System

A modern, secure authentication system built with Spring Boot and React, featuring enterprise-grade security measures.

## ✨ Features

### 🛡️ **Advanced Security**
- **Rate Limiting**: Brute force protection with configurable thresholds
- **Strong Password Validation**: 8+ chars, uppercase, lowercase, digits, special characters
- **JWT Security**: 30-minute token expiration, blacklist support
- **Security Headers**: XSS, CSRF, HSTS protection
- **Input Sanitization**: XSS and injection prevention
- **2FA Service**: Two-factor authentication support
- **Security Monitoring**: Failed login tracking and suspicious activity detection

### 🚀 **Modern Architecture**
- **Backend**: Spring Boot 2.7.14 with Java 17
- **Database**: SQLite (easily configurable to other databases)
- **Frontend**: React (coming soon)
- **Security**: Spring Security 5.7+ with modern configurations

### 📊 **Monitoring & Logging**
- Comprehensive security event logging
- Real-time security metrics dashboard
- Request correlation tracking
- Admin security endpoints

## 🚀 Quick Start

### 1. **First Time Setup**
```bash
# Clone the repository
git clone <repository-url>
cd simpleLogin

# Run the setup script
./setup-project.sh
```

### 2. **Start the Application**
```bash
# Start backend server
./start-backend.sh
```

### 3. **Verify Installation**
```bash
# Run security tests
./run-tests.sh
```

## 📋 Available Scripts

| Script | Description |
|--------|-------------|
| `./setup-project.sh` | First-time project setup |
| `./start-backend.sh` | Start the backend server |
| `./stop-backend.sh` | Stop the backend server |
| `./restart-backend.sh` | Restart the backend server |
| `./run-tests.sh` | Run comprehensive security tests |

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```bash
# JWT Security
JWT_SECRET=your-super-secure-256-bit-jwt-secret-key

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# Rate Limiting
APP_RATE_LIMIT_REQUESTS_PER_MINUTE=60
APP_RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE=5

# Server Configuration
SERVER_PORT=8080
```

### Security Configuration

The application includes the following security measures:

- **Rate Limiting**: 60 requests/min (general), 5 requests/min (auth)
- **Password Requirements**: Minimum 8 characters, mixed case, numbers, symbols
- **JWT Expiration**: 30 minutes (configurable)
- **Security Headers**: Comprehensive set of security headers
- **CORS**: Configurable allowed origins

## 📚 API Documentation

### Public Endpoints
- `GET /api/test/all` - Public content
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `GET /api/security/health` - Security health check

### Admin Endpoints (requires authentication)
- `GET /api/security/metrics` - Security metrics dashboard
- `GET /api/security/check-ip/{ip}` - Check IP status
- `POST /api/security/2fa/generate` - Generate 2FA codes
- `POST /api/security/2fa/verify` - Verify 2FA codes
- `POST /api/security/cleanup` - Force cleanup expired data

### Example Usage

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com", 
    "password": "SecurePassword123!"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePassword123!"
  }'
```

## 🔐 Security Features

### Rate Limiting
- **General API**: 60 requests per minute per IP
- **Authentication**: 5 requests per minute per IP
- **Registration**: 3 attempts per IP per time window

### Password Security
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character
- Protection against common passwords

### JWT Security
- 256-bit HMAC-SHA512 signatures
- 30-minute token expiration
- Token blacklist support
- Refresh token capability

### Security Headers
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security` with 1-year max-age
- Comprehensive Content Security Policy

## 📊 Monitoring

### Security Metrics
Access security metrics at: `GET /api/security/metrics`

Metrics include:
- Suspicious IP addresses
- Temporarily locked users
- Registration attempts
- Recent security alerts

### Logging
All security events are logged with:
- Request correlation IDs
- Client IP addresses
- Timestamps
- Event types and details

Log files are stored in: `logs/backend-YYYYMMDD-HHMMSS.log`

## 🛠️ Development

### System Requirements
- **Java**: 17 or higher
- **Maven**: 3.6+ 
- **Node.js**: 16+ (optional, for frontend)

### Project Structure
```
simpleLogin/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration files
│   └── SECURITY.md         # Security documentation
├── logs/                   # Application logs
├── *.sh                    # Management scripts
├── .env                    # Environment configuration
└── README.md              # This file
```

### Building from Source
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## 🔍 Testing

### Manual Testing
```bash
# Test security headers
curl -I http://localhost:8080/api/test/all

# Test rate limiting
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/auth/signin \
    -H "Content-Type: application/json" \
    -d '{"username":"fake","password":"fake"}'
done
```

### Automated Testing
```bash
./run-tests.sh
```

## 📋 Security Checklist

### Before Production
- [ ] Change JWT_SECRET to a strong, unique value
- [ ] Configure CORS with specific allowed origins
- [ ] Enable HTTPS/SSL with valid certificates
- [ ] Set up proper database credentials
- [ ] Configure rate limiting based on expected traffic
- [ ] Set up security monitoring and alerting
- [ ] Review and test all authentication flows
- [ ] Perform security penetration testing

### Regular Maintenance
- [ ] Update dependencies regularly
- [ ] Monitor security logs for suspicious activity
- [ ] Review and rotate JWT secrets periodically
- [ ] Regular security audits and vulnerability assessments

## 📞 Support

For security issues or questions:
- 📧 Email: security@yourdomain.com
- 📋 Documentation: `backend/SECURITY.md`
- 🐛 Issues: Create a GitHub issue

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**⚠️ Security Notice**: This application implements enterprise-grade security measures, but security is an ongoing process. Regular updates, monitoring, and security audits are essential for maintaining a secure application. 