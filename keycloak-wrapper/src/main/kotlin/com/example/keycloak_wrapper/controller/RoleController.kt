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
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<RoleDto>>> {
        val searchDto = RoleSearchDto(page, size, search)
        val roles = roleService.getRoles(searchDto)
        return ResponseEntity.ok(ApiResponse(success = true, data = roles))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_VIEWER')")
    @GetMapping("/{name}")
    fun getRole(@PathVariable name: String): ResponseEntity<ApiResponse<RoleDto>> {
        val role = roleService.getRole(name)
        return ResponseEntity.ok(ApiResponse(success = true, data = role))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createRole(@RequestBody role: RoleCreateDto): ResponseEntity<ApiResponse<RoleDto>> {
        val createdRole = roleService.createRole(role)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{name}")
    fun updateRole(
        @PathVariable name: String,
        @RequestBody role: RoleUpdateDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        val updatedRole = roleService.updateRole(name, role)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedRole))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{name}")
    fun deleteRole(@PathVariable name: String): ResponseEntity<ApiResponse<Unit>> {
        roleService.deleteRole(name)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
