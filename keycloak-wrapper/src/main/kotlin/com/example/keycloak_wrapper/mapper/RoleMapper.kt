package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.dto.RoleDto
import com.example.keycloak_wrapper.dto.RoleCreateDto
import com.example.keycloak_wrapper.dto.RoleUpdateDto
import org.keycloak.representations.idm.RoleRepresentation
import org.springframework.stereotype.Component

@Component
class RoleMapper {
    fun toDto(role: RoleRepresentation): RoleDto {
        return RoleDto(
            id = role.id,
            name = role.name,
            description = role.description,
            composite = role.isComposite,
            clientRole = role.clientRole
        )
    }

    fun toRepresentation(dto: RoleCreateDto): RoleRepresentation {
        return RoleRepresentation().apply {
            name = dto.name
            description = dto.description
            isComposite = dto.composite
        }
    }

    fun updateRepresentation(role: RoleRepresentation, dto: RoleUpdateDto): RoleRepresentation {
        return role.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.composite?.let { isComposite = it }
        }
    }
}
