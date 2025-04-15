package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.ROLE_ADMIN
import com.example.keycloak_wrapper.config.RoleConstants.ROLE_TENANT_ADMIN
import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.security.TenantSecurityEvaluator
import com.example.keycloak_wrapper.service.UserService
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.time.measureTime

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val userService: UserService,
    private val securityContextHelper: SecurityContextHelper,
    private val tenantSecurityEvaluator: TenantSecurityEvaluator
) {

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
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

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        return userService.getUser(id, currentUserId).ok()
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PostMapping
    fun createUser(@RequestBody user: UserCreateDto): ResponseEntity<ApiResponse<UserDto>> {
        val currentUserId = securityContextHelper.getCurrentUserId()
        return userService.createUser(user, currentUserId).created()
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody user: UserUpdateDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)

        return userService.updateUser(id, user).ok()
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        tenantSecurityEvaluator.verifyUserAccess(id)
        
        userService.deleteUser(id)
        return Unit.ok()
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @PutMapping("/{id}/roles")
    fun updateUserRoles(
        @PathVariable id: String,
        @RequestBody roleAssignment: RoleAssignmentDto
    ): ResponseEntity<ApiResponse<UserDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)

        val currentUserId = securityContextHelper.getCurrentUserId()
        userService.updateUserRoles(id, roleAssignment, currentUserId)
        return userService.getUser(id).ok()
    }

    @RolesAllowed(ROLE_ADMIN, ROLE_TENANT_ADMIN)
    @GetMapping("/{id}/roles")
    fun getUserRoles(@PathVariable id: String): ResponseEntity<ApiResponse<RoleAssignmentDto>> {
        tenantSecurityEvaluator.verifyUserAccess(id)
        return userService.getUserRoles(id).ok()
    }
}
