package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.RoleService
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/roles")
class RoleController(
    private val roleService: RoleService
) {
    @RolesAllowed(ROLE_ADMIN)
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
        return roleService.getRoles(searchDto).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/{id}")
    fun getRole(@PathVariable id: String): ResponseEntity<ApiResponse<RoleDto>> {
        return roleService.getRole(id).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PostMapping
    fun createRole(@RequestBody role: RoleCreateDto): ResponseEntity<ApiResponse<RoleDto>> {
        return roleService.createRole(role).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PutMapping("/{id}")
    fun updateRole(
        @PathVariable id: String,
        @RequestBody role: RoleUpdateDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        return roleService.updateRole(id, role).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        roleService.deleteRole(id)
        return Unit.ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PostMapping("/{roleId}/composites")
    fun addCompositeRoles(
        @PathVariable roleId: String,
        @RequestBody compositeRoles: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        return roleService.addCompositeRoles(roleId, compositeRoles.allRoleIds).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @DeleteMapping("/{roleId}/composites")
    fun removeCompositeRoles(
        @PathVariable roleId: String,
        @RequestBody compositeRoles: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<RoleDto>> {
        return roleService.removeCompositeRoles(roleId, compositeRoles.allRoleIds).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/{roleId}/composites")
    fun getCompositeRoles(@PathVariable roleId: String): ResponseEntity<ApiResponse<List<RoleDto>>> {
        return roleService.getCompositeRoles(roleId).ok()
    }
    
    // Group role management endpoints
    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/groups/{groupId}")
    fun getGroupRoles(@PathVariable groupId: String): ResponseEntity<ApiResponse<List<RoleDto>>> {
        return roleService.getGroupRoles(groupId).ok()
    }
    
    @RolesAllowed(ROLE_ADMIN)
    @PostMapping("/groups/{groupId}")
    fun addRolesToGroup(
        @PathVariable groupId: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<Unit>> {
        roleService.addRolesToGroup(groupId, roleAssignment.allRoleIds)
        return Unit.ok()
    }
    
    @RolesAllowed(ROLE_ADMIN)
    @DeleteMapping("/groups/{groupId}")
    fun removeRolesFromGroup(
        @PathVariable groupId: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<Unit>> {
        roleService.removeRolesFromGroup(groupId, roleAssignment.allRoleIds)
        return Unit.ok()
    }
    
    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/groups/{groupId}/clients/{clientId}")
    fun getGroupClientRoles(
        @PathVariable groupId: String,
        @PathVariable clientId: String
    ): ResponseEntity<ApiResponse<List<RoleDto>>> {
        return roleService.getGroupClientRoles(groupId, clientId).ok()
    }
    
    @RolesAllowed(ROLE_ADMIN)
    @PostMapping("/groups/{groupId}/clients/{clientId}")
    fun addClientRolesToGroup(
        @PathVariable groupId: String,
        @PathVariable clientId: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<Unit>> {
        roleService.addClientRolesToGroup(groupId, clientId, roleAssignment.allRoleIds)
        return Unit.ok()
    }
    
    @RolesAllowed(ROLE_ADMIN)
    @DeleteMapping("/groups/{groupId}/clients/{clientId}")
    fun removeClientRolesFromGroup(
        @PathVariable groupId: String,
        @PathVariable clientId: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<Unit>> {
        roleService.removeClientRolesToGroup(groupId, clientId, roleAssignment.allRoleIds)
        return Unit.ok()
    }
}
