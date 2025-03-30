package com.example.keycloak_wrapper.listener

import com.example.keycloak_wrapper.dto.RoleDto
import com.example.keycloak_wrapper.service.TenantService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// Events for role changes
data class RoleCreatedEvent(val role: RoleDto)
data class RoleUpdatedEvent(val oldRole: RoleDto, val newRole: RoleDto)
data class RoleDeletedEvent(val role: RoleDto)

@Component
class RoleEventListener(private val tenantService: TenantService) {

    @EventListener
    @Async
    fun handleRoleCreatedEvent(event: RoleCreatedEvent) {
        // When a new role is created, we need to add it to all tenant groups
        tenantService.syncTenantsWithRoles()
    }

    @EventListener
    @Async
    fun handleRoleUpdatedEvent(event: RoleUpdatedEvent) {
        // If the role name changed, we need to update all tenant subgroups
        if (event.oldRole.name != event.newRole.name) {
            tenantService.syncTenantsWithRoles()
        }
    }

    @EventListener
    @Async
    fun handleRoleDeletedEvent(event: RoleDeletedEvent) {
        // When a role is deleted, we need to remove it from all tenant groups
        tenantService.syncTenantsWithRoles()
    }
}
