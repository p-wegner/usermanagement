package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService
) {
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

    @GetMapping("/{id}")
    fun getGroup(@PathVariable id: String): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.getGroup(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = group))
    }

    @PostMapping
    fun createGroup(@RequestBody group: GroupCreateDto): ResponseEntity<ApiResponse<GroupDto>> {
        val createdGroup = groupService.createGroup(group)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdGroup))
    }

    @PutMapping("/{id}")
    fun updateGroup(
        @PathVariable id: String,
        @RequestBody group: GroupUpdateDto
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val updatedGroup = groupService.updateGroup(id, group)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedGroup))
    }

    @DeleteMapping("/{id}")
    fun deleteGroup(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        groupService.deleteGroup(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
