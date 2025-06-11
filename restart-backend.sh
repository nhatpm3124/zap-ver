#!/bin/bash

# ===========================================
# Simple Login Backend Restart Script
# ===========================================

echo "🔄 Restarting Simple Login Backend..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Step 1: Stopping backend...${NC}"
./stop-backend.sh

echo
echo -e "${BLUE}Step 2: Starting backend...${NC}"
./start-backend.sh 