package com.example.keycloak_wrapper.config

/**
 * Constants for role names used throughout the application.
 */
object RoleConstants {
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_TENANT_ADMIN = "TENANT_ADMIN"
    const val ROLE_USER = "USER"
    
    // Tenant-specific role prefix constants
    const val TENANT_ROLE_PREFIX = "TENANT_"
    const val TENANT_GROUP_PREFIX = "tenant_"
    const val TENANT_ADMIN_GROUP_SUFFIX = "_admins"
    
    /**
     * Checks if a role name represents a system role (not tenant-specific)
     */
    fun isSystemRole(roleName: String): Boolean {
        return roleName == ROLE_ADMIN || roleName == ROLE_TENANT_ADMIN || roleName == ROLE_USER
    }
    
    /**
     * Creates a tenant-specific role name
     */
    fun createTenantRoleName(tenantName: String, roleName: String): String {
        return "${tenantName.uppercase()}_$roleName"
    }
    
    /**
     * Extracts the tenant name from a tenant-specific role name
     */
    fun extractTenantFromRoleName(roleName: String): String? {
        if (isSystemRole(roleName)) {
            return null
        }
        
        val parts = roleName.split("_")
        return if (parts.size >= 2) parts[0] else null
    }
}
