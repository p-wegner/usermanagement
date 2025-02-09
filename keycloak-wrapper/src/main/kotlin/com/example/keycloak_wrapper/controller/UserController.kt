package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping
    fun getUsers(searchDto: UserSearchDto): ResponseEntity<ApiResponse<List<UserDto>>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true, data = emptyList()))
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PostMapping
    fun createUser(@RequestBody userDto: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        // TODO: Implement with service
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody userDto: UserUpdateDto
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
