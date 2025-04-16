package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*

interface ITenantService {
    fun getAccessibleTenants(userId: String, customerId: String? = null): List<TenantDto>
    fun getTenant(id: String): TenantDto
    fun getTenantsByCustomer(customerId: String): List<TenantDto>
    fun createTenant(dto: TenantCreateDto): TenantDto
    fun updateTenant(id: String, dto: TenantUpdateDto): TenantDto
    fun deleteTenant(id: String)
    fun getTenantUsers(tenantId: String, page: Int, size: Int): List<UserDto>
    fun addUserToTenant(userId: String, tenantId: String)
    fun removeUserFromTenant(userId: String, tenantId: String)
}
