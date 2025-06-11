#!/bin/bash

# ===========================================
# Simple Login Project Setup Script
# ===========================================

echo "🔧 Setting up Simple Login Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Simple Login Project Setup ===${NC}"
echo

# Check system requirements
echo -e "${BLUE}📋 Checking system requirements...${NC}"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    echo -e "${GREEN}✅ Java: $JAVA_VERSION${NC}"
else
    echo -e "${RED}❌ Java is not installed${NC}"
    echo "Please install Java 17 or higher: https://adoptium.net/"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version 2>&1 | head -1 | awk '{print $3}')
    echo -e "${GREEN}✅ Maven: $MVN_VERSION${NC}"
else
    echo -e "${RED}❌ Maven is not installed${NC}"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Check Node.js (optional)
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "${GREEN}✅ Node.js: $NODE_VERSION${NC}"
else
    echo -e "${YELLOW}⚠️  Node.js not found (optional for frontend)${NC}"
fi

echo

# Create environment file if it doesn't exist
echo -e "${BLUE}🔧 Setting up environment configuration...${NC}"

if [ ! -f ".env" ]; then
    echo "Creating .env file..."
    cat > .env << EOL
# ===========================================
# Simple Login Environment Configuration
# ===========================================

# JWT Security Configuration
JWT_SECRET=$(openssl rand -base64 64 | tr -d "\n")

# CORS Configuration  
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Rate Limiting
APP_RATE_LIMIT_REQUESTS_PER_MINUTE=60
APP_RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE=5

# Logging Level
LOGGING_LEVEL_SECURITY=INFO
LOGGING_LEVEL_ROOT=INFO

# Database (SQLite default)
DB_PATH=database.db

# Server Configuration
SERVER_PORT=8080
EOL
    echo -e "${GREEN}✅ Created .env file with secure random JWT secret${NC}"
else
    echo -e "${YELLOW}⚠️  .env file already exists${NC}"
fi

# Create logs directory
echo -e "${BLUE}📁 Creating directories...${NC}"
mkdir -p logs
mkdir -p backend/logs
echo -e "${GREEN}✅ Created logs directories${NC}"

# Build the project
echo -e "${BLUE}🔨 Building backend project...${NC}"
cd backend || {
    echo -e "${RED}❌ Backend directory not found${NC}"
    exit 1
}

mvn clean install -DskipTests -q || {
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
}

echo -e "${GREEN}✅ Backend build successful${NC}"
cd ..

# Make scripts executable
echo -e "${BLUE}🔑 Making scripts executable...${NC}"
chmod +x *.sh
echo -e "${GREEN}✅ Scripts are now executable${NC}"

echo
echo -e "${GREEN}🎉 Setup completed successfully!${NC}"
echo
echo -e "${BLUE}📋 Available Scripts:${NC}"
echo "  ./start-backend.sh    - Start the backend server"
echo "  ./stop-backend.sh     - Stop the backend server"
echo "  ./restart-backend.sh  - Restart the backend server"
echo "  ./run-tests.sh        - Run security tests"
echo "  ./setup-project.sh    - This setup script"
echo
echo -e "${BLUE}🔐 Security Features Enabled:${NC}"
echo "  - Rate Limiting & Brute Force Protection"
echo "  - Strong Password Validation"
echo "  - JWT Token Security (30min expiration)"
echo "  - Security Headers (XSS, CSRF, HSTS)"
echo "  - Input Sanitization"
echo "  - Security Monitoring & Logging"
echo "  - 2FA Service"
echo "  - JWT Blacklist Service"
echo
echo -e "${BLUE}🚀 Quick Start:${NC}"
echo "  1. Run: ./start-backend.sh"
echo "  2. Open: http://localhost:8080/api/security/health"
echo "  3. Check: backend/SECURITY.md for API documentation"
echo
echo -e "${YELLOW}⚠️  Important:${NC}"
echo "  - Update JWT_SECRET in .env for production"
echo "  - Configure ALLOWED_ORIGINS for your domain"
echo "  - Review backend/SECURITY.md for security guidelines" 