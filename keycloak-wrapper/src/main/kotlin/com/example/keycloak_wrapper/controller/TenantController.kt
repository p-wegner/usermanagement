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
@Tag(name = "Tenants", description = "Tenant management endpoints")
@SecurityRequirement(name = "OAuth2")
class TenantController(
    private val tenantService: TenantService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {
    @GetMapping
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get all tenants", 
        description = "Returns all tenants the current user has access to. System admins see all tenants, tenant admins see only their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Successfully retrieved tenants"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have required roles")
    )
    fun getTenants(): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val userId = securityContextHelper.getCurrentUserId()
            ?: return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
        
        val tenants = tenantService.getAccessibleTenants(userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get tenant by ID", 
        description = "Returns a specific tenant by ID if the user has access. System admins can access any tenant, tenant admins can only access their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Successfully retrieved tenant"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
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
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(
        summary = "Create tenant", 
        description = "Creates a new tenant. Only system administrators can create tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "201", description = "Tenant successfully created"),
        SwaggerResponse(responseCode = "400", description = "Invalid request - tenant name already exists or invalid data"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin")
    )
    fun createTenant(
        @Parameter(description = "Tenant creation details", required = true)
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
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Update tenant", 
        description = "Updates a tenant's display name. System admins can update any tenant, tenant admins can only update their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Tenant successfully updated"),
        SwaggerResponse(responseCode = "400", description = "Invalid request data"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have management access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
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
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
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

    @PostMapping("/sync")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(
        summary = "Sync tenants with roles", 
        description = "Synchronizes all tenants with available roles. This ensures that all tenants have the correct role subgroups. Only system administrators can perform this operation."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Tenants successfully synchronized with roles"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin")
    )
    fun syncTenantsWithRoles(): ResponseEntity<ApiResponse<Unit>> {
        tenantService.syncTenantsWithRoles()
        return Unit.ok()
    }
    
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get tenant users", 
        description = "Returns all users belonging to a specific tenant. System admins can see users in any tenant, tenant admins can only see users in their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Successfully retrieved tenant users"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
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
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Add user to tenant", 
        description = "Adds a user to a specific tenant. System admins can add users to any tenant, tenant admins can only add users to their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "User successfully added to tenant"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have management access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant or user not found")
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
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Remove user from tenant", 
        description = "Removes a user from a specific tenant. System admins can remove users from any tenant, tenant admins can only remove users from their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "User successfully removed from tenant"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have management access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant or user not found")
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
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get tenant statistics", 
        description = "Returns statistics for a specific tenant including user counts, group counts, and role counts. System admins can see statistics for any tenant, tenant admins can only see statistics for their assigned tenants."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "Successfully retrieved tenant statistics",
            content = [Content(schema = Schema(implementation = TenantStatisticsDto::class))]
        ),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
    )
    fun getTenantStatistics(
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<TenantStatisticsDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(id)
        
        val statistics = tenantService.getTenantStatistics(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = statistics))
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    @Operation(
        summary = "Get all tenants statistics", 
        description = "Returns aggregated statistics for all tenants including total counts. Only system administrators can access this endpoint."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "Successfully retrieved all tenants statistics",
            content = [Content(schema = Schema(implementation = AllTenantsStatisticsDto::class))]
        ),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin")
    )
    fun getAllTenantsStatistics(): ResponseEntity<ApiResponse<AllTenantsStatisticsDto>> {
        val statistics = tenantService.getAllTenantsStatistics()
        return ResponseEntity.ok(ApiResponse(success = true, data = statistics))
    }
}
