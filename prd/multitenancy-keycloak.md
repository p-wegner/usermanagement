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
- Customers and tenants must be strictly isolated: users, roles, and groups of one customer/tenant must not be visible or manageable by others.
- The UI must allow end users to manage users, roles, and groups within their permitted scope.
- There must be support for hierarchical group structures, including subgroups like usermanagement-admins, who can only manage users/roles/groups below their group.

---

## 2. Requirements

### 2.1 Multitenancy Model

- **Single Keycloak Realm**: All customers and tenants are managed within one realm for operational simplicity.
- **Customer Isolation**: No cross-customer visibility or access.
- **Tenant Isolation**: Tenants within a customer are isolated from each other unless explicitly allowed.

### 2.2 Dynamic Role Management

- **ComplexApp** must be able to create client roles dynamically for each tenant (e.g., Warehouse_Manager, Picker).
- Roles must be scoped to the correct tenant and customer.
- Simpler applications use a fixed, pre-defined set of roles per customer.

### 2.3 Hierarchical Group Structure

- Support for nested groups:
    - Top-level group per customer (e.g., IKEA)
    - Subgroups per tenant (e.g., TenantA, TenantB)
    - Further subgroups for functional areas (e.g., usermanagement-admins)
- Group-based access control: usermanagement-admins can only manage users/roles/groups within their subtree.

### 2.4 User and Role Management

- Users are assigned to groups representing their customer, tenant, and functional area.
- Roles (realm or client) are assigned at the group or user level, as appropriate.
- It must be possible to enumerate and manage users, roles, and groups within the permitted scope.

### 2.5 API and UI Integration

- The backend API must enforce all isolation and scoping rules.
- The UI must only display users, roles, and groups the current user is allowed to see/manage.

---

## 3. Keycloak Mapping

### 3.1 Groups

- **Customer Group**: Top-level group for each customer (e.g., `/IKEA`)
- **Tenant Group**: Subgroup under customer for each tenant (e.g., `/IKEA/TenantA`)
- **Functional Subgroups**: Further subgroups for admin roles (e.g., `/IKEA/TenantA/usermanagement-admins`)
- **Group Attributes**: Use attributes to mark groups as customers, tenants, or admin areas.

### 3.2 Clients

- **Application Client**: Each application (e.g., ComplexApp) is a Keycloak client.
- **Client Roles**: Roles specific to an application and tenant are created as client roles under the application client.

### 3.3 Roles

- **Realm Roles**: Used for global roles (e.g., SYSTEM_ADMIN).
- **Client Roles**: Used for application- and tenant-specific roles (e.g., Warehouse_Manager for TenantA in ComplexApp).
- **Role Naming Convention**: Use a naming pattern to encode customer and tenant (e.g., `IKEA_TenantA_Warehouse_Manager`).

### 3.4 User Assignment

- Users are assigned to the appropriate group(s) and granted roles via group membership or direct assignment.
- Group membership determines visibility and management scope in the UI and API.

### 3.5 Attributes

- Use group and user attributes to store metadata (e.g., tenant ID, customer ID, admin flags).

---

## 4. Example: IKEA Customer with ComplexApp

- **Groups**:
    - `/IKEA` (customer group)
    - `/IKEA/TenantA` (tenant group)
    - `/IKEA/TenantA/usermanagement-admins` (admins for TenantA)
- **Roles**:
    - Client roles for ComplexApp: `Warehouse_Manager`, `Picker` (created dynamically under ComplexApp client, scoped to TenantA)
    - Realm/client roles for simpler apps: `IKEA_Editor`
- **Users**:
    - Assigned to `/IKEA/TenantA` and/or `/IKEA/TenantA/usermanagement-admins`
    - Receive roles based on group membership and/or direct assignment

---

## 5. Security and Isolation

- API must enforce that users can only see/manage users, roles, and groups within their permitted subtree.
- Group-based scoping is used for hierarchical admin delegation (e.g., usermanagement-admins).
- Role and group assignments are validated to prevent privilege escalation or cross-tenant access.

---

## 6. Implementation Notes

- Dynamic client role creation can be performed via Keycloak Admin REST API by ComplexApp.
- Group and role management APIs must enforce all scoping and isolation rules.
- UI must use the backend API to determine what to display/manage for the current user.

---

## 7. Open Questions

- Should tenants be allowed to share roles or users?
- How to handle cross-tenant or cross-customer reporting (if needed)?
- What is the process for onboarding a new customer or tenant?

---

<aider-summary>
- Wrote a detailed Product Requirements Document (PRD) in markdown for supporting multitenancy in a single Keycloak realm.
- The PRD covers requirements for dynamic client role creation, customer/tenant isolation, hierarchical group structure, and user/role management.
- It maps requirements to Keycloak concepts: groups, clients, roles, client roles, and attributes.
- Includes an example for the IKEA customer and outlines security, isolation, and implementation notes.
</aider-summary>
