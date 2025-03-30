package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakGroupFacade
import com.example.keycloak_wrapper.facade.KeycloakRoleFacade
import org.springframework.stereotype.Service

@Service
class TenantService(
    private val keycloakGroupFacade: KeycloakGroupFacade,
    private val keycloakRoleFacade: KeycloakRoleFacade,
    private val roleService: RoleService,
    private val groupService: GroupService
) {

    companion object {
        const val TENANT_PREFIX = "tenant_"
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
            if (!roleNames.contains(subgroup.name)) {
                groupService.deleteGroup(subgroup.id!!)
            }
        }
    }
}
