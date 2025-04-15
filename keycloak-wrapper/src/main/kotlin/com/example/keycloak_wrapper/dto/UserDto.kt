package com.example.keycloak_wrapper.dto

data class UserDto(
    val id: String? = null,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val enabled: Boolean = true,
    val realmRoles: List<String> = emptyList(),
    val clientRoles: Map<String, List<String>> = emptyMap(),
    val isTenantAdmin: Boolean = false,
    val managedTenants: List<String> = emptyList(),
    val tenantId: String? = null
)

data class UserCreateDto(
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val enabled: Boolean = true,
    val realmRoles: List<String> = emptyList(),
    val tenantId: String? = null
)

data class UserUpdateDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val enabled: Boolean? = null,
    val realmRoles: List<String>? = null,
    val tenantId: String? = null,
    val managedTenants: List<String>? = null
)

data class UserSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val currentUserId: String? = null,
    val tenantId: String? = null
)
