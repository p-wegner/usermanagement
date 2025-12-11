package com.example.keycloak_wrapper

import com.example.keycloak_wrapper.util.SecurityContextHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SecurityContextHelperTest {
    private val helper = SecurityContextHelper()

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getCurrentUserId returns subject from jwt`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user123")
            .build()
        val auth = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = auth

        assertEquals("user123", helper.getCurrentUserId())
    }

    @Test
    fun `hasRole returns true when authority present`() {
        val auth = UsernamePasswordAuthenticationToken(
            "user", "pass",
            listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        )
        SecurityContextHolder.getContext().authentication = auth

        assertEquals(true, helper.hasRole("ADMIN"))
    }
}
