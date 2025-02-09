package com.example.keycloak_wrapper.dto

data class UserDto(
    val id: String? = null,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val enabled: Boolean = true
)

data class UserCreateDto(
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val enabled: Boolean = true
)

data class UserUpdateDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val enabled: Boolean? = null
)

data class UserSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null
)
