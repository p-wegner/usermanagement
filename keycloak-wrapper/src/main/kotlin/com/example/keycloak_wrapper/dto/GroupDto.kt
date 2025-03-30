package com.example.keycloak_wrapper.dto

data class GroupDto(
    val id: String? = null,
    val name: String,
    val path: String? = null,
    val subGroups: List<GroupDto> = emptyList(),
    val realmRoles: List<RoleDto> = emptyList(),
    val isTenant: Boolean = false,
    val tenantName: String? = null
)

data class GroupCreateDto(
    val name: String,
    val parentGroupId: String? = null,
    val realmRoles: List<String> = emptyList(),
    val isTenant: Boolean = false,
    val tenantName: String? = null
)

data class GroupUpdateDto(
    val name: String? = null,
    val tenantName: String? = null
)

data class GroupRoleAssignmentDto(
    val roleIds: List<String>
)

data class GroupSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val tenantsOnly: Boolean = false
)

data class TenantCreateDto(
    val name: String,
    val displayName: String
)

data class TenantUpdateDto(
    val displayName: String
)

data class TenantDto(
    val id: String,
    val name: String,
    val displayName: String,
    val permissionGroups: List<GroupDto>
)
