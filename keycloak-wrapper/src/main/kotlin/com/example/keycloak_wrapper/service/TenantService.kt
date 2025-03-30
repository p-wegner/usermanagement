package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakGroupFacade
import com.example.keycloak_wrapper.facade.KeycloakRoleFacade
import com.example.keycloak_wrapper.facade.KeycloakUserFacade
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TenantService(
    private val keycloakGroupFacade: KeycloakGroupFacade,
    private val keycloakRoleFacade: KeycloakRoleFacade,
    private val keycloakUserFacade: KeycloakUserFacade,
    private val roleService: RoleService,
    private val groupService: GroupService
) {
    // Cache of tenant admin assignments (userId -> list of tenantIds)
    private val tenantAdminCache = ConcurrentHashMap<String, MutableList<String>>()

    companion object {
        const val TENANT_PREFIX = "tenant_"
        const val TENANT_ADMIN_GROUP_SUFFIX = "_admins"
    }

    /**
     * Creates a new tenant with the given name and display name.
     * A tenant is represented as a top-level group with the name "tenant_{name}".
     * For each client role, a subgroup is created within the tenant group.
     */
    fun createTenant(tenantCreateDto: TenantCreateDto): GroupDto {
        // Validate tenant name
        if (tenantCreateDto.name.isBlank()) {
            throw IllegalArgumentException("Tenant name cannot be blank")
        }

        // Check if tenant already exists
        val tenantGroupName = TENANT_PREFIX + tenantCreateDto.name
        val searchDto = GroupSearchDto(search = tenantGroupName)
        val existingGroups = groupService.getGroups(searchDto)
        if (existingGroups.any { it.name == tenantGroupName }) {
            throw IllegalArgumentException("Tenant with name ${tenantCreateDto.name} already exists")
        }

        // Create tenant group
        val groupCreateDto = GroupCreateDto(
            name = tenantGroupName,
            parentGroupId = null,
            realmRoles = emptyList(),
            isTenant = true,
            tenantName = tenantCreateDto.displayName
        )
        val tenantGroup = groupService.createGroup(groupCreateDto)

        // Create subgroups for each client role
        val roleSearchDto = RoleSearchDto(includeRealmRoles = false)
        val clientRoles = roleService.getRoles(roleSearchDto)
        createRoleSubgroups(tenantGroup.id!!, clientRoles)

        return groupService.getGroup(tenantGroup.id)
    }

    /**
     * Updates an existing tenant with the given ID.
     * Only the display name can be updated.
     */
    fun updateTenant(tenantId: String, tenantUpdateDto: TenantUpdateDto): GroupDto {
        val group = groupService.getGroup(tenantId)
        if (!group.isTenant) {
            throw IllegalArgumentException("Group with ID $tenantId is not a tenant")
        }

        val groupUpdateDto = GroupUpdateDto(
            tenantName = tenantUpdateDto.displayName
        )
        return groupService.updateGroup(tenantId, groupUpdateDto)
    }

    /**
     * Deletes a tenant with the given ID.
     * This will delete the tenant group and all its subgroups.
     */
    fun deleteTenant(tenantId: String) {
        val group = groupService.getGroup(tenantId)
        if (!group.isTenant) {
            throw IllegalArgumentException("Group with ID $tenantId is not a tenant")
        }

        groupService.deleteGroup(tenantId)
    }

    /**
     * Gets all tenants.
     * A tenant is a top-level group with the name starting with "tenant_".
     */
    fun getTenants(): List<GroupDto> {
        val searchDto = GroupSearchDto(tenantsOnly = true)
        return groupService.getGroups(searchDto)
    }

    /**
     * Gets a tenant by ID.
     */
    fun getTenant(tenantId: String): GroupDto {
        val group = groupService.getGroup(tenantId)
        if (!group.isTenant) {
            throw IllegalArgumentException("Group with ID $tenantId is not a tenant")
        }
        return group
    }

    /**
     * Creates subgroups for each client role within the tenant group.
     */
    private fun createRoleSubgroups(tenantGroupId: String, roles: List<RoleDto>) {
        roles.forEach { role ->
            val subgroupCreateDto = GroupCreateDto(
                name = role.name,
                parentGroupId = tenantGroupId,
                realmRoles = listOf(role.id)
            )
            groupService.createGroup(subgroupCreateDto)
        }
    }

    /**
     * Synchronizes tenant subgroups with client roles.
     * This will create subgroups for new client roles and remove subgroups for deleted client roles.
     */
    fun syncTenantsWithRoles() {
        val tenants = getTenants()
        val roleSearchDto = RoleSearchDto(includeRealmRoles = true)
        val roles = roleService.getRoles(roleSearchDto)

        tenants.forEach { tenant ->
            syncTenantWithRoles(tenant, roles)
        }
    }

    /**
     * Synchronizes a tenant's subgroups with client roles.
     */
    private fun syncTenantWithRoles(tenant: GroupDto, roles: List<RoleDto>) {
        val existingSubgroupNames = tenant.subGroups.map { it.name }
        val roleNames = roles.map { it.name }

        // Create subgroups for new roles
        roles.forEach { role ->
            if (!existingSubgroupNames.contains(role.name)) {
                val subgroupCreateDto = GroupCreateDto(
                    name = role.name,
                    parentGroupId = tenant.id,
                    realmRoles = listOf(role.id)
                )
                groupService.createGroup(subgroupCreateDto)
            }
        }

        // Remove subgroups for deleted roles
        tenant.subGroups.forEach { subgroup ->
            if (!roleNames.contains(subgroup.name) && !subgroup.name.endsWith(TENANT_ADMIN_GROUP_SUFFIX)) {
                groupService.deleteGroup(subgroup.id!!)
            }
        }
    }
    
    /**
     * Assigns a user as an admin for a specific tenant.
     * 
     * @param assignment The tenant admin assignment data
     * @return The created tenant admin assignment
     */
    fun assignTenantAdmin(assignment: TenantAdminAssignmentDto): TenantAdminDto {
        val tenant = getTenant(assignment.tenantId)
        val user = keycloakUserFacade.getUser(assignment.userId)
        
        // Ensure the tenant admin group exists
        val adminGroupName = "${tenant.name}${TENANT_ADMIN_GROUP_SUFFIX}"
        val adminGroup = tenant.subGroups.find { it.name == adminGroupName }
        val adminGroupId = if (adminGroup != null) {
            adminGroup.id!!
        } else {
            // Create the admin group if it doesn't exist
            val adminGroupDto = GroupCreateDto(
                name = adminGroupName,
                parentGroupId = tenant.id
            )
            val createdGroup = groupService.createGroup(adminGroupDto)
            createdGroup.id!!
        }
        
        // Add the TENANT_ADMIN role to the user if they don't have it
        val userRoles = keycloakUserFacade.getUserRoles(assignment.userId)
        val hasTenantAdminRole = userRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
        if (!hasTenantAdminRole) {
            val tenantAdminRoleId = keycloakRoleFacade.getRoles(RoleConstants.ROLE_TENANT_ADMIN, 0, 1)
                .firstOrNull()?.id
                ?: throw IllegalStateException("TENANT_ADMIN role not found")
            
            keycloakRoleFacade.addRolesToUser(assignment.userId, listOf(tenantAdminRoleId))
        }
        
        // Add the user to the tenant admin group
        keycloakUserFacade.addUserToGroup(assignment.userId, adminGroupId)
        
        // Update the cache
        tenantAdminCache.computeIfAbsent(assignment.userId) { mutableListOf() }
            .add(assignment.tenantId)
        
        return TenantAdminDto(
            userId = assignment.userId,
            username = user.username,
            tenantId = assignment.tenantId,
            tenantName = tenant.name
        )
    }
    
    /**
     * Removes a user as an admin for a specific tenant.
     * 
     * @param userId The ID of the user
     * @param tenantId The ID of the tenant
     */
    fun removeTenantAdmin(userId: String, tenantId: String) {
        val tenant = getTenant(tenantId)
        
        // Find the tenant admin group
        val adminGroupName = "${tenant.name}${TENANT_ADMIN_GROUP_SUFFIX}"
        val adminGroup = tenant.subGroups.find { it.name == adminGroupName }
            ?: return // No admin group, nothing to do
        
        // Remove the user from the tenant admin group
        keycloakUserFacade.removeUserFromGroup(userId, adminGroup.id!!)
        
        // Update the cache
        tenantAdminCache[userId]?.remove(tenantId)
        
        // Check if the user is still an admin for any tenant
        val userGroups = keycloakUserFacade.getUserGroups(userId)
        val isStillTenantAdmin = userGroups.any { group ->
            group.name.endsWith(TENANT_ADMIN_GROUP_SUFFIX)
        }
        
        // If not, remove the TENANT_ADMIN role
        if (!isStillTenantAdmin) {
            val tenantAdminRoleId = keycloakRoleFacade.getRoles(RoleConstants.ROLE_TENANT_ADMIN, 0, 1)
                .firstOrNull()?.id
                ?: return
            
            keycloakRoleFacade.removeRolesFromUser(userId, listOf(tenantAdminRoleId))
        }
    }
    
    /**
     * Gets all tenant admins for a specific tenant.
     * 
     * @param tenantId The ID of the tenant
     * @return List of users who are admins for the tenant
     */
    fun getTenantAdmins(tenantId: String): TenantAdminsResponseDto {
        val tenant = getTenant(tenantId)
        
        // Find the tenant admin group
        val adminGroupName = "${tenant.name}${TENANT_ADMIN_GROUP_SUFFIX}"
        val adminGroup = tenant.subGroups.find { it.name == adminGroupName }
        
        val admins = if (adminGroup != null) {
            keycloakGroupFacade.getGroupMembers(adminGroup.id!!)
                .map { user ->
                    UserDto(
                        id = user.id,
                        username = user.username,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        enabled = user.isEnabled,
                        isTenantAdmin = true
                    )
                }
        } else {
            emptyList()
        }
        
        return TenantAdminsResponseDto(
            tenantId = tenantId,
            tenantName = tenant.name,
            admins = admins
        )
    }
    
    /**
     * Gets all tenants for which a user is an admin.
     * 
     * @param userId The ID of the user
     * @return List of tenants the user is an admin for
     */
    fun getUserTenants(userId: String): AdminTenantsResponseDto {
        val user = keycloakUserFacade.getUser(userId)
        val userGroups = keycloakUserFacade.getUserGroups(userId)
        
        // Find all admin groups the user belongs to
        val adminGroups = userGroups.filter { group ->
            group.name.endsWith(TENANT_ADMIN_GROUP_SUFFIX)
        }
        
        // For each admin group, find the parent tenant
        val tenants = adminGroups.mapNotNull { adminGroup ->
            val tenantName = adminGroup.name.removeSuffix(TENANT_ADMIN_GROUP_SUFFIX)
            val tenant = getTenants().find { it.name == tenantName }
            
            tenant?.let {
                TenantDto(
                    id = it.id!!,
                    name = it.name,
                    displayName = it.tenantName ?: it.name,
                    permissionGroups = it.subGroups
                )
            }
        }
        
        return AdminTenantsResponseDto(
            userId = userId,
            username = user.username,
            tenants = tenants
        )
    }
    
    /**
     * Checks if a user is an admin for a specific tenant.
     * 
     * @param userId The ID of the user
     * @param tenantId The ID of the tenant
     * @return true if the user is an admin for the tenant, false otherwise
     */
    fun isUserTenantAdmin(userId: String, tenantId: String): Boolean {
        // Check the cache first
        val cachedTenants = tenantAdminCache[userId]
        if (cachedTenants != null) {
            return cachedTenants.contains(tenantId)
        }
        
        // Cache miss, check from Keycloak
        val tenant = getTenant(tenantId)
        val userGroups = keycloakUserFacade.getUserGroups(userId)
        
        val adminGroupName = "${tenant.name}${TENANT_ADMIN_GROUP_SUFFIX}"
        val isAdmin = userGroups.any { it.name == adminGroupName }
        
        // Update the cache
        if (isAdmin) {
            tenantAdminCache.computeIfAbsent(userId) { mutableListOf() }
                .add(tenantId)
        }
        
        return isAdmin
    }
    
    /**
     * Gets all tenants that the current user has access to.
     * For system admins, this returns all tenants.
     * For tenant admins, this returns only the tenants they are admins for.
     * 
     * @param userId The ID of the current user
     * @return List of accessible tenants
     */
    fun getAccessibleTenants(userId: String): List<GroupDto> {
        val userRoles = keycloakUserFacade.getUserRoles(userId)
        val isSystemAdmin = userRoles.realmRoles.any { it.name == RoleConstants.ROLE_ADMIN }
        
        if (isSystemAdmin) {
            return getTenants()
        }
        
        val isTenantAdmin = userRoles.realmRoles.any { it.name == RoleConstants.ROLE_TENANT_ADMIN }
        if (isTenantAdmin) {
            val userGroups = keycloakUserFacade.getUserGroups(userId)
            
            // Find all admin groups the user belongs to
            val adminGroups = userGroups.filter { group ->
                group.name.endsWith(TENANT_ADMIN_GROUP_SUFFIX)
            }
            
            // For each admin group, find the parent tenant
            return adminGroups.mapNotNull { adminGroup ->
                val tenantName = adminGroup.name.removeSuffix(TENANT_ADMIN_GROUP_SUFFIX)
                getTenants().find { it.name == tenantName }
            }
        }
        
        return emptyList()
    }
}
