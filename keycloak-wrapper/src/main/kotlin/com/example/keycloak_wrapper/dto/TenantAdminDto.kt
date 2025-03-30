package com.example.keycloak_wrapper.dto

/**
 * Data class representing a tenant admin assignment.
 */
data class TenantAdminDto(
    val userId: String,
    val username: String,
    val tenantId: String,
    val tenantName: String
)

/**
 * Data class for assigning a user as a tenant admin.
 */
data class TenantAdminAssignmentDto(
    val userId: String,
    val tenantId: String
)

/**
 * Data class for retrieving tenant admins.
 */
data class TenantAdminsResponseDto(
    val tenantId: String,
    val tenantName: String,
    val admins: List<UserDto>
)

/**
 * Data class for retrieving tenants for an admin.
 */
data class AdminTenantsResponseDto(
    val userId: String,
    val username: String,
    val tenants: List<TenantDto>
)
