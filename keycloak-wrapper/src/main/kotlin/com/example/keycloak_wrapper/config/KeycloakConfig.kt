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

    @Value("\${keycloak.service-client.id}")
    private lateinit var serviceClientId: String

    @Value("\${keycloak.service-client.secret}")
    private lateinit var serviceClientSecret: String

    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(realm)
            .clientId(serviceClientId)
            .clientSecret(serviceClientSecret)
            .username("admin")
            .password("admin")
            .grantType("password")
//            .grantType("client_credentials")
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
