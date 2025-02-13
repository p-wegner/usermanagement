package com.example.keycloak_wrapper.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.Scopes
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
        val securitySchemeName = "OAuth2"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Keycloak Wrapper API")
                    .description("""
                        REST API for managing Keycloak users, groups and roles.
                        
                        Default credentials:
                        - Username: admin
                        - Password: admin
                        
                        Click 'Authorize' and use these credentials to login.
                    """.trimIndent())
                    .version("1.0")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                io.swagger.v3.oas.models.security.OAuthFlows()
                                    .password(
                                        io.swagger.v3.oas.models.security.OAuthFlow()
                                            .tokenUrl("$keycloakServerUrl/realms/$realm/protocol/openid-connect/token")
                                            .scopes(Scopes())
                                    )
                            )
                            .description("OAuth2 authentication with Keycloak")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
    }
}
