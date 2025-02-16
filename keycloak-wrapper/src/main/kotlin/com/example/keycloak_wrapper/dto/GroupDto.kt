package com.example.keycloak_wrapper.dto

data class GroupDto(
    val id: String? = null,
    val name: String,
    val path: String? = null,
    val subGroups: List<GroupDto> = emptyList(),
    val realmRoles: List<RoleDto> = emptyList()
)

data class GroupCreateDto(
    val name: String,
    val parentGroupId: String? = null,
    val realmRoles: List<String> = emptyList()
)

data class GroupUpdateDto(
    val name: String? = null
)

data class GroupRoleAssignmentDto(
    val roleIds: List<String>
)

data class GroupSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null
)
