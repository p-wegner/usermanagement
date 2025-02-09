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
        val roles = keycloakRoleFacade.getRoles(
            search = searchDto.search,
            first = searchDto.page * searchDto.size,
            max = searchDto.size
        )
        return roles.map { roleMapper.toDto(it) }
    }

    fun getRole(name: String): RoleDto {
        val role = keycloakRoleFacade.getRole(name)
        return roleMapper.toDto(role)
    }

    fun createRole(roleDto: RoleCreateDto): RoleDto {
        val roleRepresentation = roleMapper.toRepresentation(roleDto)
        val createdRole = keycloakRoleFacade.createRole(roleRepresentation)
        return roleMapper.toDto(createdRole)
    }

    fun updateRole(name: String, roleDto: RoleUpdateDto): RoleDto {
        val existingRole = keycloakRoleFacade.getRole(name)
        val updatedRepresentation = roleMapper.updateRepresentation(existingRole, roleDto)
        val updatedRole = keycloakRoleFacade.updateRole(name, updatedRepresentation)
        return roleMapper.toDto(updatedRole)
    }

    fun deleteRole(name: String) {
        keycloakRoleFacade.deleteRole(name)
    }
}

