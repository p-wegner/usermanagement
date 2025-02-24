package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakRoleFacade
import com.example.keycloak_wrapper.mapper.RoleMapper
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val keycloakRoleFacade: KeycloakRoleFacade,
    private val roleMapper: RoleMapper
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
        return roleMapper.toDto(createdRole)
    }

    fun updateRole(id: String, roleDto: RoleUpdateDto): RoleDto {
        val existingRole = keycloakRoleFacade.getRole(id)
        val updatedRepresentation = roleMapper.updateRepresentation(existingRole, roleDto)
        val updatedRole = keycloakRoleFacade.updateRole(id, updatedRepresentation)
        return roleMapper.toDto(updatedRole)
    }

    fun deleteRole(id: String) {
        keycloakRoleFacade.deleteRole(id)
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
}

