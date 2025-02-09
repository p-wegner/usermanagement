package com.example.keycloak_wrapper.dto.user

data class UserDto(
    val id: String? = null,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val enabled: Boolean = true
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val enabled: Boolean = true
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val enabled: Boolean? = null
)

data class UserSearchRequest(
    val search: String? = null,
    val page: Int = 0,
    val size: Int = 20
)
