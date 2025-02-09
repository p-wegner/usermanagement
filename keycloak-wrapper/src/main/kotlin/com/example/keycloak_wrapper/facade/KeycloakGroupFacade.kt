package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.exception.KeycloakException
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.core.Response

@Component
class KeycloakGroupFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    fun getGroups(search: String?, firstResult: Int, maxResults: Int): Pair<List<GroupRepresentation>, Int> {
        return try {
            val groups = keycloak.realm(realm).groups()
            val results = groups.groups(search, firstResult, maxResults)
            val total = groups.count(search)
            Pair(results, total)
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch groups", e)
        }
    }

    fun getGroup(id: String): GroupRepresentation {
        return try {
            keycloak.realm(realm).groups().group(id).toRepresentation()
        } catch (e: Exception) {
            throw KeycloakException("Failed to fetch group with id: $id", e)
        }
    }

    fun createGroup(group: GroupRepresentation): GroupRepresentation {
        try {
            val response = if (group.parentId != null) {
                keycloak.realm(realm).groups().group(group.parentId).subGroup(group)
            } else {
                keycloak.realm(realm).groups().add(group)
            }

            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create group: ${response.status} - $errorBody")
            }
            
            val locationHeader = response.location.toString()
            val groupId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create group", e)
        }
    }

    fun updateGroup(id: String, group: GroupRepresentation): GroupRepresentation {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            groupResource.update(group)
            return getGroup(id)
        } catch (e: Exception) {
            throw KeycloakException("Failed to update group with id: $id", e)
        }
    }

    fun deleteGroup(id: String) {
        try {
            keycloak.realm(realm).groups().group(id).remove()
        } catch (e: Exception) {
            throw KeycloakException("Failed to delete group with id: $id", e)
        }
    }
}
