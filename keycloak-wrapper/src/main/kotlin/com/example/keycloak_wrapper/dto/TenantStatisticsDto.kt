package com.example.keycloak_wrapper.dto

/**
 * Data class representing statistics for a tenant.
 */
data class TenantStatisticsDto(
    val tenantId: String,
    val tenantName: String,
    val userCount: Int,
    val activeUserCount: Int,
    val groupCount: Int,
    val roleCount: Int,
    val adminCount: Int
)

/**
 * Data class representing statistics for all tenants.
 */
data class AllTenantsStatisticsDto(
    val totalTenants: Int,
    val totalUsers: Int,
    val totalGroups: Int,
    val totalRoles: Int,
    val tenantStats: List<TenantStatisticsDto>
)
