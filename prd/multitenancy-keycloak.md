# Product Requirements Document (PRD): Multitenancy Support in Keycloak

## Overview

This document outlines the requirements and design for supporting multitenancy in a single Keycloak realm, with a focus on:
- Dynamic client role creation for tenants (especially for ComplexApp)
- Customer and tenant isolation
- Hierarchical group and role management
- Support for both complex and simple applications
- Mapping requirements to Keycloak's groups, clients, roles, client roles, and attributes

---

## 1. Problem Statement

- We need to support multiple customers (e.g., IKEA, Walmart) within a single Keycloak realm.
- Each customer may have multiple tenants (e.g., IKEA → TenantA, TenantB).
- Applications (such as ComplexApp) must be able to dynamically create roles for tenants (e.g., Warehouse_Manager, Picker for TenantA).
- Simpler applications require only a fixed set of roles per customer (e.g., IKEA_Editor).
- Customers must be strictly isolated: users, roles, tenants and groups of one customer must not be visible or manageable by others.
- The UI must allow end users to manage users, roles, and groups within their permitted scope.
- There must be support for hierarchical admin structures, including the possibility to assign an admin role for sub-structures below like tenants, who can only manage users/roles/groups below their respective hierarchy.

---

## 2. Requirements

### 2.1 Multitenancy Model

- **Single Keycloak Realm**: All customers and tenants are managed within one realm for operational simplicity.
- **Customer Isolation**: No cross-customer visibility or access.
- **Tenant Isolation**: Tenants within a customer are isolated from each other unless explicitly allowed. 
  - Customer admins can manage users/roles/groups below their respective hierarchy, i.e. all tenants, roles, users and groups that belong to the customer.
  - Tenant admins can manage users/roles/groups below their respective hierarchy.

### 2.2 Dynamic Role Management

- **ComplexApp** must be able to create client roles dynamically for each tenant (e.g., Warehouse_Manager, Picker).
- Roles must be scoped to the correct tenant and customer.
- e.g. IKEA_TenantA_Warehouse_Manager, IKEA_TenantB_Picker
- Simpler applications use a fixed, pre-defined set of roles per customer.

### 2.3 Hierarchical Group Structure and Subadmins

- Support for nested groups:
    - Top-level group per customer (e.g., IKEA)
    - Subgroups per tenant (e.g., TenantA, TenantB)
    - **Hierarchical subadmins**: Each group (at any level) can have a corresponding admin client role (e.g., `IKEA_TenantA_usermanagement_admin`) whose members are delegated admin rights for that subtree.
- **Delegated Administration**: Subadmins can only manage users/roles/groups within their subtree. This enables fine-grained, hierarchical delegation of admin rights (e.g., a group admin can manage only their group and its descendants).

### 2.4 Per-Group Permissions for ComplexApp

- **ComplexApp** requires permissions to be managed at the group level:
    - Each group (e.g., functional area, team, or tenant) can have its own set of client roles/permissions.
    - Permissions can be assigned to groups, and users inherit permissions from their group memberships.
    - **Group-scoped permissions**: ComplexApp must be able to enumerate, assign, and revoke permissions for any group, not just tenants.
    - **UI/UX**: The UI must allow admins to manage permissions for any group they administer, reflecting the group hierarchy.

### 2.5 User and Role Management

- Users are assigned to groups representing their customer, tenant, and functional area.
- Roles (realm or client) are assigned at the group or user level, as appropriate.
- It must be possible to enumerate and manage users, roles, and groups within the permitted scope.

### 2.6 API and UI Integration

- The backend API must enforce all isolation and scoping rules.
- The UI must only display users, roles, and groups the current user is allowed to see/manage.
- **APIs for Hierarchical Admins**: The API must provide endpoints for subadmins to manage their subtree, including user, group, and permission management.
- **APIs for Group Permissions**: The API must allow querying and updating permissions for any group, not just tenants.

---

## 3. Keycloak Concepts Mapping to Requirements

This section clarifies how Keycloak's core concepts are used to model the requirements described above. Each requirement is mapped to a specific Keycloak feature, with naming conventions and usage patterns made explicit.

### 3.1 Clients: Modeling Applications

- **Application Client**: Each application (e.g., ComplexApp, SimpleApp) is represented as a Keycloak client. This allows for application-specific roles and permissions.
- **Client Roles**: Roles that are specific to an application, tenant, or group are created as client roles under the relevant Keycloak client. This enables fine-grained, app-specific access control.

