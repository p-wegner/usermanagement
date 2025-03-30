package com.example.keycloak_wrapper.util

import com.example.keycloak_wrapper.config.RoleConstants
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class SecurityContextHelper {
    fun getCurrentUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.subject
        }
        return null
    }

    fun getCurrentUsername(): String? {
        return SecurityContextHolder.getContext().authentication?.name
    }

    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }

    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { it.authority == "ROLE_${role.uppercase()}" } ?: false
    }

    fun hasAnyRole(roles: List<String>): Boolean {
        return roles.any { hasRole(it) }
    }

    fun getAuthorities(): List<String> {
        return SecurityContextHolder.getContext().authentication?.authorities
            ?.map { it.authority }
            ?.filter { it.startsWith("ROLE_") }
            ?.map { it.removePrefix("ROLE_") }
            ?.toList() 
            ?: emptyList()
    }

    fun hasResourceRole(role: String, resource: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { 
            it.authority == "ROLE_${role.uppercase()}" && 
            it.authority.contains(resource.uppercase())
        } ?: false
    }

    fun hasAnyResourceRole(roles: List<String>, resource: String): Boolean {
        return roles.any { hasResourceRole(it, resource) }
    }

    fun getToken(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.tokenValue
        }
        return null
    }

    fun getClaim(claimName: String): Any? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.claims[claimName]
        }
        return null
    }

    fun getTokenClaims(): Map<String, Any> {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            authentication.token.claims
        } else {
            emptyMap()
        }
    }

    fun getResourceRoles(resource: String): List<String> {
        val claims = getTokenClaims()
        @Suppress("UNCHECKED_CAST")
        val resourceAccess = claims["resource_access"] as? Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val resourceRoles = (resourceAccess?.get(resource) as? Map<String, Any>)?.get("roles") as? List<String>
        return resourceRoles ?: emptyList()
    }

    fun getRealmRoles(): List<String> {
        val claims = getTokenClaims()
        @Suppress("UNCHECKED_CAST")
        val realmAccess = claims["realm_access"] as? Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val roles = realmAccess?.get("roles") as? List<String>
        return roles ?: emptyList()
    }

    fun getTokenExpirationTime(): Long? {
        val claims = getTokenClaims()
        return claims["exp"] as? Long
    }

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
}
