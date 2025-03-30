[Coding Aider Plan]

# Role Assignment Implementation

## Overview
Add endpoints to assign realm roles to users and groups in the Keycloak wrapper application. This will enable managing role assignments through the REST API.

## Problem Description
Currently, the application lacks dedicated endpoints for assigning roles to users and groups. While the underlying KeycloakUserFacade and KeycloakGroupFacade have methods to update roles, these are not exposed through REST endpoints.

## Goals
1. Implement user role assignment endpoints
   - Add endpoint to assign roles to a user
   - Add endpoint to remove roles from a user
   - Add endpoint to get user's roles
2. Implement group role assignment endpoints
   - Add endpoint to assign roles to a group
   - Add endpoint to remove roles from a group
   - Add endpoint to get group's roles

## Additional Notes and Constraints
- Must maintain existing security constraints (ADMIN and manager roles)
- Follow existing patterns for error handling and response formatting
- Use existing DTOs where possible
- Ensure proper validation of role IDs
- Maintain consistency with Keycloak's role management approach

## References
- Existing UserController implementation
- Existing GroupController implementation
- Keycloak Admin REST API documentation
