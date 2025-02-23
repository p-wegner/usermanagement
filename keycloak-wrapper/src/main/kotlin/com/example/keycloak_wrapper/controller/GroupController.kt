package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.GroupService
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("isAuthenticated()")
class GroupController(
    private val groupService: GroupService
) {
    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER', 'GROUP_VIEWER')")
    @GetMapping
    fun getGroups(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val searchDto = GroupSearchDto(page, size, search)
        val groups = groupService.getGroups(searchDto)
        return ResponseEntity.ok(ApiResponse(success = true, data = groups))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER', 'GROUP_VIEWER')")
    @GetMapping("/{id}")
    fun getGroup(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.getGroup(id)
        return group.responseEntity()
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER')")
    @PostMapping
    fun createGroup(@RequestBody group: GroupCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        val createdGroup = groupService.createGroup(group)
        return createdGroup.responseEntity()
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER')")
    @PutMapping("/{id}")
    fun updateGroup(
        @PathVariable id: String,
        @RequestBody group: GroupUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val updatedGroup = groupService.updateGroup(id, group)
        return updatedGroup.responseEntity()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteGroup(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        groupService.deleteGroup(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER')")
    @PutMapping("/{id}/roles")
    fun updateGroupRoles(
        @PathVariable id: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val updatedGroup = groupService.updateGroupRoles(id, roleAssignment)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedGroup))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GROUP_MANAGER', 'GROUP_VIEWER')")
    @GetMapping("/{id}/roles")
    fun getGroupRoles(@PathVariable id: String): ResponseEntity<ApiResponse<RoleAssignmentDto>> {
        val roles = groupService.getGroupRoles(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = roles))
    }

    private fun GroupDto.responseEntity() =
        ResponseEntity.ok(ApiResponse(success = true, data = this))
}
