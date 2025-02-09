package com.example.keycloak_wrapper.dto

data class GroupDto(
    val id: String? = null,
    val name: String,
    val path: String? = null,
    val subGroups: List<GroupDto> = emptyList()
)

data class GroupCreateDto(
    val name: String,
    val parentGroupId: String? = null
)

data class GroupUpdateDto(
    val name: String? = null
)

data class GroupSearchDto(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null
)
