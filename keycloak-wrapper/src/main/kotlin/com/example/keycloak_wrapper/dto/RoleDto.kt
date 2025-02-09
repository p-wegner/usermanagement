package com.example.keycloak_wrapper.dto

data class RoleDto(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val composite: Boolean = false,
    val clientRole: Boolean = false
)

data class RoleCreateDto(
    val name: String,
    val description: String? = null,
    val composite: Boolean = false
)

data class RoleUpdateDto(
    val name: String? = null,
    val description: String? = null,
    val composite: Boolean? = null
)

data class RoleSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null
)
