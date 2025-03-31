package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakUserFacade
import com.example.keycloak_wrapper.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class UserService(
    private val keycloakUserFacade: KeycloakUserFacade,
    private val userMapper: UserMapper,
    private val tenantService: TenantService? = null
) {
    fun getUsers(searchDto: UserSearchDto): Pair<List<UserDto>, Int> {
        val (users, total) = keycloakUserFacade.getUsers(
            search = searchDto.search,
            firstResult = searchDto.page * searchDto.size,
            maxResults = searchDto.size
        )
        
        val userDtos = users.map { userMapper.toDto(it) }
        
        // Apply tenant-specific filtering if a current user ID is provided
        return if (searchDto.currentUserId != null && tenantService != null) {
            val filteredUsers = tenantService.filterUsersByTenantAccess(searchDto.currentUserId, userDtos)
            Pair(filteredUsers, filteredUsers.size)
        } else {
            Pair(userDtos, total)
        }
    }

    fun getUser(id: String, currentUserId: String? = null): UserDto {
        val user = keycloakUserFacade.getUser(id)
        val userDto = userMapper.toDto(user)
        
        // Check if user is a tenant admin
        val userRoles = keycloakUserFacade.getUserRoles(id)
        val isTenantAdmin = userRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
        
        if (isTenantAdmin && tenantService != null) {
            // Get the tenants this user is an admin for
            val managedTenants = tenantService.getUserTenants(id).tenants.map { it.id }
            
            return userDto.copy(
                isTenantAdmin = true,
                managedTenants = managedTenants
            )
        }
        
        // If a current user ID is provided, check tenant access
        if (currentUserId != null && tenantService != null && id != currentUserId) {
            // Check if the current user has access to this user
            val hasAccess = tenantService.hasUserAccessToUser(currentUserId, id)
            if (!hasAccess) {
                throw SecurityException("User does not have access to view this user")
            }
        }
        
        return userDto
    }
    
    /**
     * Checks if the current user has access to view or manage the specified user.
     * 
     * @param currentUserId The ID of the current user
     * @param targetUserId The ID of the user to check access for
     * @return true if the current user has access, false otherwise
     */
    fun hasAccessToUser(currentUserId: String, targetUserId: String): Boolean {
        // Users can always access themselves
        if (currentUserId == targetUserId) {
            return true
        }
        
        // Check if the current user has the ADMIN role
        val currentUserRoles = keycloakUserFacade.getUserRoles(currentUserId)
        val isAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_ADMIN }
        if (isAdmin) {
            return true
        }
        
        // If tenant service is available, check tenant-specific access
        if (tenantService != null) {
            return tenantService.hasUserAccessToUser(currentUserId, targetUserId)
        }
        
        return false
    }

    fun createUser(userDto: UserCreateDto): UserDto {
        val userRepresentation = userMapper.toRepresentation(userDto)
        val createdUser = keycloakUserFacade.createUser(userRepresentation)
        return userMapper.toDto(createdUser)
    }

    fun updateUser(id: String, userDto: UserUpdateDto): UserDto {
        val existingUser = keycloakUserFacade.getUser(id)
        val updatedRepresentation = userMapper.updateRepresentation(existingUser, userDto)
        val updatedUser = keycloakUserFacade.updateUser(id, updatedRepresentation)
        return userMapper.toDto(updatedUser)
    }

    fun deleteUser(id: String) {
        keycloakUserFacade.deleteUser(id)
    }

    fun updateUserRoles(id: String, roleAssignment: RoleAssignmentDto) {
        keycloakUserFacade.updateUserRoles(id, roleAssignment)
    }

    fun getUserRoles(id: String): RoleAssignmentDto {
        return keycloakUserFacade.getUserRoles(id)
    }
}
