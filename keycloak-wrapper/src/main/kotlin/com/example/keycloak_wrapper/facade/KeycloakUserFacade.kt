package com.example.keycloak_wrapper.facade

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
    fun createUser(user: UserRepresentation): Response {
        return keycloak.realm(realm).users().create(user)
    }

    fun getUser(id: String): UserRepresentation {
        return keycloak.realm(realm).users().get(id).toRepresentation()
    }

    fun updateUser(id: String, user: UserRepresentation) {
        keycloak.realm(realm).users().get(id).update(user)
    }

    fun deleteUser(id: String) {
        keycloak.realm(realm).users().get(id).remove()
    }

    fun searchUsers(search: String?, first: Int, max: Int): List<UserRepresentation> {
        return keycloak.realm(realm).users().search(search, first, max)
    }

    fun countUsers(): Int {
        return keycloak.realm(realm).users().count()
    }
}
