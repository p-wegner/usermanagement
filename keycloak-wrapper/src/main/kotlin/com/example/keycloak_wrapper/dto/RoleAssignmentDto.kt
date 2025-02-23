package com.example.keycloak_wrapper.dto

data class RoleAssignmentDto(
    val realmRoleIds: List<String> = emptyList(),
    val clientRoleIds: Map<String, List<String>> = emptyMap()
) {
    val allRoleIds: List<String> get() {
        return realmRoleIds + clientRoleIds.values.flatten()
    }
}
