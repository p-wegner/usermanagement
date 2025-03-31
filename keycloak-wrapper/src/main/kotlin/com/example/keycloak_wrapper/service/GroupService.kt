package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakGroupFacade
import com.example.keycloak_wrapper.mapper.GroupMapper
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val keycloakGroupFacade: KeycloakGroupFacade,
    private val groupMapper: GroupMapper
) {
    fun getGroups(searchDto: GroupSearchDto): List<GroupDto> {
        val groups = keycloakGroupFacade.getGroups(
            search = searchDto.search,
            first = searchDto.page * searchDto.size,
            max = searchDto.size,
            tenantsOnly = searchDto.tenantsOnly
        )
        return groups.map { groupMapper.toDto(it) }
    }


    fun getTenants(searchDto: GroupSearchDto): List<TenantDto> {
        val tenantGroups = keycloakGroupFacade.getGroups(
            search = searchDto.search,
            first = searchDto.page * searchDto.size,
            max = searchDto.size,
            tenantsOnly = true
        )
        return tenantGroups.map { mapToTenantDto(it) }
    }

    fun getTenant(id: String): TenantDto {
        val tenantGroup = keycloakGroupFacade.getGroup(id)
        return mapToTenantDto(tenantGroup)
    }

    fun updateTenant(id: String, tenantUpdateDto: TenantUpdateDto): TenantDto {
        val updatedTenantGroup = keycloakGroupFacade.updateTenant(id, tenantUpdateDto)
        return mapToTenantDto(updatedTenantGroup)
    }

    fun deleteTenant(id: String) {
        keycloakGroupFacade.deleteTenant(id)
    }

    private fun mapToTenantDto(groupRepresentation: GroupRepresentation): TenantDto {
        // Implement mapping logic from GroupRepresentation to TenantDto
        return TenantDto(
            id = groupRepresentation.id,
            name = groupRepresentation.name,
            displayName = groupRepresentation.attributes?.get("displayName")?.firstOrNull() ?: groupRepresentation.name,
            permissionGroups = groupRepresentation.subGroups.map { groupMapper.toDto(it) }
        )
    }

    fun getGroup(id: String): GroupDto {
        val group = keycloakGroupFacade.getGroup(id)
        return groupMapper.toDto(group)
    }

    fun createGroup(groupDto: GroupCreateDto): GroupDto {
        val groupRepresentation = groupMapper.toRepresentation(groupDto)
        val createdGroup = if (groupDto.parentGroupId != null) {
            keycloakGroupFacade.createSubGroup(groupDto.parentGroupId, groupRepresentation)
        } else {
            keycloakGroupFacade.createGroup(groupRepresentation)
        }
        return groupMapper.toDto(createdGroup)
    }

    fun updateGroup(id: String, groupDto: GroupUpdateDto): GroupDto {
        val existingGroup = keycloakGroupFacade.getGroup(id)
        val updatedRepresentation = groupMapper.updateRepresentation(existingGroup, groupDto)
        val updatedGroup = keycloakGroupFacade.updateGroup(id, updatedRepresentation)
        return groupMapper.toDto(updatedGroup)
    }

    fun deleteGroup(id: String) {
        keycloakGroupFacade.deleteGroup(id)
    }

    fun updateGroupRoles(id: String, roleAssignment: RoleAssignmentDto): GroupDto {
        keycloakGroupFacade.updateGroupRoles(id, roleAssignment)
        return getGroup(id)
    }

    fun getGroupRoles(id: String): RoleAssignmentDto {
        return keycloakGroupFacade.getGroupRoles(id)
    }
}
