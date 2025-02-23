package com.example.keycloak_wrapper.dto

data class RoleDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val composite: Boolean = false,
    // TODO 23/02/2025 PWegner: could be a clientId field instead of boolean
    val clientRole: Boolean = false,
    val compositeRoles: List<RoleDto> = emptyList()
)

data class RoleCreateDto(
    val name: String,
    val description: String? = null,
    val composite: Boolean = false,
    val compositeRoleIds: List<String> = emptyList()
)

data class RoleUpdateDto(
    val name: String? = null,
    val description: String? = null,
    val composite: Boolean? = null,
    val compositeRoleIds: List<String>? = null
)

data class RoleSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val clientId: String? = null
)

