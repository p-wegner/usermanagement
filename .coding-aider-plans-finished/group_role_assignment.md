[Coding Aider Plan]

# Group Role Assignment

## Overview
This plan outlines the implementation of functionality to assign roles to groups in the Keycloak wrapper application. Currently, the application allows managing users, groups, and roles separately, but there's a need to enhance the group management functionality by enabling role assignments to groups.

## Problem Description
The current implementation allows creating and managing groups, but it lacks the ability to assign roles to these groups. In Keycloak, groups can have roles assigned to them, which are then inherited by all members of the group. This feature is essential for implementing role-based access control (RBAC) efficiently, as it allows administrators to assign permissions to groups of users rather than individual users.

The existing codebase has:
1. Group management functionality (create, read, update, delete)
2. Role management functionality
3. User role assignment functionality

However, it's missing the specific endpoints and UI components to manage role assignments for groups.

## Goals
1. Implement backend functionality to assign roles to groups
2. Create API endpoints for managing group role assignments
3. Ensure proper error handling and validation
4. Maintain consistency with the existing codebase and follow established patterns

## Implementation Details
1. Update the GroupController to add endpoints for managing group roles
2. Implement the necessary service methods in GroupService
3. Create or update DTOs for role assignment to groups
4. Implement the required facade methods for Keycloak integration

## Additional Notes and Constraints
- The implementation should follow the existing patterns in the codebase
- Security considerations must be addressed, ensuring only authorized users can assign roles to groups
- The implementation should be tested thoroughly to ensure it works correctly with Keycloak

## References
- Keycloak documentation on group role mapping: https://www.keycloak.org/docs/latest/server_admin/index.html#assigning-role-mappings
