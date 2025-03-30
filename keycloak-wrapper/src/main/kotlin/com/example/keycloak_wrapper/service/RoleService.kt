package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakRoleFacade
import com.example.keycloak_wrapper.listener.RoleCreatedEvent
import com.example.keycloak_wrapper.listener.RoleDeletedEvent
import com.example.keycloak_wrapper.listener.RoleUpdatedEvent
import com.example.keycloak_wrapper.mapper.RoleMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val keycloakRoleFacade: KeycloakRoleFacade,
    private val roleMapper: RoleMapper,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun getRoles(searchDto: RoleSearchDto): List<RoleDto> {
        val roles = mutableListOf<RoleDto>()
        
        if (searchDto.includeRealmRoles) {
            val realmRoles = keycloakRoleFacade.getRoles(
                search = searchDto.search,
                first = searchDto.page * searchDto.size,
                max = searchDto.size
            )
            roles.addAll(realmRoles.map { roleMapper.toDto(it) })
        }

        if (searchDto.includeClientRoles) {
            val clientRoles = keycloakRoleFacade.getClientRoles(
                clientId = searchDto.clientId,
                search = searchDto.search,
                first = searchDto.page * searchDto.size,
                max = searchDto.size
            )
            roles.addAll(clientRoles.map { roleMapper.toDto(it) })
        }

        return roles
    }

    fun getRole(id: String): RoleDto {
        val role = keycloakRoleFacade.getRole(id)
        return roleMapper.toDto(role)
    }

    fun createRole(roleDto: RoleCreateDto): RoleDto {
        val roleRepresentation = roleMapper.toRepresentation(roleDto)
        val createdRole = keycloakRoleFacade.createRole(roleRepresentation)
        val roleDto = roleMapper.toDto(createdRole)
        
        // Publish role created event
        applicationEventPublisher.publishEvent(RoleCreatedEvent(roleDto))
        
        return roleDto
    }

    fun updateRole(id: String, roleDto: RoleUpdateDto): RoleDto {
        val existingRole = keycloakRoleFacade.getRole(id)
        val originalRoleDto = roleMapper.toDto(existingRole)
        
        val updatedRepresentation = roleMapper.updateRepresentation(existingRole, roleDto)
        val updatedRole = keycloakRoleFacade.updateRole(id, updatedRepresentation)
        val updatedRoleDto = roleMapper.toDto(updatedRole)
        
        // Publish role updated event
        applicationEventPublisher.publishEvent(RoleUpdatedEvent(originalRoleDto, updatedRoleDto))
        
        return updatedRoleDto
    }

    fun deleteRole(id: String) {
        val role = keycloakRoleFacade.getRole(id)
        val roleDto = roleMapper.toDto(role)
        
        keycloakRoleFacade.deleteRole(id)
        
        // Publish role deleted event
        applicationEventPublisher.publishEvent(RoleDeletedEvent(roleDto))
    }

    fun addCompositeRoles(roleId: String, compositeRoleIds: List<String>): RoleDto {
        keycloakRoleFacade.addCompositeRoles(roleId, compositeRoleIds)
        return getRole(roleId)
    }

    fun removeCompositeRoles(roleId: String, compositeRoleIds: List<String>): RoleDto {
        keycloakRoleFacade.removeCompositeRoles(roleId, compositeRoleIds)
        return getRole(roleId)
    }

    fun getCompositeRoles(roleId: String): List<RoleDto> {
        val roles = keycloakRoleFacade.getCompositeRoles(roleId)
        return roles.map { roleMapper.toDto(it) }
    }
    
    // Group role management methods
    
    fun getGroupRoles(groupId: String): List<RoleDto> {
        val roles = keycloakRoleFacade.getGroupRoles(groupId)
        return roles.map { roleMapper.toDto(it) }
    }
    
    fun addRolesToGroup(groupId: String, roleIds: List<String>) {
        keycloakRoleFacade.addRolesToGroup(groupId, roleIds)
    }
    
    fun removeRolesFromGroup(groupId: String, roleIds: List<String>) {
        keycloakRoleFacade.removeRolesFromGroup(groupId, roleIds)
    }
    
    fun getGroupClientRoles(groupId: String, clientId: String): List<RoleDto> {
        val roles = keycloakRoleFacade.getGroupClientRoles(groupId, clientId)
        return roles.map { roleMapper.toDto(it) }
    }
    
    fun addClientRolesToGroup(groupId: String, clientId: String, roleIds: List<String>) {
        keycloakRoleFacade.addClientRolesToGroup(groupId, clientId, roleIds)
    }
    
    fun removeClientRolesToGroup(groupId: String, clientId: String, roleIds: List<String>) {
        keycloakRoleFacade.removeClientRolesFromGroup(groupId, clientId, roleIds)
    }
}

