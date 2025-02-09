package com.example.keycloak_wrapper.dto

data class ErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, Any>? = null
)
