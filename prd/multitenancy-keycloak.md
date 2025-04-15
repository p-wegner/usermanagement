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
- There must be support for hierarchical group structures, including the possibility to assign an admin role for a group, who can only manage users/roles/groups below their group.

---

## 2. Requirements

### 2.1 Multitenancy Model

- **Single Keycloak Realm**: All customers and tenants are managed within one realm for operational simplicity.
- **Customer Isolation**: No cross-customer visibility or access.
- **Tenant Isolation**: Tenants within a customer are isolated from each other unless explicitly allowed.

### 2.2 Dynamic Role Management

- **ComplexApp** must be able to create client roles dynamically for each tenant (e.g., Warehouse_Manager, Picker).
- Roles must be scoped to the correct tenant and customer.
- e.g. IKEA_TenantA_Warehouse_Manager, IKEA_TenantB_Picker
- Simpler applications use a fixed, pre-defined set of roles per customer.

### 2.3 Hierarchical Group Structure and Subadmins

- Support for nested groups:
    - Top-level group per customer (e.g., IKEA)
    - Subgroups per tenant (e.g., TenantA, TenantB)
    - Further subgroups for admin roles (e.g., usermanagement-admins)
    - **Hierarchical subadmins**: Each group (at any level) can have a corresponding admin role (e.g., `IKEA_TenantA_usermanagement_admin`) whose members are delegated admin rights for that subtree.
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

## 3. Keycloak Mapping

### 3.1 Groups

- **Customer Group**: Top-level group for each customer (e.g., `/IKEA`)
- **Tenant Group**: Subgroup under customer for each tenant (e.g., `/IKEA/TenantA`)
- **Functional Subgroups**: Further subgroups for functional areas (e.g., `/IKEA/TenantA/Logistics`)
- **Admin Groups**: For each group that needs delegated administration, create an admin subgroup (e.g., `/IKEA/TenantA/usermanagement-admins`) that grants admin rights for that subtree.
- **Group Attributes**: Use attributes to mark groups as customers, tenants, admin areas, or to store metadata (e.g., `isTenant`, `adminGroup`, etc.).

### 3.2 Clients

- **Application Client**: Each application (e.g., ComplexApp) is a Keycloak client.
- **Client Roles**: Roles specific to an application and tenant/group are created as client roles under the application client.

### 3.3 Roles

- **Realm Roles**: Used for global roles (e.g., SYSTEM_ADMIN).
- **Client Roles**: Used for application-, tenant-, and group-specific roles (e.g., Warehouse_Manager for TenantA in ComplexApp).
- **Role Naming Convention**: Use a naming pattern to encode customer, tenant, and optionally group (e.g., `IKEA_TenantA_Logistics_Warehouse_Manager`).
- **Admin Roles**: Create specific roles for delegated administration (e.g., `TENANT_ADMIN_IKEA_TenantA`) that grant permissions to manage a specific subtree.

### 3.4 User Assignment

- Users are assigned to the appropriate group(s) and granted roles via group membership or direct assignment.
- Group membership determines visibility and management scope in the UI and API.
- **Subadmin Assignment**: Users assigned to an admin subgroup (e.g., `/IKEA/TenantA/usermanagement-admins`) are granted delegated admin rights for that subtree.

### 3.5 Attributes

- Use group and user attributes to store metadata (e.g., tenant ID, customer ID, admin flags, group type).
- Attributes can be used for filtering and authorization decisions.

---

## 4. Example: IKEA Customer with ComplexApp

- **Groups**:
    - `/IKEA` (customer group)
    - `/IKEA/TenantA` (tenant group)
    - `/IKEA/TenantA/Logistics` (functional group)
    - `/IKEA/TenantA/Logistics/logistics-admins` (subadmin group for Logistics)
    - `/IKEA/TenantA/usermanagement-admins` (admins for TenantA)
- **Roles**:
    - Client roles for ComplexApp: `Warehouse_Manager`, `Picker` (created dynamically under ComplexApp client, scoped to TenantA or a specific group)
    - Realm/client roles for simpler apps: `IKEA_Editor`
    - **Group-scoped roles**: e.g., `IKEA_TenantA_Logistics_Warehouse_Manager`
    - **Admin roles**: e.g., `TENANT_ADMIN_IKEA_TenantA`, `GROUP_ADMIN_IKEA_TenantA_Logistics`
- **Users**:
    - Assigned to `/IKEA/TenantA`, `/IKEA/TenantA/Logistics`, and/or `/IKEA/TenantA/Logistics/logistics-admins`
    - Receive roles based on group membership and/or direct assignment
    - Users in an admin subgroup (e.g., `/IKEA/TenantA/Logistics/logistics-admins`) can manage users, groups, and permissions within `/IKEA/TenantA/Logistics` and its descendants

---

## 5. Security and Isolation

- API must enforce that users can only see/manage users, roles, and groups within their permitted subtree.
- **Hierarchical subadmin enforcement**: Subadmins (members of admin subgroups) can only manage users, groups, and permissions within their subtree.
- **Per-group permission enforcement**: For ComplexApp, permissions can be managed at any group level, and APIs must enforce that only authorized admins can modify group permissions.
- Group-based scoping is used for hierarchical admin delegation (e.g., usermanagement-admins).
- Role and group assignments are validated to prevent privilege escalation or cross-tenant access.
- **Token claims**: Include tenant and customer information in tokens to enable application-level authorization.

---

## 6. Implementation Notes

- Dynamic client role creation can be performed via Keycloak Admin REST API by ComplexApp.
- Group and role management APIs must enforce all scoping and isolation rules.
- **Hierarchical subadmin APIs**: Implement endpoints that allow subadmins to manage their subtree, including user, group, and permission management.
- **Per-group permission APIs**: Implement endpoints for querying and updating permissions for any group, with proper authorization checks.
- UI must use the backend API to determine what to display/manage for the current user.
- **Performance considerations**: Implement caching for frequently accessed group hierarchies and role assignments.

---

## 7. Open Questions and Decisions

- **Role Sharing**: Tenants can share roles within the same customer/organization. Users can be assigned roles that belong to any tenant within their organization.
- **Onboarding Process**: The process for onboarding a new customer or tenant needs to be defined, including:
  - Creation of top-level customer group
  - Creation of tenant subgroups
  - Assignment of initial admin users
  - Creation of required roles and permissions
- **Admin Visibility**: Parent/ancestor admins should have visibility into group-scoped permissions of their descendants, with the ability to override if necessary.

---
