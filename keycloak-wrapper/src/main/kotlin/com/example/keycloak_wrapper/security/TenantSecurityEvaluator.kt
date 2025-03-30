package com.example.keycloak_wrapper.security

import com.example.keycloak_wrapper.config.RoleConstants
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
        if (securityContextHelper.hasRole(RoleConstants.ROLE_ADMIN)) {
            return true
        }
        
        // Tenant admins only have access to their assigned tenants
        if (securityContextHelper.hasRole(RoleConstants.ROLE_TENANT_ADMIN)) {
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
        if (securityContextHelper.hasRole(RoleConstants.ROLE_ADMIN)) {
            return true
        }
        
        // Tenant admins can only manage their assigned tenants
        if (securityContextHelper.hasRole(RoleConstants.ROLE_TENANT_ADMIN)) {
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
}
