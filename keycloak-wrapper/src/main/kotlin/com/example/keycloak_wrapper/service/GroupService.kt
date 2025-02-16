package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakGroupFacade
import com.example.keycloak_wrapper.mapper.GroupMapper
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
            max = searchDto.size
        )
        return groups.map { groupMapper.toDto(it) }
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

    fun addGroupRoles(id: String, roles: List<String>): GroupDto {
        val group = keycloakGroupFacade.getGroup(id)
        val currentRoles = group.realmRoles?.toMutableList() ?: mutableListOf()
        currentRoles.addAll(roles)
        keycloakGroupFacade.updateGroupRoles(id, currentRoles.distinct())
        return getGroup(id)
    }

    fun removeGroupRoles(id: String, roles: List<String>): GroupDto {
        val group = keycloakGroupFacade.getGroup(id)
        val currentRoles = group.realmRoles?.toMutableList() ?: mutableListOf()
        currentRoles.removeAll(roles.toSet())
        keycloakGroupFacade.updateGroupRoles(id, currentRoles)
        return getGroup(id)
    }

    fun getGroupRoles(id: String): List<String> {
        val group = keycloakGroupFacade.getGroup(id)
        return group.realmRoles ?: emptyList()
    }
}
