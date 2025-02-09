package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<UserDto>>> {
        val searchDto = UserSearchDto(page, size, search)
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PostMapping
    fun createUser(@RequestBody user: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }
}
