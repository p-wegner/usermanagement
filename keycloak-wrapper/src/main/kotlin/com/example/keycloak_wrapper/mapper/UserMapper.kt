package com.example.keycloak_wrapper.mapper

import com.example.keycloak_wrapper.dto.UserDto
import com.example.keycloak_wrapper.dto.UserCreateDto
import com.example.keycloak_wrapper.dto.UserUpdateDto
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class UserMapper {
    fun toDto(user: UserRepresentation): UserDto {
        val realmRoles = user.realmRoles ?: emptyList()
        return UserDto(
            id = user.id,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email ?: "",
            enabled = user.isEnabled,
            realmRoles = realmRoles
        )
    }

    fun toRepresentation(dto: UserCreateDto): UserRepresentation {
        val myCredentials = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = dto.password
            isTemporary = false
        }

        return UserRepresentation().apply {
            username = dto.username
            firstName = dto.firstName
            lastName = dto.lastName
            email = dto.email
            isEnabled = dto.enabled
            isEmailVerified = false
            credentials = listOf(myCredentials)
        }
    }

    fun updateRepresentation(user: UserRepresentation, dto: UserUpdateDto): UserRepresentation {
        return user.apply {
            dto.firstName?.let { firstName = it }
            dto.lastName?.let { lastName = it }
            dto.email?.let { email = it }
            dto.enabled?.let { isEnabled = it }
        }
    }
}
