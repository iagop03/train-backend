#!/bin/bash

# Post-deployment setup script for Keycloak
# TRAIN-16: Configure realms, clients, and users after VM deployment

set -e

# Configuration
KEYCLOAK_URL="${1:-https://keycloak.yourdomain.com}"
KEYCLOAK_ADMIN_USER="${2:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${3}"
REALM_NAME="train-gym"

if [ -z "$KEYCLOAK_ADMIN_PASSWORD" ]; then
    echo "Usage: $0 <keycloak_url> <admin_user> <admin_password>"
    echo "Example: $0 https://keycloak.yourdomain.com admin mypassword"
    exit 1
fi

echo "[INFO] Keycloak Post-Deployment Setup"
echo "[INFO] URL: $KEYCLOAK_URL"
echo "[INFO] Admin User: $KEYCLOAK_ADMIN_USER"
echo "[INFO] Realm: $REALM_NAME"
echo ""

# Function to get admin token
get_admin_token() {
    local token=$(curl -s -X POST \
        "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "client_id=admin-cli" \
        -d "username=${KEYCLOAK_ADMIN_USER}" \
        -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
        -d "grant_type=password" | jq -r '.access_token')
    echo "$token"
}

# Function to make authenticated request
keycloak_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4

    local cmd="curl -s -X ${method} '${KEYCLOAK_URL}${endpoint}' \
        -H 'Authorization: Bearer ${token}' \
        -H 'Content-Type: application/json'"

    if [ -n "$data" ]; then
        cmd="${cmd} -d '${data}'"
    fi

    eval "$cmd"
}

echo "[INFO] Getting admin token..."
ADMIN_TOKEN=$(get_admin_token)

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "[ERROR] Failed to obtain admin token. Check credentials."
    exit 1
fi

echo "[SUCCESS] Admin token obtained"
echo ""

# Create realm
echo "[INFO] Creating realm: $REALM_NAME"
REALM_PAYLOAD=$(cat <<EOF
{
  "realm": "${REALM_NAME}",
  "enabled": true,
  "displayName": "TrAIn AI Gym Tracker",
  "displayNameHtml": "<b>TrAIn</b> - AI Gym Tracker",
  "loginWithEmailAsUsername": true,
  "userManagedAccessAllowed": false,
  "sslRequired": "external",
  "passwordPolicy": "upperCase(1) and length(8) and specialChars(1) and notUsername(undefined)",
  "accountTheme": "keycloak",
  "adminTheme": "keycloak",
  "emailTheme": "keycloak",
  "loginTheme": "keycloak",
  "accessTokenLifespan": 900,
  "accessTokenLifespanForImplicitFlow": 900,
  "refreshTokenMaxReuse": 0,
  "actionTokenGeneratedByAdminLifespan": 43200,
  "actionTokenGeneratedByUserLifespan": 300,
  "offlineSessionMaxLifespan": 5184000,
  "accessCodeLifespan": 60,
  "accessCodeLifespanUserAction": 300,
  "accessCodeLifespanLogin": 1800
}
EOF
)

keycloak_api POST "/admin/realms" "$REALM_PAYLOAD" "$ADMIN_TOKEN" > /dev/null
echo "[SUCCESS] Realm created"
echo ""

# Create client for backend
echo "[INFO] Creating client: train-backend"
BACKEND_CLIENT_PAYLOAD=$(cat <<EOF
{
  "clientId": "train-backend",
  "name": "TrAIn Backend",
  "description": "TrAIn Spring Boot backend application",
  "enabled": true,
  "publicClient": false,
  "protocol": "openid-connect",
  "clientAuthenticatorType": "client-secret",
  "redirectUris": [
    "https://api.yourdomain.com/*"
  ],
  "webOrigins": [
    "https://api.yourdomain.com"
  ],
  "standardFlowEnabled": true,
  "implicitFlowEnabled": false,
  "directAccessGrantsEnabled": true,
  "serviceAccountsEnabled": true,
  "bearerOnlyClient": false,
  "accessType": "CONFIDENTIAL",
  "optionalClientScopes": [
    "email",
    "profile",
    "roles"
  ],
  "defaultClientScopes": [
    "openid",
    "profile",
    "email",
    "roles"
  ]
}
EOF
)

BACKEND_CLIENT_RESPONSE=$(keycloak_api POST "/admin/realms/${REALM_NAME}/clients" "$BACKEND_CLIENT_PAYLOAD" "$ADMIN_TOKEN")
BACKEND_CLIENT_ID=$(echo "$BACKEND_CLIENT_RESPONSE" | jq -r '.id')
echo "[SUCCESS] Backend client created (ID: $BACKEND_CLIENT_ID)"

# Get backend client secret
echo "[INFO] Retrieving backend client secret..."
BACKEND_CLIENT_SECRET=$(keycloak_api GET "/admin/realms/${REALM_NAME}/clients/${BACKEND_CLIENT_ID}/client-secret" "" "$ADMIN_TOKEN" | jq -r '.value')
echo "[SUCCESS] Backend client secret retrieved"
echo ""

