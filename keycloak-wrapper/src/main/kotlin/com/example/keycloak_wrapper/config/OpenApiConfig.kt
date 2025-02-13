package com.example.keycloak_wrapper.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${keycloak.auth-server-url}")
    private val keycloakServerUrl: String,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Keycloak Wrapper API")
                    .description("""
                        REST API for managing Keycloak users, groups and roles.
                        
                        To authenticate:
                        1. Get a token from Keycloak: $keycloakServerUrl/realms/$realm/protocol/openid-connect/token
                        2. Click 'Authorize' and enter the token with format: Bearer <your-token>
                    """.trimIndent())
                    .version("1.0")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description(
                                """
                                Enter your Bearer token in the format: Bearer <token>
                                You can obtain a token from: $keycloakServerUrl/realms/$realm/protocol/openid-connect/token
                                """.trimIndent()
                            )
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
    }
}
