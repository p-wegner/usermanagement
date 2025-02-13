[Coding Aider Plan - Checklist]

1. Keycloak Setup:
    - [ ] Configure Keycloak realm
    - [ ] Set up client applications
    - [ ] Configure user federation if needed
    - [ ] Set up required roles and groups
    - [ ] Configure client scopes

2. Frontend Implementation:
    - [x] Create auth service wrapper with Keycloak Angular adapter
    - [ ] Implement login component
    - [x] Add logout functionality
    - [x] Create auth guards with role-based access
    - [x] Add token interceptor with refresh handling
    - [x] Implement token refresh logic
    - [x] Add session management
    - [ ] Create user profile component
    - [x] Add Keycloak profile integration
    - [x] Implement role-based authorization

3. Backend Integration:
    - [x] Configure Spring Security
    - [x] Add Keycloak adapter configuration
    - [x] Implement token validation
    - [x] Set up role-based access control
    - [x] Configure CORS for Angular frontend
    - [x] Add granular endpoint security
    - [x] Add proper error handling
    - [x] Add security context helper
    - [x] Enhance token handling and claims extraction
    - [x] Implement method security
    - [x] Add token propagation logic
    - [x] Enhance CORS configuration
    - [x] Secure actuator endpoints
