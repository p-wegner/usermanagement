package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.RoleService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("isAuthenticated()")
class RoleController(
    private val roleService: RoleService
) {
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_VIEWER')")
    @GetMapping
    fun getRoles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) clientId: String?,
        @RequestParam(required = false) includeRealmRoles: Boolean = true,
        @RequestParam(required = false) includeClientRoles: Boolean = true
    ): ResponseEntity<ApiResponse<List<RoleDto>>> {
        val searchDto = RoleSearchDto(
            page = page,
            size = size,
            search = search,
            clientId = clientId,
            includeRealmRoles = includeRealmRoles,
            includeClientRoles = includeClientRoles
        )
        val roles = roleService.getRoles(searchDto)
        return ResponseEntity.ok(ApiResponse(success = true, data = roles))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_VIEWER')")
    @GetMapping("/{id}")
    fun getRole(@PathVariable id: String): ResponseEntity<ApiResponse<RoleDto>> {
        val role = roleService.getRole(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = role))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createRole(@RequestBody role: RoleCreateDto): ResponseEntity<ApiResponse<RoleDto>> {
        val createdRole = roleService.createRole(role)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateRole(
        @PathVariable id: String,
        @RequestBody role: RoleUpdateDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        val updatedRole = roleService.updateRole(id, role)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        roleService.deleteRole(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{roleId}/composites")
    fun addCompositeRoles(
        @PathVariable roleId: String,
        @RequestBody compositeRoles: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        val updatedRole = roleService.addCompositeRoles(roleId, compositeRoles.allRoleIds)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{roleId}/composites")
    fun removeCompositeRoles(
        @PathVariable roleId: String,
        @RequestBody compositeRoles: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        val updatedRole = roleService.removeCompositeRoles(roleId, compositeRoles.allRoleIds)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{roleId}/composites")
    fun getCompositeRoles(@PathVariable roleId: String): ResponseEntity<ApiResponse<List<RoleDto>>> {
        val compositeRoles = roleService.getCompositeRoles(roleId)
        return ResponseEntity.ok(ApiResponse(success = true, data = compositeRoles))
    }
}
