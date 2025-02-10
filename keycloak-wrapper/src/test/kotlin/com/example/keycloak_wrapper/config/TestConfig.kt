package com.example.keycloak_wrapper.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
@ActiveProfiles("test")
class TestConfig {
    
    @Bean
    fun keycloakContainer(): GenericContainer<*> {
        return GenericContainer(DockerImageName.parse("keycloak/keycloak:23.0.7"))
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withEnv("KC_DB", "dev-file")
            .withEnv("KC_HEALTH_ENABLED", "true")
            .withCommand("start-dev")
    }
}
