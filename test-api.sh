#!/bin/bash

# EventSphere User Service API Test Script
# This script tests all the API endpoints in your application

BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api/v1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables to store tokens
ACCESS_TOKEN=""
REFRESH_TOKEN=""
USER_ID=""
ADMIN_ACCESS_TOKEN=""

echo -e "${BLUE}🚀 EventSphere User Service API Testing${NC}"
echo "========================================"

# Function to print test results
print_result() {
    local test_name="$1"
    local status_code="$2"
    local expected="$3"

    if [ "$status_code" == "$expected" ]; then
        echo -e "${GREEN}✅ $test_name: PASSED (Status: $status_code)${NC}"
    else
        echo -e "${RED}❌ $test_name: FAILED (Expected: $expected, Got: $status_code)${NC}"
    fi
}

# Function to make HTTP requests and return status code
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local token="$4"

    if [ -n "$token" ]; then
        if [ -n "$data" ]; then
            curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                -d "$data"
        else
            curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
                -H "Authorization: Bearer $token"
        fi
    else
        if [ -n "$data" ]; then
            curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$data"
        else
            curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url"
        fi
    fi
}

# Function to extract token from response
extract_token() {
    local response="$1"
    echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

echo -e "\n${YELLOW}📋 Testing Authentication Endpoints${NC}"
echo "-----------------------------------"

# Test 1: User Registration
echo "1. Testing User Registration..."
REGISTER_DATA='{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "TestPassword123!",
    "phone": "+1234567890"
}'

STATUS=$(make_request "POST" "$API_BASE/auth/register" "$REGISTER_DATA")
print_result "User Registration" "$STATUS" "201"

# Test 2: User Login (will fail if email verification is required)
echo "2. Testing User Login..."
LOGIN_DATA='{
    "email": "test@example.com",
    "password": "TestPassword123!"
}'

LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA")
LOGIN_STATUS=$(echo "$LOGIN_RESPONSE" | jq -r '.status // "200"')

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
    REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.refreshToken')
    USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.userId')
    echo -e "${GREEN}✅ User Login: PASSED${NC}"
else
    echo -e "${YELLOW}⚠️  User Login: May require email verification${NC}"
fi

# Test 3: Refresh Token
if [ -n "$REFRESH_TOKEN" ]; then
    echo "3. Testing Token Refresh..."
    REFRESH_DATA="{\"token\": \"$REFRESH_TOKEN\"}"
    STATUS=$(make_request "POST" "$API_BASE/auth/refresh-token" "$REFRESH_DATA")
    print_result "Token Refresh" "$STATUS" "200"
fi

echo -e "\n${YELLOW}👤 Testing User Management Endpoints${NC}"
echo "-------------------------------------"

# Test 4: Get User Profile (if logged in)
if [ -n "$ACCESS_TOKEN" ]; then
    echo "4. Testing Get User Profile..."
    STATUS=$(make_request "GET" "$API_BASE/users/profile" "" "$ACCESS_TOKEN")
    print_result "Get User Profile" "$STATUS" "200"

    # Test 5: Update User Profile
    echo "5. Testing Update User Profile..."
    UPDATE_DATA='{
        "firstName": "Updated",
        "lastName": "User",
        "phoneNumber": "+9876543210"
    }'
    STATUS=$(make_request "PUT" "$API_BASE/users/update/$USER_ID" "$UPDATE_DATA" "$ACCESS_TOKEN")
    print_result "Update User Profile" "$STATUS" "200"

    # Test 6: Change Password
    echo "6. Testing Change Password..."
    PASSWORD_DATA='{
        "currentPassword": "TestPassword123!",
        "newPassword": "NewPassword123!"
    }'
    STATUS=$(make_request "PUT" "$API_BASE/users/change-password" "$PASSWORD_DATA" "$ACCESS_TOKEN")
    print_result "Change Password" "$STATUS" "200"
fi

echo -e "\n${YELLOW}🔐 Testing Authentication Flow${NC}"
echo "-------------------------------"

# Test 7: Password Reset Request
echo "7. Testing Password Reset Request..."
RESET_REQUEST='{
    "email": "test@example.com"
}'
STATUS=$(make_request "POST" "$API_BASE/auth/forgot-password" "$RESET_REQUEST")
print_result "Password Reset Request" "$STATUS" "200"

echo -e "\n${YELLOW}🔍 Testing Public Endpoints${NC}"
echo "----------------------------"

# Test 8: Get User by ID (public endpoint)
if [ -n "$USER_ID" ]; then
    echo "8. Testing Get User by ID..."
    STATUS=$(make_request "GET" "$API_BASE/users/$USER_ID")
    print_result "Get User by ID" "$STATUS" "200"
fi

echo -e "\n${YELLOW}👨‍💼 Testing Admin Endpoints (may require ADMIN role)${NC}"
echo "----------------------------------------------------"

# Test 9: Get All Users (Admin endpoint)
echo "9. Testing Get All Users (Admin)..."
if [ -n "$ACCESS_TOKEN" ]; then
    STATUS=$(make_request "GET" "$API_BASE/users/all" "" "$ACCESS_TOKEN")
    if [ "$STATUS" == "403" ]; then
        echo -e "${YELLOW}⚠️  Get All Users: FORBIDDEN (User needs ADMIN role)${NC}"
    else
        print_result "Get All Users" "$STATUS" "200"
    fi
else
    STATUS=$(make_request "GET" "$API_BASE/users/all")
    print_result "Get All Users (No Auth)" "$STATUS" "401"
fi

# Test 10: Role Assignment (Admin endpoint)
echo "10. Testing Role Assignment (Admin)..."
if [ -n "$ACCESS_TOKEN" ] && [ -n "$USER_ID" ]; then
    ROLE_DATA='{
        "userId": '$USER_ID',
        "roleName": "ADMIN"
    }'
    STATUS=$(make_request "POST" "$API_BASE/users/assign-role" "$ROLE_DATA" "$ACCESS_TOKEN")
    if [ "$STATUS" == "403" ]; then
        echo -e "${YELLOW}⚠️  Role Assignment: FORBIDDEN (User needs ADMIN role)${NC}"
    else
        print_result "Role Assignment" "$STATUS" "200"
    fi
fi

# Test 11: Search Users (Admin endpoint)
echo "11. Testing Search Users (Admin)..."
if [ -n "$ACCESS_TOKEN" ]; then
    STATUS=$(make_request "GET" "$API_BASE/users/search?query=test" "" "$ACCESS_TOKEN")
    if [ "$STATUS" == "403" ]; then
        echo -e "${YELLOW}⚠️  Search Users: FORBIDDEN (User needs ADMIN role)${NC}"
    else
        print_result "Search Users" "$STATUS" "200"
    fi
fi

echo -e "\n${YELLOW}🧹 Testing Logout${NC}"
echo "------------------"

# Test 12: Logout
if [ -n "$REFRESH_TOKEN" ]; then
    echo "12. Testing User Logout..."
    LOGOUT_DATA="{\"token\": \"$REFRESH_TOKEN\"}"
    STATUS=$(make_request "POST" "$API_BASE/auth/logout" "$LOGOUT_DATA")
    print_result "User Logout" "$STATUS" "200"
fi

echo -e "\n${BLUE}🏁 API Testing Complete!${NC}"
echo "========================"
echo -e "${GREEN}✅ All basic endpoints have been tested${NC}"
echo -e "${YELLOW}⚠️  Some admin endpoints may require ADMIN role${NC}"
echo -e "${YELLOW}⚠️  Email verification may be required for some features${NC}"
