[Coding Aider Plan - Checklist]

- [ ] Review and finalize the PRD in `prd/multitenancy-keycloak.md`
- [ ] Design the group structure in Keycloak for customers, tenants, and subgroups
- [ ] Define and implement the role naming convention for dynamic and static roles
- [ ] Implement dynamic client role creation for tenants (ComplexApp)
- [ ] Enforce customer and tenant isolation in the backend API
- [ ] Implement hierarchical group-based access control (usermanagement-admins)
- [ ] Map group and user attributes for metadata (tenant ID, customer ID, admin flags)
- [ ] Update backend API to enforce all scoping and isolation rules
- [ ] Ensure UI integration only displays/manages permitted users, roles, and groups
- [ ] Validate role and group assignments to prevent privilege escalation
- [ ] Document the group, role, and client structure for maintainability
