C:\Program Files\Git\git-bash.exe

# Keycloak settings
KEYCLOAK_URL="http://localhost:8081"
REALM="master"
CLIENT_ID="keycloak-wrapper-client"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# Get admin token
echo "Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${ADMIN_USERNAME}" \
  -d "password=${ADMIN_PASSWORD}" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

# Create client
echo "Creating client..."
CLIENT_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "'"${CLIENT_ID}"'",
    "enabled": true,
    "protocol": "openid-connect",
    "publicClient": false,
    "standardFlowEnabled": true,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "directAccessGrantsEnabled": true,
    "clientAuthenticatorType": "client-secret",
    "redirectUris": [
      "http://localhost:8080/*",
      "http://localhost:8080/swagger-ui/oauth2-redirect.html"
    ],
    "webOrigins": [
      "http://localhost:8080",
      "+"
    ]
  }')

# Get client ID
echo "Getting client details..."
CLIENT_UUID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | jq -r '.[] | select(.clientId=="'"${CLIENT_ID}"'") | .id')

if [ -z "$CLIENT_UUID" ]; then
  echo "Failed to get client UUID"
  exit 1
fi

# Get client secret
echo "Getting client secret..."
CLIENT_SECRET=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM}/clients/${CLIENT_UUID}/client-secret" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  | jq -r '.value')

if [ -z "$CLIENT_SECRET" ]; then
  echo "Failed to get client secret"
  exit 1
fi

echo "Client created successfully!"
echo "Client ID: ${CLIENT_ID}"
echo "Client Secret: ${CLIENT_SECRET}"

# Update application.properties
echo "Updating application.properties..."
sed -i "s/your-client-secret/${CLIENT_SECRET}/g" ../keycloak-wrapper/src/main/resources/application.properties

echo "Setup complete!"
