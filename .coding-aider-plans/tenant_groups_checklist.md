[Coding Aider Plan - Checklist]

# Tenant Groups Implementation Checklist

## Data Models
- [x] Create TenantDto data class
- [x] Create TenantCreateDto data class
- [x] Create TenantUpdateDto data class
- [x] Add tenant-related fields to GroupDto

## Service Layer
- [x] Create TenantService class
- [x] Implement tenant creation with automatic subgroups
- [x] Implement tenant deletion
- [x] Implement tenant update functionality
- [x] Add methods to sync tenant subgroups with client roles
- [x] Add methods to manage tenant-specific permissions

## Facade Layer
- [x] Add tenant-specific methods to KeycloakGroupFacade
- [x] Implement group structure management
- [x] Add client role synchronization support
- [x] Add tenant group validation methods

## Controller Layer
- [x] Create TenantController
- [x] Implement CRUD endpoints for tenants
- [x] Add endpoints for tenant-specific operations
- [x] Add tenant permission management endpoints

## Testing
- [ ] Add unit tests for TenantService
- [ ] Add integration tests for tenant operations
- [ ] Test tenant-specific permission scenarios
- [ ] Test client role synchronization

## Documentation
- [ ] Update API documentation
- [ ] Add tenant management section to README
- [ ] Document tenant-specific permission model
