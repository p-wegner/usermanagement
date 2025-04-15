[Coding Aider Plan]

# Multitenancy Support in Keycloak

## Overview

This plan describes the implementation of multitenancy support in a single Keycloak realm, focusing on dynamic client role creation for tenants, customer and tenant isolation, hierarchical group structure, and mapping requirements to Keycloak concepts. The plan is based on the PRD and requirements outlined in `prd/multitenancy-keycloak.md`.

## Problem Description

- Multiple customers (e.g., IKEA, Walmart) must be supported within a single Keycloak realm.
- Each customer can have multiple tenants (e.g., IKEA → TenantA, TenantB).
- Complex applications (e.g., ComplexApp) require the ability to dynamically create client roles for tenants.
- Simpler applications require a fixed set of roles per customer.
- Customers and tenants must be strictly isolated: users, roles, and groups of one customer/tenant must not be visible or manageable by others.
- The UI must allow end users to manage users, roles, and groups within their permitted scope.
- Hierarchical group structures are required, including subgroups like usermanagement-admins, who can only manage users/roles/groups below their group.

## Goals

- Support dynamic client role creation for tenants by applications.
- Enforce customer and tenant isolation at the API and UI level.
- Implement a hierarchical group structure in Keycloak to represent customers, tenants, and functional subgroups.
- Map all requirements to Keycloak concepts: groups, clients, roles, client roles, and attributes.
- Ensure that the backend API enforces all isolation and scoping rules.
- Ensure the UI only displays users, roles, and groups the current user is allowed to see/manage.

## Additional Notes and Constraints

- Use group and user attributes to store metadata (e.g., tenant ID, customer ID, admin flags).
- Use a naming convention for roles to encode customer and tenant (e.g., `IKEA_TenantA_Warehouse_Manager`).
- Dynamic client role creation should be performed via the Keycloak Admin REST API.
- Group-based scoping is used for hierarchical admin delegation (e.g., usermanagement-admins).
- Role and group assignments must be validated to prevent privilege escalation or cross-tenant access.
- The implementation must be compatible with both complex and simple applications.

## References

- [PRD: Multitenancy Support in Keycloak](../prd/multitenancy-keycloak.md)