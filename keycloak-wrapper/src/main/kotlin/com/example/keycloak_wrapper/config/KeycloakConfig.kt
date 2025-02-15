package com.example.keycloak_wrapper.config

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class KeycloakConfig {

    @Value("\${keycloak.auth-server-url}")
    private lateinit var authServerUrl: String

    @Value("\${keycloak.realm}")
    private lateinit var realm: String

    @Value("\${keycloak.admin.username}")
    private lateinit var adminUsername: String

    @Value("\${keycloak.admin.password}")
    private lateinit var adminPassword: String

    @Value("\${keycloak.admin.client-id}")
    private lateinit var adminClientId: String

    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm("master")  // Always use master realm for admin operations
            .clientId(adminClientId)
            .username(adminUsername)
            .password(adminPassword)
            .grantType("password")
            .resteasyClient(
                org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl()
                    .connectionPoolSize(10)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            )
            .build()
    }
}
