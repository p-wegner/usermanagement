package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.TenantService
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String,
    private val tenantService: TenantService? = null
) {
    
    fun toDto(user: UserRepresentation): UserDto {
        val realmRoles = user.realmRoles ?: emptyList()
        val clientRoles = user.clientRoles ?: emptyMap()
        val attributes = user.attributes ?: emptyMap()
        
        // Extract tenant-specific attributes
        val isTenantAdmin = realmRoles.contains(RoleConstants.ROLE_TENANT_ADMIN)
        val managedTenants = attributes["managed_tenants"]?.toList() ?: emptyList()
        
        return UserDto(
            id = user.id,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email ?: "",
            enabled = user.isEnabled,
            realmRoles = realmRoles,
            clientRoles = clientRoles,
            isTenantAdmin = isTenantAdmin,
            managedTenants = managedTenants
        )
    }

    fun toRepresentation(dto: UserCreateDto): UserRepresentation {
        val myCredentials = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = dto.password
            isTemporary = false
        }

        val attributes = mutableMapOf<String, List<String>>()
        
        // Add tenant-specific attributes if provided
        if (dto.tenantId != null) {
            attributes["tenant_id"] = listOf(dto.tenantId)
        }

        return UserRepresentation().apply {
            username = dto.username
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            isEnabled = dto.enabled
            isEmailVerified = false
            credentials = listOf(myCredentials)
            this.attributes = attributes
            
            // Set realm roles if provided
            if (dto.realmRoles.isNotEmpty()) {
                realmRoles = dto.realmRoles
            }
        }
    }

    fun updateRepresentation(user: UserRepresentation, dto: UserUpdateDto): UserRepresentation {
        return user.apply {
            dto.firstName?.let { firstName = it }
            dto.lastName?.let { lastName = it }
            dto.email?.let { email = it }
            dto.enabled?.let { isEnabled = it }
            
            // Update realm roles if provided
            dto.realmRoles?.let { realmRoles = it }
            
            // Update attributes if needed
            val currentAttributes = attributes ?: mutableMapOf()
            
            // Initialize attributes if null
            if (attributes == null) {
                attributes = mutableMapOf()
            }
            
            // Update tenant-specific attributes if provided
            dto.tenantId?.let { 
                attributes["tenant_id"] = listOf(it)
            }
            
            // Update managed tenants if provided
            dto.managedTenants?.let {
                attributes["managed_tenants"] = it
            }
        }
    }
    
    /**
     * Enriches a UserDto with tenant-specific information
     */
    fun enrichWithTenantInfo(userDto: UserDto): UserDto {
        if (tenantService == null || userDto.id == null) {
            return userDto
        }
        
        // Check if user is a tenant admin
        val isTenantAdmin = userDto.realmRoles.contains(RoleConstants.ROLE_TENANT_ADMIN)
        
        if (isTenantAdmin) {
            try {
                // Get the tenants this user is an admin for
                val adminTenantsResponse = tenantService.getUserTenants(userDto.id)
                val managedTenantIds = adminTenantsResponse.tenants.map { it.id }
                
                return userDto.copy(
                    isTenantAdmin = true,
                    managedTenants = managedTenantIds
                )
            } catch (e: Exception) {
                // If there's an error, just return the original DTO
                return userDto
            }
        }
        
        return userDto
    }
}
