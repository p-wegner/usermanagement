package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_TENANT_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_USER_VIEWER
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.service.UserService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val userService: UserService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {

    @Operation(
        summary = "Get users",
        description = "Retrieve a paginated list of users with optional search"
    )
    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        val searchDto = UserSearchDto(page, size, search, currentUserId)
        val (users, total) = userService.getUsers(searchDto)
        val response = mapOf(
            "items" to users,
            "total" to total
        )
        return ResponseEntity.ok(ApiResponse(success = true, data = response))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        val user = userService.getUser(id, currentUserId)
        return ResponseEntity.ok(ApiResponse(success = true, data = user))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PostMapping
    fun createUser(@RequestBody user: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        val createdUser = userService.createUser(user, currentUserId)
        return ResponseEntity.ok(ApiResponse(success = true, data = createdUser))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        // Verify access to the user
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        val updatedUser = userService.updateUser(id, user)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedUser))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        // Verify access to the user
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        userService.deleteUser(id)
        return ResponseEntity.ok(ApiResponse(success = true))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PutMapping("/{id}/roles")
    fun updateUserRoles(
        @PathVariable id: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        // Verify access to the user
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        val currentUserId = securityContextHelper.getCurrentUserId()
        userService.updateUserRoles(id, roleAssignment, currentUserId)
        val updatedUser = userService.getUser(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = updatedUser))
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping("/{id}/roles")
    fun getUserRoles(@PathVariable id: String): ResponseEntity<ApiResponse<RoleAssignmentDto>> {
        // Verify access to the user
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        val roles = userService.getUserRoles(id)
        return ResponseEntity.ok(ApiResponse(success = true, data = roles))
    }
}
