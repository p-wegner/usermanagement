package com.example.keycloak_wrapper.dto

data class AuthConfigDto(
    val authServerUrl: String,
    val realm: String,
    val clientId: String,
    val resourceServerUrl: String
)
