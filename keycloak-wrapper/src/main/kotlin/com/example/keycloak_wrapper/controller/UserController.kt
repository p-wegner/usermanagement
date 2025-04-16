package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_TENANT_ADMIN
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.security.TenantSecurityEvaluator
import com.example.keycloak_wrapper.service.UserService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "OAuth2")
class UserController(
    private val userService: UserService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {

    @GetMapping
    @Operation(
        summary = "Get users", 
        description = "Returns a paginated list of users. System admins can see all users, tenant admins can only see users in their assigned tenants."
    )
    fun getUsers(
        @Parameter(description = "Page number (zero-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "Search string to filter users by username, email, or name")
        @RequestParam(required = false) search: String?,
        @Parameter(description = "Filter users by tenant ID")
        @RequestParam(required = false) tenantId: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        
        // If tenantId is provided, verify access
        if (tenantId != null) {
            tenantSecurityEvaluator.verifyTenantAccess(tenantId)
        }
        
        val searchDto = UserSearchDto(page, size, search, currentUserId, tenantId)
        val (users, total) = userService.getUsers(searchDto)
        
        val response = mapOf(
            "items" to users,
            "total" to total
        )
        return response.ok()
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID", 
        description = "Returns a specific user by ID if the current user has access. System admins can access any user, tenant admins can only access users in their assigned tenants."
    )
    fun getUser(
        @Parameter(description = "ID of the user to retrieve", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        return userService.getUser(id, currentUserId).ok()
    }

    @PostMapping
    @Operation(
        summary = "Create user", 
        description = "Creates a new user. System admins can create users in any tenant, tenant admins can only create users in their assigned tenants."
    )
    fun createUser(
        @Parameter(description = "User creation details", required = true)
        @RequestBody user: UserCreateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        return userService.createUser(user, currentUserId).created()
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user", 
        description = "Updates an existing user. System admins can update any user, tenant admins can only update users in their assigned tenants."
    )
    fun updateUser(
        @Parameter(description = "ID of the user to update", required = true)
        @PathVariable id: String,
        @Parameter(description = "User update details", required = true)
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)

        return userService.updateUser(id, user).ok()
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user", 
        description = "Deletes a user. System admins can delete any user, tenant admins can only delete users in their assigned tenants."
    )
    fun deleteUser(
        @Parameter(description = "ID of the user to delete", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Unit>> {
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        userService.deleteUser(id)
        return Unit.ok()
    }

    @PutMapping("/{id}/roles")
    @Operation(
        summary = "Update user roles", 
        description = "Updates the roles assigned to a user. System admins can assign any roles, tenant admins can only assign roles within their tenant scope."
    )
    fun updateUserRoles(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable id: String,
        @Parameter(description = "Role assignment details", required = true)
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)

        val currentUserId = securityContextHelper.getCurrentUserId()
        userService.updateUserRoles(id, roleAssignment, currentUserId)
        return userService.getUser(id).ok()
    }

    @GetMapping("/{id}/roles")
    @Operation(
        summary = "Get user roles", 
        description = "Returns all roles assigned to a user. System admins can see roles for any user, tenant admins can only see roles for users in their assigned tenants."
    )
    fun getUserRoles(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<RoleAssignmentDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)
        return userService.getUserRoles(id).ok()
    }
}
