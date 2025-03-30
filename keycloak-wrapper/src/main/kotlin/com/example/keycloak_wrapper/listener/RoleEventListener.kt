package com.example.keycloak_wrapper.listener

import com.example.keycloak_wrapper.service.RoleCreatedEvent
import com.example.keycloak_wrapper.service.RoleDeletedEvent
import com.example.keycloak_wrapper.service.RoleUpdatedEvent
import com.example.keycloak_wrapper.service.TenantService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

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
