#!/bin/bash

# ===========================================
# Simple Login Security Tests Script
# ===========================================

echo "🧪 Running Simple Login Security Tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
SERVER_URL="http://localhost:8080"
TESTS_PASSED=0
TESTS_FAILED=0

# Function to run a test
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_result="$3"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    
    result=$(eval "$test_command" 2>/dev/null)
    
    if [[ "$result" == *"$expected_result"* ]]; then
        echo -e "${GREEN}✅ PASSED: $test_name${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAILED: $test_name${NC}"
        echo "   Expected: $expected_result"
        echo "   Got: $result"
        ((TESTS_FAILED++))
    fi
    echo
}

# Check if server is running
echo -e "${BLUE}📋 Checking if server is running...${NC}"
if ! curl -s "$SERVER_URL/api/security/health" >/dev/null; then
    echo -e "${RED}❌ Server is not running on $SERVER_URL${NC}"
    echo "Please start the server first: ./start-backend.sh"
    exit 1
fi
echo -e "${GREEN}✅ Server is running${NC}"
echo

echo -e "${BLUE}🔐 Running Security Tests...${NC}"
echo "=================================================="

# Test 1: Security Health Check
run_test "Security Health Check" \
    "curl -s $SERVER_URL/api/security/health | grep -o '\"status\":\"[^\"]*\"'" \
    '"status":"healthy"'

# Test 2: Security Headers
run_test "Security Headers (X-Frame-Options)" \
    "curl -I -s $SERVER_URL/api/test/all | grep 'X-Frame-Options'" \
    "X-Frame-Options: DENY"

run_test "Security Headers (X-Content-Type-Options)" \
    "curl -I -s $SERVER_URL/api/test/all | grep 'X-Content-Type-Options'" \
    "X-Content-Type-Options: nosniff"

run_test "Security Headers (Content-Security-Policy)" \
    "curl -I -s $SERVER_URL/api/test/all | grep 'Content-Security-Policy'" \
    "Content-Security-Policy:"

# Test 3: Password Validation (Weak Password)
run_test "Password Validation (Weak Password)" \
    "curl -X POST -s $SERVER_URL/api/auth/signup -H 'Content-Type: application/json' -d '{\"username\":\"testweakpw\",\"email\":\"weak@test.com\",\"password\":\"123\"}'" \
    "400"

# Test 4: Strong Password Registration
run_test "Strong Password Registration" \
    "curl -X POST -s $SERVER_URL/api/auth/signup -H 'Content-Type: application/json' -d '{\"username\":\"teststrongpw$(date +%s)\",\"email\":\"strong$(date +%s)@test.com\",\"password\":\"StrongPassword123!\"}'" \
    "User registered successfully"

# Test 5: Rate Limiting (Multiple Failed Logins)
echo -e "${BLUE}Testing: Rate Limiting (Multiple Failed Logins)${NC}"
echo "Sending 6 failed login attempts..."

for i in {1..6}; do
    result=$(curl -X POST -s "$SERVER_URL/api/auth/signin" \
        -H "Content-Type: application/json" \
        -d '{"username":"fakeuser","password":"fakepassword"}')
    
    if [[ $i -gt 3 && "$result" == *"Too many requests"* ]]; then
        echo -e "${GREEN}✅ PASSED: Rate Limiting (Request $i blocked)${NC}"
        ((TESTS_PASSED++))
        break
    elif [[ $i -le 3 && "$result" == *"Invalid"* ]]; then
        echo "   Request $i: Failed as expected"
    else
        echo -e "${RED}❌ FAILED: Rate Limiting not working properly${NC}"
        ((TESTS_FAILED++))
        break
    fi
    
    sleep 1
done
echo

# Test 6: CORS Headers
run_test "CORS Headers" \
    "curl -I -s $SERVER_URL/api/test/all | grep 'Vary'" \
    "Vary:"

# Test 7: Input Sanitization
echo -e "${BLUE}Testing: Input Sanitization${NC}"
# Wait for rate limit to reset
sleep 60
result=$(curl -X POST -s "$SERVER_URL/api/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"username":"<script>alert(1)</script>","email":"xss@test.com","password":"StrongPassword123!"}')

if [[ "$result" != *"<script>"* ]]; then
    echo -e "${GREEN}✅ PASSED: Input Sanitization (XSS filtered)${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAILED: Input Sanitization (XSS not filtered)${NC}"
    ((TESTS_FAILED++))
fi
echo

# Test Results Summary
echo "=================================================="
echo -e "${BLUE}📊 Test Results Summary${NC}"
echo "=================================================="
echo -e "${GREEN}✅ Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}❌ Tests Failed: $TESTS_FAILED${NC}"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo "📋 Total Tests: $TOTAL_TESTS"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All security tests passed!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some security tests failed. Please review.${NC}"
    exit 1
fi 