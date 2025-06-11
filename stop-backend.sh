#!/bin/bash

# ===========================================
# Simple Login Backend Stop Script
# ===========================================

echo "🛑 Stopping Simple Login Backend..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if any process is running on port 8080
if ! lsof -i:8080 >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  No process found running on port 8080${NC}"
    exit 0
fi

echo "Finding processes on port 8080..."

# Find and display running processes
PIDS=$(lsof -ti:8080)
if [ -n "$PIDS" ]; then
    echo "Found the following processes:"
    lsof -i:8080
    echo
    
    echo "Stopping processes gracefully..."
    
    # Try graceful shutdown first (SIGTERM)
    echo "$PIDS" | xargs kill -TERM 2>/dev/null
    
    # Wait a few seconds for graceful shutdown
    sleep 5
    
    # Check if processes are still running
    if lsof -i:8080 >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Processes still running, forcing shutdown...${NC}"
        echo "$PIDS" | xargs kill -9 2>/dev/null
        sleep 2
    fi
    
    # Final check
    if ! lsof -i:8080 >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend stopped successfully${NC}"
    else
        echo -e "${RED}❌ Failed to stop some processes${NC}"
        echo "You may need to manually stop them:"
        lsof -i:8080
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  No processes found on port 8080${NC}"
fi

echo -e "${GREEN}🎯 Simple Login Backend has been stopped${NC}" 