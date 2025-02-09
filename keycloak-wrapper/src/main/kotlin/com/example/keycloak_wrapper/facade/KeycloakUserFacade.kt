package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.exception.KeycloakException
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.core.Response

@Component
class KeycloakUserFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}") private val realm: String
) {
    fun getUsers(search: String?, firstResult: Int, maxResults: Int): List<UserRepresentation> {
        return try {
            keycloak.realm(realm).users()
                .search(search, firstResult, maxResults, true)
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
                throw KeycloakException("Failed to create user: ${response.status}")
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
            keycloak.realm(realm).users().get(id).update(user)
            return getUser(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update user with id: $id", e)
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
}
