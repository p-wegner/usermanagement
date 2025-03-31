package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.GroupService
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
@RolesAllowed("AUTHENTICATED")
class GroupController(
    private val groupService: GroupService
) {

    @RolesAllowed(ROLE_ADMIN)
    @GetMapping
    fun getGroups(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val searchDto = GroupSearchDto(page, size, search)
        return groupService.getGroups(searchDto).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/{id}")
    fun getGroup(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        return groupService.getGroup(id).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PostMapping
    fun createGroup(@RequestBody group: GroupCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        return groupService.createGroup(group).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PutMapping("/{id}")
    fun updateGroup(
        @PathVariable id: String,
        @RequestBody group: GroupUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        return groupService.updateGroup(id, group).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @DeleteMapping("/{id}")
    fun deleteGroup(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        groupService.deleteGroup(id)
        return Unit.ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @PutMapping("/{id}/roles")
    fun updateGroupRoles(
        @PathVariable id: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        return groupService.updateGroupRoles(id, roleAssignment).ok()
    }

    @RolesAllowed(ROLE_ADMIN)
    @GetMapping("/{id}/roles")
    fun getGroupRoles(@PathVariable id: String): ResponseEntity<ApiResponse<RoleAssignmentDto>> {
        return groupService.getGroupRoles(id).ok()
    }

}

fun <T> T.ok(): ResponseEntity<ApiResponse<T>> = ResponseEntity.ok(ApiResponse(success = true, data = this))
fun <T> T.created(): ResponseEntity<ApiResponse<T>> = ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(success = true, data = this))
