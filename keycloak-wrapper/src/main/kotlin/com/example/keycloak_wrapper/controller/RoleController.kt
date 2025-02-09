package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
class RoleController(
    private val roleService: RoleService
) {
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

    @GetMapping("/{name}")
    fun getRole(@PathVariable name: String): ResponseEntity<ApiResponse<RoleDto>> {
        val role = roleService.getRole(name)
        return ResponseEntity.ok(ApiResponse(success = true, data = role))
    }

    @PostMapping
    fun createRole(@RequestBody role: RoleCreateDto): ResponseEntity<ApiResponse<RoleDto>> {
        val createdRole = roleService.createRole(role)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdRole))
    }

    @PutMapping("/{name}")
    fun updateRole(
        @PathVariable name: String,
        @RequestBody role: RoleUpdateDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        val updatedRole = roleService.updateRole(name, role)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedRole))
    }

    @DeleteMapping("/{name}")
    fun deleteRole(@PathVariable name: String): ResponseEntity<ApiResponse<Unit>> {
        roleService.deleteRole(name)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
