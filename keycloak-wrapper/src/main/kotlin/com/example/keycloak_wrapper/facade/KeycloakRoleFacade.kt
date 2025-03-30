package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.exception.KeycloakException
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.RoleRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeycloakRoleFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    fun getRoles(search: String?, first: Int, max: Int): List<RoleRepresentation> {
        return try {
            val roles = keycloak.realm(realm).roles()
            if (search.isNullOrBlank()) {
                roles.list(first, max)
            } else {
                roles.list(search, first, max)
            }
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch roles", e)
        }
    }
    
    fun getRoles(name: String, first: Int, max: Int): List<RoleRepresentation> {
        return try {
            val roles = keycloak.realm(realm).roles()
            roles.list(name, first, max)
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch roles with name: $name", e)
        }
    }

    fun getRole(id: String): RoleRepresentation {
        return try {
            keycloak.realm(realm).rolesById().getRole(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch role with name: $id", e)
        }
    }

    fun createRole(role: RoleRepresentation): RoleRepresentation {
        try {
            keycloak.realm(realm).roles().create(role)
            // TODO: keycloak client lib doesn't provide a response
//            if (response.status != Response.Status.CREATED.statusCode) {
//                val errorBody = response.readEntity(String::class.java)
//                throw KeycloakException("Failed to create role: ${response.status} - $errorBody")
//            }
            return getRole(role.id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create role", e)
        }
    }

    fun updateRole(id: String, role: RoleRepresentation): RoleRepresentation {
        try {
            val roleResource = keycloak.realm(realm).rolesById().getRole(id)
            keycloak.realm(realm).roles().get(roleResource.name).update(role)
            return getRole(role.id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update role with name: $id", e)
        }
    }

    fun deleteRole(id: String) {
        try {
            keycloak.realm(realm).rolesById().deleteRole(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete role with id: $id", e)
        }
    }

    fun addCompositeRoles(roleId: String, compositeRoleIds: List<String>) {
        try {
            val compositeRoles = compositeRoleIds.map {
                keycloak.realm(realm).rolesById().getRole(it)
            }
            keycloak.realm(realm).rolesById().addComposites(roleId, compositeRoles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to add composite roles to role with id: $roleId", e)
        }
    }

    fun removeCompositeRoles(roleId: String, compositeRoleIds: List<String>) {
        try {
            val compositeRoles = compositeRoleIds.map {
                keycloak.realm(realm).roles().get(it).toRepresentation()
            }
            keycloak.realm(realm).rolesById().deleteComposites(roleId,compositeRoles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to remove composite roles from role with id: $roleId", e)
        }
    }

    fun getCompositeRoles(roleId: String): List<RoleRepresentation> {
        try {
            return keycloak.realm(realm).roles().get(roleId).roleComposites.toList()
        } catch (e: Exception) {
            throw KeycloakException("Failed to get composite roles for role with id: $roleId", e)
        }
    }

    fun getClientRoles(clientId: String?, search: String?, first: Int, max: Int): List<RoleRepresentation> {
        return try {
            if (clientId == null) {
                return emptyList()
            }

            val clientResource = keycloak.realm(realm).clients().get(clientId)
            if (search.isNullOrBlank()) {
                clientResource.roles().list(first, max)
            } else {
                clientResource.roles().list(search, first, max)
            }
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch client roles for clientId: $clientId", e)
        }
    }
    
    // Group role management methods
    
    fun getGroupRoles(groupId: String): List<RoleRepresentation> {
        try {
            return keycloak.realm(realm).groups().group(groupId).roles().realmLevel().listAll()
        } catch (e: Exception) {
            throw KeycloakException("Failed to get roles for group with id: $groupId", e)
        }
    }
    
    fun addRolesToGroup(groupId: String, roleIds: List<String>) {
        try {
            val roles = roleIds.map { keycloak.realm(realm).rolesById().getRole(it) }
            keycloak.realm(realm).groups().group(groupId).roles().realmLevel().add(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to add roles to group with id: $groupId", e)
        }
    }
    
    fun removeRolesFromGroup(groupId: String, roleIds: List<String>) {
        try {
            val roles = roleIds.map { keycloak.realm(realm).rolesById().getRole(it) }
            keycloak.realm(realm).groups().group(groupId).roles().realmLevel().remove(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to remove roles from group with id: $groupId", e)
        }
    }
    
    fun getGroupClientRoles(groupId: String, clientId: String): List<RoleRepresentation> {
        try {
            return keycloak.realm(realm).groups().group(groupId).roles().clientLevel(clientId).listAll()
        } catch (e: Exception) {
            throw KeycloakException("Failed to get client roles for group with id: $groupId and client id: $clientId", e)
        }
    }
    
    fun addClientRolesToGroup(groupId: String, clientId: String, roleIds: List<String>) {
        try {
            val clientResource = keycloak.realm(realm).clients().get(clientId)
            val roles = roleIds.map { clientResource.roles().get(it).toRepresentation() }
            keycloak.realm(realm).groups().group(groupId).roles().clientLevel(clientId).add(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to add client roles to group with id: $groupId and client id: $clientId", e)
        }
    }
    
    fun removeClientRolesFromGroup(groupId: String, clientId: String, roleIds: List<String>) {
        try {
            val clientResource = keycloak.realm(realm).clients().get(clientId)
            val roles = roleIds.map { clientResource.roles().get(it).toRepresentation() }
            keycloak.realm(realm).groups().group(groupId).roles().clientLevel(clientId).remove(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to remove client roles from group with id: $groupId and client id: $clientId", e)
        }
    }
    
    fun addRolesToUser(userId: String, roleIds: List<String>) {
        try {
            val roles = roleIds.map { keycloak.realm(realm).rolesById().getRole(it) }
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to add roles to user with id: $userId", e)
        }
    }
    
    fun removeRolesFromUser(userId: String, roleIds: List<String>) {
        try {
            val roles = roleIds.map { keycloak.realm(realm).rolesById().getRole(it) }
            keycloak.realm(realm).users().get(userId).roles().realmLevel().remove(roles)
        } catch (e: Exception) {
            throw KeycloakException("Failed to remove roles from user with id: $userId", e)
        }
    }
}
