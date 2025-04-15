package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.dto.TenantStatisticsDto
import com.example.keycloak_wrapper.dto.AllTenantsStatisticsDto
import com.example.keycloak_wrapper.security.TenantSecurityEvaluator
import com.example.keycloak_wrapper.service.TenantService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "Tenant management endpoints")
class TenantController(
    private val tenantService: TenantService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Get all tenants", description = "Returns all tenants the current user has access to")
    fun getTenants(): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val userId = securityContextHelper.getCurrentUserId()
            ?: return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
        
        val tenants = tenantService.getAccessibleTenants(userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Get tenant by ID", description = "Returns a specific tenant by ID if the user has access")
    fun getTenant(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        return tenantService.getTenant(id).ok()
    }

    @PostMapping
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(summary = "Create tenant", description = "Creates a new tenant (system admin only)")
    fun createTenant(@RequestBody tenantCreateDto: TenantCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        // Validate tenant name uniqueness
        val existingTenants = tenantService.getTenants()
        val tenantExists = existingTenants.any { 
            it.name == TenantService.TENANT_PREFIX + tenantCreateDto.name 
        }
        
        if (tenantExists) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false, 
                    error = "Tenant with name '${tenantCreateDto.name}' already exists"
                )
            )
        }
        
        return tenantService.createTenant(tenantCreateDto).created()
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Update tenant", description = "Updates a tenant's display name")
    fun updateTenant(
        @PathVariable id: String,
        @RequestBody tenantUpdateDto: TenantUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        return tenantService.updateTenant(id, tenantUpdateDto).ok()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(summary = "Delete tenant", description = "Deletes a tenant and all its subgroups (system admin only)")
    fun deleteTenant(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        tenantService.deleteTenant(id)
        return Unit.ok()
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(summary = "Sync tenants with roles", description = "Synchronizes all tenants with available roles")
    fun syncTenantsWithRoles(): ResponseEntity<ApiResponse<Unit>> {
        tenantService.syncTenantsWithRoles()
        return Unit.ok()
    }
    
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Get tenant users", description = "Returns all users belonging to a specific tenant")
    fun getTenantUsers(
        @PathVariable id: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<UserDto>>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        val users = tenantService.getTenantUsers(id, page, size)
        return ResponseEntity.ok(ApiResponse(success = true, data = users))
    }
    
    @PostMapping("/{id}/users/{userId}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Add user to tenant", description = "Adds a user to a specific tenant")
    fun addUserToTenant(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        tenantService.addUserToTenant(userId, id)
        return Unit.ok()
    }
    
    @DeleteMapping("/{id}/users/{userId}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Remove user from tenant", description = "Removes a user from a specific tenant")
    fun removeUserFromTenant(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        tenantService.removeUserFromTenant(userId, id)
        return Unit.ok()
    }
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(summary = "Get tenant statistics", description = "Returns statistics for a specific tenant")
    fun getTenantStatistics(@PathVariable id: String): ResponseEntity<ApiResponse<TenantStatisticsDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        val statistics = tenantService.getTenantStatistics(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = statistics))
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(summary = "Get all tenants statistics", description = "Returns statistics for all tenants (system admin only)")
    fun getAllTenantsStatistics(): ResponseEntity<ApiResponse<AllTenantsStatisticsDto>> {
        val statistics = tenantService.getAllTenantsStatistics()
        return ResponseEntity.ok(ApiResponse(success = true, data = statistics))
    }
}
