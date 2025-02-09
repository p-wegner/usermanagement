package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "Get users",
        description = "Retrieve a paginated list of users with optional search"
    )
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

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUser(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = user))
    }

    @PostMapping
    fun createUser(@RequestBody user: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val createdUser = userService.createUser(user)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdUser))
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val updatedUser = userService.updateUser(id, user)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedUser))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
