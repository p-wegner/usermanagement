package com.example.keycloak_wrapper.facade

import com.example.keycloak_wrapper.exception.KeycloakException
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.GroupRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeycloakGroupFacade(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {
    fun getGroups(search: String?, first: Int, max: Int): List<GroupRepresentation> {
        return try {
            keycloak.realm(realm).groups().groups(search, first, max)
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
            val response = keycloak.realm(realm).groups().add(group)
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

    fun createSubGroup(parentId: String, group: GroupRepresentation): GroupRepresentation {
        try {
            val response = keycloak.realm(realm).groups().group(parentId).subGroup(group)
            if (response.status != Response.Status.CREATED.statusCode) {
                val errorBody = response.readEntity(String::class.java)
                throw KeycloakException("Failed to create subgroup: ${response.status} - $errorBody")
            }
            
            val locationHeader = response.location.toString()
            val groupId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1)
            return getGroup(groupId)
        } catch (e: Exception) {
            throw KeycloakException("Failed to create subgroup", e)
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

    fun updateGroupRoles(id: String, roleAssignment: RoleAssignmentDto) {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            
            // Update realm roles
            val realmRoleReps = roleAssignment.realmRoleIds.map { 
                keycloak.realm(realm).roles().get(it).toRepresentation() 
            }
            groupResource.roles().realmLevel().remove(groupResource.roles().realmLevel().listAll())
            if (realmRoleReps.isNotEmpty()) {
                groupResource.roles().realmLevel().add(realmRoleReps)
            }

            // Update client roles
            roleAssignment.clientRoleIds.forEach { (clientId, roleIds) ->
                val client = keycloak.realm(realm).clients().get(clientId)
                val clientRoleReps = roleIds.map { 
                    client.roles().get(it).toRepresentation() 
                }
                groupResource.roles().clientLevel(clientId).remove(groupResource.roles().clientLevel(clientId).listAll())
                if (clientRoleReps.isNotEmpty()) {
                    groupResource.roles().clientLevel(clientId).add(clientRoleReps)
                }
            }
        } catch (e: Exception) {
            throw KeycloakException("Failed to update roles for group with id: $id", e)
        }
    }

    fun getGroupRoles(id: String): RoleAssignmentDto {
        try {
            val groupResource = keycloak.realm(realm).groups().group(id)
            
            val realmRoleIds = groupResource.roles().realmLevel().listAll()
                .map { it.id }

            val clientRoleIds = mutableMapOf<String, List<String>>()
            keycloak.realm(realm).clients().findAll().forEach { client ->
                val roleIds = groupResource.roles().clientLevel(client.id).listAll()
                    .map { it.id }
                if (roleIds.isNotEmpty()) {
                    clientRoleIds[client.id] = roleIds
                }
            }

            return RoleAssignmentDto(realmRoleIds, clientRoleIds)
        } catch (e: Exception) {
            throw KeycloakException("Failed to get roles for group with id: $id", e)
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
