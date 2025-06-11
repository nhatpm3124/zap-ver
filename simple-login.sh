#!/bin/bash

# ===========================================
# Simple Login Project Management Script
# ===========================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Display banner
show_banner() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "🔐 Simple Login - Secure Authentication System"
    echo "=================================================="
    echo -e "${NC}"
    echo -e "${CYAN}Enterprise-grade security features:${NC}"
    echo "  • Rate Limiting & Brute Force Protection"
    echo "  • Strong Password Validation & JWT Security"
    echo "  • Security Headers & Input Sanitization"
    echo "  • 2FA Service & Security Monitoring"
    echo
}

# Show current status
show_status() {
    echo -e "${BLUE}📊 Current Status:${NC}"
    
    # Check if server is running
    if lsof -i:8080 >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend Server: RUNNING (port 8080)${NC}"
        echo -e "   🌐 Admin Dashboard: http://localhost:8080/api/security/metrics"
        echo -e "   💚 Health Check: http://localhost:8080/api/security/health"
    else
        echo -e "${RED}❌ Backend Server: STOPPED${NC}"
    fi
    
    # Check if .env exists
    if [ -f ".env" ]; then
        echo -e "${GREEN}✅ Environment: CONFIGURED${NC}"
    else
        echo -e "${YELLOW}⚠️  Environment: NOT CONFIGURED${NC}"
    fi
    
    # Check recent logs
    if [ -d "logs" ] && [ "$(ls -A logs 2>/dev/null)" ]; then
        LATEST_LOG=$(ls -t logs/backend-*.log 2>/dev/null | head -1)
        if [ -n "$LATEST_LOG" ]; then
            echo -e "${GREEN}✅ Latest Log: $LATEST_LOG${NC}"
        fi
    fi
    
    echo
}

# Show menu
show_menu() {
    echo -e "${PURPLE}📋 Available Actions:${NC}"
    echo
    echo "  [1] 🚀 Start Backend Server"
    echo "  [2] 🛑 Stop Backend Server"
    echo "  [3] 🔄 Restart Backend Server"
    echo "  [4] 🔧 Setup Project (First Time)"
    echo "  [5] 🧪 Run Security Tests"
    echo "  [6] 📊 View Security Metrics"
    echo "  [7] 📄 View Latest Logs"
    echo "  [8] 🔍 Check Security Health"
    echo "  [9] 📚 Show API Documentation"
    echo "  [0] ❌ Exit"
    echo
}

# View security metrics
view_metrics() {
    echo -e "${BLUE}📊 Fetching Security Metrics...${NC}"
    
    if ! curl -s "http://localhost:8080/api/security/health" >/dev/null; then
        echo -e "${RED}❌ Server is not running. Please start the server first.${NC}"
        return 1
    fi
    
    echo
    echo -e "${CYAN}🔐 Security Health Status:${NC}"
    curl -s "http://localhost:8080/api/security/health" | jq '.' 2>/dev/null || curl -s "http://localhost:8080/api/security/health"
    
    echo
    echo -e "${CYAN}📈 Security Metrics:${NC}"
    curl -s "http://localhost:8080/api/security/metrics" | jq '.' 2>/dev/null || curl -s "http://localhost:8080/api/security/metrics"
    echo
}

# View latest logs
view_logs() {
    echo -e "${BLUE}📄 Latest Application Logs...${NC}"
    
    if [ -d "logs" ] && [ "$(ls -A logs 2>/dev/null)" ]; then
        LATEST_LOG=$(ls -t logs/backend-*.log 2>/dev/null | head -1)
        if [ -n "$LATEST_LOG" ]; then
            echo -e "${CYAN}📋 Showing last 50 lines of: $LATEST_LOG${NC}"
            echo "=================================================="
            tail -50 "$LATEST_LOG"
            echo "=================================================="
        else
            echo -e "${YELLOW}⚠️  No log files found in logs directory${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Logs directory not found or empty${NC}"
    fi
    echo
}

