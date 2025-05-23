[Coding Aider Plan - Checklist]

- [x] Design the group structure in Keycloak for customers, tenants, and subgroups
- [x] Define and implement the role naming convention for dynamic and static roles
- [x] Implement dynamic client role creation for tenants (ComplexApp)
- [x] Enforce customer and tenant isolation in the backend API
- [x] Implement hierarchical group-based access control (usermanagement-admins)
- [x] Map group and user attributes for metadata (tenant ID, customer ID, admin flags)
- [x] Update backend API to enforce all scoping and isolation rules
- [ ] Ensure UI integration only displays/manages permitted users, roles, and groups
- [x] Validate role and group assignments to prevent privilege escalation
- [x] Enhance UserMapper to support tenant-specific attributes
- [x] Update UserDto to include tenant-specific fields
- [x] Document the group, role, and client structure for maintainability
- [x] Add comprehensive API documentation with Swagger annotations
- [x] Implement tenant user management endpoints
- [x] Add tenant statistics and reporting endpoints
