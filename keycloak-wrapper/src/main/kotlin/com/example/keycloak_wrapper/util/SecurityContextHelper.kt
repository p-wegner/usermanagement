package com.example.keycloak_wrapper.util

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
}
