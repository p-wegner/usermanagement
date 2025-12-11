package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*

interface ITenantService {
    fun getAccessibleTenants(userId: String, customerId: String? = null): List<TenantDto>
    fun getTenants(): List<GroupDto>
    fun getTenant(id: String): TenantDto
    fun getTenantsByCustomer(customerId: String): List<TenantDto>
    fun createTenant(dto: TenantCreateDto): TenantDto
    fun updateTenant(id: String, dto: TenantUpdateDto): TenantDto
    fun deleteTenant(id: String)
    fun getTenantUsers(tenantId: String, page: Int, size: Int): List<UserDto>
    fun addUserToTenant(userId: String, tenantId: String)
    fun removeUserFromTenant(userId: String, tenantId: String)
    fun assignTenantAdmin(assignment: TenantAdminAssignmentDto): TenantAdminDto
    fun removeTenantAdmin(userId: String, tenantId: String)
    fun getTenantAdmins(tenantId: String): TenantAdminsResponseDto
    fun getUserTenants(userId: String): AdminTenantsResponseDto
    fun isUserTenantAdmin(userId: String, tenantId: String): Boolean
    fun hasUserAccessToUser(currentUserId: String, targetUserId: String): Boolean
    fun filterUsersByTenantAccess(currentUserId: String, users: List<UserDto>): List<UserDto>
}
