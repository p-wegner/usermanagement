package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.config.RoleConstants.AUTHENTICATED
import com.example.keycloak_wrapper.dto.AuthConfigDto
import com.example.keycloak_wrapper.dto.ApiResponse
import jakarta.annotation.security.RolesAllowed
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@RolesAllowed(*AUTHENTICATED)
class AuthController(
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.resource}") private val clientId: String,
    @Value("\${server.servlet.context-path:}") private val contextPath: String
) {
    @GetMapping("/config")
    fun getAuthConfig(): ResponseEntity<ApiResponse<AuthConfigDto>> {
        val config = AuthConfigDto(
            authServerUrl = authServerUrl,
            realm = realm,
            clientId = clientId,
            resourceServerUrl = "http://localhost:8080$contextPath"
        )
        return ResponseEntity.ok(ApiResponse(success = true, data = config))
    }
}
