# Keycloak Wrapper API Documentation

## Overview

This document provides comprehensive documentation for the Keycloak Wrapper API, including endpoint descriptions, request/response formats, and authentication requirements.

## Authentication

All API endpoints require OAuth2 authentication. Include a valid Bearer token in the Authorization header:

```
Authorization: Bearer <token>
```

## Common Response Format

All API endpoints return responses in a standard format:

```json
{
  "success": true|false,
  "data": <response data>,
  "error": "<error message if success is false>"
}
```

## Endpoints

### Tenant Management

#### GET /api/tenants

Returns all tenants the current user has access to.

**Authorization:** Requires ADMIN or TENANT_ADMIN role

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "tenant-id",
      "name": "tenant_CustomerA",
      "path": "/tenant_CustomerA",
      "subGroups": [...],
      "realmRoles": [...],
      "isTenant": true,
      "tenantName": "Customer A"
    },
    ...
  ]
}
```

#### GET /api/tenants/{id}

Returns a specific tenant by ID.

**Authorization:** Requires ADMIN or TENANT_ADMIN role with access to the specified tenant

**Parameters:**
- `id` (path): ID of the tenant to retrieve

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "tenant-id",
    "name": "tenant_CustomerA",
    "path": "/tenant_CustomerA",
    "subGroups": [...],
    "realmRoles": [...],
    "isTenant": true,
    "tenantName": "Customer A"
  }
}
```

#### POST /api/tenants

Creates a new tenant.

**Authorization:** Requires ADMIN role

**Request Body:**
```json
{
  "name": "CustomerB",
  "displayName": "Customer B"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "new-tenant-id",
    "name": "tenant_CustomerB",
    "path": "/tenant_CustomerB",
    "subGroups": [...],
    "realmRoles": [...],
    "isTenant": true,
    "tenantName": "Customer B"
  }
}
```

#### PUT /api/tenants/{id}

Updates a tenant's display name.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role with management access to the specified tenant

**Parameters:**
- `id` (path): ID of the tenant to update

**Request Body:**
```json
{
  "displayName": "Updated Customer Name"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "tenant-id",
    "name": "tenant_CustomerA",
    "path": "/tenant_CustomerA",
    "subGroups": [...],
    "realmRoles": [...],
    "isTenant": true,
    "tenantName": "Updated Customer Name"
  }
}
```

#### DELETE /api/tenants/{id}

Deletes a tenant and all its subgroups.

**Authorization:** Requires ADMIN role

**Parameters:**
- `id` (path): ID of the tenant to delete

**Response:**
```json
{
  "success": true
}
```

#### GET /api/tenants/{id}/users

Returns all users belonging to a specific tenant.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role with access to the specified tenant

**Parameters:**
- `id` (path): ID of the tenant
- `page` (query, optional): Page number (zero-based, default: 0)
- `size` (query, optional): Page size (default: 20)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "user-id",
      "username": "user1",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "enabled": true,
      "isTenantAdmin": false
    },
    ...
  ]
}
```

#### GET /api/tenants/{id}/statistics

Returns statistics for a specific tenant.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role with access to the specified tenant

**Parameters:**
- `id` (path): ID of the tenant

**Response:**
```json
{
  "success": true,
  "data": {
    "tenantId": "tenant-id",
    "tenantName": "tenant_CustomerA",
    "userCount": 25,
    "activeUserCount": 20,
    "groupCount": 5,
    "roleCount": 10,
    "adminCount": 3
  }
}
```

### User Management

#### GET /api/users

Returns a paginated list of users.

**Authorization:** Requires ADMIN or TENANT_ADMIN role

**Parameters:**
- `page` (query, optional): Page number (zero-based, default: 0)
- `size` (query, optional): Page size (default: 20)
- `search` (query, optional): Search string to filter users
- `tenantId` (query, optional): Filter users by tenant ID

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "user-id",
        "username": "user1",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "enabled": true,
        "realmRoles": ["TENANT_ADMIN"],
        "clientRoles": {},
        "isTenantAdmin": true,
        "managedTenants": ["tenant-id-1", "tenant-id-2"],
        "tenantId": "tenant-id-1"
      },
      ...
    ],
    "total": 100
  }
}
```

#### GET /api/users/{id}

Returns a specific user by ID.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role with access to the user's tenant

**Parameters:**
- `id` (path): ID of the user to retrieve

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "user-id",
    "username": "user1",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "enabled": true,
    "realmRoles": ["TENANT_ADMIN"],
    "clientRoles": {},
    "isTenantAdmin": true,
    "managedTenants": ["tenant-id-1", "tenant-id-2"],
    "tenantId": "tenant-id-1"
  }
}
```

#### POST /api/users

Creates a new user.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role

**Request Body:**
```json
{
  "username": "newuser",
  "firstName": "New",
  "lastName": "User",
  "email": "new.user@example.com",
  "password": "password123",
  "enabled": true,
  "realmRoles": ["USER"],
  "tenantId": "tenant-id"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "new-user-id",
    "username": "newuser",
    "firstName": "New",
    "lastName": "User",
    "email": "new.user@example.com",
    "enabled": true,
    "realmRoles": ["USER"],
    "clientRoles": {},
    "isTenantAdmin": false,
    "managedTenants": [],
    "tenantId": "tenant-id"
  }
}
```

### Tenant Admin Management

#### POST /api/tenant-admins

Assigns a user as an administrator for a specific tenant.

**Authorization:** Requires ADMIN role

**Request Body:**
```json
{
  "userId": "user-id",
  "tenantId": "tenant-id"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "user-id",
    "username": "admin1",
    "tenantId": "tenant-id",
    "tenantName": "tenant_CustomerA"
  }
}
```

#### GET /api/tenant-admins/tenants/{tenantId}

Returns all administrators for a specific tenant.

**Authorization:** Requires ADMIN role or TENANT_ADMIN role with access to the specified tenant

**Parameters:**
- `tenantId` (path): ID of the tenant

**Response:**
```json
{
  "success": true,
  "data": {
    "tenantId": "tenant-id",
    "tenantName": "tenant_CustomerA",
    "admins": [
      {
        "id": "user-id-1",
        "username": "admin1",
        "firstName": "Admin",
        "lastName": "One",
        "email": "admin1@example.com",
        "enabled": true,
        "isTenantAdmin": true
      },
      ...
    ]
  }
}
```

## Error Handling

When an error occurs, the API returns a response with `success: false` and an error message:

```json
{
  "success": false,
  "error": "Detailed error message"
}
```

Common HTTP status codes:
- 400: Bad Request - Invalid input data
- 401: Unauthorized - Missing or invalid authentication
- 403: Forbidden - Insufficient permissions
- 404: Not Found - Resource not found
- 500: Internal Server Error - Unexpected server error
