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

    fun getRole(name: String): RoleRepresentation {
        return try {
            keycloak.realm(realm).roles().get(name).toRepresentation()
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch role with name: $name", e)
        }
    }

    fun createRole(role: RoleRepresentation): RoleRepresentation {
        try {
            keycloak.realm(realm).roles().create(role)
//            if (response.status != Response.Status.CREATED.statusCode) {
//                val errorBody = response.readEntity(String::class.java)
//                throw KeycloakException("Failed to create role: ${response.status} - $errorBody")
//            }
            return getRole(role.name)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create role", e)
        }
    }

    fun updateRole(name: String, role: RoleRepresentation): RoleRepresentation {
        try {
            val roleResource = keycloak.realm(realm).roles().get(name)
            roleResource.update(role)
            return getRole(role.name)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update role with name: $name", e)
        }
    }

    fun deleteRole(name: String) {
        try {
            keycloak.realm(realm).roles().deleteRole(name)
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete role with name: $name", e)
        }
    }
}
