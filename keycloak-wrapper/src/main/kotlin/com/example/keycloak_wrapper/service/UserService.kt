package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.facade.KeycloakUserFacade
import com.example.keycloak_wrapper.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class UserService(
    private val keycloakUserFacade: KeycloakUserFacade,
    private val userMapper: UserMapper
) {
    fun getUsers(searchDto: UserSearchDto): Pair<List<UserDto>, Int> {
        val (users, total) = keycloakUserFacade.getUsers(
            search = searchDto.search,
            firstResult = searchDto.page * searchDto.size,
            maxResults = searchDto.size
        )
        return Pair(users.map { userMapper.toDto(it) }, total)
    }

    fun getUser(id: String): UserDto {
        val user = keycloakUserFacade.getUser(id)
        return userMapper.toDto(user)
    }

    fun createUser(userDto: UserCreateDto): UserDto {
        val userRepresentation = userMapper.toRepresentation(userDto)
        val createdUser = keycloakUserFacade.createUser(userRepresentation)
        return userMapper.toDto(createdUser)
    }

    fun updateUser(id: String, userDto: UserUpdateDto): UserDto {
        val existingUser = keycloakUserFacade.getUser(id)
        val updatedRepresentation = userMapper.updateRepresentation(existingUser, userDto)
        val updatedUser = keycloakUserFacade.updateUser(id, updatedRepresentation)
        return userMapper.toDto(updatedUser)
    }

    fun deleteUser(id: String) {
        keycloakUserFacade.deleteUser(id)
    }

    fun updateUserRoles(id: String, roles: List<String>) {
        keycloakUserFacade.updateUserRoles(id, roles)
    }
}
