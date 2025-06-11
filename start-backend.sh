#!/bin/bash

# ===========================================
# Simple Login Backend Startup Script
# ===========================================

echo "🚀 Starting Simple Login Backend..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if port 8080 is already in use
if lsof -i:8080 >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port 8080 is already in use${NC}"
    echo "Killing existing process..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    sleep 2
fi

# Navigate to backend directory
cd backend || {
    echo -e "${RED}❌ Backend directory not found${NC}"
    exit 1
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed${NC}"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed${NC}"
    echo "Please install Java 17 or higher"
    exit 1
fi

echo -e "${BLUE}📋 Environment Check:${NC}"
echo "Java Version: $(java -version 2>&1 | head -1)"
echo "Maven Version: $(mvn -version 2>&1 | head -1)"
echo "Working Directory: $(pwd)"
echo

# Set environment variables if .env file exists
if [ -f "../.env" ]; then
    echo -e "${BLUE}🔧 Loading environment variables from .env${NC}"
    export $(cat ../.env | grep -v '^#' | xargs)
fi

# Set default JWT secret if not provided
if [ -z "$JWT_SECRET" ]; then
    export JWT_SECRET="mySecretKeyForJWTTokenGenerationThatIsSecureEnoughWith256BitsLength"
    echo -e "${YELLOW}⚠️  Using default JWT_SECRET (change in production!)${NC}"
fi

# Set default CORS origins if not provided
if [ -z "$ALLOWED_ORIGINS" ]; then
    export ALLOWED_ORIGINS="http://localhost:3000,http://localhost:3001"
fi

echo -e "${BLUE}🔨 Building project...${NC}"
mvn clean compile -q || {
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
}

echo -e "${GREEN}✅ Build successful${NC}"
echo

echo -e "${BLUE}🔐 Security Features Enabled:${NC}"
echo "- Rate Limiting: ✅ (60 requests/min general, 5 requests/min auth)"
echo "- Password Validation: ✅ (Strong password requirements)"
echo "- Security Headers: ✅ (XSS, CSRF, HSTS protection)"
echo "- Input Sanitization: ✅ (XSS prevention)"
echo "- Security Monitoring: ✅ (Failed login tracking)"
echo "- JWT Blacklist: ✅ (Token invalidation)"
echo "- 2FA Service: ✅ (Two-factor authentication)"
echo

echo -e "${GREEN}🚀 Starting Simple Login Backend on port 8080...${NC}"
echo -e "${BLUE}📊 Admin endpoints: http://localhost:8080/api/security/*${NC}"
echo -e "${BLUE}📈 Health check: http://localhost:8080/api/security/health${NC}"
echo -e "${BLUE}📋 API docs: See SECURITY.md for all endpoints${NC}"
echo

# Create logs directory if it doesn't exist
mkdir -p logs

# Start the application
echo -e "${YELLOW}💡 To stop the server, press Ctrl+C or run ./stop-backend.sh${NC}"
echo "=================================================="

mvn spring-boot:run 2>&1 | tee logs/backend-$(date +%Y%m%d-%H%M%S).log 