#!/bin/bash

# Keycloak settings
KEYCLOAK_URL="http://localhost:8081"
REALM="master"
FRONTEND_CLIENT_ID="keycloak-wrapper-frontend"
BACKEND_CLIENT_ID="keycloak-wrapper-backend"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# Get admin token
echo "Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USERNAME" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

# Create frontend client (public)
echo "Creating frontend client..."
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "'$FRONTEND_CLIENT_ID'",
    "enabled": true,
    "protocol": "openid-connect",
    "publicClient": true,
    "standardFlowEnabled": true,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": true,
    "serviceAccountsEnabled": false,
    "authorizationServicesEnabled": false,
    "redirectUris": [
      "http://localhost:4200/*"
    ],
    "webOrigins": [
      "http://localhost:4200",
      "+"
    ]
  }'

# Create backend client (confidential)
echo "Creating backend client..."
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "'$BACKEND_CLIENT_ID'",
    "enabled": true,
    "protocol": "openid-connect",
    "publicClient": false,
    "standardFlowEnabled": false,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": false,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "clientAuthenticatorType": "client-secret"
  }'

# Get backend client UUID
echo "Getting backend client details..."
BACKEND_CLIENT_UUID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | grep -o '"id":"[^"]*","clientId":"'$BACKEND_CLIENT_ID'"' | cut -d'"' -f4)

if [ -z "$BACKEND_CLIENT_UUID" ]; then
  echo "Failed to get backend client UUID"
  exit 1
fi

# Get backend client secret
echo "Getting backend client secret..."
BACKEND_CLIENT_SECRET=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM}/clients/${BACKEND_CLIENT_UUID}/client-secret" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | grep -o '"value":"[^"]*"' | cut -d'"' -f4)

if [ -z "$BACKEND_CLIENT_SECRET" ]; then
  echo "Failed to get backend client secret"
  exit 1
fi

echo "Clients created successfully!"
echo "Frontend Client ID: ${FRONTEND_CLIENT_ID}"
echo "Backend Client ID: ${BACKEND_CLIENT_ID}"
echo "Backend Client Secret: ${BACKEND_CLIENT_SECRET}"

# Update application.properties
echo "Updating application.properties..."
sed -i "s/keycloak.resource=.*/keycloak.resource=${FRONTEND_CLIENT_ID}/" ../keycloak-wrapper/src/main/resources/application.properties
sed -i "s/keycloak.service-client.id=.*/keycloak.service-client.id=${BACKEND_CLIENT_ID}/" ../keycloak-wrapper/src/main/resources/application.properties
sed -i "s/keycloak.service-client.secret=.*/keycloak.service-client.secret=${BACKEND_CLIENT_SECRET}/" ../keycloak-wrapper/src/main/resources/application.properties

echo "Setup complete!"
