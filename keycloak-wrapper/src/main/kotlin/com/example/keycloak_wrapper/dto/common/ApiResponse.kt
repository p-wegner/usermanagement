package com.example.keycloak_wrapper.dto.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

data class PagedResponse<T>(
    val items: List<T>,
    val totalItems: Long,
    val pageSize: Int,
    val currentPage: Int
)
