package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.TenantService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants")
class TenantController(private val tenantService: TenantService) {


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}')")
    fun getTenant(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        return tenantService.getTenant(id).ok()

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}')")
    fun createTenant(@RequestBody tenantCreateDto: TenantCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        return tenantService.createTenant(tenantCreateDto).created()
    }

    // TODO 31.03.2025 pwegner: display name uniqueness check needed?
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}')")
    fun updateTenant(
        @PathVariable id: String,
        @RequestBody tenantUpdateDto: TenantUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        return tenantService.updateTenant(id, tenantUpdateDto).ok()
    }

    // TODO 31.03.2025 pwegner: properly delete subgroups
    // TODO 31.03.2025 pwegner: delete users?
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('${RoleConstants.ROLE_ADMIN}')")
    fun deleteTenant(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        tenantService.deleteTenant(id)
        return Unit.ok()
    }

    // TODO 31.03.2025 pwegner: when will this be triggered? cron?
    @PostMapping("/sync")
    @PreAuthorize("hasRole('${RoleConstants.ROLE_ADMIN}')")
    fun syncTenantsWithRoles(): ResponseEntity<ApiResponse<Unit>> {
        tenantService.syncTenantsWithRoles()
        return Unit.ok()
    }
}
