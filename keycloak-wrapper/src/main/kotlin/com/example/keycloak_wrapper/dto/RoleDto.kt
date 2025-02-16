package com.example.keycloak_wrapper.dto

data class RoleDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val composite: Boolean = false,
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
    val search: String? = null
)

data class RoleAssignmentDto(
    val roleIds: List<String>
)
