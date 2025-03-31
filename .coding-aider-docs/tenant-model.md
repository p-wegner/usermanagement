# Tenant Model in Keycloak Wrapper

## Overview

This document describes how multi-tenancy is implemented in the Keycloak Wrapper application using Keycloak's group functionality.

## Tenant Structure

Tenants are modeled as top-level Keycloak groups with a specific naming convention:

- Tenant groups are named with the prefix `tenant_` (e.g., `tenant_acme`)
- Each tenant has a display name stored as a group attribute
- Within each tenant group, subgroups represent permission sets based on client roles

```
tenant_acme (Top-level tenant group)
├── user_manager (Permission subgroup)
├── report_viewer (Permission subgroup)
├── admin (Permission subgroup)
└── acme_admins (Tenant admin group)
```

## Tenant Creation and Management

When a new tenant is created:

1. A top-level group with the prefix `tenant_` is created
2. Subgroups are automatically created for each client role
3. A special admin group with suffix `_admins` is created for tenant administrators

When client roles change:
- New client roles trigger creation of corresponding subgroups in all tenant groups
- Deleted client roles result in removal of corresponding subgroups

## Tenant Admins

Tenant administrators are users who can manage a specific tenant but not others. They are implemented as follows:

1. Users assigned as tenant admins:
   - Receive the `TENANT_ADMIN` realm role
   - Are added to the tenant's admin group (e.g., `tenant_acme_admins`)

2. Tenant admin capabilities:
   - Can view and manage users within their tenant
   - Can view and manage groups within their tenant
   - Cannot access users or groups from other tenants

3. Access control:
   - `TenantSecurityEvaluator` enforces tenant-specific access rules
   - `TenantService` provides methods to check tenant access permissions
   - User filtering is applied based on tenant membership

## User-Tenant Relationship

Users are associated with tenants through group membership:
- A user can belong to multiple tenants
- A user can have different permissions in different tenants
- Tenant admins can only see and manage users in their assigned tenants

## API Endpoints

Tenant management is exposed through several API endpoints:
- `/api/tenants` - CRUD operations for tenants
- `/api/tenant-admins` - Manage tenant admin assignments
- `/api/groups/tenant` - Alternative tenant management endpoints

## Security Implications

This tenant model has several security consequences:

1. Tenant isolation is enforced at the application level, not by Keycloak directly
2. System administrators can access all tenants
3. Tenant administrators are restricted to their assigned tenants
4. Regular users can only access resources within their tenant groups
5. Tenant membership determines data visibility and access permissions
