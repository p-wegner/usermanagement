package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_TENANT_ADMIN
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.security.TenantSecurityEvaluator
import com.example.keycloak_wrapper.service.TenantService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenant-admins")
class TenantAdminController(
    private val tenantService: TenantService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {
    /**
     * Assigns a user as an admin for a specific tenant.
     * Only system admins can assign tenant admins.
     */
    @PostMapping
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    fun assignTenantAdmin(
        @RequestBody assignment: TenantAdminAssignmentDto
    ): ResponseEntity<ApiResponse<TenantAdminDto>> {
        val tenantAdmin = tenantService.assignTenantAdmin(assignment)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenantAdmin))
    }
    
    /**
     * Removes a user as an admin for a specific tenant.
     * Only system admins can remove tenant admins.
     */
    @DeleteMapping("/{userId}/tenants/{tenantId}")
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    fun removeTenantAdmin(
        @PathVariable userId: String,
        @PathVariable tenantId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        tenantService.removeTenantAdmin(userId, tenantId)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
    
    /**
     * Gets all tenant admins for a specific tenant.
     * System admins can see admins for any tenant.
     * Tenant admins can only see admins for tenants they manage.
     */
    @GetMapping("/tenants/{tenantId}")
    @PreAuthorize("hasAnyRole('${ROLE_ADMIN}', '${ROLE_TENANT_ADMIN}')")
    fun getTenantAdmins(
        @PathVariable tenantId: String
    ): ResponseEntity<ApiResponse<TenantAdminsResponseDto>> {
        // Verify tenant access
        tenantSecurityEvaluator.verifyTenantAccess(tenantId)
        
        val admins = tenantService.getTenantAdmins(tenantId)
        return ResponseEntity.ok(ApiResponse(success = true, data = admins))
    }
    
    /**
     * Gets all tenants for which the current user is an admin.
     * For system admins, this returns all tenants.
     */
    @GetMapping("/my-tenants")
    @PreAuthorize("hasAnyRole('${ROLE_ADMIN}', '${ROLE_TENANT_ADMIN}')")
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
    
    /**
     * Gets all tenants for which a specific user is an admin.
     * Only system admins can see tenants for other users.
     */
    @GetMapping("/users/{userId}/tenants")
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    fun getUserTenants(
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<AdminTenantsResponseDto>> {
        val tenants = tenantService.getUserTenants(userId)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }
}