# Create client for web (Angular)
echo "[INFO] Creating client: train-web"
WEB_CLIENT_PAYLOAD=$(cat <<EOF
{
  "clientId": "train-web",
  "name": "TrAIn Web",
  "description": "TrAIn Angular web application",
  "enabled": true,
  "publicClient": true,
  "protocol": "openid-connect",
  "redirectUris": [
    "https://app.yourdomain.com/callback",
    "http://localhost:4200/callback"
  ],
  "webOrigins": [
    "https://app.yourdomain.com",
    "http://localhost:4200"
  ],
  "standardFlowEnabled": true,
  "implicitFlowEnabled": false,
  "directAccessGrantsEnabled": false,
  "serviceAccountsEnabled": false,
  "bearerOnlyClient": false,
  "optionalClientScopes": [
    "email",
    "profile",
    "roles"
  ],
  "defaultClientScopes": [
    "openid",
    "profile",
    "email",
    "roles"
  ]
}
EOF
)

WEB_CLIENT_RESPONSE=$(keycloak_api POST "/admin/realms/${REALM_NAME}/clients" "$WEB_CLIENT_PAYLOAD" "$ADMIN_TOKEN")
WEB_CLIENT_ID=$(echo "$WEB_CLIENT_RESPONSE" | jq -r '.id')
echo "[SUCCESS] Web client created (ID: $WEB_CLIENT_ID)"
echo ""

# Create client for mobile (Flutter)
echo "[INFO] Creating client: train-mobile"
MOBILE_CLIENT_PAYLOAD=$(cat <<EOF
{
  "clientId": "train-mobile",
  "name": "TrAIn Mobile",
  "description": "TrAIn Flutter mobile application",
  "enabled": true,
  "publicClient": true,
  "protocol": "openid-connect",
  "redirectUris": [
    "com.train.gym://oauth-callback",
    "io.train.gym://oauth-callback"
  ],
  "webOrigins": [],
  "standardFlowEnabled": true,
  "implicitFlowEnabled": false,
  "directAccessGrantsEnabled": false,
  "serviceAccountsEnabled": false,
  "bearerOnlyClient": false,
  "optionalClientScopes": [
    "email",
    "profile",
    "roles"
  ],
  "defaultClientScopes": [
    "openid",
    "profile",
    "email",
    "roles"
  ]
}
EOF
)

MOBILE_CLIENT_RESPONSE=$(keycloak_api POST "/admin/realms/${REALM_NAME}/clients" "$MOBILE_CLIENT_PAYLOAD" "$ADMIN_TOKEN")
MOBILE_CLIENT_ID=$(echo "$MOBILE_CLIENT_RESPONSE" | jq -r '.id')
echo "[SUCCESS] Mobile client created (ID: $MOBILE_CLIENT_ID)"
echo ""

# Create realm roles
echo "[INFO] Creating realm roles..."
for ROLE in "admin" "trainer" "user"; do
    ROLE_PAYLOAD=$(cat <<EOF
{
  "name": "${ROLE}",
  "description": "Role for ${ROLE}",
  "composite": false,
  "clientRole": false
}
EOF
)
    keycloak_api POST "/admin/realms/${REALM_NAME}/roles" "$ROLE_PAYLOAD" "$ADMIN_TOKEN" > /dev/null
    echo "[SUCCESS] Role created: $ROLE"
done
echo ""

# Create sample test user
echo "[INFO] Creating sample test user..."
TEST_USER_PAYLOAD=$(cat <<EOF
{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@train-gym.local",
  "emailVerified": true,
  "username": "testuser",
  "enabled": true,
  "credentials": [
    {
      "type": "password",
      "value": "TestPassword123!",
      "temporary": false
    }
  ]
}
EOF
)

TEST_USER_RESPONSE=$(keycloak_api POST "/admin/realms/${REALM_NAME}/users" "$TEST_USER_PAYLOAD" "$ADMIN_TOKEN")
echo "[SUCCESS] Test user created (username: testuser)"
echo ""

# Output configuration details
echo "================================================================"
echo "[SUCCESS] Keycloak Post-Deployment Setup Complete!"
echo "================================================================"
echo ""
echo "Realm Configuration:"
echo "  Realm Name: $REALM_NAME"
echo "  Realm URL: ${KEYCLOAK_URL}/realms/${REALM_NAME}"
echo ""
echo "Client Configuration:"
echo ""
echo "  Backend (Spring Boot):"
echo "    Client ID: train-backend"
echo "    Client Secret: $BACKEND_CLIENT_SECRET"
echo ""
echo "  Web (Angular):"
echo "    Client ID: train-web"
echo ""
echo "  Mobile (Flutter):"
echo "    Client ID: train-mobile"
echo ""
echo "Test User:"
echo "  Username: testuser"
echo "  Email: test@train-gym.local"
echo "  Password: TestPassword123!"
echo ""
echo "Admin Console:"
echo "  URL: ${KEYCLOAK_URL}/admin"
echo "  Username: ${KEYCLOAK_ADMIN_USER}"
echo ""
echo "================================================================"
echo ""
echo "[INFO] Save the backend client secret in a secure location!"
echo "[INFO] Update your application configurations with the above details."