**Example:**
- `ComplexApp` client in Keycloak
- Roles like `IKEA_TenantA_Warehouse_Manager` are created as client roles under the `ComplexApp` client.

### 3.2 Roles and Groups: Modeling Permissions and Delegated Administration

- **Realm Roles**: Used for global or cross-application roles (e.g., `SYSTEM_ADMIN`). These are not tenant- or customer-specific.
- **Client Roles**: Used for application-, tenant-, and group-specific permissions. Naming conventions encode the scope (e.g., `IKEA_TenantA_Warehouse_Manager`).
- **Uniform Permission Modeling**: Permissions are modeled in a clear and uniform way using roles (preferably client roles). Each permission is represented as a role, and roles are assigned directly to users or to groups (which then propagate to users via group membership).
- **Role Naming Convention**: Roles are named to reflect their scope and purpose, using the pattern:  
  `<Customer>_<Tenant>_<PermissionName>`  
  Examples:  
  - `IKEA_TenantA_Warehouse_Manager` (tenant-scoped)  
  - `IKEA_TenantA_Picker` (tenant-scoped)
- **Admin Roles**: Special roles for delegated administration, such as `TENANT_ADMIN_IKEA_TenantA`. These roles are assigned to users who need to manage a specific subtree.

**Summary Table:**

| Requirement                | Keycloak Role Type | Example Role Name                   |
|----------------------------|-------------------|-------------------------------------|
| Global Admin               | Realm Role        | `SYSTEM_ADMIN`                      |
| Customer Admin             | Realm Role        | `IKEA_Usermanagement_Admin`         |
| Tenant Admin               | Client Role       | `IKEA_TenantA_Usermanagement_Admin` |
| Permission (tenant-scoped) | Client Role       | `IKEA_TenantA_Warehouse_Manager` :  |


## 4. Example: IKEA Customer with ComplexApp

- **Groups**:
    - `/IKEA` (customer group)
    - `/IKEA/TenantA` (tenant group)
- **Roles**:
    - Client roles for ComplexApp: `Warehouse_Manager`, `Picker` (created dynamically under ComplexApp client, scoped to TenantA)
    - Realm/client roles for simpler apps: `IKEA_Editor`
    - **Tenant-scoped roles**: e.g., `IKEA_TenantA_Warehouse_Manager`
    - **Tenant-scoped Admin roles**: e.g., `IKEA_TenantA_Usermanagement_admin` (tenant-scoped)`
- **Users**:
    - Receive roles based on group membership and/or direct assignment

---

## 5. Security and Isolation

- API must enforce that users can only see/manage users, roles, and groups within their permitted subtree.
- **Hierarchical subadmin enforcement**: Tenant or Customer Scoped Subadmins  can only manage users, groups, and permissions within their subtree.
- **Per-group permission enforcement**: For ComplexApp, permissions can be managed at any group level, and APIs must enforce that only authorized admins can modify group permissions.
- **Customer admin isolation**: Customer admins using the API should only see their respective client roles, users, and groups. The API must filter all responses to include only resources within the admin's scope.
- Role and group assignments are validated to prevent privilege escalation or cross-tenant access.
- **Token claims**: Include tenant and customer information in tokens to enable application-level authorization.

---

## 6. Implementation Notes

- Dynamic client role creation can be performed via Keycloak Admin REST API by ComplexApp.
- Group and role management APIs must enforce all scoping and isolation rules.
- **Hierarchical subadmin APIs**: Implement endpoints that allow subadmins to manage their subtree, including user, group, and permission management.
- **Per-group permission APIs**: Implement endpoints for querying and updating permissions for any group, with proper authorization checks.
- **Scoped API responses**: All API endpoints must filter responses based on the caller's scope. Customer admins should only receive data for their respective client roles, users, and groups.
- UI must use the backend API to determine what to display/manage for the current user.
- **Performance considerations**: Implement caching for frequently accessed group hierarchies and role assignments.

---

## 7. Open Questions and Decisions

- **Role Sharing**: Users can be assigned roles that belong to any tenant within their customer.
- **Onboarding Process**: The process for onboarding a new customer or tenant needs to be defined, including:
  - Creation of top-level customer group
  - Creation of tenant subgroups
  - Assignment of initial admin users
  - Creation of required roles and permissions
- **Admin Visibility**: Parent/ancestor admins should have visibility into group-scoped permissions of their descendants, with the ability to override if necessary.

---
