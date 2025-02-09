package com.example.keycloak_wrapper.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Keycloak Wrapper API")
                    .description("REST API for managing Keycloak users, groups and roles")
                    .version("1.0")
            )
    }
}
