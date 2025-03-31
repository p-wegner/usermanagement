package com.example.keycloak_wrapper.security

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.GroupDto
import com.example.keycloak_wrapper.dto.RoleAssignmentDto
import com.example.keycloak_wrapper.service.TenantService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

/**
 * Security evaluator for tenant-specific authorization checks.
 */
@Component
class TenantSecurityEvaluator(
    private val securityContextHelper: SecurityContextHelper,
    private val tenantService: TenantService
) {
    /**
     * Checks if the current user has access to the specified tenant.
     *
     * @param tenantId The ID of the tenant to check access for
     * @return true if the user has access, false otherwise
     */
    fun hasTenantAccess(tenantId: String): Boolean {
        // System admins have access to all tenants
        if (securityContextHelper.isSystemAdmin()) {
            return true
        }

        // Tenant admins only have access to their assigned tenants
        if (securityContextHelper.isTenantAdmin()) {
            val userId = securityContextHelper.getCurrentUserId() ?: return false
            return tenantService.isUserTenantAdmin(userId, tenantId)
        }

        return false
    }

    /**
     * Verifies that the current user has access to the specified tenant.
     * Throws AccessDeniedException if the user does not have access.
     *
     * @param tenantId The ID of the tenant to verify access for
     */
    fun verifyTenantAccess(tenantId: String) {
        if (!hasTenantAccess(tenantId)) {
            throw AccessDeniedException("User does not have access to tenant with ID: $tenantId")
        }
    }

    /**
     * Checks if the current user can manage the specified tenant.
     *
     * @param tenantId The ID of the tenant to check management access for
     * @return true if the user can manage the tenant, false otherwise
     */
    fun canManageTenant(tenantId: String): Boolean {
        // System admins can manage all tenants
        if (securityContextHelper.isSystemAdmin()) {
            return true
        }

        // Tenant admins can only manage their assigned tenants
        if (securityContextHelper.isTenantAdmin()) {
            val userId = securityContextHelper.getCurrentUserId() ?: return false
            return tenantService.isUserTenantAdmin(userId, tenantId)
        }

        return false
    }

    /**
     * Verifies that the current user can manage the specified tenant.
     * Throws AccessDeniedException if the user cannot manage the tenant.
     *
     * @param tenantId The ID of the tenant to verify management access for
     */
    fun verifyTenantManagement(tenantId: String) {
        if (!canManageTenant(tenantId)) {
            throw AccessDeniedException("User does not have management access to tenant with ID: $tenantId")
        }
    }

    /**
     * Checks if the current user has access to the specified group.
     *
     * @param group The group to check access for
     * @return true if the user has access, false otherwise
     */
    fun hasGroupAccess(group: GroupDto): Boolean {
        // System admins have access to all groups
        if (securityContextHelper.isSystemAdmin()) {
            return true
        }

        // Find the tenant ID for this group
        val tenantId = findTenantIdForGroup(group) ?: return false

        // Check if the user has access to the tenant
        return hasTenantAccess(tenantId)
    }

    /**
     * Verifies that the current user has access to the specified group.
     * Throws AccessDeniedException if the user does not have access.
     *
     * @param group The group to verify access for
     */
    fun verifyGroupAccess(group: GroupDto) {
        if (!hasGroupAccess(group)) {
            throw AccessDeniedException("User does not have access to group with ID: ${group.id}")
        }
    }

    /**
     * Helper method to find the tenant ID for a group
     */
    private fun findTenantIdForGroup(group: GroupDto): String? {
        // If the group is a tenant, return its ID
        if (group.isTenant) {
            return group.id
        }

        // If the group has a path, extract the tenant ID from it
        val path = group.path
        if (path != null) {
            val pathParts = path.split("/")
            if (pathParts.size >= 2) {
                val tenantName = pathParts[1]
                val tenants = tenantService.getTenants()
                return tenants.find { it.name == tenantName }?.id
            }
        }

        return null
    }

    /**
     * Checks if the current user has access to a specific user.
     * Users can access themselves, users in their tenant, or all users if they're a system admin.
     *
     * @param userId The ID of the user to access
     * @return true if the current user has access, false otherwise
     */
    fun hasAccessToUser(userId: String): Boolean {
        val currentUserId = securityContextHelper.getCurrentUserId() ?: return false
        return tenantService.hasUserAccessToUser(currentUserId, userId)
    }

    /**
     * Verifies that the current user has access to a specific user.
     * Throws AccessDeniedException if the user does not have access.
     *
     * @param userId The ID of the user to verify access for
     */
    fun verifyUserAccess(userId: String) {
        if (!hasAccessToUser(userId)) {
            throw AccessDeniedException("User does not have access to user with ID: $userId")
        }
    }
    
    /**
     * Verifies that the current user has access to view or manage the specified user.
     * Throws SecurityException if access is denied.
     *
     * @param currentUserId The ID of the current user
     * @param targetUserId The ID of the user to check access for
     */
    fun verifyUserAccess(currentUserId: String, targetUserId: String) {
        // Users can always access themselves
        if (currentUserId == targetUserId) {
            return
        }
        
        // System admins have access to all users
        if (securityContextHelper.hasRole(RoleConstants.ROLE_ADMIN)) {
            return
        }
        
        // Check tenant-specific access
        val hasAccess = tenantService.hasUserAccessToUser(currentUserId, targetUserId)
        if (!hasAccess) {
            throw SecurityException("User does not have access to view or manage this user")
        }
    }
    
    /**
     * Verifies that the current user has access to the specified user.
     * System admins have access to all users.
     * Tenant admins only have access to users in their tenants.
     * Regular users only have access to themselves.
     *
     * @param targetUserId The ID of the user to check access for
     * @throws SecurityException if the current user doesn't have access to the user
     */
    fun verifyUserAccess(targetUserId: String) {
        val currentUserId = securityContextHelper.getCurrentUserId()
            ?: throw SecurityException("User not authenticated")
            
        verifyUserAccess(currentUserId, targetUserId)
    }
    
    /**
     * Verifies that the current user has access to assign the specified roles.
     * Tenant admins can only assign roles within their tenant scope.
     * Throws SecurityException if access is denied.
     *
     * @param currentUserId The ID of the current user
     * @param roleAssignment The role assignment to check
     */
    fun verifyRoleAssignmentAccess(currentUserId: String, roleAssignment: RoleAssignmentDto) {
        // System admins can assign any roles
        if (securityContextHelper.hasRole(RoleConstants.ROLE_ADMIN)) {
            return
        }
        
        // Tenant admins can only assign roles within their tenant scope
        val isTenantAdmin = securityContextHelper.hasRole(RoleConstants.ROLE_TENANT_ADMIN)
        if (isTenantAdmin) {
            // Get the tenants this user is an admin for
            val adminTenants = tenantService.getUserTenants(currentUserId).tenants
            
            // Check if all roles in the assignment are within the admin's tenant scope
            val adminTenantRoleIds = adminTenants.flatMap { tenant -> 
                tenant.permissionGroups.flatMap { group -> group.realmRoles.map { it.id } }
            }.toSet()
            
            // Check if there are any roles in the assignment that are not in the admin's tenant scope
            val unauthorizedRoleIds = roleAssignment.allRoleIds.filter { roleId ->
                !adminTenantRoleIds.contains(roleId)
            }
            
            if (unauthorizedRoleIds.isNotEmpty()) {
                throw SecurityException("User does not have access to assign some of the specified roles")
            }
        } else {
            // Regular users cannot assign roles
            throw SecurityException("User does not have permission to assign roles")
        }
    }
}