# Show API documentation
show_api_docs() {
    echo -e "${BLUE}📚 Simple Login API Documentation${NC}"
    echo "=================================================="
    echo
    echo -e "${CYAN}🌐 Public Endpoints:${NC}"
    echo "  GET  /api/test/all           - Public content"
    echo "  POST /api/auth/signup        - User registration"
    echo "  POST /api/auth/signin        - User login"
    echo "  GET  /api/security/health    - Security health check"
    echo
    echo -e "${CYAN}🔒 Admin Endpoints (Auth Required):${NC}"
    echo "  GET  /api/security/metrics   - Security metrics dashboard"
    echo "  GET  /api/security/check-ip/{ip} - Check IP status"
    echo "  POST /api/security/2fa/generate  - Generate 2FA codes"
    echo "  POST /api/security/2fa/verify    - Verify 2FA codes"
    echo "  POST /api/security/cleanup       - Force cleanup expired data"
    echo
    echo -e "${CYAN}📋 Example Usage:${NC}"
    echo
    echo -e "${YELLOW}Register User:${NC}"
    echo 'curl -X POST http://localhost:8080/api/auth/signup \'
    echo '  -H "Content-Type: application/json" \'
    echo '  -d '"'"'{"username":"john","email":"john@test.com","password":"SecurePass123!"}'"'"
    echo
    echo -e "${YELLOW}Login:${NC}"
    echo 'curl -X POST http://localhost:8080/api/auth/signin \'
    echo '  -H "Content-Type: application/json" \'
    echo '  -d '"'"'{"username":"john","password":"SecurePass123!"}'"'"
    echo
    echo -e "${CYAN}📖 For complete documentation, see: backend/SECURITY.md${NC}"
    echo
}

# Check security health
check_health() {
    echo -e "${BLUE}🔍 Checking Security Health...${NC}"
    
    if ! curl -s "http://localhost:8080/api/security/health" >/dev/null; then
        echo -e "${RED}❌ Server is not running. Please start the server first.${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ Server is responding${NC}"
    
    # Test security headers
    echo -e "${BLUE}🛡️  Testing Security Headers...${NC}"
    HEADERS=$(curl -I -s "http://localhost:8080/api/test/all")
    
    if echo "$HEADERS" | grep -q "X-Frame-Options"; then
        echo -e "${GREEN}✅ X-Frame-Options header present${NC}"
    else
        echo -e "${RED}❌ X-Frame-Options header missing${NC}"
    fi
    
    if echo "$HEADERS" | grep -q "X-Content-Type-Options"; then
        echo -e "${GREEN}✅ X-Content-Type-Options header present${NC}"
    else
        echo -e "${RED}❌ X-Content-Type-Options header missing${NC}"
    fi
    
    if echo "$HEADERS" | grep -q "Content-Security-Policy"; then
        echo -e "${GREEN}✅ Content-Security-Policy header present${NC}"
    else
        echo -e "${RED}❌ Content-Security-Policy header missing${NC}"
    fi
    
    echo -e "${GREEN}🎯 Security health check completed${NC}"
    echo
}

# Main script
main() {
    show_banner
    
    # If arguments provided, handle them directly
    if [ $# -gt 0 ]; then
        case $1 in
            "start"|"run")
                ./start-backend.sh
                ;;
            "stop")
                ./stop-backend.sh
                ;;
            "restart")
                ./restart-backend.sh
                ;;
            "setup")
                ./setup-project.sh
                ;;
            "test")
                ./run-tests.sh
                ;;
            "status")
                show_status
                ;;
            "metrics")
                view_metrics
                ;;
            "logs")
                view_logs
                ;;
            "health")
                check_health
                ;;
            "help"|"--help"|"-h")
                show_api_docs
                ;;
            *)
                echo -e "${RED}❌ Unknown command: $1${NC}"
                echo "Available commands: start, stop, restart, setup, test, status, metrics, logs, health, help"
                exit 1
                ;;
        esac
        exit 0
    fi
    
    # Interactive mode
    while true; do
        show_status
        show_menu
        
        read -p "👉 Select an action [0-9]: " choice
        echo
        
        case $choice in
            1)
                echo -e "${BLUE}🚀 Starting Backend Server...${NC}"
                ./start-backend.sh
                ;;
            2)
                echo -e "${BLUE}🛑 Stopping Backend Server...${NC}"
                ./stop-backend.sh
                ;;
            3)
                echo -e "${BLUE}🔄 Restarting Backend Server...${NC}"
                ./restart-backend.sh
                ;;
            4)
                echo -e "${BLUE}🔧 Setting up Project...${NC}"
                ./setup-project.sh
                ;;
            5)
                echo -e "${BLUE}🧪 Running Security Tests...${NC}"
                ./run-tests.sh
                ;;
            6)
                view_metrics
                ;;
            7)
                view_logs
                ;;
            8)
                check_health
                ;;
            9)
                show_api_docs
                ;;
            0)
                echo -e "${GREEN}👋 Goodbye! Thanks for using Simple Login!${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}❌ Invalid choice. Please select 0-9.${NC}"
                ;;
        esac
        
        echo
        read -p "Press Enter to continue..."
        clear
    done
}

# Run main function
main "$@" 