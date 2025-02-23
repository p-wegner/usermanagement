[Coding Aider Plan - Checklist]

# Role Assignment Implementation Checklist

## User Role Assignment
- [x] Implement updateUserRoles method in UserService
- [x] Add role assignment endpoints in UserController
  - [x] PUT /api/users/{id}/roles endpoint
  - [x] GET /api/users/{id}/roles endpoint

## Group Role Assignment
- [x] Add role assignment endpoints in GroupController
  - [x] PUT /api/groups/{id}/roles endpoint
  - [x] GET /api/groups/{id}/roles endpoint

## Role Search Enhancement
- [x] Extend role search endpoint to include client roles
  - [x] Add includeRealmRoles and includeClientRoles query params
  - [x] Update RoleSearchDto with new fields
  - [x] Modify RoleService to handle both realm and client roles
