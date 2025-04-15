package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_TENANT_ADMIN
import com.example.keycloak_wrapper.dto.*
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
@RequestMapping("/api/tenant-admins")
@Tag(name = "Tenant Admins", description = "Tenant administrator management endpoints")
@SecurityRequirement(name = "OAuth2")
class TenantAdminController(
    private val tenantService: TenantService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {
    @PostMapping
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @Operation(
        summary = "Assign tenant admin", 
        description = "Assigns a user as an administrator for a specific tenant. Only system administrators can assign tenant admins."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "User successfully assigned as tenant admin",
            content = [Content(schema = Schema(implementation = TenantAdminDto::class))]
        ),
        SwaggerResponse(responseCode = "400", description = "Invalid request data"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin"),
        SwaggerResponse(responseCode = "404", description = "User or tenant not found")
    )
    fun assignTenantAdmin(
        @Parameter(description = "Tenant admin assignment details", required = true)
        @RequestBody assignment: TenantAdminAssignmentDto
    ): ResponseEntity<ApiResponse<TenantAdminDto>> {
        val tenantAdmin = tenantService.assignTenantAdmin(assignment)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenantAdmin))
    }
    
    @DeleteMapping("/{userId}/tenants/{tenantId}")
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @Operation(
        summary = "Remove tenant admin", 
        description = "Removes a user as an administrator for a specific tenant. Only system administrators can remove tenant admins."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "User successfully removed as tenant admin"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin"),
        SwaggerResponse(responseCode = "404", description = "User or tenant not found")
    )
    fun removeTenantAdmin(
        @Parameter(description = "ID of the user to remove as admin", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable tenantId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        tenantService.removeTenantAdmin(userId, tenantId)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
    
    @GetMapping("/tenants/{tenantId}")
    @PreAuthorize("hasAnyRole('${ROLE_ADMIN}', '${ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get tenant admins", 
        description = "Returns all administrators for a specific tenant. System admins can see admins for any tenant, tenant admins can only see admins for tenants they manage."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "Successfully retrieved tenant admins",
            content = [Content(schema = Schema(implementation = TenantAdminsResponseDto::class))]
        ),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have access to this tenant"),
        SwaggerResponse(responseCode = "404", description = "Tenant not found")
    )
    fun getTenantAdmins(
        @Parameter(description = "ID of the tenant", required = true)
        @PathVariable tenantId: String
    ): ResponseEntity<ApiResponse<TenantAdminsResponseDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(tenantId)
        
        val admins = tenantService.getTenantAdmins(tenantId)
        return ResponseEntity.ok(ApiResponse(success = true, data = admins))
    }
    
    @GetMapping("/my-tenants")
    @PreAuthorize("hasAnyRole('${ROLE_ADMIN}', '${ROLE_TENANT_ADMIN}')")
    @Operation(
        summary = "Get my administered tenants", 
        description = "Returns all tenants for which the current user is an administrator. For system admins, this returns all tenants."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "Successfully retrieved administered tenants",
            content = [Content(schema = Schema(implementation = TenantDto::class))]
        ),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user doesn't have required roles")
    )
    fun getMyTenants(): ResponseEntity<ApiResponse<List<TenantDto>>> {
        val userId = securityContextHelper.getCurrentUserId()
            ?: return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
        
        val tenants = if (securityContextHelper.hasRole(ROLE_ADMIN)) {
            // System admins can see all tenants
            tenantService.getTenants().map { 
                TenantDto(
                    id = it.id!!,
                    name = it.name,
                    displayName = it.tenantName ?: it.name,
                    permissionGroups = it.subGroups
                )
            }
        } else {
            // Tenant admins can only see their assigned tenants
            tenantService.getUserTenants(userId).tenants
        }
        
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }
    
    @GetMapping("/users/{userId}/tenants")
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @Operation(
        summary = "Get user's administered tenants", 
        description = "Returns all tenants for which a specific user is an administrator. Only system administrators can see tenants for other users."
    )
    @ApiResponses(
        SwaggerResponse(
            responseCode = "200", 
            description = "Successfully retrieved user's administered tenants",
            content = [Content(schema = Schema(implementation = AdminTenantsResponseDto::class))]
        ),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin"),
        SwaggerResponse(responseCode = "404", description = "User not found")
    )
    fun getUserTenants(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<AdminTenantsResponseDto>> {
        val tenants = tenantService.getUserTenants(userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }
}
