package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.dto.TenantStatisticsDto
import com.example.keycloak_wrapper.dto.AllTenantsStatisticsDto
import com.example.keycloak_wrapper.security.TenantSecurityEvaluator
import com.example.keycloak_wrapper.service.TenantService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants")
class TenantController(
    private val tenantService: TenantService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {
    @GetMapping
    @Operation(
        summary = "Get all tenants", 
        description = "Returns all tenants the current user has access to. System admins see all tenants, tenant admins see only their assigned tenants."
    )
    fun getTenants(): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val userId = securityContextHelper.getCurrentUserId()
            ?: return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
        
        val tenants = tenantService.getAccessibleTenants(userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get tenant by ID", 
        description = "Returns a specific tenant by ID if the user has access. System admins can access any tenant, tenant admins can only access their assigned tenants."
    )
    fun getTenant(
        @Parameter(description = "ID of the tenant to retrieve", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<GroupDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        return tenantService.getTenant(id).ok()
    }

    @PostMapping
    @Operation(
        summary = "Create tenant", 
        description = "Creates a new tenant. Only system administrators can create tenants."
    )
    fun createTenant(
        @Parameter( required = true)
        @RequestBody tenantCreateDto: TenantCreateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
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
    @Operation(
        summary = "Update tenant", 
        description = "Updates a tenant's display name. System admins can update any tenant, tenant admins can only update their assigned tenants."
    )
    fun updateTenant(
        @Parameter(description = "ID of the tenant to update", required = true)
        @PathVariable id: String,
        @Parameter(description = "Tenant update details", required = true)
        @RequestBody tenantUpdateDto: TenantUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        return tenantService.updateTenant(id, tenantUpdateDto).ok()
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete tenant",
        description = "Deletes a tenant and all its subgroups. Only system administrators can delete tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Tenant successfully deleted"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
    )
    fun deleteTenant(
        @Parameter(description = "ID of the tenant to delete", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Unit>> {
        tenantService.deleteTenant(id)
        return Unit.ok()
    }


    @GetMapping("/{id}/users")
    @Operation(
        summary = "Get tenant users", 
        description = "Returns all users belonging to a specific tenant. System admins can see users in any tenant, tenant admins can only see users in their assigned tenants."
    )
    fun getTenantUsers(
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable id: String,
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<UserDto>>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        val users = tenantService.getTenantUsers(id, page, size)
        return ResponseEntity.ok(ApiResponse(success = true, data = users))
    }
    
    @PostMapping("/{id}/users/{userId}")
    @Operation(
        summary = "Add user to tenant", 
        description = "Adds a user to a specific tenant. System admins can add users to any tenant, tenant admins can only add users to their assigned tenants."
    )
    fun addUserToTenant(
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable id: String,
        @Parameter(description = "ID of the user to add", required = true)
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        tenantService.addUserToTenant(userId, id)
        return Unit.ok()
    }
    
    @DeleteMapping("/{id}/users/{userId}")
    @Operation(
        summary = "Remove user from tenant", 
        description = "Removes a user from a specific tenant. System admins can remove users from any tenant, tenant admins can only remove users from their assigned tenants."
    )
    fun removeUserFromTenant(
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable id: String,
        @Parameter(description = "ID of the user to remove", required = true)
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        // Verify tenant management access
        tenantSecurityEvaluator.verifyTenantManagement(id)
        
        tenantService.removeUserFromTenant(userId, id)
        return Unit.ok()
    }
}
