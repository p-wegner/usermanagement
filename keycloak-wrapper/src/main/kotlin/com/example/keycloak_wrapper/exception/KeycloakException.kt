package com.example.keycloak_wrapper.exception

class KeycloakException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
