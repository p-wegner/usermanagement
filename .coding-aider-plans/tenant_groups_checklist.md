[Coding Aider Plan - Checklist]

# Tenant Groups Implementation Checklist

## Data Models
- [ ] Create TenantDto data class
- [ ] Create TenantCreateDto data class
- [ ] Create TenantUpdateDto data class
- [ ] Add tenant-related fields to GroupDto

## Service Layer
- [ ] Create TenantService class
- [ ] Implement tenant creation with automatic subgroups
- [ ] Implement tenant deletion
- [ ] Implement tenant update functionality
- [ ] Add methods to sync tenant subgroups with client roles
- [ ] Add methods to manage tenant-specific permissions

## Facade Layer
- [ ] Add tenant-specific methods to KeycloakGroupFacade
- [ ] Implement group structure management
- [ ] Add client role synchronization support
- [ ] Add tenant group validation methods

## Controller Layer
- [ ] Create TenantController
- [ ] Implement CRUD endpoints for tenants
- [ ] Add endpoints for tenant-specific operations
- [ ] Add tenant permission management endpoints

## Testing
- [ ] Add unit tests for TenantService
- [ ] Add integration tests for tenant operations
- [ ] Test tenant-specific permission scenarios
- [ ] Test client role synchronization

## Documentation
- [ ] Update API documentation
- [ ] Add tenant management section to README
- [ ] Document tenant-specific permission model
