package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.exception.KeycloakException
import com.example.keycloak_wrapper.facade.KeycloakUserFacade
import com.example.keycloak_wrapper.mapper.UserMapper
import org.springframework.stereotype.Service
import javax.ws.rs.NotFoundException

@Service
class UserService(
    private val keycloakUserFacade: KeycloakUserFacade,
    private val userMapper: UserMapper
) {
    fun getUsers(searchDto: UserSearchDto): List<UserDto> {
        return try {
            keycloakUserFacade.getUsers(searchDto.search, searchDto.page, searchDto.size)
                .map { userMapper.toDto(it) }
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch users", e)
        }
    }

    fun getUser(id: String): UserDto {
        return try {
            val user = keycloakUserFacade.getUser(id)
            userMapper.toDto(user)
        } catch (e: NotFoundException) {
            throw KeycloakException("User not found with id: $id", e)
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch user", e)
        }
    }

    fun createUser(userDto: UserCreateDto): UserDto {
        return try {
            val userRepresentation = userMapper.toRepresentation(userDto)
            val createdUser = keycloakUserFacade.createUser(userRepresentation)
            userMapper.toDto(createdUser)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create user", e)
        }
    }

    fun updateUser(id: String, userDto: UserUpdateDto): UserDto {
        return try {
            val existingUser = keycloakUserFacade.getUser(id)
            userMapper.updateRepresentation(existingUser, userDto)
            val updatedUser = keycloakUserFacade.updateUser(id, existingUser)
            userMapper.toDto(updatedUser)
        } catch (e: NotFoundException) {
            throw KeycloakException("User not found with id: $id", e)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update user", e)
        }
    }

    fun deleteUser(id: String) {
        try {
            keycloakUserFacade.deleteUser(id)
        } catch (e: NotFoundException) {
            throw KeycloakException("User not found with id: $id", e)
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete user", e)
        }
    }
}
