package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakUserFacade
import com.example.keycloak_wrapper.mapper.UserMapper
import com.example.keycloak_wrapper.util.SecurityContextHelper
import org.springframework.stereotype.Service

@Service
class UserService(
    private val keycloakUserFacade: KeycloakUserFacade,
    private val userMapper: UserMapper,
    private val tenantService: TenantService? = null,
    private val securityContextHelper: SecurityContextHelper
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

    fun createUser(userDto: UserCreateDto, currentUserId: String? = null): UserDto {
        val userRepresentation = userMapper.toRepresentation(userDto)
        val createdUser = keycloakUserFacade.createUser(userRepresentation)
        
        // If the current user is a tenant admin, add the new user to their tenant(s)
        if (currentUserId != null && tenantService != null) {
            val currentUserRoles = keycloakUserFacade.getUserRoles(currentUserId)
            val isTenantAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
            val isAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_ADMIN }
            
            if (isTenantAdmin && !isAdmin) {
                // Get the tenants this admin manages
                val managedTenants = tenantService.getUserTenants(currentUserId).tenants
                
                // Add the new user to each tenant the admin manages
                managedTenants.forEach { tenant ->
                    // Add the user to the tenant group
                    keycloakUserFacade.addUserToGroup(createdUser.id, tenant.id)
                }
            }
        }
        
        return userMapper.toDto(createdUser)
    }

    fun updateUser(id: String, userDto: UserUpdateDto): UserDto {
        val existingUser = keycloakUserFacade.getUser(id)
        
        // Ensure tenant admins can't change critical fields like realm roles
        val currentUserId = securityContextHelper.getCurrentUserId()
        if (currentUserId != null) {
            val currentUserRoles = keycloakUserFacade.getUserRoles(currentUserId)
            val isTenantAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
            val isAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_ADMIN }
            
            // If tenant admin but not system admin, restrict what can be updated
            if (isTenantAdmin && !isAdmin && userDto.realmRoles != null) {
                // Tenant admins can't modify system roles
                val restrictedRoles = listOf(RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_TENANT_ADMIN)
                val safeRoles = userDto.realmRoles.filter { role -> !restrictedRoles.contains(role) }
                
                // Create a safe copy of the update DTO
                val safeUserDto = userDto.copy(realmRoles = safeRoles)
                val updatedRepresentation = userMapper.updateRepresentation(existingUser, safeUserDto)
                val updatedUser = keycloakUserFacade.updateUser(id, updatedRepresentation)
                return userMapper.toDto(updatedUser)
            }
        }
        
        // For system admins or updates without role changes
        val updatedRepresentation = userMapper.updateRepresentation(existingUser, userDto)
        val updatedUser = keycloakUserFacade.updateUser(id, updatedRepresentation)
        return userMapper.toDto(updatedUser)
    }

    fun deleteUser(id: String) {
        keycloakUserFacade.deleteUser(id)
    }

    fun updateUserRoles(id: String, roleAssignment: RoleAssignmentDto, currentUserId: String? = null) {
        // If the current user is a tenant admin, we need to restrict the roles they can assign
        if (currentUserId != null && tenantService != null) {
            val currentUserRoles = keycloakUserFacade.getUserRoles(currentUserId)
            val isTenantAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
            val isAdmin = currentUserRoles.realmRoles.any { it.name == RoleConstants.ROLE_ADMIN }
            
            if (isTenantAdmin && !isAdmin) {
                // Tenant admins can't assign system-level roles
                val restrictedRoles = listOf(
                    RoleConstants.ROLE_ADMIN
                )
                
                // Filter out restricted roles
                val filteredRealmRoles = roleAssignment.realmRoles.filter { role ->
                    !restrictedRoles.contains(role.name)
                }
                
                // Create a new role assignment with the filtered roles
                val filteredRoleAssignment = roleAssignment.copy(
                    realmRoles = filteredRealmRoles
                )
                
                keycloakUserFacade.updateUserRoles(id, filteredRoleAssignment)
                return
            }
        }
        
        // For system admins, proceed with the original role assignment
        keycloakUserFacade.updateUserRoles(id, roleAssignment)
    }

    fun getUserRoles(id: String): RoleAssignmentDto {
        return keycloakUserFacade.getUserRoles(id)
    }
}
