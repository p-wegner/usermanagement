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
            // First check accessible tenants from token
            val accessibleTenantIds = securityContextHelper.getAccessibleTenantIds()
            if (accessibleTenantIds.contains(tenantId)) {
                return true
            }
            
            // If not found in token, check from service
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
            // First check accessible tenants from token
            val accessibleTenantIds = securityContextHelper.getAccessibleTenantIds()
            if (accessibleTenantIds.contains(tenantId)) {
                return true
            }
            
            // If not found in token, check from service
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

        // If the group is not tenant-related, only system admins have access
        if (!group.isTenant && !isTenantSubgroup(group)) {
            return false
        }

        // Find the tenant ID for this group
        val tenantId = findTenantIdForGroup(group) ?: return false

        // Check if the user has access to the tenant
        return hasTenantAccess(tenantId)
    }

    /**
     * Determines if a group is a subgroup of a tenant
     */
    private fun isTenantSubgroup(group: GroupDto): Boolean {
        val path = group.path ?: return false
        return path.startsWith("/tenant_")
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
     * Checks if the current user has access to view or manage users in the specified tenant.
     *
     * @param tenantId The ID of the tenant to check access for
     * @return true if the user has access, false otherwise
     */
    fun hasTenantUserManagementAccess(tenantId: String): Boolean {
        // System admins can manage users in any tenant
        if (securityContextHelper.isSystemAdmin()) {
            return true
        }
        
        // Tenant admins can only manage users in their assigned tenants
        if (securityContextHelper.isTenantAdmin()) {
            // First check accessible tenants from token
            val accessibleTenantIds = securityContextHelper.getAccessibleTenantIds()
            if (accessibleTenantIds.contains(tenantId)) {
                return true
            }
            
            // If not found in token, check from service
            val userId = securityContextHelper.getCurrentUserId() ?: return false
            return tenantService.isUserTenantAdmin(userId, tenantId)
        }
        
        return false
    }
    
    /**
     * Verifies that the current user has access to view or manage users in the specified tenant.
     * Throws AccessDeniedException if the user does not have access.
     *
     * @param tenantId The ID of the tenant to verify access for
     */
    fun verifyTenantUserManagementAccess(tenantId: String) {
        if (!hasTenantUserManagementAccess(tenantId)) {
            throw AccessDeniedException("User does not have access to manage users in tenant with ID: $tenantId")
        }
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
                tenant.groups.flatMap { group -> group.realmRoles.map { it.id } }
            }.toSet()
            
            // Check if there are any roles in the assignment that are not in the admin's tenant scope
            val unauthorizedRoleIds = roleAssignment.allRoleIds.filter { roleId ->
                !adminTenantRoleIds.contains(roleId)
            }
            
            // Prevent assignment of system roles
            val systemRoleIds = roleAssignment.realmRoles
                .filter { it.name == RoleConstants.ROLE_ADMIN || it.name == RoleConstants.ROLE_TENANT_ADMIN }
                .map { it.id }
                
            if (unauthorizedRoleIds.isNotEmpty() || systemRoleIds.isNotEmpty()) {
                throw SecurityException("User does not have access to assign some of the specified roles")
            }
        } else {
            // Regular users cannot assign roles
            throw SecurityException("User does not have permission to assign roles")
        }
    }
    
    /**
     * Checks if a role is a tenant-specific role.
     * 
     * @param roleName The name of the role to check
     * @return true if the role is tenant-specific, false otherwise
     */
    fun isTenantRole(roleName: String): Boolean {
        // System roles are not tenant-specific
        if (roleName == RoleConstants.ROLE_ADMIN || 
            roleName == RoleConstants.ROLE_TENANT_ADMIN || 
            roleName == RoleConstants.ROLE_USER) {
            return false
        }
        
        // Check if the role name follows tenant-specific naming convention
        // For example: TENANT_ROLENAME or TENANT_APP_ROLENAME
        return roleName.contains("_")
    }
    
    /**
     * Extracts the tenant name from a tenant-specific role name.
     * 
     * @param roleName The name of the role
     * @return The tenant name or null if not a tenant-specific role
     */
    fun extractTenantFromRole(roleName: String): String? {
        if (!isTenantRole(roleName)) {
            return null
        }
        
        val parts = roleName.split("_")
        return if (parts.size >= 2) parts[0] else null
    }
}
