package com.example.keycloak_wrapper.dto

/**
 * Represents a customer (top-level group) in the multitenancy model.
 */
data class CustomerDto(
    val id: String,
    val name: String,
    val displayName: String,
    val tenants: List<TenantDto> = emptyList()
)

/**
 * Represents a tenant (subgroup under a customer).
 */
data class TenantDto(
    val id: String,
    val name: String,
    val displayName: String,
    val customerId: String? = null,
    val groups: List<GroupDto> = emptyList()
)

/**
 * Represents an ordinary user group (under a tenant or customer).
 */
data class GroupDto(
    val id: String? = null,
    val name: String,
    val path: String? = null,
    val subGroups: List<GroupDto> = emptyList(),
    val realmRoles: List<RoleDto> = emptyList(),
    val clientRoles: List<RoleDto> = emptyList(),
    val groupType: String = "group", // "group", "customer", or "tenant"
    val parentId: String? = null,
    val isTenant: Boolean = false,
    val tenantName: String? = null,
    val permissionGroups: List<GroupDto> = emptyList()
)

/**
 * DTO for creating a customer.
 */
data class CustomerCreateDto(
    val name: String,
    val displayName: String
)

/**
 * DTO for updating a customer.
 */
data class CustomerUpdateDto(
    val displayName: String
)

/**
 * DTO for creating a tenant under a customer.
 */
data class TenantCreateDto(
    val name: String,
    val displayName: String,
    val customerId: String
)

/**
 * DTO for updating a tenant.
 */
data class TenantUpdateDto(
    val displayName: String
)

/**
 * DTO for creating a group under a tenant or customer.
 */
data class GroupCreateDto(
    val name: String,
    val parentGroupId: String? = null,
    val realmRoles: List<String> = emptyList(),
    val clientRoles: List<String> = emptyList(),
    val isTenant: Boolean = false,
    val tenantName: String? = null
)

/**
 * DTO for updating a group.
 */
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
    val customerId: String? = null,
    val tenantId: String? = null,
    val tenantsOnly: Boolean = false
)
