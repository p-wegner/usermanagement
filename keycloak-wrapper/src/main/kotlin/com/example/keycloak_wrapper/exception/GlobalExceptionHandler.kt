package com.example.keycloak_wrapper.exception

import com.example.keycloak_wrapper.dto.ApiResponse
import com.example.keycloak_wrapper.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(KeycloakException::class)
    fun handleKeycloakException(ex: KeycloakException): ResponseEntity<ApiResponse<Nothing>> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "An error occurred with Keycloak",
            code = "KEYCLOAK_ERROR",
            details = mapOf("cause" to (ex.cause?.message ?: ""))
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, error = errorResponse.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val errorResponse = ErrorResponse(
            message = "An unexpected error occurred",
            code = "INTERNAL_SERVER_ERROR",
            details = mapOf("exception" to (ex.message ?: ""))
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, error = errorResponse.message))
    }
}
