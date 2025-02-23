package com.example.keycloak_wrapper.dto

data class ClientRoleDto(
    val clientId: String,
    val clientName: String,
    val roles: List<RoleDto>
)

data class RoleAssignmentDto(
    val realmRoles: List<RoleDto> = emptyList(),
    val clientRoles: List<ClientRoleDto> = emptyList()
) {
    val allRoleIds: List<String> get() {
        return realmRoles.map { it.id } + clientRoles.flatMap { it.roles.map { r -> r.id } }
    }
}
