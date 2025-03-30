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
    private val tenantService: TenantService? = null // Circular dependency prevention
) {
    fun getUsers(searchDto: UserSearchDto): Pair<List<UserDto>, Int> {
        val (users, total) = keycloakUserFacade.getUsers(
            search = searchDto.search,
            firstResult = searchDto.page * searchDto.size,
            maxResults = searchDto.size
        )
        return Pair(users.map { userMapper.toDto(it) }, total)
    }

    fun getUser(id: String): UserDto {
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
        
        return userDto
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
