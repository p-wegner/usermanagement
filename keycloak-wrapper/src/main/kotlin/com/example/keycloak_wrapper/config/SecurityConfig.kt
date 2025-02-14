package com.example.keycloak_wrapper.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
class SecurityConfig(
    @Value("\${app.cors.allowed-origins:http://localhost:4200}")
    private val allowedOrigins: List<String>,
    @Value("\${keycloak.auth-server-url}")
    private val keycloakServerUrl: String,
    @Value("\${keycloak.realm}")
    private val realm: String
) {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwkSetUri = "$keycloakServerUrl/realms/$realm/protocol/openid-connect/certs"
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
    }
    @Bean
    fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter(KeycloakRoleConverter())
        return jwtConverter
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Allow both frontend and Swagger UI origins
            allowedOrigins = listOf("http://localhost:8081", "http://localhost:8080")
            allowedMethods = listOf(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
            )
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
            )
            exposedHeaders = listOf(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
            )
            allowCredentials = true
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }
            .oauth2ResourceServer { 
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .authorizeHttpRequests {
                // Public endpoints
                it.requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()

                // Secured actuator endpoints
                it.requestMatchers("/actuator/**").hasRole("ADMIN")


                // Require authentication for all other endpoints
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, exception ->
                    response.sendError(401, "Unauthorized: ${exception.message}")
                }
                it.accessDeniedHandler { request, response, exception ->
                    response.sendError(403, "Access Denied: ${exception.message}")
                }
            }

        return http.build()
    }
}

class KeycloakRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        
        // Extract realm roles
        @Suppress("UNCHECKED_CAST")
        val realmAccess = jwt.claims["realm_access"] as? Map<String, Any>
        val realmRoles = realmAccess?.get("roles") as? List<String> ?: emptyList()
        
        // Add realm roles with ROLE_ prefix
        realmRoles.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
        }
        
        // Extract resource roles
        @Suppress("UNCHECKED_CAST")
        val resourceAccess = jwt.claims["resource_access"] as? Map<String, Any>
        resourceAccess?.forEach { (resource, access) ->
            val resourceRoles = (access as? Map<String, Any>)?.get("roles") as? List<String> ?: emptyList()
            resourceRoles.forEach { role ->
                authorities.add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
            }
        }
        
        return authorities
    }
}
