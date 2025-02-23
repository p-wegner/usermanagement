package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@PreAuthorize("isAuthenticated()")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "Get users",
        description = "Retrieve a paginated list of users with optional search"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER', 'USER_VIEWER')")
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val searchDto = UserSearchDto(page, size, search)
        val (users, total) = userService.getUsers(searchDto)
        val response = mapOf(
            "items" to users,
            "total" to total
        )
        return ResponseEntity.ok(ApiResponse(success = true, data = response))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER', 'USER_VIEWER')")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUser(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = user))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @PostMapping
    fun createUser(@RequestBody user: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val createdUser = userService.createUser(user)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdUser))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val updatedUser = userService.updateUser(id, user)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedUser))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER')")
    @PutMapping("/{id}/roles")
    fun updateUserRoles(
        @PathVariable id: String,
        @RequestBody roles: List<String>
    ): ResponseEntity<ApiResponse<UserDto>> {
        userService.updateUserRoles(id, roles)
        val updatedUser = userService.getUser(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedUser))
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER_MANAGER', 'USER_VIEWER')")
    @GetMapping("/{id}/roles")
    fun getUserRoles(@PathVariable id: String): ResponseEntity<ApiResponse<List<String>>> {
        val user = userService.getUser(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = user.realmRoles))
    }
}
