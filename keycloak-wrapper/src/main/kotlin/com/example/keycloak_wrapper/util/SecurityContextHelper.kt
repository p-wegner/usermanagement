package com.example.keycloak_wrapper.util

import com.example.keycloak_wrapper.config.RoleConstants
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class SecurityContextHelper {
    /**
     * Gets the current user's ID from the security context.
     * 
     * @return The user ID or null if not authenticated
     */
    fun getCurrentUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.subject
        }
        return null
    }

    /**
     * Gets the current username from the security context.
     * 
     * @return The username or null if not authenticated
     */
    fun getCurrentUsername(): String? {
        return SecurityContextHolder.getContext().authentication?.name
    }

    /**
     * Checks if the current user is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }

    /**
     * Checks if the current user has a specific role.
     * 
     * @param role The role to check for
     * @return true if the user has the role, false otherwise
     */
    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { it.authority == "ROLE_${role.uppercase()}" } ?: false
    }

    /**
     * Checks if the current user has any of the specified roles.
     * 
     * @param roles The roles to check for
     * @return true if the user has any of the roles, false otherwise
     */
    fun hasAnyRole(roles: List<String>): Boolean {
        return roles.any { hasRole(it) }
    }

    /**
     * Gets all authorities for the current user.
     * 
     * @return List of authority names without the "ROLE_" prefix
     */
    fun getAuthorities(): List<String> {
        return SecurityContextHolder.getContext().authentication?.authorities
            ?.map { it.authority }
            ?.filter { it.startsWith("ROLE_") }
            ?.map { it.removePrefix("ROLE_") }
            ?.toList() 
            ?: emptyList()
    }

    /**
     * Checks if the current user has a specific role for a resource.
     * 
     * @param role The role to check for
     * @param resource The resource to check against
     * @return true if the user has the role for the resource, false otherwise
     */
    fun hasResourceRole(role: String, resource: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { 
            it.authority == "ROLE_${role.uppercase()}" && 
            it.authority.contains(resource.uppercase())
        } ?: false
    }

    /**
     * Checks if the current user has any of the specified roles for a resource.
     * 
     * @param roles The roles to check for
     * @param resource The resource to check against
     * @return true if the user has any of the roles for the resource, false otherwise
     */
    fun hasAnyResourceRole(roles: List<String>, resource: String): Boolean {
        return roles.any { hasResourceRole(it, resource) }
    }

    /**
     * Gets the JWT token from the security context.
     * 
     * @return The token or null if not authenticated with JWT
     */
    fun getToken(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.tokenValue
        }
        return null
    }

    /**
     * Gets a specific claim from the JWT token.
     * 
     * @param claimName The name of the claim to retrieve
     * @return The claim value or null if not found
     */
    fun getClaim(claimName: String): Any? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.claims[claimName]
        }
        return null
    }

    /**
     * Gets all claims from the JWT token.
     * 
     * @return Map of claim names to values
     */
    fun getTokenClaims(): Map<String, Any> {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            authentication.token.claims
        } else {
            emptyMap()
        }
    }

    /**
     * Gets roles for a specific resource from the JWT token.
     * 
     * @param resource The resource to get roles for
     * @return List of role names
     */
    fun getResourceRoles(resource: String): List<String> {
        val claims = getTokenClaims()
        @Suppress("UNCHECKED_CAST")
        val resourceAccess = claims["resource_access"] as? Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val resourceRoles = (resourceAccess?.get(resource) as? Map<String, Any>)?.get("roles") as? List<String>
        return resourceRoles ?: emptyList()
    }

    /**
     * Gets all realm roles from the JWT token.
     * 
     * @return List of role names
     */
    fun getRealmRoles(): List<String> {
        val claims = getTokenClaims()
        @Suppress("UNCHECKED_CAST")
        val realmAccess = claims["realm_access"] as? Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val roles = realmAccess?.get("roles") as? List<String>
        return roles ?: emptyList()
    }

    /**
     * Gets the expiration time of the JWT token.
     * 
     * @return The expiration time in seconds since epoch, or null if not found
     */
    fun getTokenExpirationTime(): Long? {
        val claims = getTokenClaims()
        return claims["exp"] as? Long
    }

    /**
     * Checks if the JWT token is expired.
     * 
     * @return true if expired, false otherwise
     */
    fun isTokenExpired(): Boolean {
        val expirationTime = getTokenExpirationTime()
        return if (expirationTime != null) {
            System.currentTimeMillis() / 1000 >= expirationTime
        } else {
            true
        }
    }
    
    /**
     * Checks if the current user is a tenant admin.
     * 
     * @return true if the user has the TENANT_ADMIN role, false otherwise
     */
    fun isTenantAdmin(): Boolean {
        return hasRole(RoleConstants.ROLE_TENANT_ADMIN)
    }
    
    /**
     * Checks if the current user is a system admin.
     * 
     * @return true if the user has the ADMIN role, false otherwise
     */
    fun isSystemAdmin(): Boolean {
        return hasRole(RoleConstants.ROLE_ADMIN)
    }
    
    /**
     * Gets the tenant context from the token claims.
     * This can be used to determine which tenant the user is currently operating in.
     * 
     * @return The tenant ID if present, null otherwise
     */
    fun getCurrentTenantContext(): String? {
        val claims = getTokenClaims()
        return claims["tenant_id"] as? String
    }
    
    /**
     * Gets the list of tenant IDs the current user has access to.
     * 
     * @return List of tenant IDs or empty list if none
     */
    fun getAccessibleTenantIds(): List<String> {
        val claims = getTokenClaims()
        @Suppress("UNCHECKED_CAST")
        return claims["accessible_tenants"] as? List<String> ?: emptyList()
    }
    
    /**
     * Checks if the current user has access to a specific tenant.
     * 
     * @param tenantId The tenant ID to check access for
     * @return true if the user has access, false otherwise
     */
    fun hasTenantAccess(tenantId: String): Boolean {
        // System admins have access to all tenants
        if (isSystemAdmin()) {
            return true
        }
        
        // Check if the tenant is in the accessible tenants list
        val accessibleTenants = getAccessibleTenantIds()
        return accessibleTenants.contains(tenantId)
    }
}
