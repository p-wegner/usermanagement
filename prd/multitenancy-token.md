# Keycloak Token Contents: Multitenancy Context

This document describes the structure and contents of the Keycloak access token (JWT) as used in the multitenancy user management system. It explains which claims are present, how multitenancy context is encoded, and how applications and APIs can use this information for authorization and scoping.

---

## 1. Standard Token Claims

A Keycloak access token is a JSON Web Token (JWT) containing standard OpenID Connect claims, such as:

| Claim         | Description                                 | Example Value                |
|---------------|---------------------------------------------|------------------------------|
| `sub`         | Subject (user ID)                           | `e3b0c442-98fc-1fc2-9c2d...` |
| `preferred_username` | Username                             | `alice`                      |
| `email`       | User's email address                        | `alice@ikea.com`             |
| `name`        | Full name                                   | `Alice Smith`                |
| `given_name`  | First name                                  | `Alice`                      |
| `family_name` | Last name                                   | `Smith`                      |
| `realm_access.roles` | List of realm roles assigned         | `["ROLE_ADMIN"]`             |
| `resource_access`    | Map of client roles per client       | `{ "ComplexApp": { "roles": [...] } }` |
| `exp`         | Expiration timestamp                        | `1713200000`                 |
| `iat`         | Issued-at timestamp                         | `1713196400`                 |

---

## 2. Multitenancy-Specific Claims

To support multitenancy, the following information is included in the token, either as custom claims or as part of the roles:

### a. Customer and Tenant Context

- **Customer and tenant information** is typically encoded in custom claims or as part of role names.
- Example custom claims (if configured via Keycloak protocol mappers):

    ```json
    {
      "customer": "IKEA",
      "tenant": "TenantA"
    }
    ```

- If not present as custom claims, the customer and tenant context can be inferred from assigned roles or group membership.

### b. Roles and Permissions

- **Realm Roles**: Found in `realm_access.roles`. Used for system-wide or customer admin roles.
- **Client Roles**: Found in `resource_access[client].roles`. Used for application-specific, customer- or tenant-scoped roles.

  Example:
    ```json
    "realm_access": {
      "roles": [
        "ROLE_ADMIN",
        "IKEA_usermanagement_admin"
      ]
    },
    "resource_access": {
      "ComplexApp": {
        "roles": [
          "IKEA_TenantA_usermanagement_admin",
          "IKEA_TenantA_WarehouseManager"
        ]
      }
    }
    ```

- **Role Naming Convention**: Roles are named to encode customer and tenant context, e.g.:
    - `IKEA_usermanagement_admin` (customer admin)
    - `IKEA_TenantA_usermanagement_admin` (tenant admin)
    - `IKEA_TenantA_WarehouseManager` (tenant-scoped permission)

### c. Group Membership

- The token may include a `groups` claim listing the full paths of groups the user belongs to:

    ```json
    "groups": [
      "/IKEA",
      "/IKEA/TenantA",
      "/IKEA/TenantA/Team1"
    ]
    ```

- Applications can use group paths and group attributes (e.g., `groupType`) to determine the user's customer, tenant, and group context.

---

## 3. Example Token Payload

Below is an example of a decoded Keycloak access token payload for a user who is a tenant admin in `IKEA/TenantA`:

```json
{
  "sub": "e3b0c442-98fc-1fc2-9c2d-7a1b2c3d4e5f",
  "preferred_username": "alice",
  "email": "alice@ikea.com",
  "name": "Alice Smith",
  "realm_access": {
    "roles": [
      "IKEA_usermanagement_admin"
    ]
  },
  "resource_access": {
    "ComplexApp": {
      "roles": [
        "IKEA_TenantA_usermanagement_admin",
        "IKEA_TenantA_WarehouseManager"
      ]
    }
  },
  "groups": [
    "/IKEA",
    "/IKEA/TenantA"
  ],
  "customer": "IKEA",
  "tenant": "TenantA",
  "exp": 1713200000,
  "iat": 1713196400
}
```

---

## 4. How Applications and APIs Use Token Claims

- **Scoping and Isolation**: APIs use the `groups`, `customer`, and `tenant` claims (or infer from roles) to restrict access to resources within the user's permitted scope.
- **Role-Based Access Control**: Applications check for specific roles in `realm_access` and `resource_access` to determine permissions (e.g., admin, manager).
- **Delegated Administration**: The presence of roles like `IKEA_TenantA_usermanagement_admin` enables tenant-level admin features in the UI and API.

---

## 5. Custom Claims and Protocol Mappers

- To include custom claims (such as `customer` and `tenant`), configure Keycloak protocol mappers for the client.
- These claims can be mapped from group membership, user attributes, or other sources.

---

## 6. Security Considerations

- **Never trust only the token**: Always validate the token signature and expiration.
- **Enforce scoping in the backend**: Do not rely solely on frontend checks; always enforce customer/tenant isolation in backend APIs.
- **Minimal Claims Principle**: Only include necessary claims to avoid leaking sensitive information.

---

## 7. Summary Table: Key Token Claims

| Claim/Section         | Purpose                                    | Example Value / Structure         |
|-----------------------|--------------------------------------------|-----------------------------------|
| `sub`                 | User ID                                    | `e3b0c442-...`                    |
| `preferred_username`  | Username                                   | `alice`                           |
| `email`               | Email address                              | `alice@ikea.com`                  |
| `realm_access.roles`  | System/customer roles                      | `["ROLE_ADMIN", "IKEA_usermanagement_admin"]` |
| `resource_access`     | Application/tenant roles                   | `{ "ComplexApp": { "roles": [...] } }` |
| `groups`              | Group membership paths                     | `["/IKEA", "/IKEA/TenantA"]`      |
| `customer` (custom)   | Customer context                           | `IKEA`                            |
| `tenant` (custom)     | Tenant context                             | `TenantA`                         |

---