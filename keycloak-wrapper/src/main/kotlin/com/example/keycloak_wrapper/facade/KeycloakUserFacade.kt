package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.dto.ClientRoleDto
import com.example.keycloak_wrapper.dto.RoleAssignmentDto
import com.example.keycloak_wrapper.dto.RoleDto
import com.example.keycloak_wrapper.exception.KeycloakException
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeycloakUserFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}") private val realm: String
) {
    fun getUsers(search: String?, firstResult: Int, maxResults: Int): Pair<List<UserRepresentation>, Int> {
        return try {
            val users = keycloak.realm(realm).users()
            val results = users.search(search, firstResult, maxResults, true)
            val total = users.count(search)
            Pair(results, total)
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch users", e)
        }
    }

    fun getUser(id: String): UserRepresentation {
        return try {
            keycloak.realm(realm).users().get(id).toRepresentation()
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch user with id: $id", e)
        }
    }

    fun createUser(user: UserRepresentation): UserRepresentation {
        try {
            val response = keycloak.realm(realm).users().create(user)
            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create user: ${response.status} - $errorBody")
            }

            val locationHeader = response.location.toString()
            val userId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getUser(userId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create user", e)
        }
    }

    fun updateUser(id: String, user: UserRepresentation): UserRepresentation {
        try {
            val userResource = keycloak.realm(realm).users().get(id)
            userResource.update(user)
            return getUser(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update user with id: $id", e)
        }
    }

    fun updateUserRoles(id: String, roleAssignment: RoleAssignmentDto) {
        try {
            val userResource = keycloak.realm(realm).users().get(id)

            updateRealmRoles(roleAssignment, userResource)
            updateClientRoles(roleAssignment, userResource)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update roles for user with id: $id", e)
        }
    }

    private fun updateClientRoles(
        roleAssignment: RoleAssignmentDto,
        userResource: UserResource
    ) {
        // Get all clients to handle removal of roles from clients not in the assignment
        val allClients = keycloak.realm(realm).clients().findAll()
        val clientsInAssignment = roleAssignment.clientRoles.map { it.clientId }.toSet()
        
        // Remove roles from clients not in the assignment
        allClients.forEach { client ->
            if (!clientsInAssignment.contains(client.id)) {
                val currentRoles = userResource.roles().clientLevel(client.id).listAll()
                if (currentRoles.isNotEmpty()) {
                    userResource.roles().clientLevel(client.id).remove(currentRoles)
                }
            }
        }
        
        // Update roles for clients in the assignment
        roleAssignment.clientRoles.forEach { clientRoleDto ->
            val clientId = clientRoleDto.clientId
            val client = keycloak.realm(realm).clients().get(clientId)
            val clientRoleReps = clientRoleDto.roles.map { role ->
                client.roles().get(role.name).toRepresentation()
            }
            userResource.roles().clientLevel(clientId).remove(userResource.roles().clientLevel(clientId).listAll())
            if (clientRoleReps.isNotEmpty()) {
                userResource.roles().clientLevel(clientId).add(clientRoleReps)
            }
        }
    }

    private fun updateRealmRoles(
        roleAssignment: RoleAssignmentDto,
        userResource: UserResource
    ) {
        val realmRoleReps = roleAssignment.realmRoles.map {
            keycloak.realm(realm).roles().get(it.id).toRepresentation()
        }
        userResource.roles().realmLevel().remove(userResource.roles().realmLevel().listAll())
        if (realmRoleReps.isNotEmpty()) {
            userResource.roles().realmLevel().add(realmRoleReps)
        }
    }

    fun getUserRoles(id: String): RoleAssignmentDto {
        try {
            val userResource = keycloak.realm(realm).users().get(id)
            val realmRoles = getRealmRoles(userResource)
            val clientRoles = getClientRoles(userResource)
            return RoleAssignmentDto(realmRoles, clientRoles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to get roles for user with id: $id", e)
        }
    }

    private fun getRealmRoles(userResource: UserResource) =
        userResource.roles().realmLevel().listAll()
            .map { role ->
                RoleDto(
                    id = role.id,
                    name = role.name,
                    description = role.description,
                    composite = role.isComposite,
                    clientRole = role.clientRole
                )
            }

    private fun getClientRoles(userResource: UserResource) =
        keycloak.realm(realm).clients().findAll().mapNotNull { client ->
            val roles = userResource.roles().clientLevel(client.id).listAll()
            if (roles.isNotEmpty()) {
                ClientRoleDto(
                    clientId = client.id,
                    clientName = client.clientId,
                    roles = roles.map { role ->
                        RoleDto(
                            id = role.id,
                            name = role.name,
                            description = role.description,
                            composite = role.isComposite,
                            clientRole = role.clientRole
                        )
                    }
                )
            } else {
                null
            }
        }

    fun updatePassword(id: String, password: String) {
        try {
            val credentials = org.keycloak.representations.idm.CredentialRepresentation().apply {
                type = org.keycloak.representations.idm.CredentialRepresentation.PASSWORD
                value = password
                isTemporary = false
            }
            keycloak.realm(realm).users().get(id).resetPassword(credentials)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update password for user with id: $id", e)
        }
    }

    fun deleteUser(id: String) {
        try {
            keycloak.realm(realm).users().get(id).remove()
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete user with id: $id", e)
        }
    }

    fun countUsers(): Int {
        return try {
            keycloak.realm(realm).users().count()
        } catch (e: Exception) {
            throw KeycloakException("Failed to count users", e)
        }
    }

    fun getUserGroups(userId: String): List<GroupRepresentation> {
        return try {
            keycloak.realm(realm).users().get(userId).groups()
        } catch (e: Exception) {
            throw KeycloakException("Failed to get groups for user with id: $userId", e)
        }
    }

    fun addUserToGroup(userId: String, groupId: String) {
        try {
            keycloak.realm(realm).users().get(userId).joinGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to add user with id: $userId to group with id: $groupId", e)
        }
    }

    fun removeUserFromGroup(userId: String, groupId: String) {
        try {
            keycloak.realm(realm).users().get(userId).leaveGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to remove user with id: $userId from group with id: $groupId", e)
        }
    }
}
