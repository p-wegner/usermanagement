package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.dto.GroupDto
import com.example.keycloak_wrapper.dto.GroupCreateDto
import com.example.keycloak_wrapper.dto.GroupUpdateDto
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.stereotype.Component

@Component
class GroupMapper {
    fun toDto(group: GroupRepresentation): GroupDto {
        return GroupDto(
            id = group.id,
            name = group.name,
            path = group.path,
            subGroups = group.subGroups?.map { toDto(it) } ?: emptyList()
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
