package com.example.keycloak_wrapper.exception

import com.example.keycloak_wrapper.dto.common.ApiResponse
import com.example.keycloak_wrapper.dto.common.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(KeycloakWrapperException::class)
    fun handleKeycloakWrapperException(ex: KeycloakWrapperException): ResponseEntity<ApiResponse<Nothing>> {
        val errorResponse = ErrorResponse(
            code = ex.errorCode,
            message = ex.message,
            details = ex.details
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(success = false, error = errorResponse))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val errorResponse = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            details = mapOf("error" to (ex.message ?: "Unknown error"))
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(success = false, error = errorResponse))
    }
}
