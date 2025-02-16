package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.dto.GroupDto
import com.example.keycloak_wrapper.dto.GroupCreateDto
import com.example.keycloak_wrapper.dto.GroupUpdateDto
import com.example.keycloak_wrapper.dto.RoleDto
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GroupMapper(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    fun toDto(group: GroupRepresentation): GroupDto {
        val realmRoles = group.realmRoles?.map { roleName ->
            val roleRep = keycloak.realm(realm).roles().get(roleName).toRepresentation()
            RoleDto(
                id = roleRep.id,
                name = roleRep.name,
                description = roleRep.description,
                composite = roleRep.isComposite,
                clientRole = roleRep.clientRole
            )
        } ?: emptyList()
        
        return GroupDto(
            id = group.id,
            name = group.name,
            path = group.path,
            subGroups = group.subGroups?.map { toDto(it) } ?: emptyList(),
            realmRoles = realmRoles
        )
    }

    fun toRepresentation(dto: GroupCreateDto): GroupRepresentation {
        return GroupRepresentation().apply {
            name = dto.name
        }
    }

    fun updateRepresentation(group: GroupRepresentation, dto: GroupUpdateDto): GroupRepresentation {
        return group.apply {
            dto.name?.let { name = it }
        }
    }
}
