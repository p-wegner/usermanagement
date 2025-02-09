package com.example.keycloak_wrapper.exception

class KeycloakWrapperException(
    val errorCode: String,
    override val message: String,
    val details: Map<String, Any>? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
