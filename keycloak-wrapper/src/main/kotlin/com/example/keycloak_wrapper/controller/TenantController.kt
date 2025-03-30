package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.TenantService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants")
class TenantController(private val tenantService: TenantService) {

    @GetMapping
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_GROUP_VIEWER}')")
    fun getTenants(): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val tenants = tenantService.getTenants()
        return ResponseEntity.ok(ApiResponse(success = true, data = tenants))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_GROUP_VIEWER}')")
    fun getTenant(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        val tenant = tenantService.getTenant(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenant))
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_GROUP_MANAGER}')")
    fun createTenant(@RequestBody tenantCreateDto: TenantCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        val tenant = tenantService.createTenant(tenantCreateDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(success = true, data = tenant))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_GROUP_MANAGER}')")
    fun updateTenant(
        @PathVariable id: String,
        @RequestBody tenantUpdateDto: TenantUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val tenant = tenantService.updateTenant(id, tenantUpdateDto)
        return ResponseEntity.ok(ApiResponse(success = true, data = tenant))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}', '${RoleConstants.ROLE_GROUP_MANAGER}')")
    fun deleteTenant(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        tenantService.deleteTenant(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    fun syncTenantsWithRoles(): ResponseEntity<ApiResponse<Unit>> {
        tenantService.syncTenantsWithRoles()
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
