[Coding Aider Plan]

# Keycloak Wrapper Backend Implementation

## Overview
Create a Spring Boot Kotlin backend that wraps the Keycloak Admin Client to provide REST endpoints for user management functionality. The backend will follow a classical layered architecture and serve as an intermediary between the Angular frontend and Keycloak.

## Problem Description
The application needs a backend service that:
- Abstracts Keycloak's complexity from the frontend
- Provides clean REST APIs for user management operations
- Handles data transformation between DTOs and Keycloak models
- Manages Keycloak client connections and error handling

## Goals
1. Implement a layered architecture with:
    - Controllers for REST endpoints
    - DTOs for data transfer
    - Mappers for object transformation
    - Services for business logic
    - Facades for Keycloak client interaction

2. Create endpoints for:
    - User management (CRUD + search)
    - Group management (CRUD + search)
    - Role management (CRUD + search)
    - Role assignments (users and groups)

3. Provide proper error handling and response formatting

## Additional Notes and Constraints
- Using Keycloak Admin Client version 26.0.4
- No authentication/authorization initially
- No test coverage required in first iteration
- Pagination support required for list operations
- Error responses should be consistent and informative

## References
- [Keycloak Admin Client Documentation](https://www.keycloak.org/docs-api/26.0/javadocs/org/keycloak/admin/client/package-summary.html)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)