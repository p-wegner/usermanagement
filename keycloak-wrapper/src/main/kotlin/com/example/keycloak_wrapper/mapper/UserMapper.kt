package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.dto.UserDto
import com.example.keycloak_wrapper.dto.UserCreateDto
import com.example.keycloak_wrapper.dto.UserUpdateDto
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class UserMapper {
    fun toDto(user: UserRepresentation): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            enabled = user.isEnabled
        )
    }

    fun toRepresentation(dto: UserCreateDto): UserRepresentation {
        return UserRepresentation().apply {
            username = dto.username
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            isEnabled = dto.enabled
            isEmailVerified = false
        }
    }

    fun updateRepresentation(user: UserRepresentation, dto: UserUpdateDto) {
        dto.firstName?.let { user.firstName = it }
        dto.lastName?.let { user.lastName = it }
        dto.email?.let { user.email = it }
        dto.enabled?.let { user.isEnabled = it }
    }
}
